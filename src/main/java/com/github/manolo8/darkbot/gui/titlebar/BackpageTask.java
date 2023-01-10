package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.OSUtil;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.http.Http;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BackpageTask extends Thread {
    private static final Path BACKPAGE_PATH = OSUtil.getDataPath("backpage");
    private static final Path VERSION_PATH = BACKPAGE_PATH.resolve(".version");

    private static final String EXECUTABLE_NAME = "dark_backpage";
    private static final String RELEASE_URL = "https://api.github.com/repos/darkbot-reloaded/DarkBackpage/releases/latest";

    private static final Timer VERSION_CHECK_TIMER = Timer.get(Time.HOUR * 12);

    private final Main main;
    private final BackpageButton button;

    public BackpageTask(Main main, BackpageButton button) {
        setDaemon(true);
        this.main = main;
        this.button = button;
    }

    @Override
    public void run() {
        if (main.backpage.isInstanceValid()) { // open backpage even if sid is KO
            try {
                if (canRun()) {
                    new ProcessBuilder(BACKPAGE_PATH.resolve(EXECUTABLE_NAME).toAbsolutePath().toString(),
                            "--sid", main.backpage.getSid(),
                            "--url", main.backpage.getInstanceURI().toString())
                            .start().waitFor();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } // inform user that instance/sid is not valid?
        SwingUtilities.invokeLater(() -> button.setEnabled(true));
    }

    private boolean canRun() {
        try {
            Version currentVersion = readVersionFile();

            // have downloaded backpage and version was checked recently
            return (currentVersion != null && VERSION_CHECK_TIMER.isActive())
                    || checkVersion(currentVersion);
        } catch (IOException e) {
            e.printStackTrace();
            VERSION_CHECK_TIMER.disarm(); // IOException happen - reset timer

            String message = e instanceof DirectoryClearException ? ((DirectoryClearException) e).getPopupMessage() : e.toString();
            Popups.of("Backpage exception", message)
                    .messageType(JOptionPane.ERROR_MESSAGE)
                    .showAsync();
        } finally {
            button.removeProgressBar();
        }

        return false;
    }

    private boolean checkVersion(Version current) throws IOException {
        JProgressBar progressBar = button.createProgressBar();

        ReleaseInfo releaseInfo = ReleaseInfo.get();
        if (releaseInfo == null) return false;

        Version remoteVersion = releaseInfo.getVersion();
        ReleaseInfo.Asset asset = releaseInfo.getValidAsset();
        if (remoteVersion == null || asset == null) return false;

        VERSION_CHECK_TIMER.activate(); // activate the timer here - version will be successfully checked
        return current != null && current.compareTo(remoteVersion) >= 0
                || askUserToDownload(progressBar, current, remoteVersion, asset);
    }

    //return true if is possible to run a backpage
    private boolean askUserToDownload(JProgressBar progressBar,
                                      Version current, Version remote, ReleaseInfo.Asset asset) throws IOException {
        String message = current == null ? I18n.get("gui.backpage_button.download_message")
                : I18n.get("gui.backpage_button.new_version_message");
        message += "\n -Ver: " + (current != null ? current + " -> " : "") + remote + ", size: " + (asset.size >> 20) + "MB";

        JButton download = new JButton(I18n.get("gui.backpage_button.download"));
        Popups.Builder builder = Popups.of("Backpage browser", message)
                .messageType(JOptionPane.QUESTION_MESSAGE)
                .options(download, I18n.get("gui.backpage_button.cancel"));

        JOptionPane optionPane = builder.build();
        download.addActionListener(l -> optionPane.setValue(download));

        if (download == builder.showSync()) {
            if (!clearDirectory())
                throw new DirectoryClearException(BACKPAGE_PATH);

            FileUtils.createDirectories(BACKPAGE_PATH);
            downloadBackpage(progressBar, asset);
            writeVersionFile(remote);
        }

        return readVersionFile() != null;
    }

    // true if backpage folder is clear
    private boolean clearDirectory() throws IOException {
        if (Files.notExists(BACKPAGE_PATH)) return true;

        try (Stream<Path> walk = Files.walk(BACKPAGE_PATH)) {
            boolean backapgeFileAccessible = walk.sorted(Comparator.reverseOrder())
                    .filter(path -> path.toString().contains(EXECUTABLE_NAME))
                    .map(Path::toFile)
                    .map(File::delete)
                    .findFirst().orElse(true);

            if (!backapgeFileAccessible) return false; // probably used by another process - cannot delete
        }

        try (Stream<Path> walk = Files.walk(BACKPAGE_PATH)) {
            return walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .allMatch(File::delete);
        }
    }

    // true if everything downloaded successfully
    private void downloadBackpage(JProgressBar progressBar, ReleaseInfo.Asset asset) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(asset.size);
        });

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(asset.openStream()))) {
            int downloadedBytes = 0;
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                Path to = BACKPAGE_PATH.resolve(entry.getName());

                if (entry.isDirectory())
                    Files.createDirectories(to);
                else {
                    File file = to.toFile();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        double compressionRatio = (double) entry.getCompressedSize() / entry.getSize();

                        int read;
                        byte[] buffer = new byte[8192];
                        while ((read = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                            setProgress(progressBar, downloadedBytes += (read * compressionRatio));
                        }

                        if (OSUtil.isLinux() && file.getName().startsWith(EXECUTABLE_NAME))
                            file.setExecutable(true);
                    }
                }
            }
        }
    }

    private void setProgress(JProgressBar progressBar, int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    private @Nullable Version readVersionFile() throws IOException {
        if (Files.notExists(VERSION_PATH)) return null;
        return new Version(Files.readString(VERSION_PATH));
    }

    private void writeVersionFile(Version version) throws IOException {
        Files.writeString(VERSION_PATH, version.toString());
    }

    public static class ReleaseInfo {
        public String name;
        public List<Asset> assets;

        public static ReleaseInfo get() throws IOException, JsonParseException {
            return new Gson().fromJson(Http.create(RELEASE_URL).getContent(), ReleaseInfo.class);
        }

        public Version getVersion() {
            return new Version(name);
        }

        public Asset getValidAsset() {
            return assets.stream()
                    .filter(ReleaseInfo.Asset::isSupported)
                    .findFirst().orElse(null);
        }

        public static class Asset {
            @SerializedName("browser_download_url")
            public String downloadUrl;
            public String state;
            public int size;

            public InputStream openStream() throws IOException {
                return new URL(downloadUrl).openStream();
            }

            public boolean isSupported() {
                if (state == null || !state.equals("uploaded")) return false;

                switch (OSUtil.getCurrentOs()) { // maybe should check architecture?
                    case WINDOWS:
                        if (downloadUrl.contains("win-x64.zip")) return true;
                        break;
                    case MACOS:
                        if (downloadUrl.contains("mac-x64.zip")) return true;
                        break;
                    case LINUX:
                        if (downloadUrl.contains("linux-x64.zip")) return true;
                        break;
                }

                return false;
            }
        }
    }

    private static class DirectoryClearException extends IOException {
        private final String popupMessage;

        public DirectoryClearException(Path path) {
            super("Failed to clear directory: " + path.toString());
            this.popupMessage = "Failed to clear backpage directory.\n" +
                    " -Make sure to close every backpage window!\n -" + path;
        }

        public String getPopupMessage() {
            return popupMessage;
        }
    }
}

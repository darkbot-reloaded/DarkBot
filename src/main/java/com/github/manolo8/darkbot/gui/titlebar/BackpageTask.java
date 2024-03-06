package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.OSUtil;
import com.github.manolo8.darkbot.utils.Time;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import eu.darkbot.util.Timer;
import eu.darkbot.util.http.Http;
import org.jetbrains.annotations.Nullable;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    public static boolean isSupported(Version minVersion) {
        try {
            Version version = readVersionFile();
            return version != null && !version.isOlderThan(minVersion);
        } catch (IOException ignored) {
            return false;
        }
    }

    public static Process createBrowser(String... args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(BACKPAGE_PATH.resolve(EXECUTABLE_NAME).toAbsolutePath().toString());
        builder.command().addAll(List.of(args));
        return builder.start();
    }

    private static ReleaseInfo getLatestRelease() throws IOException, JsonParseException {
        return Http.create(RELEASE_URL).fromJson(ReleaseInfo.class);
    }

    @Override
    public void run() {
        if (main.backpage.isInstanceValid()) { // open backpage even if sid is KO
            try {
                if (canRun()) {
                    createBrowser("--sid", main.backpage.getSid(),
                            "--url", main.backpage.getInstanceURI().toString())
                            .waitFor();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } else {
          Popups.of("Backpage",
                          "No SID available, wait until client loads and try again.",
                          JOptionPane.INFORMATION_MESSAGE)
                  .showAsync();
        }
        SwingUtilities.invokeLater(() -> button.setEnabled(true));
    }

    private boolean canRun() {
        try {
            Version currentVersion = readVersionFile();

            return (currentVersion != null && VERSION_CHECK_TIMER.isActive())
                    || checkVersion(currentVersion);
        } catch (IOException e) {
            e.printStackTrace();
            VERSION_CHECK_TIMER.disarm(); // IOException happen - reset timer

            Popups.of("Backpage exception", e.getLocalizedMessage())
                    .messageType(JOptionPane.ERROR_MESSAGE)
                    .showAsync();
        } finally {
            button.removeProgressBar();
        }

        return false;
    }

    private boolean checkVersion(Version current) throws IOException {
        JProgressBar progressBar = button.addProgressBar();

        ReleaseInfo releaseInfo = getLatestRelease();
        if (releaseInfo == null) return false;

        Version remoteVersion = releaseInfo.getVersion();
        Asset asset = releaseInfo.getValidAsset();
        if (remoteVersion == null || asset == null) return false;

        VERSION_CHECK_TIMER.activate(); // activate the timer here - version will be successfully checked
        return current != null && current.compareTo(remoteVersion) >= 0
                || askUserToDownload(progressBar, current, remoteVersion, asset);
    }

    private boolean askUserToDownload(JProgressBar progressBar,
                                      Version current, Version remote, Asset asset) throws IOException {
        String message = current == null ? I18n.get("gui.backpage_button.download_message")
                : I18n.get("gui.backpage_button.new_version_message");
        message += "\n -Ver: " + (current != null ? current + " -> " : "") + remote + ", size: " + (asset.size >> 20) + "MB";

        int selection = Popups.of("Backpage browser", message)
                .messageType(JOptionPane.QUESTION_MESSAGE)
                .options(I18n.get("gui.backpage_button.download"), I18n.get("gui.backpage_button.cancel"))
                .showOptionSync();

        // download selected
        if (selection == 0) {
            FileUtils.clearDirectory(BACKPAGE_PATH, EXECUTABLE_NAME);
            FileUtils.createDirectories(BACKPAGE_PATH);

            downloadBackpage(progressBar, asset);
            Files.writeString(VERSION_PATH, remote.toString());
            return true;
        }

        return current != null;
    }

    private void downloadBackpage(JProgressBar progressBar, Asset asset) throws IOException {
        SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(asset.openStream()))) {
            int downloadedBytes = 0, progress = 0, partSize = asset.size / 50;

            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                Path to = BACKPAGE_PATH.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(to);
                    continue;
                }

                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(to))) {
                    double compressionRatio = (double) entry.getCompressedSize() / entry.getSize();

                    int read;
                    byte[] buffer = new byte[8192];
                    while ((read = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, read);

                        downloadedBytes += (read * compressionRatio);
                        int currentProgress = downloadedBytes / partSize;
                        if (progress != currentProgress)
                            setProgress(progressBar, progress = currentProgress);
                    }

                    if (OSUtil.isLinux() && to.getFileName().toString().startsWith(EXECUTABLE_NAME)) {
                        to.toFile().setExecutable(true);
                    }
                }
            }
        }
    }

    private void setProgress(JProgressBar progressBar, int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    private static @Nullable Version readVersionFile() throws IOException {
        if (Files.notExists(VERSION_PATH)) return null;
        return new Version(Files.readString(VERSION_PATH));
    }

    public static class ReleaseInfo {
        @SerializedName("name")
        public Version version;
        public List<Asset> assets;

        public Version getVersion() {
            return version;
        }

        public Asset getValidAsset() {
            return assets.stream()
                    .filter(Asset::isSupported)
                    .findFirst().orElse(null);
        }
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
            return downloadUrl.contains(OSUtil.getCurrentOs().getShortName() + "-x64.zip");
        }
    }
}

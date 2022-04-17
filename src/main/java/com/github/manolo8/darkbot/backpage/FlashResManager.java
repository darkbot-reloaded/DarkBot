package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.OS;
import com.github.manolo8.darkbot.utils.XmlHelper;
import com.github.manolo8.darkbot.utils.http.Http;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.GameResourcesAPI;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Responsible for searching & holding flash resource file, containing translations for in-game things
 * <p>
 * To be able to undo translations (convert a translated text into it's original translation id) in an efficient
 * manner we will substring all resources to the first N characters to be able to do an initial fast search.
 */
@Feature(name = "Flash resource manager", description = "Holds the resources for the in-game language")
public class FlashResManager implements Task, GameResourcesAPI {

    private static final String URL = "https://darkorbit-22.bpsecure.com/spacemap/templates/{lang}/flashres.xml";

    private final Main main;
    private final Queue<Runnable> tasksQueue = new ConcurrentLinkedQueue<>();

    private String lang = null;
    private Locale inGameLocale;

    private volatile Map<String, String> ALL_TRANSLATIONS = Collections.emptyMap();

    public FlashResManager(Main main) {
        this.main = main;
    }

    @Override
    public void onTickTask() {
        if (main == null) return;
        String currLang = main.settingsManager.lang;
        if (currLang == null
                || currLang.isEmpty()
                || currLang.equals(lang)) return;

        try {
            Element root = Http.create(URL.replace("{lang}", currLang))
                    .consumeInputStream(inputStream -> DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(inputStream)
                            .getDocumentElement());

            ALL_TRANSLATIONS = XmlHelper.stream(root.getElementsByTagName("item")).collect(Collectors.toMap(
                    i -> i.getAttributes().getNamedItem("name").getNodeValue(), Node::getTextContent, (a, b) -> a));

            // TODO: store in an efficient way to reverse-translate
            lang = currLang;

            try {
                inGameLocale = new Locale(currLang);
            } catch (IllformedLocaleException e) {
                e.printStackTrace();
                inGameLocale = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            lang = null;
        }
    }

    @Override
    public void onBackgroundTick() {
        if (tasksQueue.isEmpty()) return;

        Runnable task;
        while ((task = tasksQueue.poll()) != null)
            task.run();
    }

    public String getTranslation(String key) {
        return ALL_TRANSLATIONS.get(key);
    }

    @Override
    public Locale getLanguage() {
        return inGameLocale;
    }

    @Override
    public Optional<String> findTranslation(@NotNull String key) {
        return Optional.ofNullable(getTranslation(key));
    }

    public CompletableFuture<Image> getBackgroundImage(GameMap gameMap) {
        MinimapBackgroundTask task = new MinimapBackgroundTask(gameMap.getId());

        tasksQueue.add(task);
        return task;
    }

    private static class MinimapBackgroundTask extends CompletableFuture<Image> implements Runnable {
        private static final Pattern MINIMAP_ID_PATTERN = Pattern.compile("id=\"(\\d+)\".*minimap=\"([^\"]+)\"");
        private static final Path MINIMAPS_PATH = OS.getDataPath().resolve("Images/minimaps");

        private static Map<Integer, String> mapBackgroundIds;

        private final int mapId;

        private MinimapBackgroundTask(int mapId) {
            this.mapId = mapId;
        }

        @Override
        public void run() {
            if (parseMinimapIds()) {
                try {
                    if (!FileUtils.ensureDirectoriesExists(MINIMAPS_PATH)) {
                        complete(null);
                        return; // if can't create directory, return null cause can't cache images
                    }

                    String name = "minimap-" + mapBackgroundIds.getOrDefault(mapId, String.valueOf(mapId)) + "-700.jpg";
                    Path minimapPath = MINIMAPS_PATH.resolve(name);

                    if (Files.exists(minimapPath))
                        complete(ImageIO.read(minimapPath.toFile()));

                    else {
                        URL backgroundURL = new URL("https://darkorbit-22.bpsecure.com/spacemap/graphics/minimaps/" + name);
                        BufferedImage img = ImageIO.read(backgroundURL);
                        ImageIO.write(img, "jpg", minimapPath.toFile());

                        complete(img);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    complete(null);
                }
            }
        }

        private boolean parseMinimapIds() {
            if (mapBackgroundIds != null && !mapBackgroundIds.isEmpty())
                return true;

            try {
                InputStream is = Http.create("https://darkorbit-22.bpsecure.com/spacemap/graphics/maps-config.xml")
                        .getInputStream();

                Matcher matcher = MINIMAP_ID_PATTERN.matcher("");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    mapBackgroundIds = br.lines()
                            .map(String::trim)
                            .filter(s -> s.startsWith("<map "))
                            .map(matcher::reset)
                            .filter(Matcher::find)
                            .collect(Collectors.toMap(m -> Integer.parseInt(m.group(1)), m -> m.group(2)));

                } catch (IOException e) {
                    e.printStackTrace();
                    mapBackgroundIds = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                mapBackgroundIds = null;
            }

            return mapBackgroundIds != null && !mapBackgroundIds.isEmpty();
        }
    }
}
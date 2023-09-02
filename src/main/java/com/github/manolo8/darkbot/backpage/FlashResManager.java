package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.OSUtil;
import com.github.manolo8.darkbot.utils.XmlHelper;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.GameResourcesAPI;
import eu.darkbot.util.XmlUtils;
import eu.darkbot.util.http.Http;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Responsible for searching &amp; holding flash resource file, containing translations for in-game things
 * <p>
 * To be able to undo translations (convert a translated text into it's original translation id) in an efficient
 * manner we will substring all resources to the first N characters to be able to do an initial fast search.
 */
@Feature(name = "Flash resource manager", description = "Holds the resources for the in-game language")
public class FlashResManager implements Task, GameResourcesAPI {
    private static final Path MINIMAPS_PATH = OSUtil.getDataPath("Images", "minimaps");
    private static final String URL = "https://darkorbit-22.bpsecure.com/spacemap/templates/{lang}/flashres.xml";

    private final Main main;
    private final Queue<Runnable> tasksQueue = new ConcurrentLinkedQueue<>();

    private String lang = null;
    private Locale inGameLocale;

    private Map<Integer, MapInfo> BACKGROUND_MAP_IDS = Collections.emptyMap();
    private volatile Map<String, String> ALL_TRANSLATIONS = Collections.emptyMap();

    public FlashResManager(Main main) {
        this.main = main;
    }

    @Override
    public void onTickTask() {
    }

    @Override
    public void onBackgroundTick() {
        loadTranslations();
        loadMinimapIds();

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
        Path minimapPath = MINIMAPS_PATH.resolve(gameMap.getId() + "-700.jpg");
        CompletableFuture<Image> future = new CompletableFuture<>();

        if (Files.exists(minimapPath)) {
            try {
                future.complete(ImageIO.read(minimapPath.toFile()));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
            return future;
        }

        tasksQueue.add(() -> {
            FileUtils.ensureDirectoryExists(MINIMAPS_PATH);
            try {
                MapInfo mapInfo = BACKGROUND_MAP_IDS.get(gameMap.getId());

                String name = "minimap-" + (mapInfo == null ? gameMap.getId()
                        : mapInfo.getOrDefaultId(gameMap.getId())) + "-700.jpg";

                URL backgroundURL = new URL("https://darkorbit-22.bpsecure.com/spacemap/graphics/minimaps/" + name);
                BufferedImage img = ImageIO.read(backgroundURL);
                ImageIO.write(img, "jpg", minimapPath.toFile());
                future.complete(img);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private void loadTranslations() {
        if (main == null) return;
        String currLang = main.settingsManager.lang;
        if (currLang == null
                || currLang.isEmpty()
                || currLang.equals(lang)
                || currLang.equals("ERROR")) return;

        try {
            Element root = Http.create(URL.replace("{lang}", currLang))
                    .consumeInputStream(inputStream -> XmlUtils.parse(inputStream).getDocumentElement());

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

    private void loadMinimapIds() {
        if (!BACKGROUND_MAP_IDS.isEmpty()) return;

        try {
            BACKGROUND_MAP_IDS = XmlHelper.stream(Http.create("https://darkorbit-22.bpsecure.com/spacemap/graphics/maps-config.xml")
                            .consumeInputStream(inputStream -> XmlUtils.parse(inputStream).getDocumentElement())
                            .getElementsByTagName("map"))
                    .collect(Collectors.toMap(e -> Integer.parseInt(e.getAttribute("id")), MapInfo::new));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MapInfo {
        private final String minimapId, mapName, groupMapName;

        private MapInfo(Element e) {
            this.minimapId = e.getAttribute("minimap");
            this.mapName = e.getAttribute("name");
            this.groupMapName = e.getAttribute("groupSystemName");
        }

        public String getOrDefaultId(int id) {
            return minimapId.isEmpty() ? String.valueOf(id) : minimapId;
        }

        @Override
        public String toString() {
            return "MapInfo{" +
                    "minimapId=" + minimapId +
                    ", mapName='" + mapName + '\'' +
                    ", groupMapName='" + groupMapName + '\'' +
                    '}';
        }
    }
}
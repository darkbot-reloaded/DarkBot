package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.utils.XmlHelper;
import com.github.manolo8.darkbot.utils.http.Http;
import eu.darkbot.api.managers.GameResourcesAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for searching & holding flash resource file, containing translations for in-game things
 *
 * To be able to undo translations (convert a translated text into it's original translation id) in an efficient
 * manner we will substring all resources to the first N characters to be able to do an initial fast search.
 */
@Feature(name = "Flash resource manager", description = "Holds the resources for the in-game language")
public class FlashResManager implements Task, GameResourcesAPI {

    private static final String URL = "https://darkorbit-22.bpsecure.com/spacemap/templates/{lang}/flashres.xml";

    private Main main;

    private String lang = null;
    private Locale inGameLocale;

    private volatile Map<String, String> ALL_TRANSLATIONS = Collections.emptyMap();


    @Override
    public void install(Main main) {
        this.main = main;
    }

    @Override
    public void tick() {
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
                    i -> i.getAttributes().getNamedItem("name").getNodeValue(), Node::getTextContent, (a,b) -> a));

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

    public String getTranslation(String key) {
        return ALL_TRANSLATIONS.get(key);
    }

    @Override
    public Locale getLanguage() {
        return inGameLocale;
    }

    @Override
    public Optional<String> findTranslation(String key) {
        return Optional.ofNullable(getTranslation(key));
    }
}
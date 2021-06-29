package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.dispatch.BiIntConsumer;
import com.github.manolo8.darkbot.backpage.dispatch.DispatchData;
import com.github.manolo8.darkbot.backpage.dispatch.InProgress;
import com.github.manolo8.darkbot.backpage.dispatch.Retriever;
import com.github.manolo8.darkbot.utils.http.Method;
import org.intellij.lang.annotations.Language;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DispatchManager {
    private final Main main;
    private final DispatchData data;
    private long lastDispatcherUpdate;

    DispatchManager(Main main) {
        this.main = main;
        this.data = new DispatchData();
    }

    // TODO: remove before release, just parser testing
    public static void main(String[] args) throws Exception {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        String page = Files.lines(Paths.get("dispatcher.html")).collect(Collectors.joining("\n"));

        DispatchData data = new DispatchData();

        boolean upd = InfoReader.updateAll(page, data);
        int i = 0;
    }

    public DispatchData getData() {
        return data;
    }

    public boolean update(int expiryTime) {
        try {
            return update(main.backpage, expiryTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(BackpageManager manager, int expiryTime) throws Exception {
        if (System.currentTimeMillis() <= lastDispatcherUpdate + expiryTime) return false;
        String page = manager.getConnection("indexInternal.es?action=internalDispatch", Method.GET).getContent();

        if (page == null || page.isEmpty()) return false;
        lastDispatcherUpdate = System.currentTimeMillis();
        return InfoReader.updateAll(page, data);
    }

    public boolean hireRetriever(Retriever retriever) {
        if (data.getAvailableSlots() <= 0) return false;
            //retriever.getCost(); // TODO: check cost
            try {
                String response = main.backpage.getConnection("ajax/dispatch.php", Method.POST)
                        .setRawParam("command", "sendDispatch")
                        .setRawParam("dispatchId", retriever.getId())
                        .getContent();
                return handleResponse("Hired dispatcher", retriever.getId(), response);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception hiring dispatcher: " + e);
            }
        }
        return false;
    }

    public boolean collect(InProgress progress) {
        try {
            if (progress.getCollectable().equals("0")) return false;
            System.out.println("Collecting: Slot " + progress.getSlotId());
            String response = main.backpage.getConnection("ajax/dispatch.php", Method.POST)
                    .setRawParam("command", "collectDispatch")
                    .setRawParam("slot", progress.getSlotId())
                    .getContent();

            //parse response
            //{"result":"OK","message":"Collected the following:","rewardsLog":[{"lootId":"Solidus","amount":3},{"lootId":"PLT-2026","amount":97},{"lootId":"Scrap","amount":3}]}

            return handleResponse("Collected retriever", progress.getId(), response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception collecting dispatcher: " + e);
        }
        return false;
    }

    public boolean handleResponse(String type, String id, String response) {
        boolean failed = response.contains("\"result\":\"ERROR\"");
        System.out.println(type + " (" + id + ") " + (failed ? "failed" : "succeeded") + ": " + response);
        update(-1);
        return !failed;
    }


    public List<String> collectAll() {
        return data.getInProgress().values().stream()
                .filter(this::collect)
                .map(InProgress::getName)
                .collect(Collectors.toList());
    }


    private enum InfoReader {
        PERMIT("name=\"permit\" value=\"([0-9]+)\"", DispatchData::setPermit),
        GATE_UNIT("name=\"permit\" value=\"([0-9]+)\"", DispatchData::setGateUnits),
        SLOTS(":([0-9]+).*class=\"userCurrentMax\">([0-9]+)", DispatchData::setAvailableSlots, DispatchData::setMaxSlots),
        ITEMS("<tr class=\"dispatchItemRow([\\S\\s]+?)</tr>", DispatchData::parseRow);

        private final Pattern regex;
        private final List<BiConsumer<DispatchData, String>> consumers;

        InfoReader(@Language("RegExp") String regex, List<BiConsumer<DispatchData, String>> consumers) {
            this.regex = Pattern.compile(regex);
            this.consumers = consumers;
        }

        @SafeVarargs
        InfoReader(@Language("RegExp") String regex, BiConsumer<DispatchData, String>... consumers) {
            this(regex, Arrays.asList(consumers));
        }

        @SafeVarargs
        InfoReader(@Language("RegExp") String regex, BiIntConsumer<DispatchData>... consumers) {
            this(regex, Arrays.stream(consumers)
                    .map(c -> (BiConsumer<DispatchData, String>) (d, s) -> c.accept(d, Integer.parseInt(s)))
                    .collect(Collectors.toList()));
        }

        public static boolean updateAll(String page, DispatchData data) {
            boolean updated = true;
            for (InfoReader reader : InfoReader.values()) {
                updated &= reader.update(page, data);
            }
            return updated;
        }

        private boolean update(String page, DispatchData data) {
            Matcher m = regex.matcher(page);
            if (!m.find()) return false;

            do {
                int max = Math.min(m.groupCount(), consumers.size());
                for (int i = 0; i < max; i++)
                    consumers.get(i).accept(data, m.group(i + 1));
            } while (m.find());

            return m.groupCount() == consumers.size();
        }
    }


}

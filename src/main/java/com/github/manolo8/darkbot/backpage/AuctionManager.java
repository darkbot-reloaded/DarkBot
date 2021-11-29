package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.auction.AuctionData;
import com.github.manolo8.darkbot.backpage.auction.AuctionItems;
import com.github.manolo8.darkbot.backpage.auction.BiIntConsumer;
import com.github.manolo8.darkbot.utils.http.Method;
import org.intellij.lang.annotations.Language;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuctionManager {
    private final Main main;
    private final BackpageManager backpage;
    private final AuctionData data;
    private long lasAuctionUpdate;

    AuctionManager(Main main, BackpageManager backpage) {
        this.main = main;
        this.backpage = backpage;
        this.data = new AuctionData();
    }

    public AuctionData getData() {
        return data;
    }

    public boolean update(int expiryTime) throws Exception {
        if (System.currentTimeMillis() <= lasAuctionUpdate + expiryTime) return false;
        String page = backpage.getConnection("indexInternal.es?action=internalAuction", Method.GET).getContent();

        if (page == null || page.isEmpty()) return false;
        lasAuctionUpdate = System.currentTimeMillis();
        return InfoReader.updateAll(page, data);
    }

    public boolean bidItem(AuctionItems auctionItem){
        return bidItem(auctionItem, auctionItem.getCurrentBid()+10000);
    }
    public boolean bidItem(AuctionItems auctionItem, double amount) {
        try {
            String token = main.backpage.getConnection("indexInternal.es", Method.GET)
                    .setRawParam("action", "internalAuction")
                    .consumeInputStream(main.backpage::getReloadToken);

            String response = main.backpage.getConnection("indexInternal.es", Method.POST)
                    .setRawParam("action", "internalAuction")
                    .setRawParam("reloadToken", token)
                    .setRawParam("auctionType", "hour")
                    .setRawParam("subAction", "bid")
                    .setRawParam("lootId", auctionItem.getLootID())
                    .setRawParam("itemId", auctionItem.getId())
                    .setRawParam("credits", String.valueOf(amount))
                    .setRawParam("auction_buy_button", "BID")
                    .getContent();
            return handleResponse("Bid on Item", auctionItem.getLootID(), response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception Bidding on Item: " + e);
        }
        return false;
    }

    private boolean handleResponse(String type, String id, String response) throws Exception {
        boolean failed = response.contains("question icon_error");
        System.out.println(type + " (" + id + ") " + (failed ? "failed" : "succeeded") + ": " + response);
        update(-1);
        return !failed;
    }

    private enum InfoReader {
        ITEMS("<tr class=\"auctionItemRow ([\\S\\s]+?)</tr>", AuctionData::parseRow);

        private final Pattern regex;
        private final List<BiConsumer<AuctionData, String>> consumers;

        InfoReader(@Language("RegExp") String regex, List<BiConsumer<AuctionData, String>> consumers) {
            this.regex = Pattern.compile(regex);
            this.consumers = consumers;
        }

        @SafeVarargs
        InfoReader(@Language("RegExp") String regex, BiConsumer<AuctionData, String>... consumers) {
            this(regex, Arrays.asList(consumers));
        }

        @SafeVarargs
        InfoReader(@Language("RegExp") String regex, BiIntConsumer<AuctionData>... consumers) {
            this(regex, Arrays.stream(consumers)
                    .map(c -> (BiConsumer<AuctionData, String>) (d, s) -> c.accept(d, Integer.parseInt(s)))
                    .collect(Collectors.toList()));
        }

        public static boolean updateAll(String page, AuctionData data) {
            boolean updated = true;
            for (InfoReader reader : InfoReader.values()) {
                updated &= reader.update(page, data);
            }
            return updated;
        }

        private boolean update(String page, AuctionData data) {
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

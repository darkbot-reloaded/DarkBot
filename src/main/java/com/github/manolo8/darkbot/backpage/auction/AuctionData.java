package com.github.manolo8.darkbot.backpage.auction;

import com.github.manolo8.darkbot.backpage.dispatch.InProgress;
import com.github.manolo8.darkbot.backpage.dispatch.Retriever;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionData {
    private final DataBuilder dataBuilder = new DataBuilder();
    private final Map<String, AuctionItems> auctionItems = new LinkedHashMap<>();

    public Map<String, AuctionItems> getAuctionItems() {
        return auctionItems;
    }

    public void parseRow(String str) {
        if (dataBuilder.buildAuctionItems(str)) return;
    }

    @Override
    public String toString() {
        return "AuctionData";
    }

    public class DataBuilder {
        private final Pattern AUCTION_PATTERN = Pattern.compile("itemKey=\"item_hour_(.+?)\".*?" +
                "auction_item_name_col\">\\s+(.+?)\\s+<.*?" +
                "auction_item_type\">\\s+(.+?)\\s+<.*?" +
                "auction_item_current\">\\s+(.+?)\\s+<.*?" +
                "auction_item_you\">\\s+(.+?)\\s+<.*?" +
                "item_hour_0_buyPrice\" value=\"(.+?)\".*?" +
                "item_hour_0_lootId\" value=\"(.+?)\".*?", Pattern.DOTALL);

        public boolean buildAuctionItems(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("itemKey") ||
                    !string.contains("auction_item_name_col") ||
                    !string.contains("auction_item_type") ||
                    !string.contains("auction_item_current") ||
                    !string.contains("auction_item_you") ||
                    !string.contains("item_hour_0_buyPrice") ||
                    !string.contains("item_hour_0_lootId")) return false;
            Matcher m = AUCTION_PATTERN.matcher(string);
            if (!m.find()) return false;

            String id = m.group(1);
            AuctionItems r = auctionItems.get(id);
            if (r == null) auctionItems.put(id, r = new AuctionItems());

            r.setId(id);
            r.setName(m.group(2));
            r.setType(m.group(3));
            r.setCurrent(m.group(4));
            r.setYou(m.group(5));
            r.setInstantBuy(m.group(6));
            r.setLootID(m.group(7));
            return true;
        }
    }

}

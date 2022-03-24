package com.github.manolo8.darkbot.backpage.auction;

import com.github.manolo8.darkbot.utils.Base62;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionData {
    private final Map<String, AuctionItems> auctionItems = new LinkedHashMap<>();
    private final Pattern AUCTION_TABLE = Pattern.compile("<tr class=\"auctionItemRow .+? (itemKey=\"item_hour[\\S\\s]+?)</tr>", Pattern.DOTALL);

    private final String AUCTION_PATTERN = "auction_item_name_col\">\\s+(.+?)\\s+<.*?" +
            "auction_item_type\">\\s+(.+?)\\s+<.*?" +
            "auction_item_highest\" (.+?)>.*?" +
            "auction_item_current\">\\s+(.+?)\\s+<.*?" +
            "auction_item_you\">\\s+(.+?)\\s+<.*?" +
            "item_hour_\\d+_buyPrice\" value=\"(.+?)\".*?" +
            "item_hour_\\d+_lootId\" value=\"(.+?)\".*?";
    private final Pattern AUCTION_PATTERN_HOUR = Pattern.compile("itemKey=\"item_hour_(.+?)\".*?" + AUCTION_PATTERN, Pattern.DOTALL);
    private final Pattern AUCTION_PATTERN_DAY = Pattern.compile("itemKey=\"item_day_(.+?)\".*?" + AUCTION_PATTERN, Pattern.DOTALL);
    private final Pattern AUCTION_PATTERN_WEEK = Pattern.compile("itemKey=\"item_week_(.+?)\".*?" + AUCTION_PATTERN, Pattern.DOTALL);

    public Map<String, AuctionItems> getAuctionItems() {
        return auctionItems;
    }

    public boolean parse(String page) {
        Matcher m = AUCTION_TABLE.matcher(page);
        while (m.find()) {
            if (m.group().contains("item_hour")) buildAuctionItems(m.group(), AUCTION_PATTERN_HOUR);
            //if (m.group(max).contains("item_day")) buildAuctionItems(m.group(max), AUCTION_PATTERN_DAY);
            //if (m.group(max).contains("item_week")) buildAuctionItems(m.group(max), AUCTION_PATTERN_WEEK);
        }
        return true;
    }

    @Override
    public String toString() {
        return "AuctionData";
    }

    private boolean buildAuctionItems(String string, Pattern type) {
        if (string == null || string.isEmpty()) return false;
        string = "itemKey=" + string;
        if (!string.contains("itemKey") ||
                !string.contains("auction_item_name_col") ||
                !string.contains("auction_item_type") ||
                !string.contains("auction_item_highest") ||
                !string.contains("auction_item_current") ||
                !string.contains("auction_item_you") ||
                !string.contains("_buyPrice") ||
                !string.contains("_lootId")) return false;
        Matcher m = type.matcher(string);
        if (!m.find()) return false;

        String id = m.group(1);
        AuctionItems r = null;

        if (string.contains("hour")) {
            r = auctionItems.get("item_hour_" + id);
            if (r == null) auctionItems.put(id, r = new AuctionItems()); //need to clean up this somehow
            r.setId("item_hour_" + id);
            r.setAuctionType(AuctionItems.Type.HOUR);
        } else if (string.contains("day")) {
            r = auctionItems.get("item_day_" + id);
            if (r == null) auctionItems.put(id, r = new AuctionItems()); //need to clean up this somehow
            r.setId("item_day_" + id);
            r.setAuctionType(AuctionItems.Type.DAY);
        } else if (string.contains("week")) {
            r = auctionItems.get("item_week_" + id);
            if (r == null) auctionItems.put(id, r = new AuctionItems()); //need to clean up this somehow
            r.setId("item_week_" + id);
            r.setAuctionType(AuctionItems.Type.WEEK);
        }
        if (r == null) auctionItems.put(id, r = new AuctionItems()); //need to clean up this somehow

        r.setName(m.group(2));
        r.setItemType(m.group(3));
        Matcher user = Pattern.compile("showUser=\"(.+?)\"").matcher(m.group(4));
        if (user.find()) {
            long userId = Base62.decode(user.group(1));
            r.setHighestBidderId(userId);
        } else {
            r.setHighestBidderId(1L);
        }
        r.setCurrentBid(Long.parseLong(m.group(5).replace(",", "").replace(".", "")));
        r.setOwnBid(Long.parseLong(m.group(6).replace(",", "").replace(".", "")));
        r.setInstantBuy(Long.parseLong(m.group(7).replace(",", "").replace(".", "")));
        r.setLootId(m.group(8));

        return true;
    }

}

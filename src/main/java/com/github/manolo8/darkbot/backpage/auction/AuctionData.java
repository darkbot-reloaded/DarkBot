package com.github.manolo8.darkbot.backpage.auction;

import com.github.manolo8.darkbot.backpage.dispatch.InProgress;
import com.github.manolo8.darkbot.backpage.dispatch.Retriever;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionData {
    private final Map<String, AuctionItems> auctionItems = new LinkedHashMap<>();
    private final Pattern AUCTION_TABLE = Pattern.compile("<tr class=\"auctionItemRow ([\\S\\s]+?)</tr>", Pattern.DOTALL);

    private final String AUCTION_PATTERN = "auction_item_name_col\">\\s+(.+?)\\s+<.*?" + "auction_item_type\">\\s+(.+?)\\s+<.*?" +"auction_item_highest\" (.+?)>.*?" + "auction_item_current\">\\s+(.+?)\\s+<.*?" + "auction_item_you\">\\s+(.+?)\\s+<.*?" + "item_hour_\\d+_buyPrice\" value=\"(.+?)\".*?" + "item_hour_\\d+_lootId\" value=\"(.+?)\".*?";
    private final Pattern AUCTION_PATTERN_HOUR = Pattern.compile("itemKey=\"item_hour_(.+?)\".*?" + AUCTION_PATTERN, Pattern.DOTALL);
    private final Pattern AUCTION_PATTERN_DAY = Pattern.compile("itemKey=\"item_day_(.+?)\".*?" + AUCTION_PATTERN, Pattern.DOTALL);
    private final Pattern AUCTION_PATTERN_WEEK = Pattern.compile("itemKey=\"item_week_(.+?)\".*?" + AUCTION_PATTERN, Pattern.DOTALL);

    public Map<String, AuctionItems> getAuctionItems() {
        return auctionItems;
    }

    public boolean parse(String page) {
        Matcher m = AUCTION_TABLE.matcher(page);
        if (!m.find()) return false;
        do {
            int max = m.groupCount();
            if (m.group(max).contains("item_hour")) buildAuctionItems(m.group(max), AUCTION_PATTERN_HOUR);
            if (m.group(max).contains("item_day")) buildAuctionItems(m.group(max), AUCTION_PATTERN_DAY);
            if (m.group(max).contains("item_week")) buildAuctionItems(m.group(max), AUCTION_PATTERN_WEEK);

        } while(m.find());
        return true;
    }

    @Override
    public String toString() {
        return "AuctionData";
    }

    private boolean buildAuctionItems(String string, Pattern type) {
        if (string == null || string.isEmpty()) return false;
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
        AuctionItems r = auctionItems.get(id);
        if (r == null) auctionItems.put(id, r = new AuctionItems());

        r.setId(id);
        r.setName(m.group(2));
        r.setItemType(m.group(3));
        Matcher x = Pattern.compile("showUser=\"(.+?)\"").matcher(m.group(4));
        if (x.find()) {
            long user_id = Base62toBase10(x.group(1));
            r.setHighestBidderID(user_id);
        } else {
            r.setHighestBidderID(1L);
        }
        r.setCurrentBid(Long.parseLong(m.group(5).replace(",","")));
        r.setOwnBid(Long.parseLong(m.group(6).replace(",","")));
        r.setInstantBuy(Long.parseLong(m.group(7)));
        r.setLootID(m.group(8));
        if (string.contains("hour")) r.setAuctionType(0);
        if (string.contains("day")) r.setAuctionType(1);
        if (string.contains("week")) r.setAuctionType(2);

        return true;
    }
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE62_BASE = BASE62_ALPHABET.length();

    private static Long Base62toBase10(final String base62) {
        return Base62toBase10(new StringBuilder(base62).reverse().toString().toCharArray());
    }

    private static Long Base62toBase10(final char[] chars) {
        long base10 = 0L;
        for (int i = chars.length - 1; i >= 0; i--) {
            base10 += Base62toBase10(BASE62_ALPHABET.indexOf(chars[i]), i);
        }
        return base10;
    }

    private static int Base62toBase10(final int n, final int pow) {
        return n * (int) Math.pow(BASE62_BASE, pow);
    }

}

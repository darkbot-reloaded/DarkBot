package com.github.manolo8.darkbot.backpage.auction;

import com.github.manolo8.darkbot.utils.Base62;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuctionData {
    private final Map<String, AuctionItems> auctionItems = new LinkedHashMap<>();
    private final Pattern AUCTION_TABLE = Pattern.compile("(itemKey=\"item_[\\S\\s]+?)</tr>", Pattern.DOTALL);

    private final String AUCTION_PATTERN_STRING = "itemKey=\"item_[a-zA-Z]+_(.+?)\".*?" +
            "auction_item_name_col\">\\s+(.+?)\\s+<.*?" +
            "auction_item_type\">\\s+(.+?)\\s+<.*?" +
            "auction_item_highest\" (.+?)>.*?" +
            "auction_item_current\">\\s+(.+?)\\s+<.*?" +
            "auction_item_you\">\\s+(.+?)\\s+<.*?" +
            "item_[a-zA-Z]+_\\d+_buyPrice\" value=\"(.+?)\".*?" +
            "item_[a-zA-Z]+_\\d+_lootId\" value=\"(.+?)\".*?";
    private final Pattern AUCTION_PATTERN = Pattern.compile(AUCTION_PATTERN_STRING, Pattern.DOTALL);

    public Map<String, AuctionItems> getAuctionItems() {
        return auctionItems;
    }

    public Map<String, AuctionItems> getAuctionHourItems() {
        return auctionItems.entrySet().stream().filter(a -> a.getValue().type == AuctionItems.Type.HOUR)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, AuctionItems> getAuctionDayItems() {
        return auctionItems.entrySet().stream().filter(a -> a.getValue().type == AuctionItems.Type.DAY)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, AuctionItems> getAuctionWeekItems() {
        return auctionItems.entrySet().stream().filter(a -> a.getValue().type == AuctionItems.Type.WEEK)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean parse(String page) {
        Matcher m = AUCTION_TABLE.matcher(page);
        auctionItems.forEach((k, v) -> v.setForRemoval(true));
        while (m.find()) {
            buildAuctionItems(m.group(), AUCTION_PATTERN);
        }
        auctionItems.values().removeIf(AuctionItems::getForRemoval);
        return true;
    }

    @Override
    public String toString() {
        return "AuctionData";
    }

    private boolean buildAuctionItems(String string, Pattern type) {
        if (string == null || string.isEmpty()) return false;
        if (string.contains("value=\"\"") ||
                !string.contains("itemKey") ||
                !string.contains("auction_item_name_col") ||
                !string.contains("auction_item_type") ||
                !string.contains("auction_item_highest") ||
                !string.contains("auction_item_current") ||
                !string.contains("auction_item_you") ||
                !string.contains("buyPrice") ||
                !string.contains("lootId")) return false;
        Matcher m = type.matcher(string);
        if (!m.find()) return false;

        String id = m.group(1);
        AuctionItems r;

        if (string.contains("hour")) {
            r = auctionItems.computeIfAbsent("item_hour_" + id, i -> new AuctionItems());
            r.setId("item_hour_" + id);
            r.setAuctionType(AuctionItems.Type.HOUR);
        } else if (string.contains("day")) {
            r = auctionItems.computeIfAbsent("item_day_" + id, i -> new AuctionItems());
            r.setId("item_day_" + id);
            r.setAuctionType(AuctionItems.Type.DAY);
        } else if (string.contains("week")) {
            r = auctionItems.computeIfAbsent("item_week_" + id, i -> new AuctionItems());
            r.setId("item_week_" + id);
            r.setAuctionType(AuctionItems.Type.WEEK);
        } else {
            r = auctionItems.computeIfAbsent(id, i -> new AuctionItems());
        }

        r.setName(m.group(2));
        r.setItemType(m.group(3));
        Matcher user = Pattern.compile("showUser=\"(.+?)\"").matcher(m.group(4));
        if (user.find()) {
            long userId = Base62.decode(user.group(1));
            r.setHighestBidderId(userId);
        } else {
            r.setHighestBidderId(1L);
        }
        r.setCurrentBid(Long.parseLong("0" + m.group(5).replace(",", "").replace(".", "").replace("-", "")));
        r.setOwnBid(Long.parseLong("0" + m.group(6).replace(",", "").replace(".", "").replace("-", "")));
        r.setInstantBuy(Long.parseLong("0" + m.group(7).replace(",", "").replace(".", "").replace("-", "")));
        r.setLootId(m.group(8));
        r.setForRemoval(false);

        return true;
    }

}

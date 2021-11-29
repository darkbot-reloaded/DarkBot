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
    private final Pattern AUCTION_TABLE = Pattern.compile("<tr class=\"auctionItemRow ([\\S\\s]+?)</tr>", Pattern.DOTALL);

    public Map<String, AuctionItems> getAuctionItems() {
        return auctionItems;
    }
    public boolean parse(String page){
        Matcher m = AUCTION_TABLE.matcher(page);
        if (!m.find()) return false;
        do{
            int max = m.groupCount();
            dataBuilder.buildAuctionItems(m.group(max));

        }while(m.find());
        return true;
    }

    @Override
    public String toString() {
        return "AuctionData";
    }

    public class DataBuilder {
        private final Pattern AUCTION_PATTERN = Pattern.compile("itemKey=\"item_hour_(.+?)\".*?" +
                "auction_item_name_col\">\\s+(.+?)\\s+<.*?" +
                "auction_item_type\">\\s+(.+?)\\s+<.*?" +
                "auction_item_highest\" (.+?)>.*?" +
                "auction_item_current\">\\s+(.+?)\\s+<.*?" +
                "auction_item_you\">\\s+(.+?)\\s+<.*?" +
                "item_hour_\\d+_buyPrice\" value=\"(.+?)\".*?" +
                "item_hour_\\d+_lootId\" value=\"(.+?)\".*?", Pattern.DOTALL);

        public boolean buildAuctionItems(String string) {
            if (string == null || string.isEmpty()) return false;
            if (!string.contains("itemKey") ||
                    !string.contains("auction_item_name_col") ||
                    !string.contains("auction_item_type") ||
                    !string.contains("auction_item_highest") ||
                    !string.contains("auction_item_current") ||
                    !string.contains("auction_item_you") ||
                    !string.contains("_buyPrice") ||
                    !string.contains("_lootId")) return false;
            Matcher m = AUCTION_PATTERN.matcher(string);
            if (!m.find()) return false;

            String id = m.group(1);
            AuctionItems r = auctionItems.get(id);
            if (r == null) auctionItems.put(id, r = new AuctionItems());

            r.setId(id);
            r.setName(m.group(2));
            r.setType(m.group(3));
            Matcher x = Pattern.compile("showUser=\"(.+?)\"").matcher(m.group(4));
            if(x.find()){
                long user_id = Base62toBase10(x.group(1));;
                r.setHighestBidderID(user_id);
            }else{
                r.setHighestBidderID(1L);
            }
            r.setCurrentBid(Long.parseLong(m.group(5).replace(",","")));
            r.setOwnBid(Long.parseLong(m.group(6).replace(",","")));
            r.setInstantBuy(Long.parseLong(m.group(7)));
            r.setLootID(m.group(8));
            return true;
        }
    }
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE62_BASE = BASE62_ALPHABET.length();

    public static Long Base62toBase10(final String base62) {
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

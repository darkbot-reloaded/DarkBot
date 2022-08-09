package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.auction.AuctionData;
import com.github.manolo8.darkbot.backpage.auction.AuctionItems;
import com.github.manolo8.darkbot.utils.BetterLogUtils;
import com.github.manolo8.darkbot.utils.http.Method;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionManager {
    private final Main main;
    private final BackpageManager backpage;
    private final AuctionData data;
    private final Pattern AUCTION_ERROR_PATTERN = Pattern.compile("infoText = '(.*?)';.*?" + "icon = '(.*)';", Pattern.DOTALL);
    private long lasAuctionUpdate;

    AuctionManager(Main main, BackpageManager backpage) {
        this.main = main;
        this.backpage = backpage;
        this.data = new AuctionData();
    }

    public AuctionData getData() {
        return data;
    }

    public boolean update(int expiryTime) {
        try {
            if (System.currentTimeMillis() <= lasAuctionUpdate + expiryTime) return false;
            String page = backpage.getConnection("indexInternal.es?action=internalAuction", Method.GET).getContent();

            if (page == null || page.isEmpty()) return false;
            lasAuctionUpdate = System.currentTimeMillis();
            return data.parse(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean bidItem(AuctionItems auctionItem) {
        return bidItem(auctionItem, auctionItem.getCurrentBid() + 10000);
    }

    public boolean bidItem(AuctionItems auctionItem, long amount) {
        try {
            String token = main.backpage.getConnection("indexInternal.es", Method.GET)
                    .setRawParam("action", "internalAuction")
                    .consumeInputStream(main.backpage::getReloadToken);
            String response = main.backpage.getConnection("indexInternal.es", Method.POST)
                    .setRawParam("action", "internalAuction")
                    .setRawParam("reloadToken", token)
                    .setRawParam("auctionType", auctionItem.getAuctionType().getId())
                    .setRawParam("subAction", "bid")
                    .setRawParam("lootId", auctionItem.getLootId())
                    .setRawParam("itemId", auctionItem.getId())
                    .setRawParam("credits", String.valueOf(amount))
                    .setRawParam("auction_buy_button", "BID")
                    .getContent();
            return handleResponse("Bid on Item", auctionItem.getName(), response);
        } catch (Exception e) {
            e.printStackTrace();
            BetterLogUtils.getInstance().PrintLn("Exception Bidding on Item: " + e);
        }
        return false;
    }

    private boolean handleResponse(String type, String id, String response) {
        Matcher m = AUCTION_ERROR_PATTERN.matcher(response);
        boolean valid = false;
        if (m.find()) {
            valid = !m.group(2).contains("error");
            BetterLogUtils.getInstance().PrintLn("AuctionManager: " + type + " (" + id + ") " + (valid ? "succeeded" : "failed") + " : " + m.group(1));
        }
        return valid;
    }
}

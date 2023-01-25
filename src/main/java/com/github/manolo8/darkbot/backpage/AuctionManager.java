package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.auction.AuctionData;
import com.github.manolo8.darkbot.backpage.auction.AuctionItems;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.utils.CaptchaHandler;
import com.github.manolo8.darkbot.utils.http.Method;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionManager {
    private final BackpageManager backpageManager;
    private final AuctionData data;
    private final Pattern AUCTION_ERROR_PATTERN = Pattern.compile("infoText = '(.*?)';.*?" + "icon = '(.*)';", Pattern.DOTALL);
    private long lastAuctionUpdate;
    private final CaptchaHandler captchaHandler;

    AuctionManager(BackpageManager backpageManager, ConfigAPI configAPI) {
        this.backpageManager = backpageManager;
        this.data = new AuctionData();
        this.captchaHandler = new CaptchaHandler(backpageManager, configAPI,
                "indexInternal.es?action=internalAuction", "auction");
    }

    public AuctionData getData() {
        return data;
    }

    @Deprecated
    public boolean update(int expiryTime) {
        return this.update((long) expiryTime);
    }

    public boolean update(long expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastAuctionUpdate + expiryTime) return false;
            if (captchaHandler.isSolvingCaptcha()) return false;
            String page = backpageManager.getConnection("indexInternal.es?action=internalAuction", Method.GET).getContent();
            if (this.captchaHandler.needsCaptchaSolve(page)) {
                return captchaHandler.solveCaptcha();
            }
            lastAuctionUpdate = System.currentTimeMillis();
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
            String token = backpageManager.getConnection("indexInternal.es", Method.GET)
                    .setParam("action", "internalAuction")
                    .consumeInputStream(backpageManager::getReloadToken);
            String response = backpageManager.getConnection("indexInternal.es", Method.POST)
                    .setParam("action", "internalAuction")
                    .setParam("reloadToken", token)
                    .setParam("auctionType", auctionItem.getAuctionType().getId())
                    .setParam("subAction", "bid")
                    .setParam("lootId", auctionItem.getLootId())
                    .setParam("itemId", auctionItem.getId())
                    .setParam("credits", String.valueOf(amount))
                    .setParam("auction_buy_button", "BID")
                    .getContent();
            return handleResponse("Bid on Item", auctionItem.getName(), response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception Bidding on Item: " + e);
        }
        return false;
    }

    private boolean handleResponse(String type, String id, String response) {
        Matcher m = AUCTION_ERROR_PATTERN.matcher(response);
        boolean valid = false;
        if (m.find()) {
            valid = !m.group(2).contains("error");
            System.out.println("AuctionManager: " + type + " (" + id + ") " + (valid ? "succeeded" : "failed") + " : " + m.group(1));
        }
        return valid;
    }
}

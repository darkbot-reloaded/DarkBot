package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.auction.AuctionData;
import com.github.manolo8.darkbot.backpage.auction.AuctionItems;
import com.github.manolo8.darkbot.utils.CaptchaHandler;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.util.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
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
    public Boolean update(int expiryTime) {
        return this.update((long) expiryTime);
    }

    /**
     * @param expiryTime only update if within
     * @return null if update wasn't required (non-expired), true if updated ok, false if update failed
     */
    public Boolean update(long expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastAuctionUpdate + expiryTime) return null;
            if (captchaHandler.isSolvingCaptcha()) return false;
            HttpURLConnection httpURLConnection = backpageManager.getHttp("indexInternal.es?action=internalAuction").getConnection();
            String page = IOUtils.read(httpURLConnection.getInputStream());
            if (this.captchaHandler.needsCaptchaSolve(httpURLConnection.getURL(), page)) {
                System.out.println("AuctionManager: Captcha Detected");
                captchaHandler.solveCaptcha();
                return false;
            }
            lastAuctionUpdate = System.currentTimeMillis();
            return data.parse(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean bidItem(AuctionItems auctionItem) {
        return bidItem(auctionItem, auctionItem.getCurrentBid() + 10000L);
    }

    public boolean bidItem(AuctionItems auctionItem, long amount) {
        try {
            String token = backpageManager.getHttp("indexInternal.es")
                    .setParam("action", "internalAuction")
                    .consumeInputStream(backpageManager::getReloadToken);
            String response = backpageManager.postHttp("indexInternal.es")
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

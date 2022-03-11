package com.github.manolo8.darkbot.utils.login;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;

import static com.github.manolo8.darkbot.utils.login.LoginUtils.cookieManager;

public class LoginBrowser extends Application {

    private final String url = "https://www.darkorbit.com/";
    private String user;
    private String password;

    public void start(Stage primaryStage) {
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        Parameters params = getParameters();
        List<String> list = params.getRaw();
        int c = 0;
        for (String each : list) {
            switch (c) {
                case 0:
                    user = each;
                case 1:
                    password = each;
            }
            c++;
        }

        primaryStage.setTitle("Manual login");
        WebView webView = new WebView();

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                final KeyFrame kf = new KeyFrame(Duration.seconds(1), e -> {
                    if (webView.getEngine().getLocation().equals(url)) {
                        webView.getEngine().executeScript("document.getElementById('qc-cmp2-container').remove();");
                        webView.getEngine().executeScript("document.getElementById('bgcdw_login_form_username').value = " + "'" + user + "'" + ";");
                        webView.getEngine().executeScript("document.getElementById('bgcdw_login_form_password').value = " + "'" + password + "'" + ";");
                    } else if (webView.getEngine().getLocation().contains("indexInternal")) {
                        Platform.exit();
                    } else if (webView.getEngine().getLocation().contains("sas.bpsecure")) {
                        throw new LoginUtils.LoginException("Cross detected");
                    }
                });
                final Timeline timeline = new Timeline(kf);
                Platform.runLater(timeline::play);
            }
        });

        webView.getEngine().setUserAgent("BigpointClient/1.5.2");
        webView.getEngine().loadContent("<html><body></body></html>", "text/html; charset=utf8"); //avoid bug of javafx of user agent
        webView.getEngine().load(url);

        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 1280, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
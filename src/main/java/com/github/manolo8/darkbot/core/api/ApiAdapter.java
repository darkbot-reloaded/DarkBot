package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;

import java.util.function.BooleanSupplier;

public abstract class ApiAdapter implements IDarkBotAPI {

    protected final StartupParams params;
    protected final LoginData loginData;
    protected final BooleanSupplier fullyHide;
    protected boolean autoHidden = false;

    private static final String FALLBACK_STRING = "ERROR";
    private final ByteUtils.StringReader stringReader = new ByteUtils.StringReader(this);

    protected ApiAdapter(StartupParams params, BooleanSupplier fullyHide) {
        this.params = params;
        this.fullyHide = fullyHide;
        this.loginData = this instanceof NoopApiAdapter ? null : LoginUtils.performUserLogin(params);
    }

    protected void relogin() {
        if (loginData == null || loginData.getUsername() == null) {
            System.out.println("Re-logging in is unsupported for this browser/API, or you logged in with SID");
            return;
        }
        try {
            System.out.println("Reloading, updating flash vars/preloader");
            LoginUtils.findPreloader(loginData);
        } catch (LoginUtils.WrongCredentialsException e) {
            System.out.println("Re-logging in: Logging in (1/2)");
            LoginUtils.usernameLogin(loginData);
            System.out.println("Re-logging in: Loading spacemap (2/2)");
            LoginUtils.findPreloader(loginData);
        }
    }

    protected boolean tryHideIfValid(boolean isValid) {
        if (!autoHidden && isValid && params.getAutoHide()) {
            if (fullyHide.getAsBoolean()) setMinimized(true);
            else setVisible(false);
            autoHidden = true;
        }
        return isValid;
    }

    @Override
    public String readMemoryString(long address) {
        return readMemoryStringFallback(address, FALLBACK_STRING);
    }

    @Override
    public String readMemoryStringFallback(long address, String fallback) {
        String str = stringReader.readString(address);
        return str == null ? fallback : str;
    }

    public void setMinimized(boolean visible) {
        this.setVisible(visible);
    }

    public void replaceInt(long addr, int oldValue, int newValue) {
        writeMemoryInt(addr, newValue);
    }

    public long getMemoryUsage() {
        return HeroManager.instance.main.facadeManager.stats.getMemory();
    }

    public void resetCache() {
        stringReader.reset();
    }

    public boolean isInitiallyShown() {
        return true;
    }

}

package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.gui.utils.PidSelector;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.EnumSet;

public class GameAPIImpl<
        W extends GameAPI.Window,
        H extends GameAPI.Handler,
        M extends GameAPI.Memory,
        S extends GameAPI.StringReader,
        I extends GameAPI.Interaction> implements IDarkBotAPI {

    private static final String FALLBACK_STRING = "ERROR";

    protected final StartupParams params;

    protected final W window;
    protected final H handler;
    protected final M memory;
    protected final S stringReader;
    protected final I interaction;

    protected final EnumSet<GameAPI.Capability> capabilities;

    protected final String version;

    protected final LoginData loginData; // Used only if api supports LOGIN
    protected int pid; // Used only if api supports ATTACH
    protected boolean initiallyShown;
    protected boolean autoHidden = false;

    public GameAPIImpl(StartupParams params,
                       W window, H handler, M memory, S stringReader, I interaction,
                       GameAPI.Capability... capabilityArr) {
        this.params = params;

        this.window = window;
        this.handler = handler;
        this.memory = memory;
        this.stringReader = stringReader;
        this.interaction = interaction;

        this.capabilities = EnumSet.noneOf(GameAPI.Capability.class);
        this.capabilities.addAll(Arrays.asList(capabilityArr));

        this.version = window.getVersion() + "w " +
                handler.getVersion() + "h " +
                memory.getVersion() + "m " +
                stringReader.getVersion() + "s " +
                interaction.getVersion() + "i";


        this.loginData = hasCapability(GameAPI.Capability.LOGIN) ? LoginUtils.performUserLogin(params) : null;
        this.initiallyShown = hasCapability(GameAPI.Capability.INITIALLY_SHOWN) && !params.getAutoHide();
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

    public boolean hasCapability(GameAPI.Capability capability) {
        return capabilities.contains(capability);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isInitiallyShown() {
        return initiallyShown;
    }

    @Override
    public void createWindow() {
        if (hasCapability(GameAPI.Capability.LOGIN)) setData();

        if (hasCapability(GameAPI.Capability.ATTACH)) {
            PidSelector pidSelector = new PidSelector(window.getProcesses());

            int result = JOptionPane.showOptionDialog(HeroManager.instance.main.getGui(), pidSelector,
                    "Select flash process", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, null, null);

            if (result != JOptionPane.OK_OPTION) return;

            try {
                this.pid = pidSelector.getPid();
            } catch (NumberFormatException e) {
                Popups.showMessageAsync("Error, invalid PID",
                        "Invalid PID, expected a number", JOptionPane.ERROR_MESSAGE);
            }

            window.openProcess(this.pid);
        } else if (hasCapability(GameAPI.Capability.CREATE_WINDOW_THREAD)) {
            Thread apiThread = new Thread(window::createWindow);
            apiThread.setDaemon(true);
            apiThread.start();
        } else {
            window.createWindow();
        }
    }

    protected void setData() {
        String url = "https://" + loginData.getUrl() + "/", sid = "dosid=" + loginData.getSid();

        window.setData(url, sid, loginData.getPreloaderUrl(), loginData.getParams());
    }

    @Override
    public void setSize(int width, int height) {
        handler.setSize(width, height);
    }

    @Override
    public boolean isValid() {
        boolean isValid = handler.isValid();
        if (!autoHidden && isValid && params.getAutoHide()) {
            setVisible(false, HeroManager.instance.main.config.BOT_SETTINGS.API_CONFIG.FULLY_HIDE_API);
            autoHidden = true;
        }
        return isValid;
    }

    @Override
    public long getMemoryUsage() {
        return handler.getMemoryUsage();
    }

    @Override
    public void mouseMove(int x, int y) {
        interaction.mouseMove(x, y);
    }

    @Override
    public void mouseDown(int x, int y) {
        interaction.mouseDown(x, y);
    }

    @Override
    public void mouseUp(int x, int y) {
        interaction.mouseUp(x, y);
    }

    @Override
    public void mouseClick(int x, int y) {
        interaction.mouseClick(x, y);
    }

    @Override
    public void rawKeyboardClick(char btn) {
        interaction.keyClick(btn);
    }

    @Override
    public void sendText(String string) {
        interaction.sendText(string);
    }

    @Override
    public double readMemoryDouble(long address) {
        return memory.readDouble(address);
    }

    @Override
    public long readMemoryLong(long address) {
        return memory.readLong(address);
    }

    @Override
    public int readMemoryInt(long address) {
        return memory.readInt(address);
    }

    @Override
    public boolean readMemoryBoolean(long address) {
        return memory.readBoolean(address);
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

    @Override
    public byte[] readMemory(long address, int length) {
        return memory.readBytes(address, length);
    }

    @Override
    public void readMemory(long address, byte[] buffer, int length) {
        memory.readBytes(address, buffer, length);
    }

    @Override
    public void writeMemoryInt(long address, int value) {
        memory.writeInt(address, value);
    }

    @Override
    public void writeMemoryLong(long address, long value) {
        memory.writeLong(address, value);
    }

    @Override
    public void writeMemoryDouble(long address, double value) {
        memory.writeDouble(address, value);
    }

    @Override
    public long[] queryMemoryInt(int value, int maxQuantity) {
        return memory.queryInt(value, maxQuantity);
    }

    @Override
    public long[] queryMemoryLong(long value, int maxQuantity) {
        return memory.queryLong(value, maxQuantity);
    }

    @Override
    public long[] queryMemory(byte[] query, int maxQuantity) {
        return memory.queryBytes(query, maxQuantity);
    }

    @Override
    public void setVisible(boolean visible) {
        handler.setVisible(visible);
    }

    @Override
    public void setMinimized(boolean minimized) {
        handler.setMinimized(minimized);
    }

    @Override
    public void resetCache() {
        stringReader.resetCache();
    }

    @Override
    public void handleRefresh() {
        if (hasCapability(GameAPI.Capability.LOGIN)) {
            relogin();
            setData();
        }
        handler.reload();

        stringReader.resetCache();
    }

}

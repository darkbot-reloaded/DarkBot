package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.gui.utils.PidSelector;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.OreAPI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;

public class GameAPIImpl<
        W extends GameAPI.Window,
        H extends GameAPI.Handler,
        M extends GameAPI.Memory,
        S extends GameAPI.StringReader,
        I extends GameAPI.Interaction,
        D extends GameAPI.DirectInteraction> implements IDarkBotAPI {

    private static final String FALLBACK_STRING = "ERROR";

    protected final StartupParams params;

    protected final W window;
    protected final H handler;
    protected final M memory;
    protected final S stringReader;
    protected final I interaction;
    protected final D direct;

    protected final EnumSet<GameAPI.Capability> capabilities;

    protected final String version;

    private final Consumer<Integer> fpsLimitListener; // Needs to be kept as a strong reference to avoid GC

    protected final LoginData loginData; // Used only if api supports LOGIN
    protected int pid; // Used only if api supports ATTACH
    protected boolean initiallyShown;
    protected boolean autoHidden = false;

    protected long lastFailedLogin;

    public GameAPIImpl(StartupParams params,
                       W window, H handler, M memory, S stringReader, I interaction, D direct,
                       GameAPI.Capability... capabilityArr) {
        this.params = params;

        this.window = window;
        this.handler = handler;
        this.memory = memory;
        this.stringReader = stringReader;
        this.interaction = interaction;
        this.direct = direct;

        this.capabilities = EnumSet.noneOf(GameAPI.Capability.class);
        this.capabilities.addAll(Arrays.asList(capabilityArr));

        this.version = window.getVersion() + "w " +
                handler.getVersion() + "h " +
                memory.getVersion() + "m " +
                stringReader.getVersion() + "s " +
                interaction.getVersion() + "i" +
                direct.getVersion() + "d";


        this.loginData = hasCapability(GameAPI.Capability.LOGIN) ? LoginUtils.performUserLogin(params) : null;
        this.initiallyShown = hasCapability(GameAPI.Capability.INITIALLY_SHOWN) && !params.getAutoHide();

        if (hasCapability(GameAPI.Capability.DIRECT_LIMIT_FPS)) {
            ConfigAPI config = HeroManager.instance.main.configHandler;
            ConfigSetting<Integer> maxFps = config.requireConfig("bot_settings.api_config.max_fps");
            maxFps.addListener(fpsLimitListener = direct::setMaxFps);
            direct.setMaxFps(maxFps.getValue());
        } else {
            this.fpsLimitListener = null;
        }
    }

    protected void reload() {
        if (loginData == null || loginData.getUsername() == null) {
            System.out.println("Re-logging in is unsupported for this browser/API, or you logged in with SID");
            return;
        }

        if (lastFailedLogin + 30_000 > System.currentTimeMillis()) {
            System.out.println("Last failed login was <30s ago, ignoring re-login attempt.");
            return;
        }

        try {
            System.out.println("Reloading, updating flash vars/preloader");
            LoginUtils.findPreloader(loginData);
        } catch (IOException e) {
            System.out.println("Failed to find preloader, aborting re-login");
            e.printStackTrace();
            lastFailedLogin = System.currentTimeMillis();
        } catch (LoginUtils.WrongCredentialsException e) {
            // SID probably expired, time to log in again
            relogin();
        }
    }

    protected void relogin() {
        try {
            System.out.println("Re-logging in: Logging in (1/2)");
            LoginUtils.usernameLogin(loginData);
            System.out.println("Re-logging in: Loading spacemap (2/2)");
            LoginUtils.findPreloader(loginData);
        } catch (IOException e) {
            System.err.println("IOException trying to perform re-login, servers may be down");
            e.printStackTrace();
            lastFailedLogin = System.currentTimeMillis();
        } catch (LoginUtils.WrongCredentialsException e) {
            System.err.println("Wrong credentials, check your username and password");
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
    public void tick() {
        window.tick();
        handler.tick();
        memory.tick();
        stringReader.tick();
        interaction.tick();
        direct.tick();
    }

    @Override
    public boolean isInitiallyShown() {
        return initiallyShown;
    }

    @Override
    public void createWindow() {
        if (hasCapability(GameAPI.Capability.LOGIN)) setData();

        if (hasCapability(GameAPI.Capability.BACKGROUND_ONLY)) return;

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
    public void replaceInt(long address, int oldValue, int newValue) {
        memory.replaceInt(address, oldValue, newValue);
    }

    @Override
    public void replaceLong(long address, long oldValue, long newValue) {
        memory.replaceLong(address, oldValue, newValue);
    }

    @Override
    public void replaceDouble(long address, double oldValue, double newValue) {
        memory.replaceDouble(address, oldValue, newValue);
    }

    @Override
    public void replaceBoolean(long address, boolean oldValue, boolean newValue) {
        memory.replaceBoolean(address, oldValue, newValue);
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
            reload();
            setData();
        }
        handler.reload();

        stringReader.resetCache();
    }

    @Override
    public void setMaxFps(int maxFps) {
        direct.setMaxFps(maxFps);
    }

    @Override
    public void lockEntity(int id) {
        direct.lockEntity(id);
    }

    @Override
    public void moveShip(Locatable destination) {
        direct.moveShip(destination);
    }

    @Override
    public void collectBox(Locatable destination, long collectableAddress) {
        direct.collectBox(destination, collectableAddress);
    }

    @Override
    public void refine(long refineUtilAddress, OreAPI.Ore ore, int amount) {
        direct.refine(refineUtilAddress, ore, amount);
    }

    @Override
    public long callMethod(int index, long... arguments) {
        return direct.callMethod(index, arguments);
    }

}

package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.MapNpc;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.gui.utils.PidSelector;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.LibUtils;
import com.github.manolo8.darkbot.utils.OSUtil;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.OreAPI;
import eu.darkbot.util.Timer;
import eu.darkbot.utils.KekkaPlayerProxyServer;
import org.intellij.lang.annotations.Language;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.LongPredicate;

public class GameAPIImpl<
        W extends GameAPI.Window,
        H extends GameAPI.Handler,
        M extends GameAPI.Memory,
        E extends GameAPI.ExtraMemoryReader,
        I extends GameAPI.Interaction,
        D extends GameAPI.DirectInteraction> implements IDarkBotAPI {

    private static final int BAD_PTR = 0xFFFF;
    private static final String FALLBACK_STRING = "ERROR";

    protected final StartupParams params;

    protected final W window;
    protected final H handler;
    protected final M memory;
    protected final E extraMemoryReader;
    protected final I interaction;
    protected final D direct;

    protected final EnumSet<GameAPI.Capability> capabilities;

    protected final String version;

    private final Consumer<Integer> fpsLimitListener; // Needs to be kept as a strong reference to avoid GC

    protected final LoginData loginData; // Used only if api supports LOGIN
    protected int pid; // Used only if api supports ATTACH
    protected boolean initiallyShown;
    protected boolean autoHidden = false;

    private int refreshCount = 0;
    protected long lastFailedLogin;

    protected Timer clearRamTimer = Timer.get(5 * Time.MINUTE);

    private final MapManager mapManager;

    public GameAPIImpl(StartupParams params,
                       W window, H handler, M memory, E extraMemoryReader, I interaction, D direct,
                       GameAPI.Capability... capabilityArr) {
        this.params = params;

        this.window = window;
        this.handler = handler;
        this.memory = memory;
        this.extraMemoryReader = extraMemoryReader;
        this.interaction = interaction;
        this.direct = direct;

        this.capabilities = EnumSet.noneOf(GameAPI.Capability.class);
        this.capabilities.addAll(Arrays.asList(capabilityArr));

        this.version = window.getVersion() + "w " +
                handler.getVersion() + "h " +
                memory.getVersion() + "m " +
                extraMemoryReader.getVersion() + "e " +
                interaction.getVersion() + "i" +
                direct.getVersion() + "d";


        this.loginData = hasCapability(GameAPI.Capability.LOGIN) ? LoginUtils.performUserLogin(params) : null;
        this.initiallyShown = hasCapability(GameAPI.Capability.INITIALLY_SHOWN) && !params.getAutoHide();

        this.mapManager = HeroManager.instance.main.mapManager;

        ConfigAPI config = HeroManager.instance.main.configHandler;
        if (hasCapability(GameAPI.Capability.DIRECT_LIMIT_FPS)) {
            ConfigSetting<Integer> maxFps = config.requireConfig("bot_settings.api_config.max_fps");
            maxFps.addListener(fpsLimitListener = this::setMaxFps);

            HeroManager.instance.main.status.add(running -> setMaxFps(maxFps.getValue()));
            setMaxFps(maxFps.getValue());
        } else {
            this.fpsLimitListener = null;
        }

        if (hasCapability(GameAPI.Capability.PROXY)) {
            ConfigSetting<Boolean> useProxy = config.requireConfig("bot_settings.api_config.use_proxy");
            if (useProxy.getValue() || OSUtil.isWindows7OrLess())
                new KekkaPlayerProxyServer(handler).start();
        }

        if (hasCapability(GameAPI.Capability.HANDLER_FLASH_PATH) && OSUtil.isWindows()) {
            setFlashOcxPath(LibUtils.getFlashOcxPath().toString());
        }

        if (hasCapability(GameAPI.Capability.HANDLER_MIN_CLIENT_SIZE)) {
            setMinClientSize(800, 600); // API window can be smaller, but game client cannot
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
        } catch (LoginUtils.CaptchaException e) {
            System.err.println("Captcha detected & no Captcha-Solver is configured");
            lastFailedLogin = System.currentTimeMillis() + Time.MINUTE * 5; // wait minimum 5:30m to next login attempt on refresh
        } catch (LoginUtils.LoginException e) {
            System.err.println("Wrong credentials, check your username and password");
        }
    }

    @Override
    public int getRefreshCount() {
        return refreshCount;
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
        extraMemoryReader.tick();
        interaction.tick();
        direct.tick();

        if (hasCapability(GameAPI.Capability.HANDLER_CLEAR_RAM) && clearRamTimer.tryActivate())
            emptyWorkingSet();
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

            int result = Popups.of("Select flash process", pidSelector, JOptionPane.QUESTION_MESSAGE)
                    .optionType(JOptionPane.OK_CANCEL_OPTION)
                    .parent(HeroManager.instance.main.getGui())
                    .showOptionSync();

            if (result != JOptionPane.OK_OPTION) return;

            try {
                this.pid = pidSelector.getPid();
            } catch (NumberFormatException e) {
                Popups.of("Error, invalid PID",
                        "Invalid PID, expected a number", JOptionPane.ERROR_MESSAGE).showAsync();
            }

            window.openProcess(this.pid);
        } else if (hasCapability(GameAPI.Capability.CREATE_WINDOW_THREAD)) {
            Thread apiThread = new Thread(window::createWindow, "API thread");
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
    public double getCpuUsage() {
        return handler.getCpuUsage();
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

    private char lastChar;
    private final Timer keyClickTimer = Timer.get(500);
    @Override
    public void rawKeyboardClick(char btn, boolean deduplicate) {
        if (!deduplicate || (lastChar != btn || keyClickTimer.isInactive())) {
            interaction.keyClick(lastChar = btn);
            keyClickTimer.activate();
        }
    }

    @Override
    public void sendText(String string) {
        interaction.sendText(string);
    }

    @Override
    public double readMemoryDouble(long address) {
        if (address <= BAD_PTR) return 0;
        return memory.readDouble(address);
    }

    @Override
    public long readMemoryLong(long address) {
        if (address <= BAD_PTR) return 0;
        return memory.readLong(address);
    }

    @Override
    public int readMemoryInt(long address) {
        if (address <= BAD_PTR) return 0;
        return memory.readInt(address);
    }

    @Override
    public boolean readMemoryBoolean(long address) {
        if (address <= BAD_PTR) return false;
        return memory.readBoolean(address);
    }

    @Override
    public String readMemoryString(long address) {
        if (address <= BAD_PTR) return FALLBACK_STRING;
        return readMemoryStringFallback(address, FALLBACK_STRING);
    }

    @Override
    public String readMemoryStringFallback(long address, String fallback) {
        if (address <= BAD_PTR) return fallback;

        String str = extraMemoryReader.readString(address);
        return str == null ? fallback : str;
    }

    @Override
    public byte[] readMemory(long address, int length) {
        if (address <= BAD_PTR) return new byte[0];
        return memory.readBytes(address, length);
    }

    @Override
    public void readMemory(long address, byte[] buffer, int length) {
        if (address <= BAD_PTR) {
            Arrays.fill(buffer, 0, length, (byte) 0);
            return;
        }
        memory.readBytes(address, buffer, length);
    }

    @Override
    public void replaceInt(long address, int oldValue, int newValue) {
        if (address <= BAD_PTR) return;
        memory.replaceInt(address, oldValue, newValue);
    }

    @Override
    public void replaceLong(long address, long oldValue, long newValue) {
        if (address <= BAD_PTR) return;
        memory.replaceLong(address, oldValue, newValue);
    }

    @Override
    public void replaceDouble(long address, double oldValue, double newValue) {
        if (address <= BAD_PTR) return;
        memory.replaceDouble(address, oldValue, newValue);
    }

    @Override
    public void replaceBoolean(long address, boolean oldValue, boolean newValue) {
        if (address <= BAD_PTR) return;
        memory.replaceBoolean(address, oldValue, newValue);
    }

    @Override
    public void writeMemoryInt(long address, int value) {
        if (address <= BAD_PTR) return;
        memory.writeInt(address, value);
    }

    @Override
    public void writeMemoryLong(long address, long value) {
        if (address <= BAD_PTR) return;
        memory.writeLong(address, value);
    }

    @Override
    public void writeMemoryDouble(long address, double value) {
        if (address <= BAD_PTR) return;
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
    public long queryMemory(byte... query) {
        return memory.queryBytes(query);
    }

    @Override
    public long searchClassClosure(LongPredicate pattern) {
        return extraMemoryReader.searchClassClosure(pattern);
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
        extraMemoryReader.resetCache();
    }

    @Override
    public void handleRefresh() {
        if (hasCapability(GameAPI.Capability.LOGIN)) {
            reload();
            setData();
        }

        refreshCount++;
        handler.reload();

        extraMemoryReader.resetCache();
    }

    @Override
    public void handleRelogin() {
        if (hasCapability(GameAPI.Capability.LOGIN))
            relogin();
    }

    @Override
    public void setMaxFps(int maxFps) {
        direct.setMaxFps(HeroManager.instance.main.isRunning() ? maxFps : 0);
    }

    @Override
    public void lockEntity(int id) {
        direct.lockEntity(id);
    }

    @Override
    public void selectEntity(Entity entity) {
        if (mapManager.mapClick(true)) {
            if (entity instanceof Lockable) {
                //assuming that selectEntity selects only ships & is supported by every API
                //actually this should be called on every entity with LockType trait

                if (mapManager.isTarget(entity)) return;
                if (!(entity instanceof Ship) || entity instanceof MapNpc) {
                    if (!mapManager.setTarget(entity.address)) {
                        return;
                    }
                } /*else mapManager.setTarget(0);*/

                direct.selectEntity(entity);
            } else {
                entity.clickable.click();
            }
        }
    }

    @Override
    public void moveShip(Locatable destination) {
        if (mapManager.mapClick(false)) {
            direct.moveShip(destination);
        }
    }

    @Override
    public void collectBox(Box box) {
        if (mapManager.mapClick(true)) {
            direct.collectBox(box);
        }
    }

    @Override
    public void refine(long refineUtilAddress, OreAPI.Ore ore, int amount) {
        direct.refine(refineUtilAddress, ore, amount);
    }

    @Override
    public long callMethod(int index, long... arguments) {
        return direct.callMethod(index, arguments);
    }

    @Override
    public boolean callMethodChecked(boolean checkName, String signature, int index, long... arguments) {
        return direct.callMethodChecked(checkName, signature, index, arguments);
    }

    @Override
    public boolean callMethodAsync(int index, long... arguments) {
        return direct.callMethodAsync(index, arguments);
    }

    @Override
    public boolean useItem(Item item) {
        throw new UnsupportedOperationException("useItem not implemented!");
    }

    @Override
    public boolean isUseItemSupported() {
        return false;
    }

    @Override
    public void postActions(long... actions) {
        throw new UnsupportedOperationException("postActions not implemented!");
    }

    @Override
    public void pasteText(String text, long... actions) {
        throw new UnsupportedOperationException("pasteText not implemented!");
    }

    @Override
    public void clearCache(@Language("RegExp") String pattern) {
        handler.clearCache(pattern);
    }

    @Override
    public void emptyWorkingSet() {
        handler.emptyWorkingSet();
    }

    @Override
    public void setLocalProxy(int port) {
        handler.setLocalProxy(port);
    }

    @Override
    public void setPosition(int x, int y) {
        handler.setPosition(x, y);
    }

    @Override
    public void setFlashOcxPath(String path) {
        handler.setFlashOcxPath(path);
    }

    @Override
    public void setUserInput(boolean enable) {
        handler.setUserInput(enable);
    }

    @Override
    public void setClientSize(int width, int height) {
        handler.setClientSize(width, height);
    }

    @Override
    public void setMinClientSize(int width, int height) {
        handler.setMinClientSize(width, height);
    }

    @Override
    public void setTransparency(int transparency) {
        handler.setTransparency(transparency);
    }

    @Override
    public void setVolume(int volume) {
        handler.setVolume(volume);
    }

    @Override
    public void setQuality(GameAPI.Handler.GameQuality quality) {
        handler.setQuality(quality.ordinal());
    }
}

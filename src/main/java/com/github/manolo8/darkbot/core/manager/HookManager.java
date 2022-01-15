package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.utils.Location;
import eu.darkbot.api.API;
import eu.darkbot.api.DarkHook;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.hook.HookFlag;
import eu.darkbot.api.hook.JNIUtil;
import eu.darkbot.api.hook.NativeCallback;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Manager used as DirectInteraction api in darkboat with darkhook api.
 * The instance will always exist, but tick() will only be called if it's in use, therefore, any initialization
 * will be done on tick.
 */
public class HookManager implements Manager, GameAPI.DirectInteraction, API.Singleton {

    private final Main main;
    private final HeroManager hero;
    private @Nullable DarkHook hook;

    private boolean canLoad = true;
    private long guiAddress;
    private long staticEventAddress;
    private int lastFps;
    private List<CallbackHolder> callbacks;

    public HookManager(Main main) {
        this.main = main;
        this.hero = main.hero;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(state -> clear());
        botInstaller.guiManagerAddress.add(value -> guiAddress = value);
        botInstaller.screenManagerAddress.add(value -> staticEventAddress = value + 200);
    }

    public void tick() {
        setup();
        if (hook == null) return; // Hook is disabled

        if (callbacks != null)
            callbacks.forEach(CallbackHolder::checkCallback);

        tryHook();
        setMaxFps(main.config.BOT_SETTINGS.API_CONFIG.MAX_FPS);
    }

    private void setup() {
        boolean isEnabled = canLoad && Main.API instanceof DarkBoatAdapter &&
                main.config.BOT_SETTINGS.API_CONFIG.DARK_HOOK.ENABLED;

        if ((hook != null) == isEnabled) return;

        if (hook == null) {
            try {
                hook = new DarkHook();
            } catch (UnsatisfiedLinkError e) {
                canLoad = false;
                System.out.println("Darkhook could not be enabled as the dll is not present.");
                e.printStackTrace();
            }
        } else {
            clear();
            hook = null;
        }
    }

    public boolean isHookEnabled() {
        return hook != null;
    }

    public boolean isTravelEnabled() {
        return isHookEnabled() && main.config.BOT_SETTINGS.API_CONFIG.DARK_HOOK.TRAVEL;
    }

    public boolean isCollectEnabled() {
        return isHookEnabled() && main.config.BOT_SETTINGS.API_CONFIG.DARK_HOOK.COLLECT;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void setMaxFps(int maxFps) {
        if (hook == null) throw new IllegalStateException("Tried to setFps while hook isn't initialized");
        if (lastFps == maxFps) return;
        hook.setMaxCps(lastFps = maxFps);
    }

    @Override
    public void lockEntity(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveShip(Locatable dest) {
        callMethodAsync(10, Main.API.readMemoryLong(staticEventAddress), (long) dest.getX(), (long) dest.getY());
    }

    @Override
    public void collectBox(Locatable dest, long boxAddr) {
        callMethodSync(10, Main.API.readMemoryLong(staticEventAddress), (long) dest.getX(), (long) dest.getY(), boxAddr);
    }

    @Override
    public long callMethod(int index, long... arguments) {
        return callMethodSync(index, arguments);
    }


    public long callMethodSync(int methodIdx, long... args) {
        if (hook == null) throw new IllegalStateException("Tried to callMethodSync while hook isn't initialized");
        return hook.callMethodSync(methodIdx, args);
    }

    public boolean callMethodAsync(int methodIdx, long... args) {
        if (hook == null) throw new IllegalStateException("Tried to callMethodASync while hook isn't initialized");
        return hook.callMethodAsync(methodIdx, args);
    }

    private void tryHook() {
        if (hook == null) throw new IllegalStateException("Tried to tryHook while hook isn't initialized");

        if (!hook.isTaskRunnerValid() && hero.address > 0xFFFF)
            hook.setTaskRunnerHook(guiAddress, 3, HookFlag.ENV.ordinal());
    }

    private void clear() {
        if (hook == null) return;
        hook.clearTaskRunner();
        hook.clearAllCallbacks();
    }

    public boolean addMethodCallback(Object object, Supplier<Long> scriptObject) {
        return addMethodCallback(object, scriptObject, 0);
    }

    public boolean addMethodCallback(Object object, Supplier<Long> scriptObject, int callbackId) {
        if (hook == null) return false;
        if (callbacks == null)
            callbacks = new ArrayList<>();

        for (Method m : object.getClass().getDeclaredMethods()) {
            NativeCallback nativeCallback = m.getAnnotation(NativeCallback.class);

            if (nativeCallback != null && nativeCallback.callbackId() == callbackId)
                callbacks.add(new CallbackHolder(object, scriptObject, m, nativeCallback));
        }

        return true;
    }

    /**
     * Remove any callback associated with given object.
     * @return true if any callback was removed.
     */
    public boolean removeCallbacks(Object object) {
        if (hook == null || callbacks == null) return false;
        return callbacks.removeIf(callbackHolder -> callbackHolder.remove(object));
    }

    private class CallbackHolder {
        private final Object object;
        private final Supplier<Long> scriptObject;

        private final Method method;
        private final NativeCallback callback;

        private long methodEnv;

        public CallbackHolder(Object object, Supplier<Long> scriptObject, Method method, NativeCallback callback) {
            this.object = object;
            this.scriptObject = scriptObject;
            this.method = method;
            this.callback = callback;
        }

        private boolean remove(Object o) {
            if (hook != null && object == o) {
                hook.clearCallback(methodEnv);
                return true;
            }
            return false;
        }

        private void checkCallback() {
            if (hook == null) return; //can't check if hook is null

            if (methodEnv <= 0 || !hook.isCallbackValid(methodEnv)) {
                if (methodEnv != 0) hook.clearCallback(methodEnv); //already hooked? remove old methodEnv entry in native map

                long address = scriptObject.get();
                if (address <= 0xFFFF) return; //invalid address, return

                methodEnv = hook.setMethodCallback(address, callback.methodIdx(),
                        callback.hookFlag().ordinal(), object, method.getName(), JNIUtil.getJNIMethodSignature(method));
            }
        }
    }
}

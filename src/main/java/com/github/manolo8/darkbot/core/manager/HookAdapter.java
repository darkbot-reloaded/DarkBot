package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.API;
import eu.darkbot.api.DarkHook;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.hook.HookFlag;
import eu.darkbot.api.hook.JNIUtil;
import eu.darkbot.api.hook.NativeCallback;
import eu.darkbot.api.managers.OreAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Manager used as DirectInteraction api in darkboat with darkhook api.
 * The instance will only exist if darkboat with darkhook API is in use.
 */
public class HookAdapter implements GameAPI.DirectInteraction, API.Singleton {

    private final Main main;
    private final HeroManager hero;
    private final DarkHook hook;

    private long guiAddress;
    private long staticEventAddress;
    private int lastFps;
    private List<CallbackHolder> callbacks;

    public HookAdapter(Main main, HeroManager hero, PluginAPI pluginAPI, BotInstaller botInstaller) {
        this.main = main;
        this.hero = hero;
        this.hook = createDarkHook(pluginAPI);

        botInstaller.invalid.add(state -> {
            if (hook == null) return;
            hook.clearTaskRunner();
            hook.clearAllCallbacks();
        });
        botInstaller.guiManagerAddress.add(value -> guiAddress = value);
        botInstaller.screenManagerAddress.add(value -> staticEventAddress = value + 200);
    }

    private static DarkHook createDarkHook(PluginAPI api) {
        try {
            return api.requireInstance(DarkHook.class);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("DarkHook could not be enabled as the dll is not present.");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void tick() {
        if (isHookDisabled()) return;

        if (callbacks != null)
            callbacks.forEach(CallbackHolder::checkCallback);

        if (!hook.isTaskRunnerValid() && ByteUtils.isValidPtr(hero.address))
            hook.setTaskRunnerHook(guiAddress, 3, HookFlag.ENV.ordinal());
    }

    public boolean isHookDisabled() {
        return hook == null;
    }

    public boolean isEnabled(Flag flag) {
        return !isHookDisabled() && main.config.BOT_SETTINGS.API_CONFIG.DARK_HOOK_FLAGS.contains(flag);
    }

    private void checkEnabled(String operation) {
        if (isHookDisabled())
            throw new IllegalStateException("Tried to " + operation + " while hook isn't initialized");
    }

    // Direct interaction api implementation
    @Override
    public int getVersion() {
        return hook.getVersion();
    }

    @Override
    public void setMaxFps(int maxFps) { // NOTE: unused, max fps is now enforced via dark boat
        checkEnabled("set max fps");
        if (lastFps == maxFps) return;
        hook.setMaxCps(lastFps = maxFps);
    }

    @Override
    public void lockEntity(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectEntity(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveShip(Locatable dest) {
        checkEnabled("move ship");
        callMethodAsync(10, Main.API.readMemoryLong(staticEventAddress), (long) dest.getX(), (long) dest.getY());
    }

    @Override
    public void collectBox(Box box) {
        checkEnabled("collect box");
        callMethod(10, Main.API.readMemoryLong(staticEventAddress), (long) box.getX(), (long) box.getY(), box.address);
    }

    @Override
    public void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount) {
        checkEnabled("refine");
        hook.refine(refineUtilAddress, oreType.getId(), amount);
    }

    @Override
    public long callMethod(int index, long... arguments) {
        checkEnabled("call method");
        return hook.callMethodSync(index, arguments);
    }

    @Override
    public boolean callMethodAsync(int index, long... arguments) {
        checkEnabled("call method async");
        return hook.callMethodAsync(index, arguments);
    }

    @Configuration("config.bot_settings.api_config.dark_hook_flags")
    public enum Flag {
        TRAVEL(Capability.DIRECT_MOVE_SHIP),
        COLLECT(Capability.DIRECT_COLLECT_BOX),
        REFINE(Capability.DIRECT_REFINE);

        private final Capability capability;

        Flag(Capability capability) {
            this.capability = capability;
        }

        public static @Nullable Flag of(Capability capability) {
            for (Flag flag : values())
                if (flag.capability == capability)
                    return flag;
            return null;
        }
    }

    // Callbacks

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
                if (ByteUtils.isValidPtr(address)) {
                    methodEnv = hook.setMethodCallback(address, callback.methodIdx(),
                            callback.hookFlag().ordinal(), object, method.getName(), JNIUtil.getJNIMethodSignature(method));
                }
            }
        }
    }
}

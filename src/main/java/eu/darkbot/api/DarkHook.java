package eu.darkbot.api;

import com.github.manolo8.darkbot.utils.LibUtils;
import eu.darkbot.api.hook.NativeCallbackManager;
import eu.darkbot.api.hook.NativeTaskRunner;

import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class DarkHook implements NativeTaskRunner, NativeCallbackManager, API.Singleton {
    static {
        LibUtils.loadLibrary("DarkHookAPI");
    }

    private final @Native LongBuffer buffer = ByteBuffer
            .allocateDirect(32 * Long.BYTES)
            .order(ByteOrder.nativeOrder())
            .asLongBuffer();

    private final @Native ByteBuffer callbackBuffer = ByteBuffer
            .allocateDirect(32 * Long.BYTES)
            .order(ByteOrder.nativeOrder());

    public DarkHook() {
        onLoad();
    }

    @Override
    public ByteBuffer getCallbackBuffer() {
        return callbackBuffer;
    }

    /* Native */
    private native void onLoad();

    public native int getVersion();
    public native void setMaxCps(int maxCps);
    public native void refine(long refineUtilAddress, int oreId, int amount);

    /* Native Task Runner*/
    public @Override native boolean clearTaskRunner();
    public @Override native boolean isTaskRunnerValid();
    public @Override native boolean setTaskRunnerHook(long scriptObject, int methodIdx, int hookFlag);

    @Override
    public long callMethodSync(int methodIdx, long... args) {
        return callMethod(methodIdx, true, args);
    }

    @Override
    public boolean callMethodAsync(int methodIdx, long... args) {
        return callMethod(methodIdx, false, args) == 1;
    }

    public long callMethod(int methodIdx, boolean sync, long... args) {
        buffer.clear();
        for (long arg : args) buffer.put(arg);
        return callMethod(methodIdx, args.length, sync);
    }

    /** Requires arguments to be put in buffer, see {@link #callMethod(int, boolean, long...)} */
    private native long callMethod(int methodIdx, int argc, boolean sync);


    /* NativeCallbackManager */
    public @Override native void    clearAllCallbacks();
    public @Override native boolean clearCallback(long methodEnv);
    public @Override native boolean isCallbackValid(long methodEnv);
    public @Override native long    setMethodCallback(long scriptObject, int methodIdx, int hookFlag,
                                                      Object object, String methodName, String methodSignature);
}

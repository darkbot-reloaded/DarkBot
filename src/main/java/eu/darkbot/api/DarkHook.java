package eu.darkbot.api;

import com.github.manolo8.darkbot.utils.LibUtils;
import eu.darkbot.api.hook.NativeCallbackManager;
import eu.darkbot.api.hook.NativeTaskRunner;

import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class DarkHook implements NativeTaskRunner, NativeCallbackManager {
    private final @Native LongBuffer buffer = ByteBuffer
            .allocateDirect(32 * Long.BYTES)
            .order(ByteOrder.nativeOrder())
            .asLongBuffer();

    private final @Native ByteBuffer callbackBuffer = ByteBuffer
            .allocateDirect(32 * Long.BYTES)
            .order(ByteOrder.nativeOrder());

    public DarkHook() throws UnsatisfiedLinkError {
            LibUtils.loadLibrary("DarkHookAPI");
            onLoad();
    }

    private native void onLoad();

    public native void setMaxCps(int maxCps);
    public native boolean clearTaskRunner();
    public native boolean isTaskRunnerValid();
    public native boolean setTaskRunnerHook(long scriptObject, int methodIdx, int hookFlag);

    @Override
    public long callMethodSync(int methodIdx, long... args) {
        return callMethod(methodIdx, true, args);
    }

    @Override
    public boolean callMethodAsync(int methodIdx, long... args) {
        return callMethod(methodIdx, false, args) == 1;
    }

    private long callMethod(int methodIdx, boolean sync, long... args) {
        LongBuffer buf = buffer;
        buf.clear();
        for (long arg : args)
            buf.put(arg);

        return callMethod(methodIdx, args.length, sync);
    }

    private native long callMethod(int methodIdx, int argc, boolean sync);

    @Override
    public ByteBuffer getCallbackBuffer() {
        return callbackBuffer;
    }

    //Callback Manager
    public native void clearAllCallbacks();
    //returns true if there was removed MethodEnv from native map
    public native boolean clearCallback(long methodEnv);
    public native boolean isCallbackValid(long methodEnv);
    /** @return methodEnv address if valid, 0 otherwise */
    public native long setMethodCallback(long scriptObject, int methodIdx, int hookFlag,
                                          Object object, String methodName, String methodSignature);
}

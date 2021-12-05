package eu.darkbot.api.hook;

import java.nio.ByteBuffer;

public interface NativeCallbackManager {

    ByteBuffer getCallbackBuffer();

    void clearAllCallbacks();
    boolean clearCallback(long methodEnv);
    boolean isCallbackValid(long methodEnv);

    /**
     * The callback function should look like this:
     * <pre>
     *     \@NativeCallback(31, HookFlag.INVOKER)
     *     int callback(long methodEnv, int argc, int methodId) {
     *
     *         return 0; // to call original method with original values.
     *         return -1; // to return 0 in native code without call to original method.
     *         return argc; // to call original method with custom argc and argv(callback buffer)
     *     }
     * </pre>
     *
     * @return methodEnv address if valid, 0 otherwise
     */
    long setMethodCallback(long scriptObject, int methodIdx, int hookFlag,
                           Object object, String methodName, String methodSignature);

}

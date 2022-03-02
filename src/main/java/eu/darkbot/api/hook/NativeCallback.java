package eu.darkbot.api.hook;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NativeCallback {
    //native method index in object
    int methodIdx();

    //flag where to hook
    HookFlag hookFlag() default HookFlag.ENV;

    //Used by HookManager to know which methods in given object set as callback with provided ScriptObject
    int callbackId() default 0;
}

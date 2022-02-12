package eu.darkbot.api.hook;

public interface NativeTaskRunner {

    boolean clearTaskRunner();
    boolean isTaskRunnerValid();
    boolean setTaskRunnerHook(long scriptObject, int methodIdx, int hookFlag);

    long callMethodSync(int methodIdx, long... args);
    boolean callMethodAsync(int methodIdx, long... args);
}

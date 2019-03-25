package com.github.manolo8.darkbot.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public interface IDarkBotAPI {

    void createWindow();

    boolean isValid();

    void mousePress(int x, int y);

    void mouseMove(int x, int y);

    void mouseRelease(int x, int y);

    void mouseClick(int x, int y);

    void keyboardClick(char btn);

    default void keyboardClick(Character ch) {
        if (ch != null) keyboardClick((char) ch);
    }

    double readMemoryDouble(long address);

    long readMemoryLong(long address);

    int readMemoryInt(long address);

    boolean readMemoryBoolean(long address);

    String readMemoryString(long address);

    byte[] readMemory(long address, int length);

    void writeMemoryDouble(long address, double value);

    void writeMemoryLong(long address, long value);

    void writeMemoryInt(long address, int value);

    long[] queryMemoryInt(int value, int maxQuantity);

    long[] queryMemoryLong(long value, int maxQuantity);

    long[] queryMemory(byte[] query, int maxQuantity);

    void setVisible(boolean visible);

    void refresh();

    static LoggingAPIHandler getLoggingHandler() {
        return new LoggingAPIHandler();
    }
}
class LoggingAPIHandler implements InvocationHandler {

    private DarkBotAPI API = new DarkBotAPI();

    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        boolean log = method.getName().startsWith("write") && !method.getName().equals("writeMemoryDouble");
        if (log) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (int i = 3; i < trace.length - 3; i++) System.out.println(trace[i]);
            System.out.println("API CALL: " + method.getName() + (args != null ? Arrays.toString(args) : ""));
        }
        Object res = method.invoke(API, args);
        if (res != null && log) System.out.println("  -> " + res);
        return res;
    }

}

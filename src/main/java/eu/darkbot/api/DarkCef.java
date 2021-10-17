package eu.darkbot.api;

import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface DarkCef {

    Path DARK_CEF = Paths.get("lib", "DarkCef.jar");

    static DarkCef getInstance() {
        return ReflectionUtils.createInstance("eu.darkbot.browser.DarkCefImpl", DARK_CEF);
    }

    void    setData(String url, String sid, String preloader, String vars);
    void    createWindow();
    void    setSize(int width, int height);
    void    setVisible(boolean visible);

    void    setMinimized(boolean visible);
    void    reload();
    boolean isValid();
    long    getMemoryUsage();

    void keyClick  (int keyCode);
    void sendText  (String text);

    void mouseMove (int x, int y);
    void mouseDown (int x, int y);
    void mouseUp   (int x, int y);
    void mouseClick(int x, int y);

}
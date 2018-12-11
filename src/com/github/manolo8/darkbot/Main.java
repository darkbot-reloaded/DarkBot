package com.github.manolo8.darkbot;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.def.Module;
import com.github.manolo8.darkbot.core.def.WindowsAPI;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.gui.MainForm;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

public class Main extends Thread {

    public static final Object UPDATE_LOCKER = new Object();

    public static WindowsAPI API;

    public final Config config;
    public final MapManager mapManager;
    public final StarManager starManager;
    public final HeroManager hero;
    public final GuiManager guiManager;

    public Module module;

    private final BotManager botManager;
    private final MainForm form;

    private volatile boolean running;

    public Main() throws IOException {
        API = new WindowsAPI();
        config = new Config();

        botManager = new BotManager();

        guiManager = new GuiManager(this);
        starManager = new StarManager();
        mapManager = new MapManager(this);
        hero = new HeroManager(this);

        botManager.add(guiManager);
        botManager.add(mapManager);
        botManager.add(hero);

        form = new MainForm(this);

        JFrame frame = new JFrame("DarkBOT");

        frame.setContentPane(form.content);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setIconImage(ImageIO.read(getClass().getResource("/resources/icon.png")));

        setModule(new LootModule(this));

        start();
    }

    @Override
    public void run() {
        long time;

        while (true) {
            time = System.currentTimeMillis();


            if (botManager.isInvalid()) {
                botManager.install();
                sleepMax(time, 5000);
            } else {

                hero.tick();
                mapManager.tick();

                if (running && guiManager.canTickModule()) {
                    guiManager.tick();
                    hero.checkMove();
                    module.tick();
                }

                form.tick();

                sleepMax(time, 100);
            }

        }
    }

    public <A extends Module> A setModule(A module) {
        this.module = module;
        this.module.install();
        return module;
    }

    public void setRunning(boolean running) {
        if (this.running != running) {

            hero.statistics.toggle(running);

            this.running = running;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void updateConfig() {
        switch (config.CURRENT_MODULE) {
            case 0:
                if (isModule(CollectorModule.class)) {
                    setModule(new CollectorModule(this));
                }
                break;
            case 1:
                if (isModule(LootModule.class)) {
                    setModule(new LootModule(this));
                }
                break;
        }
    }

    private boolean isModule(Class clazz) {
        return module != null && module.getClass() == clazz;
    }

    private void sleepMax(long time, int total) {
        time = System.currentTimeMillis() - time;
        if (time < total) {
            try {
                Thread.sleep(total - time);
            } catch (InterruptedException ignored) {
            }
        }
    }
}

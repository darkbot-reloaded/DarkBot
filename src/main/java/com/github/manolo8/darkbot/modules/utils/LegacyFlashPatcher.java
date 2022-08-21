package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.api.DarkBoatHookAdapter;
import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LegacyFlashPatcher {

    private final List<Fixer> FIXERS = Arrays.asList(
            new FlashInstaller(),
            new FlashConfigSetter());

    protected void runPatcher() {
        if (!(Main.API instanceof DarkBoatAdapter || Main.API instanceof DarkBoatHookAdapter)) {
            // Linux APIs or kekka player do not require this at all. Only darkboat requires it.
            return;
        }
        List<Fixer> needFixing = FIXERS.stream().filter(Fixer::needsFix).collect(Collectors.toList());

        if (needFixing.isEmpty()) return;

        Popups.of("Flash installer & patcher",
                        "Flash is nowadays uninstalled by default in windows updates.\n" +
                                "Darkbot will need a one-time admin permission to install flash and make it work.\n" +
                                "Accept on the next pop-up to run as admin to be able to continue using DarkBot.\n" +
                                "After the script runs, refresh the game or restart the bot to make it work.\n",
                        JOptionPane.INFORMATION_MESSAGE)
                .showSync(); //TODO translate text

        for (Fixer fixer : needFixing) fixer.ApplyPatch(); //TODO make loop for force check if sha256 is ok
    }

    protected void cleanupCache() {
        try {
            // Delete cookies
            Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
            // Delete temp files
            Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
            // Delete form data
            Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private interface Fixer {
        boolean needsFix();

        void ApplyPatch();
    }

    private static class FlashInstaller implements Fixer {
        private static final Path
                FLASH_DIR = Paths.get(System.getenv("APPDATA"), "DarkBot", "Flash"),
                FLASH_OCX = FLASH_DIR.resolve("Flash.ocx"),
                REG_SVR = Paths.get(System.getenv("WINDIR"), "SysWOW64", "regsvr32");


        public boolean needsFix() {
            return !Files.exists(FLASH_DIR) || !Files.exists(FLASH_OCX);
        }

        public void ApplyPatch() {
            File file = new File(FLASH_DIR.toUri());
            file.mkdirs();
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://darkbot.eu/downloads/Flash.ocx").openStream());
                 FileOutputStream out = new FileOutputStream(FLASH_OCX.toAbsolutePath().toFile())) {
                final byte[] data = new byte[1024];
                int count;

                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class FlashConfigSetter implements Fixer {
        private static final Path
                FLASH_FOLDER = Paths.get(System.getenv("WINDIR"), "SysWOW64", "Macromed", "Flash"),
                FLASH_CONFIG = FLASH_FOLDER.resolve("mms.cfg"),
                TMP_CONFIG = Paths.get("mms.cfg");


        private static final List<String> CONFIG_CONTENT = Arrays.asList(
                "EnableAllowList=1",
                "AllowListPreview=1",
                "AllowListRootMovieOnly=1",
                "AllowListUrlPattern=https://*.bpsecure.com/",
                "SilentAutoUpdateEnable=1",
                "EnableWhiteList=1",
                "WhiteListPreview=1",
                "WhiteListRootMovieOnly=1",
                "WhiteListUrlPattern=https://*.bpsecure.com/",
                "AutoUpdateDisable=1",
                "EOLUninstallDisable=1");

        public boolean needsFix() {
            if (!Files.exists(FLASH_FOLDER) || !Files.exists(FLASH_CONFIG)) return true;
            try {
                return !Files.readAllLines(FLASH_CONFIG).equals(CONFIG_CONTENT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void ApplyPatch() {
            try {
                Files.write(TMP_CONFIG, CONFIG_CONTENT);

                File file = new File(FLASH_FOLDER.toUri());
                file.mkdirs();
                Files.move(Paths.get(TMP_CONFIG.toUri()), Paths.get(FLASH_CONFIG.toUri()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

package com.github.manolo8.darkbot.modules.utils;

import java.io.IOException;

public class LegacyFlashPatcher {

    @Deprecated
    protected void runPatcher() {
    }

    @Deprecated
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

}

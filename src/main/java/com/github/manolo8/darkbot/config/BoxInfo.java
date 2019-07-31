package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;

@Option("Resource")
public class BoxInfo {
    @Option("Collect")
    public boolean collect;
    @Option("Wait (ms)")
    public int waitTime;
    @Option("Priority")
    public int priority;
}

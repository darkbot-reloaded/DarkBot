package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;

@Option(key = "config.collect.box_table.resource")
public class BoxInfo {
    @Option(key = "config.collect.box_table.collect")
    public boolean collect;
    @Option(key = "config.collect.box_table.wait")
    public int waitTime;
    @Option(key = "config.collect.box_table.priority")
    public int priority;
}

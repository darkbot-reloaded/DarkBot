package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;

@Option(key = "config.collect.box_table.resource")
public class BoxInfo implements eu.darkbot.api.config.types.BoxInfo {
    @Option(key = "config.collect.box_table.collect")
    public boolean collect;
    @Option(key = "config.collect.box_table.wait")
    public int waitTime;
    @Option(key = "config.collect.box_table.priority")
    public int priority;

    public transient String name;

    @Override
    public boolean shouldCollect() {
        return collect;
    }

    @Override
    public void setShouldCollect(boolean collect) {
        this.collect = collect;
    }

    @Override
    public int getWaitTime() {
        return waitTime;
    }

    @Override
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
}

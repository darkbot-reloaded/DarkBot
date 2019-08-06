package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;

@Option(value = "Resource", description = "Boxes or other resources the bot has seen")
public class BoxInfo {
    @Option(value = "Collect", description = "If the resource should be collected")
    public boolean collect;
    @Option(value = "Wait (ms)", description = "Time to wait to pick up resource, in milliseconds")
    public int waitTime;
    @Option(value = "Priority", description = "#1 priority will be collected before #2 priority")
    public int priority;
}

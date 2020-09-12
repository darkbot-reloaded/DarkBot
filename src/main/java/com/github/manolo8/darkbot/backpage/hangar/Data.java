package com.github.manolo8.darkbot.backpage.hangar;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Data {
    private Ret ret;
    private Money money;
    @Nullable public Map<String, String[]> map;

    public Ret getRet() {
        return ret;
    }

    public Money getMoney() {
        return money;
    }

    @Override
    public String toString() {
        return "Data{" +
                "ret=" + ret +
                ", money=" + money +
                '}';
    }
}

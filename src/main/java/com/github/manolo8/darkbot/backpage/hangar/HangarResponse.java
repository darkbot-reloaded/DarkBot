package com.github.manolo8.darkbot.backpage.hangar;

public class HangarResponse {
    private int isError;
    private Data data;

    public int getIsError() {
        return isError;
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return "HangarResponse{" +
                "isError=" + isError +
                ", data=" + data +
                '}';
    }
}

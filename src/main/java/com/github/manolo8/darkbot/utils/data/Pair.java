package com.github.manolo8.darkbot.utils.data;

import lombok.Value;

@Value
public class Pair<L, R> {
    L left;
    R right;
}

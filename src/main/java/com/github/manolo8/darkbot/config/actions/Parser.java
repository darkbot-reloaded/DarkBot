package com.github.manolo8.darkbot.config.actions;

public interface Parser {
    String parse(String str) throws SyntaxException;
}

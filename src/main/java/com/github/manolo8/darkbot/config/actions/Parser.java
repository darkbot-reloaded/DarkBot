package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;

public interface Parser {
    void parse(ParsingNode node) throws SyntaxException;
}

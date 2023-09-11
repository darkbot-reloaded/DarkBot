package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;

@ValueData(name = "string", description = "String constant", example = "string(\"hello\")")
public class StringConstant implements Value<String>, Parser {

    public String string;

    @Override
    public String get(Main main) {
        return string;
    }

    @Override
    public String toString() {
        return "string(" + ParsingNode.escape(string) + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(1, getClass());
        string = node.getParamStr(0);
    }

}

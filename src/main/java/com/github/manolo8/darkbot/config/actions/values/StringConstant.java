package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;

@ValueData(name = "string", description = "String constant", example = "string(hello)")
public class StringConstant implements Value<String>, Parser {

    public String string;

    @Override
    public String get(Main main) {
        return string;
    }

    @Override
    public String toString() {
        return "string(" + string.replace(")", "\\)") + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("(?<!\\\\)\\)", 2);

        string = params[0].replace("\\)", ")");

        if (params.length != 2 && string.isEmpty())
            throw new SyntaxException("Missing string or ) for empty string", "");

        return ParseUtil.separate(params, getClass(), ")");
    }

}

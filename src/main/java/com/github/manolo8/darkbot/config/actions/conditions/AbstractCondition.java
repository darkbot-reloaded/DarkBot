package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static eu.darkbot.api.config.types.Condition.Result.ABSTAIN;
import static eu.darkbot.api.config.types.Condition.Result.ALLOW;
import static eu.darkbot.api.config.types.Condition.Result.DENY;

public abstract class AbstractCondition implements LegacyCondition, Parser {

    public List<Condition> children;

    public AbstractCondition() {
        children = new ArrayList<>();
    }

    public Condition.Result getValue(PluginAPI api, int min, int max) {
        int[] states = new int[]{0, 0, 0};
        for (int i = 0; i < children.size(); i++) {
            states[children.get(i).get(api).ordinal()]++;
            if ((states[1] + states[2] > children.size() - min && states[0] + states[1] > 0) /* Can't reach min anymore */
                    || states[0] > max /* Allow bigger than max */) {
                return DENY;
            }
            if (states[0] >= min && states[0] + (children.size() - 1 - i) >= max) { /* Won't go bigger than max */
                return ALLOW;
            }
        }
        return states[0] >= min ? ALLOW : ABSTAIN;
    }

    protected String name() {
        return getClass().getAnnotation(ValueData.class).name();
    }

    @Override
    public String toString() {
        return name() + "(" + children.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        boolean hasNext;
        do {
            ParseResult<Result> pr = ValueParser.parse(str, Result.class);

            children.add(pr.asCondition(str, getClass()));
            str = pr.leftover.trim();

            hasNext = !str.isEmpty() && str.charAt(0) == ',';
            str = ParseUtil.separate(str, getClass(), ",", ")");
        } while (hasNext);
        return str;
    }

}

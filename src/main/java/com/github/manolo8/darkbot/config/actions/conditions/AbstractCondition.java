package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.parser.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCondition implements Condition, Parser {

    public int min, max;
    public List<Condition> children;

    public AbstractCondition() {
        children = new ArrayList<>();
    }

    public Result getValue(Main main, int min, int max) {
        int[] states = new int[]{0, 0, 0};
        for (int i = 0; i < children.size(); i++) {
            states[children.get(i).get(main).ordinal()]++;
            if ((states[1] + states[2] > children.size() - min && states[0] + states[1] > 0) /* Can't reach min anymore */
                    || states[0] > max /* Allow bigger than max */) {
                return Condition.Result.DENY;
            }
            if (states[0] >= min && states[0] + (children.size() - 1 - i) >= max) { /* Won't go bigger than max */
                return Condition.Result.ALLOW;
            }
        }
        return states[0] >= min ? Condition.Result.ALLOW : Condition.Result.ABSTAIN;
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
            if (!(pr.value instanceof Condition))
                throw new SyntaxException("Error: Expected boolean condition", str, Values.getMeta(getClass()));

            children.add((Condition) pr.value);
            str = pr.leftover.trim();

            hasNext = !str.isEmpty() && str.charAt(0) == ',';
            str = ParseUtil.separate(str, getClass(), ",", ")");
        } while (hasNext);
        return str;
    }

}

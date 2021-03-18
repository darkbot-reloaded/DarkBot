package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.ValueParser;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@ValueData("if")
public class NumericalCondition implements Condition, Parser {

    public Value<Number> a;
    public Operation operation;
    public Value<Number> b;

    @Override
    public @NotNull Condition.Result get(Main main) {
        Number numA, numB;
        if ((numA = Value.get(a, main)) == null || (numB = Value.get(b, main)) == null ||
                operation == null) return Condition.Result.ABSTAIN;

        return Condition.Result.fromBoolean(operation.check.test(numA, numB));
    }

    public enum Operation {
        BIGGER(">", (a, b) -> a.doubleValue() > b.doubleValue()),
        BIGGER_OR_EQUAL(">=", (a, b) -> a.doubleValue() >= b.doubleValue()),
        EQUAL("=", (a, b) -> a.doubleValue() == b.doubleValue()),
        SMALLER_OR_EQUAL("<=", (a, b) -> a.doubleValue() <= b.doubleValue()),
        SMALLER("<", (a, b) -> a.doubleValue() < b.doubleValue());

        private final String display;
        private final BiPredicate<Number, Number> check;

        Operation(String display, BiPredicate<Number, Number> check) {
            this.display = display;
            this.check = check;
        }

        @Override
        public String toString() {
            return display;
        }

        public static Operation of(String operation) {
            for (Operation op : Operation.values()) {
                if (op.toString().equals(operation)) return op;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "if(" + a + " " + operation + " " + b + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        ValueParser.Result prA = ValueParser.parse(str, Number.class);

        String[] params = prA.leftover.trim().split(" *,? *", 2);

        operation = Operation.of(params[0].trim());
        if (operation == null)
            throw new SyntaxException("Unknown operation '" + params[0] + "'", prA.leftover, Operation.class);

        if (params.length != 2)
            throw new SyntaxException("Missing end separator in 'if'", prA.leftover);

        ValueParser.Result prB = ValueParser.parse(params[1], Number.class);

        a = (Value<Number>) prA.value;
        b = (Value<Number>) prB.value;

        str = prB.leftover.trim();
        if (str.isEmpty() || str.charAt(0) != ')')
            throw new SyntaxException("Missing end separator in 'if'", str, ")");

        return str.substring(1);
    }
}

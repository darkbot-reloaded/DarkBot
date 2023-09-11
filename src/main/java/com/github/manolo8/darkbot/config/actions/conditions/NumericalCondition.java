package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@ValueData(name = "if", description = "Compares two numbers", example = "if(a > b)")
public class NumericalCondition implements LegacyCondition, Parser {

    public Value<Number> a;
    public Operation operation;
    public Value<Number> b;

    @Override
    public @NotNull Condition.Result get(PluginAPI main) {
        Number numA, numB;
        if ((numA = Value.get(a, main)) == null || (numB = Value.get(b, main)) == null || operation == null)
            return Condition.Result.ABSTAIN;

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

        public static Operation of(ParsingNode node) {
            String operation = node.getString();
            for (Operation op : Operation.values()) {
                if (op.toString().equals(operation)) return op;
            }
            throw new SyntaxException("Unknown operation '" + operation + "'", node, Operation.class);
        }

    }

    @Override
    public String toString() {
        return "if(" + a + " " + operation + " " + b + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(3, getClass());

        a = ValueParser.parse(node.getParam(0), Number.class);
        operation = Operation.of(node.getParam(1));
        a = ValueParser.parse(node.getParam(2), Number.class);
    }
}

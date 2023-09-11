package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.tree.DocumentReader;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import com.github.manolo8.darkbot.gui.utils.highlight.Locatable;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.config.types.Condition;

import javax.swing.text.Document;

public class ValueParser {

    public static Condition parseCondition(String str) {
        return parseCondition(new DocumentReader(str));
    }

    public static Condition parseCondition(Document document) {
        return parseCondition(new DocumentReader(document));
    }

    public static Condition parseCondition(DocumentReader reader) throws SyntaxException {
        reader.reset();
        ParsingNode root = new ParsingNode(reader);
        return parseCondition(root);
    }

    public static Condition parseCondition(ParsingNode node) {
        // Technically it's a Value<legacy result> that is implemented by api Condition.
        return (Condition) parseImpl(node, com.github.manolo8.darkbot.config.actions.Condition.Result.class).value;
    }

    public static <T> Value<T> parse(ParsingNode node, Class<T> type) {
        return parseImpl(node, type).value;
    }

    public static ParseResult<?> parseGeneric(ParsingNode node) {
        return parseImpl(node, Object.class);
    }

    private static <T> ParseResult<T> parseImpl(ParsingNode node, Class<T> type) {
        Values.Meta<T> vm = Values.getMeta(node, type);

        Value<T> val = ReflectionUtils.createInstance(vm.clazz);
        if (val instanceof Parser) ((Parser) val).parse(node);
        else {
            if (vm.params.length > node.getChildCount()) {
                int p = node.getEnd().getOffset() - 1;
                throw new SyntaxException("Missing separator in '" + node.getFunction() + "'", Locatable.of(p, p), vm, ",");
            } else if (vm.params.length < node.getChildCount()) {
                throw new SyntaxException("Too many parameters, expected " + vm.params.length, Locatable.of(
                        node.getChildAt(vm.params.length).getStart().getOffset(),
                        node.getChildAt(node.getChildCount() - 1).getEnd().getOffset()), vm);
            }

            int idx = 0;
            for (Values.Param param : vm.params) {
                ReflectionUtils.set(param.field, val, parseImpl(node.getParam(idx++), param.type).value);
            }
        }

        return new ParseResult<>(val, vm.type);
    }

}

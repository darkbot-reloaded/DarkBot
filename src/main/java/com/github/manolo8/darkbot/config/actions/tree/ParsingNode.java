package com.github.manolo8.darkbot.config.actions.tree;

import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.gui.utils.highlight.Locatable;
import lombok.AccessLevel;
import lombok.Getter;

import javax.swing.text.Position;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ParsingNode implements TreeNode, Locatable {
    private final ParsingNode parent;

    private Position start;
    private String function = "";
    private List<ParsingNode> children;
    private Position end;

    @Getter(AccessLevel.NONE)
    private int totalChildren;

    public ParsingNode() {
        parent = null;
    }

    public ParsingNode(DocumentReader reader) {
        this();
        reader.reset();
        parse(reader);
        reader.done();
    }

    private ParsingNode(ParsingNode parent) {
        this.parent = parent;
    }

    public void parse(DocumentReader str) {
        str.skipWhitespace();
        start = str.getPosition();
        this.function = str.readText();

        // Standalone leaf node! a raw value with no (), eg the VAL in:
        // string(VAL) or has-effect(VAL, hero())
        if (!str.pollIf('(')) {
            this.children = null;
            this.totalChildren = 0;
        } else {
            this.function = this.function.trim();
            this.children = new ArrayList<>();

            // 0 parameter function
            if (!str.pollIf(')')) {
                while (true) {
                    ParsingNode node = new ParsingNode(this);
                    node.parse(str);
                    children.add(node);

                    if (str.pollIf(')')) break;
                    if (str.pollIf(',') || str.pollIf(' ')) continue;

                    throw new SyntaxException(
                            "Expected one of ')' or ',' but found '" + str.peekNext() + "'",
                            str.getPosition().getOffset());
                }
            }
            totalChildren = children.stream().mapToInt(ch -> ch.totalChildren + 1).sum();
        }
        end = str.getPosition();
    }

    public boolean isSmall() {
        return totalChildren <= 7;
    }

    public String getPrettyPrinted() {
        return function + " " + (
                children == null ? "" :
                children.isEmpty() ? "()" :
                !isSmall() ? "(...)" :
                children.stream().map(ParsingNode::toString)
                        .collect(Collectors.joining(", ", "( ", " )")));
    }

    public String toString() {
        return escape(function) + (children == null ? "" :
                children.stream().map(ParsingNode::toString)
                        .collect(Collectors.joining(", ", "(", ")")));
    }

    public static String escape(String val) {
        for (char c : val.toCharArray()) {
            if (c == '(' || c == ',' || c == ')' || c == ' ')
                return '"' + val.replaceAll("([\\\\\"])", "\\$1") + '"';
        }
        return val;
    }

    public ParsingNode getAtPosition(int min, int max) {
        if (children == null) return this;

        for (ParsingNode param : children) {
            if (param.getStart().getOffset() <= min && param.getEnd().getOffset() >= max) {
                return param.getAtPosition(min, max);
            }
        }
        return this;
    }

    public void requireParamSize(int size, Class<? extends Value<?>> type) {
        if ((children == null ? -1 : children.size()) != size) {
            String curr = children == null ? "none" : String.valueOf(getChildCount());
            String expected = size == -1 ? "none" : String.valueOf(size);
            throw new SyntaxException("Invalid parameter count, found " + curr + " but expected " + expected, this, Values.getMeta(type));
        }
    }

    public ParsingNode getParam(int idx) {
        if (idx >= getChildCount()) throw new SyntaxException("Missing parameter #" + (idx + 1), this);
        return children.get(idx);
    }

    public String getParamStr(int idx) {
        return getParam(idx).getString();
    }

    public String getString() {
        if (children != null) throw new SyntaxException("Expected no parameters", this);
        return function;
    }

    @Override
    public ParsingNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children == null ? 0 : children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf((ParsingNode) node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children == null;
    }

    @Override
    public Enumeration<? extends javax.swing.tree.TreeNode> children() {
        return Collections.enumeration(children);
    }
}

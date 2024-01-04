package com.github.manolo8.darkbot.config.actions.tree;

import com.github.manolo8.darkbot.config.actions.SyntaxException;
import lombok.SneakyThrows;

import javax.swing.text.Document;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import java.util.regex.Pattern;

public class DocumentReader {
    // "\\\\" compiles to string literal \\, which is one regex escaped \
    private static final Pattern ESCAPES_REGEX = Pattern.compile("\\\\(.)");

    private final Segment BUFFER = new Segment();

    private final Document document;
    private int nextIdx;

    @SneakyThrows
    public DocumentReader(String string) {
        this.document = new PlainDocument(new GapContent(string.length()));
        this.document.insertString(0, string, null);
    }

    public DocumentReader(Document document) {
        this.document = document;
    }

    /**
     * Reads a text (arbitrary string) as defined below:
     * Any character is eligible, except for '(' ',' ')' and any whitespace char.
     * You may use a quoted string "string" for any char except " being eligible.
     * Either of them may be escaped by using \, and \ can be escaped by \\.
     * @return The next word read in full.
     */
    public String readText() {
        boolean quoted = pollIf('"');

        for (int i = nextIdx; i < document.getLength(); i++) {
            char ch = charAt(i);
            if (ch == '\\') {
                // Skip the next char, it's been escaped
                i++;
            } else if (quoted ? ch == '"' : ch == '(' || ch == ',' || ch == ')' || ch == ' ') {
                if (nextIdx == i)
                    throw new SyntaxException("Invalid text, expected at least one char", nextIdx);

                String result = ESCAPES_REGEX.matcher(substring(nextIdx, i)).replaceAll("$1");
                nextIdx = i + (quoted ? 1 : 0);
                return result;
            }
        }

        if (quoted) {
            throw new SyntaxException("Unfinished quoted string", nextIdx, "\"");
        }

        String remaining = substring(nextIdx, document.getLength());
        nextIdx = document.getLength();
        return remaining;
    }

    public void done() {
        if (nextIdx < document.getLength())
            throw new SyntaxException("Unused characters after end", nextIdx);
    }

    public char peekNext() {
        if (nextIdx >= document.getLength()) return '\04'; // EOF
        return charAt(nextIdx);
    }

    @SneakyThrows
    public Position getPosition() {
        return document.createPosition(nextIdx);
    }

    public void skipWhitespace() {
        for (int i = nextIdx; i < document.getLength(); i++) {
            if (!Character.isWhitespace(charAt(i))) {
                nextIdx = i;
                break;
            }
        }
    }

    /**
     * Peek at the next char ignoring whitespace, if it's {@param ch}, poll it and return true
     * @param ch the char to maybe expect next
     * @return true if the next char is ch, and it was skipped, false otherwise
     */
    public boolean pollIf(char ch) {
        for (int i = nextIdx; i < document.getLength(); i++) {
            char currChar = charAt(i);
            if (currChar == ch) {
                nextIdx = i + 1;
                return true;
            } else if (!Character.isWhitespace(currChar)) {
                return false;
            }
        }
        return false;
    }

    public void reset() {
        this.nextIdx = 0;
    }

    @SneakyThrows
    public String substring(int start, int end) {
        return document.getText(start, end - start);
    }

    @SneakyThrows
    public char charAt(int pos) {
        document.getText(pos, 1, BUFFER);
        return BUFFER.charAt(0);
    }


}

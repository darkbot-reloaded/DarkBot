package com.github.manolo8.darkbot.config.actions.tree;

import com.github.manolo8.darkbot.Bot;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.gui.tree.editors.ConditionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.highlight.BorderHighlightPainter;
import com.github.manolo8.darkbot.gui.utils.highlight.HighlightHandler;
import com.github.manolo8.darkbot.gui.utils.highlight.Locatable;
import eu.darkbot.util.Popups;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * In-testing new condition editor
 */
public class ConditionEditorPanel extends JPanel {

    private final ConditionEditor textArea = new ConditionEditor();
    private final DocumentReader reader = new DocumentReader(textArea.getDocument());

    private final ParsingNode root = new ParsingNode();
    private final ParseTreeModel treeModel = new ParseTreeModel(root);
    private final JTree tree = new ConditionTree(treeModel);

    private final HighlightHandler locationHighlight =
            new HighlightHandler(textArea, new BorderHighlightPainter(UIUtils.BLUE_HIGHLIGHT));
    private final HighlightHandler errorHighlight =
            new HighlightHandler(textArea, new DefaultHighlighter.DefaultHighlightPainter(UIUtils.RED_HIGHLIGHT));


    public ConditionEditorPanel(String initialText) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(800, 500));

        add(new JScrollPane(textArea));
        add(new JScrollPane(tree));

        textArea.getDocument().addDocumentListener((GeneralDocumentListener) e -> {
            try {
                reader.reset();
                root.parse(reader);
                errorHighlight.remove();
            } catch (SyntaxException ex) {
                errorHighlight.setHighlight(ex.getIdx(""), textArea.getDocument().getLength());
            }

            treeModel.updateListeners();
            for (int i = 0; i < tree.getRowCount(); i++) {
                TreeNode node = (TreeNode) tree.getPathForRow(i).getLastPathComponent();
                if (node instanceof ParsingNode && !((ParsingNode) node).isSmall()) tree.expandRow(i);
            }
        });

        // Sync tree selection to editor highlight
        tree.addTreeSelectionListener(event -> {
            TreeNode node = (TreeNode) event.getPath().getLastPathComponent();
            if (node instanceof Locatable) locationHighlight.setHighlight((Locatable) node);
        });

        // Sync editor caret to tree highlight
        textArea.addCaretListener(e -> {
            int a = e.getDot(), b = e.getMark();
            ParsingNode node = root.getAtPosition(Math.min(a, b), Math.max(a, b));
            if (node != null) tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(node)));
        });

        textArea.setText(initialText);
        textArea.init();
    }

    private static class ConditionTree extends JTree {
        public ConditionTree(TreeModel model) {
            super(model);
        }


        // Simple custom node rendering
        @Override
        public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            return value instanceof ParsingNode ? ((ParsingNode) value).getPrettyPrinted() : value.toString();
        }

    }

    // Condition editor testing
    public static void main(String[] args) {
        Bot.setupUI();
        UIManager.put("Tree.paintLines", true);
        Popups.of("Condition editor", new ConditionEditorPanel(
                "any(\n" +
                        "  if(hp-type(hp-percent, health(hero())) <= percent(35)), \n" +
                        "  all(none(has-effect(leech, hero())), if(hp-type(shield-percent, health(hero())) <= percent(10))))"))
                .border(BorderFactory.createEmptyBorder())
                .showAsync();
    }

}

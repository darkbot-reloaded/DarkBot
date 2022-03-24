package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.FloatingDialog;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.util.function.Function;

public class DiagnosticsPanel extends JPanel {

    private static final String STYLE = "ul{margin:0px;list-style-type:none;}ul.content{margin-left:10px}",
            HEADER = "<html><style>" + STYLE + "</style>";

    /* Component events for visibility didn't fire at all. Ended up making the timer clean itself up. */
    private final Timer timer = new Timer(500, e -> {
        if (!isDisplayable()) {
            this.timer.stop();
            return;
        }
        updateInfo();
    });

    private final Main main;
    private final JLabel infoLabel = new JLabel();
    private final DiagnosticSection[] sections = Sections.values();

    public DiagnosticsPanel(Main main, JComponent parent) {
        this.main = main;

        add(infoLabel);
        updateInfo();

        setBorder(UIUtils.getBorder());

        FloatingDialog.show(this, parent);
        timer.start();
    }

    private void updateInfo() {
        infoLabel.setText(getInfo());
    }

    private String getInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(HEADER);

        builder.append("<ul class='top'>");
        for (DiagnosticSection section : sections) {
            builder.append("<li>").append(section.getTitle());
            String[] content = section.getContent(main);

            if (content != null && content.length > 0) {
                builder.append("<ul class='content'>");
                for (String c : content) builder.append("<li>").append(c).append("</li>");
                builder.append("</ul>");
            }
        }
        builder.append("</ul>");

        builder.append("</html>");

        return builder.toString();
    }

    private interface DiagnosticSection {
        String getTitle();
        String[] getContent(Main main);
    }

    private enum Sections implements DiagnosticSection {
        BOT("Bot", main -> String.format("%.1f ms tick\n%s API Version", main.avgTick, Main.API.getVersion())),
        INGAME("In-game", main -> String.format("%d MB\n%d FPS\n%d ping",
                Main.API.getMemoryUsage(), main.facadeManager.stats.getFps(), main.pingManager.ping));

        private final String title;
        private final Function<Main, String> content;

        Sections(String title, Function<Main, String> content) {
            this.title = title;
            this.content = content;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String[] getContent(Main main) {
            return content.apply(main).split("\n");
        }
    }

}

package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

public class StatsButton extends TitleBarButton<MainGui> {

    //private final StatsFrame statsFrame;

    StatsButton(Main main, MainGui frame) {
        super(UIUtils.getIcon("stats"), frame);
        super.setVisible(false);
        setToolTipText("Open stats view");

        //statsFrame = new StatsFrame(main.statsManager);
    }
/*
    @Override
    public void actionPerformed(ActionEvent e) {
        statsFrame.setVisible(!statsFrame.isVisible());
    }

    private static class StatsFrame extends JFrame {
        private final XYChart chart;
        private final XChartPanel<XYChart> chartPanel;
        private final StatsAPI stats;
        private StatsAPI.TimeSeries series;

        private final Timer timer = new Timer(250, a -> this.updateGraph());

        public StatsFrame(StatsAPI stats) throws HeadlessException {
            super("Stats");
            this.stats = stats;
            this.series = stats.getStat(Stats.Bot.TICK_TIME).getTimeSeries();

            // Create Chart
            chart = new XYChart(600, 400);
            // Customize Chart
            chart.setTitle("Stats");
            chart.setXAxisTitle("Time");
            chart.setYAxisTitle("Value");

            XYSeries series = chart.addSeries("Data", Arrays.asList(1L, 2L, 3L), Arrays.asList(1d, 3d, 2d));
            series.setMarker(SeriesMarkers.NONE);

            chartPanel = new XChartPanel<>(chart);
            add(chartPanel, BorderLayout.CENTER);

            // Display the window.
            pack();
            timer.start();
        }

        private void updateGraph() {
            chart.updateXYSeries("Data", series.time(), series.value(), null);
            chartPanel.revalidate();
            chartPanel.repaint();
        }

    }
*/
}

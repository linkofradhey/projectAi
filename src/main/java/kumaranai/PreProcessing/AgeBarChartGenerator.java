package kumaranai.PreProcessing;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class AgeBarChartGenerator {

    // ── Returns PNG bytes (same pattern as AgeChartGenerator) ─────────
    public byte[] generateBarChart(Map<String, Integer> valueCounts,
                                   Map<String, Integer> encodingMap) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
            String label = entry.getKey()
                         + " (enc=" + encodingMap.getOrDefault(entry.getKey(), -1) + ")";
            dataset.addValue(entry.getValue(), "Count", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Age Distribution (Bar Chart)",   // title
            "Age",                            // x-axis
            "Count",                          // y-axis
            dataset,
            PlotOrientation.VERTICAL,
            false,    // legend
            true,     // tooltips
            false     // urls
        );

        // ── Styling ──────────────────────────────────────────────
        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(245, 248, 252));
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(56, 161, 105));  // green bars
        renderer.setMaximumBarWidth(0.08);
        renderer.setShadowVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // ── Write to bytes ───────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 700, 400);
        return baos.toByteArray();
    }
}
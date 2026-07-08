package kumaranai.PreProcessing;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class AgeChartGenerator {

    public byte[] generatePieChart(Map<String, Integer> valueCounts,
                                   Map<String, Integer> encodingMap) throws Exception {

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
            String label = entry.getKey() + " (enc=" + encodingMap.getOrDefault(entry.getKey(), -1) + ")";
            dataset.setValue(label, entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Age Distribution", dataset, true, true, false
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 600, 400);
        return baos.toByteArray();   
    }
}
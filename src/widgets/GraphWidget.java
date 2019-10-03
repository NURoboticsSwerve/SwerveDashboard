package widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class GraphWidget extends DecoratedWidget {

	private static final Color[] COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.YELLOW };
	private static final int NUM_VALUES = 5;
	private final String[] trackedValues;

	private final ArrayList<double[]> valueHistory;

	private final JFreeChart graph;
	private final JCheckBox trackCheckBox;

	public GraphWidget() {

		trackedValues = new String[NUM_VALUES];
		for (int i = 0; i < NUM_VALUES; i++) {
			trackedValues[i] = "";
		}

		valueHistory = new ArrayList<double[]>();

		String chartTitle = "Objects Movement Chart";
		String xAxisLabel = "Time";
		String yAxisLabel = "Values";

		graph = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, null);
		graph.setTitle("Graph");
		
		ChartPanel chartPanel = new ChartPanel(graph);
		chartPanel.setPreferredSize(new Dimension(200, 200));

		add(chartPanel, BorderLayout.CENTER);

		trackCheckBox = new JCheckBox("Track", true);
		add(trackCheckBox, BorderLayout.SOUTH);

		decoratedWidgetLoaded();
	}

	private void updateGraph() {

		XYSeriesCollection dataset = new XYSeriesCollection();

		valueHistory.add(new double[NUM_VALUES]);

		for (int i = 0; i < NUM_VALUES; i++) {
			if (!trackedValues[i].isEmpty()) {

				double curValue = NetworkClient.getInstance().readDouble(trackedValues[i]);
				valueHistory.get(valueHistory.size() - 1)[i] = curValue;

				XYSeries curSeries = new XYSeries(trackedValues[i]);
				for (int j = 0; j < valueHistory.size(); j++) {
					curSeries.add(j, valueHistory.get(j)[i]);
				}
				dataset.addSeries(curSeries);
			}
		}

		graph.getXYPlot().setDataset(dataset);
	}

	private void createSettingsDialog() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);

		JPanel outerPanel = new JPanel(new BorderLayout());

		outerPanel.add(new JLabel("Values to Graph"), JLabel.CENTER);

		JPanel valuesPanel = new JPanel(new GridLayout(NUM_VALUES, 2));

		JTextField[] valueFields = new JTextField[NUM_VALUES];
		for (int i = 0; i < NUM_VALUES; i++) {
			JLabel colorLabel = new JLabel();
			colorLabel.setBackground(COLORS[i]);

			JTextField valueField = new JTextField(10);
			if (trackedValues[i] != null) {
				valueField.setText(trackedValues[i]);
			}

			valueFields[i] = valueField;

			valuesPanel.add(colorLabel);
			valuesPanel.add(valueField);
		}

		outerPanel.add(valuesPanel, BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener((ActionEvent) -> {

			for (int i = 0; i < NUM_VALUES; i++) {
				trackedValues[i] = valueFields[i].getText();
			}

			settingsDialog.dispose();
		});
		outerPanel.add(closeButton, BorderLayout.SOUTH);
		
		settingsDialog.add(outerPanel);

		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setVisible(true);
	}

	@Override
	protected void decoratedWidgetLoaded() {
		settingsButton.addActionListener((ActionEvent) -> createSettingsDialog());
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				updateGraph();
			}
		}, 0, 20);
	}
}

package widgets;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

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

	public static final String NAME = "Graph";

	private static final int PLOT_PERIOD = 20;
	private static final int NUM_VALUES = 5;

	private boolean running;
	private int historyTime;

	private final String[] trackedValues;

	private final ArrayList<Datapoint[]> valueHistory;

	private final ChartPanel chartPanel;
	private final JFreeChart graph;

	public GraphWidget() {

		trackedValues = new String[NUM_VALUES];
		for (int i = 0; i < NUM_VALUES; i++) {
			trackedValues[i] = "";
		}

		valueHistory = new ArrayList<Datapoint[]>();

		running = true;
		historyTime = 10;

		String chartTitle = "Objects Movement Chart";
		String xAxisLabel = "Time (s)";
		String yAxisLabel = "Values";

		graph = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, null);
		graph.setTitle("");

		chartPanel = new ChartPanel(graph);
		chartPanel.setPreferredSize(new Dimension(400, 200));
		chartPanel.setPopupMenu(null);

		add(chartPanel, BorderLayout.CENTER);

		titleLabel.setText("Graph");

		settingsButton.addActionListener((ActionEvent) -> createSettingsDialog());
		Timer t = new Timer(true);
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (running) {
					updateValueHistory();
				}
			}
		}, 0, PLOT_PERIOD);

		t.schedule(new TimerTask() {
			@Override
			public void run() {
				updateGraph();
			}
		}, 0, PLOT_PERIOD);

		chartPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					running = !running;
				}
			}
		});
	}

	private void updateValueHistory() {
		valueHistory.add(new Datapoint[NUM_VALUES]);

		while (valueHistory.size() > (1000 / PLOT_PERIOD) * historyTime) {
			valueHistory.remove(0);
		}

		for (int i = 0; i < NUM_VALUES; i++) {
			if (!trackedValues[i].isEmpty()) {
				if (NetworkClient.getInstance().hasValue(trackedValues[i])) {
					try {
						double curValue = NetworkClient.getInstance().readDouble(trackedValues[i]);
						valueHistory.get(valueHistory.size() - 1)[i] = new Datapoint(System.currentTimeMillis(),
								curValue);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}

	private void updateGraph() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < NUM_VALUES; i++) {
			if (!trackedValues[i].isEmpty()) {
				XYSeries curSeries = new XYSeries(trackedValues[i]);
				for (int j = 0; j < valueHistory.size(); j++) {
					if (valueHistory.get(j)[i] != null) {
						double displayTime = (valueHistory.get(j)[i].time - System.currentTimeMillis()) / 1000.0;
						curSeries.add(displayTime, valueHistory.get(j)[i].value);
					}
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

		outerPanel.add(new JLabel("Values to Graph", JLabel.CENTER), BorderLayout.NORTH);

		JPanel valuesPanel = new JPanel(new GridLayout(NUM_VALUES + 2, 2));

		JTextField[] valueFields = new JTextField[NUM_VALUES];
		for (int i = 0; i < NUM_VALUES; i++) {
			JTextField valueField = new JTextField(10);
			if (trackedValues[i] != null) {
				valueField.setText(trackedValues[i]);
			}

			valueFields[i] = valueField;

			valuesPanel.add(new JLabel("Value " + (i + 1) + ":"));
			valuesPanel.add(valueField);
		}

		valuesPanel.add(new JLabel());
		valuesPanel.add(new JLabel());
		valuesPanel.add(new JLabel("History Length (s):"));

		JSpinner historyLengthSpinner = new JSpinner(new SpinnerNumberModel(historyTime, 1, 100, 1));
		valuesPanel.add(historyLengthSpinner);

		outerPanel.add(valuesPanel, BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener((ActionEvent) -> {

			for (int i = 0; i < NUM_VALUES; i++) {
				trackedValues[i] = valueFields[i].getText();
			}

			historyTime = (int) ((SpinnerNumberModel) historyLengthSpinner.getModel()).getNumber();

			settingsDialog.dispose();
		});
		outerPanel.add(closeButton, BorderLayout.SOUTH);

		settingsDialog.add(outerPanel);

		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setVisible(true);
	}

	private class Datapoint implements Serializable {

		private final long time;
		private final double value;

		private Datapoint(long time, double value) {
			this.time = time;
			this.value = value;
		}
	}

	@Override
	protected void widgetLoaded(Map<String, String> args) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Map<String, String> widgetSaved() {
		// TODO Auto-generated method stub
		return null;
	}
}

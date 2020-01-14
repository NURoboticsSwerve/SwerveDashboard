package widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dashboard.Widget;
import network.NetworkClient;
import network.ValueNotFoundException;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class GraphWidget extends Widget {

	public static final String NAME = "Graph";

	private final JLabel titleLabel;
	private final GraphPanel graphPanel;

	private final ArrayList<Double> valueHistory;

	private final Thread timingThread;
	private boolean stopThread;

	private int historyLength;
	private int pollingRate;

	private String valueToGraph;

	public GraphWidget() {

		titleLabel = new JLabel("New Graph", SwingConstants.CENTER);
		this.add(titleLabel, BorderLayout.NORTH);

		graphPanel = new GraphPanel();
		this.add(graphPanel, BorderLayout.CENTER);

		valueHistory = new ArrayList<Double>();
		valueToGraph = "";

		historyLength = 10;
		pollingRate = 20;

		stopThread = false;
		timingThread = new Thread(() -> run(), "Graph-Timing-Thread");
		timingThread.setDaemon(true);
	}

	private void run() {
		while (!stopThread) {
			long startTime = System.currentTimeMillis();

			updateValueHistory();
			graphPanel.repaint();

			int timeElapsed = (int) (System.currentTimeMillis() - startTime);
			int timeToSleep = (int) ((1000.0 / pollingRate) - timeElapsed);

			try {
				Thread.sleep(Math.max(timeToSleep, 1));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void setGraphedValue(String newValue) {
		if (!newValue.equals(valueToGraph)) {
			valueHistory.clear();
			valueToGraph = newValue;
		}
	}

	private void updateValueHistory() {
		try {
			double curVal = NetworkClient.getInstance().readDouble(valueToGraph);
			valueHistory.add(curVal);
		} catch (ValueNotFoundException | NumberFormatException e) {
			valueHistory.add(null);
		}

		while (valueHistory.size() > historyLength * pollingRate) {
			valueHistory.remove(0);
		}
	}

	@Override
	protected void deconstruct() {
		stopThread = true;
	}

	@Override
	protected void widgetLoaded(Map<String, String> args) {
	}

	@Override
	protected Map<String, String> widgetSaved() {
		return null;
	}

	@Override
	protected void showSettingsWindow() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setTitle("Settings");
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);

		JPanel outerPanel = new JPanel(new BorderLayout(5, 5));
		outerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		outerPanel.add(new JLabel("Settings", SwingConstants.CENTER), BorderLayout.NORTH);

		JPanel innerPanel = new JPanel(new GridLayout(0, 2, 5, 5));
		innerPanel.add(new JLabel("Value to Graph:"));

		JTextField valueField = new JTextField(valueToGraph, 20);
		innerPanel.add(valueField);

		innerPanel.add(new JLabel("History to Track (s):"));

		JSpinner historyLengthSpinner = new JSpinner(new SpinnerNumberModel(historyLength, 1, 100, 1));
		innerPanel.add(historyLengthSpinner);

		innerPanel.add(new JLabel("Polling Rate (hz):"));

		JSpinner pollingRateSpinner = new JSpinner(new SpinnerNumberModel(pollingRate, 1, 100, 1));
		innerPanel.add(pollingRateSpinner);

		outerPanel.add(innerPanel, BorderLayout.CENTER);

		JButton acceptButton = new JButton("Accept");
		acceptButton.addActionListener((ActionEvent) -> {
			setGraphedValue(valueField.getText());
			historyLength = (int) historyLengthSpinner.getValue();
			pollingRate = (int) pollingRateSpinner.getValue();

			settingsDialog.dispose();
		});
		outerPanel.add(acceptButton, BorderLayout.SOUTH);
		settingsDialog.add(outerPanel);

		settingsDialog.pack();
		settingsDialog.setResizable(false);
		settingsDialog.setVisible(true);
	}

	private class GraphPanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			// Clear the chart
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			// Draw in a vertical line every second
			g2d.setColor(Color.GRAY);
			int tickMarkSpacing = getWidth() / historyLength;
			int offset = (int) (((System.currentTimeMillis() % 1000) / 1000.0) * tickMarkSpacing);
			for (int i = 0; i <= historyLength; i++) {
				int x = (i * tickMarkSpacing) - offset;
				g2d.drawLine(x, 0, x, getHeight());
			}
		}
	}
}

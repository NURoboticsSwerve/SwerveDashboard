package widgets;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

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
		graphPanel.setPreferredSize(new Dimension(400, 200));
		this.add(graphPanel, BorderLayout.CENTER);

		valueHistory = new ArrayList<Double>();
		valueToGraph = "";

		historyLength = 10;
		pollingRate = 20;

		stopThread = false;
		timingThread = new Thread(() -> run(), "Graph-Timing-Thread");
		timingThread.setDaemon(true);
		timingThread.start();
	}

	private void run() {

		while (!stopThread) {
			long startTime = System.currentTimeMillis();

			updateValueHistory();

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
		if (newValue != null) {
			if (!newValue.equals(valueToGraph)) {
				valueHistory.clear();
			}

			valueToGraph = newValue;

			if (valueToGraph.isEmpty()) {
				titleLabel.setText("New Graph");
			} else {
				titleLabel.setText("Graph of '" + valueToGraph + "'");
			}
		}
	}

	private void updateValueHistory() {
		try {
			double curVal = NetworkClient.getInstance().readDouble(valueToGraph);
			valueHistory.add(0, curVal);
		} catch (IllegalArgumentException | ValueNotFoundException e) {
			valueHistory.add(null);
		}

		while (valueHistory.size() > historyLength * pollingRate) {
			valueHistory.remove(valueHistory.size() - 1);
		}
	}

	private double getMinHistoryValue() {
		if (valueHistory.isEmpty()) {
			return 0;
		} else {
			double min = 0;
			for (int i = 1; i < valueHistory.size(); i++) {
				if (valueHistory.get(i) != null) {
					min = Math.min(min, valueHistory.get(i));
				}
			}
			return min;
		}
	}

	private double getMaxHistoryValue() {
		if (valueHistory.isEmpty()) {
			return 0;
		} else {
			double max = 0;
			for (int i = 1; i < valueHistory.size(); i++) {
				if (valueHistory.get(i) != null) {
					max = Math.max(max, valueHistory.get(i));
				}
			}
			return max;
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
			double horzTickMarkSpacing = (double) getWidth() / historyLength;
			int horzOffset = (int) (((System.currentTimeMillis() % 1000) / 1000.0) * horzTickMarkSpacing);
			for (int i = 0; i <= historyLength; i++) {
				int x = (int) ((i * horzTickMarkSpacing) - horzOffset);
				g2d.drawLine(x, 0, x, getHeight());
			}

			int maxDisplayValue = (int) (getMaxHistoryValue() + 1);
			int minDisplayValue = (int) (getMinHistoryValue() - 0.99);

			maxDisplayValue = Math.max(maxDisplayValue, 1);
			minDisplayValue = Math.min(minDisplayValue, 0);

			int vertValueRange = maxDisplayValue - minDisplayValue;
			int vertTickMarkSpacing = (getHeight() / vertValueRange);

			for (int i = 0; i <= vertValueRange; i++) {
				int y = (i * vertTickMarkSpacing);
				g2d.drawLine(0, y, getWidth(), y);
			}

			int zeroPosition = (maxDisplayValue * vertTickMarkSpacing);
			g2d.setStroke(new BasicStroke(3));
			g2d.drawLine(0, zeroPosition, getWidth(), zeroPosition);

			g2d.setColor(Color.BLUE);
			double pointSpacing = getWidth() / ((double) historyLength * pollingRate);
			for (int i = valueHistory.size() - 1; i >= 0; i--) {
				if (valueHistory.get(i) != null) {
					int x = (int) (getWidth() - (i * pointSpacing));
					int y = (int) (zeroPosition - (valueHistory.get(i) * vertTickMarkSpacing));
					g2d.fillOval(x, y, 1, 1);
				}
			}

			g2d.setColor(Color.RED);
			g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
			int vertTextOffset = g2d.getFontMetrics().getHeight();
			for (int i = 0; i <= vertValueRange * 2; i++) {
				int negHorzTextOffset = g2d.getFontMetrics().stringWidth(-i + "") / 2;
				int posHorzTextOffset = g2d.getFontMetrics().stringWidth(i + "") / 2;
				g2d.drawString(-i + "", negHorzTextOffset,
						zeroPosition + (i * vertTickMarkSpacing) - (vertTextOffset / 2));
				g2d.drawString(i + "", posHorzTextOffset, zeroPosition - (i * vertTickMarkSpacing) + vertTextOffset);
			}
		}
	}
}

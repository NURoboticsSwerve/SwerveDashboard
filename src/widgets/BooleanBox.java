package widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dashboard.Widget;
import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class BooleanBox extends Widget {

	public static final String NAME = "Boolean Box";

	private Color displayColor;

	private String valueToWatch;
	private String callbackName;

	public BooleanBox() {
		this.setPreferredSize(new Dimension(100, 100));
		this.setMinimumSize(new Dimension(100, 100));

		setMonitoredValue("");
	}

	private void setMonitoredValue(String toWatch) {
		if (toWatch != null) {
			if (valueToWatch != null && !valueToWatch.isEmpty()) {
				NetworkClient.getInstance().removeValueMonitor(valueToWatch, callbackName);
			}

			valueToWatch = toWatch;

			if (!valueToWatch.isEmpty()) {
				callbackName = "BooleanBox-" + Math.random() + "-" + System.currentTimeMillis();
				NetworkClient.getInstance().addValueMonitor(valueToWatch, callbackName, () -> updateValue());
				displayColor = Color.GRAY;
			}
		}
	}

	private void updateValue() {
		String readValue = NetworkClient.getInstance().readString(valueToWatch);

		if (readValue.equalsIgnoreCase("true")) {
			displayColor = Color.GREEN;
		} else if (readValue.equalsIgnoreCase("false")) {
			displayColor = Color.RED;
		} else {
			displayColor = Color.YELLOW;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(displayColor);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	protected void deconstruct() {
		NetworkClient.getInstance().removeValueMonitor(valueToWatch, callbackName);
	}

	@Override
	protected void widgetLoaded(Map<String, String> args) {
		setMonitoredValue(args.get("valueToWatch"));
	}

	@Override
	protected Map<String, String> widgetSaved() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("valueToWatch", valueToWatch);
		return map;
	}

	@Override
	protected void showSettingsWindow() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setTitle("Settings");
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);

		JPanel innerPanel = new JPanel(new BorderLayout(5, 5));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		innerPanel.add(new JLabel("Settings", SwingConstants.CENTER), BorderLayout.NORTH);

		innerPanel.add(new JLabel("Value to Display:"), BorderLayout.WEST);

		JTextField field = new JTextField(valueToWatch, 20);
		innerPanel.add(field, BorderLayout.EAST);

		JButton acceptButton = new JButton("Accept");
		acceptButton.addActionListener((ActionEvent) -> {
			setMonitoredValue(field.getText());
			settingsDialog.dispose();
		});
		innerPanel.add(acceptButton, BorderLayout.SOUTH);

		settingsDialog.add(innerPanel);
		settingsDialog.pack();
		settingsDialog.setResizable(false);
		settingsDialog.setVisible(true);
	}
}

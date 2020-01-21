package widgets;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
public class TextBoxWidget extends Widget {

	public static final String NAME = "Text Box";

	private final JLabel titleLabel;
	private final JTextField textField;
	private String valueToDisplay;

	private String callbackName;

	public TextBoxWidget() {

		titleLabel = new JLabel("New Text Box", SwingConstants.CENTER);
		this.add(titleLabel, BorderLayout.NORTH);

		setMonitoredValue("");

		textField = new JTextField(20);
		textField.setEditable(false);
		textField.setHorizontalAlignment(JTextField.CENTER);

		textField.addActionListener((ActionEvent) -> {
			if (textField.isEditable()) {
				NetworkClient.getInstance().writeString(valueToDisplay, textField.getText());
			}
		});

		this.add(textField, BorderLayout.CENTER);
	}

	private void setMonitoredValue(String toWatch) {
		if (toWatch != null) {

			if (valueToDisplay != null && !valueToDisplay.isEmpty()) {
				NetworkClient.getInstance().removeValueMonitor(valueToDisplay, callbackName);
			}

			valueToDisplay = toWatch;

			if (valueToDisplay.isEmpty()) {
				titleLabel.setText("New Text Box");
			} else {
				titleLabel.setText("Text Box: '" + valueToDisplay + "'");
				callbackName = "TextBox-" + Math.random() + "-" + System.currentTimeMillis();
				NetworkClient.getInstance().addValueMonitor(valueToDisplay, callbackName, () -> {
					textField.setText(NetworkClient.getInstance().readString(valueToDisplay));
				});
			}
		}
	}

	@Override
	protected void deconstruct() {
		if (callbackName != null && !callbackName.isEmpty()) {
			NetworkClient.getInstance().removeValueMonitor(valueToDisplay, callbackName);
		}
	}

	@Override
	protected void widgetLoaded(Map<String, String> args) {
		setMonitoredValue(args.get("valueToDisplay"));
		textField.setEditable("true".equals(args.get("editable")));
	}

	@Override
	protected Map<String, String> widgetSaved() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("valueToDisplay", valueToDisplay);
		map.put("editable", Boolean.toString(textField.isEditable()));
		return map;
	}

	@Override
	protected void showSettingsWindow() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setTitle("Settings");
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
		settingsDialog.setLayout(new BorderLayout(5, 5));

		settingsDialog.add(new JLabel("Settings", SwingConstants.CENTER), BorderLayout.NORTH);

		JPanel settingsPanel = new JPanel(new BorderLayout(5, 5));

		settingsPanel.add(new JLabel("Value to Display:"), BorderLayout.WEST);
		JTextField displayValueTextField = new JTextField(valueToDisplay, 20);
		settingsPanel.add(displayValueTextField, BorderLayout.EAST);

		JCheckBox editableCheckBox = new JCheckBox("Editable Value:");
		editableCheckBox.setSelected(textField.isEditable());
		editableCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		editableCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
		settingsPanel.add(editableCheckBox, BorderLayout.SOUTH);

		settingsDialog.add(settingsPanel, BorderLayout.CENTER);

		JButton acceptButton = new JButton("Accept");
		acceptButton.addActionListener((ActionEvent) -> {
			setMonitoredValue(displayValueTextField.getText());
			textField.setEditable(editableCheckBox.isSelected());
			settingsDialog.dispose();
		});
		settingsDialog.add(acceptButton, BorderLayout.SOUTH);

		settingsDialog.pack();
		settingsDialog.setResizable(false);
		settingsDialog.setVisible(true);
	}
}

package widgets;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class TextBoxWidget extends DecoratedWidget {

	private final JTextField textField;
	private String valueToDisplay;

	private String callbackName;

	public TextBoxWidget() {

		textField = new JTextField();
		textField.setEditable(false);
		textField.setHorizontalAlignment(JTextField.CENTER);

		textField.addActionListener((ActionEvent) -> {
			if (textField.isEditable()) {
				NetworkClient.getInstance().writeString(valueToDisplay, textField.getText());
			}
		});

		add(textField, BorderLayout.CENTER);

		decoratedWidgetLoaded();
	}

	private void displaySettingsDialog() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
		settingsDialog.setLayout(new BorderLayout(5, 5));

		settingsDialog.add(new JLabel("Settings", SwingConstants.CENTER), BorderLayout.NORTH);

		JPanel settingsPanel = new JPanel(new BorderLayout(5, 5));

		settingsPanel.add(new JLabel("Value to Display:"), BorderLayout.WEST);
		JTextField displayValueTextField = new JTextField(valueToDisplay, 20);
		settingsPanel.add(displayValueTextField, BorderLayout.EAST);

		JCheckBox editableCheckBox = new JCheckBox("Editable Value:");
		editableCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		editableCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
		settingsPanel.add(editableCheckBox, BorderLayout.SOUTH);

		settingsDialog.add(settingsPanel, BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener((ActionEvent) -> {

			NetworkClient.getInstance().removeValueMonitor(valueToDisplay, callbackName);

			valueToDisplay = displayValueTextField.getText();
			titleLabel.setText(valueToDisplay);
			textField.setEditable(editableCheckBox.isSelected());

			callbackName = valueToDisplay + System.currentTimeMillis();
			NetworkClient.getInstance().addValueMonitor(valueToDisplay, callbackName, () -> {
				textField.setText(NetworkClient.getInstance().readString(valueToDisplay));
			});

			settingsDialog.dispose();
		});
		settingsDialog.add(closeButton, BorderLayout.SOUTH);

		settingsDialog.pack();
		settingsDialog.setVisible(true);
	}

	@Override
	protected void decoratedWidgetLoaded() {
		settingsButton.addActionListener((ActionEvent) -> displaySettingsDialog());
	}
}

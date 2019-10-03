package widgets;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class TextBoxWidget extends DecoratedWidget {

	private final JTextField textField;
	private String valueToDisplay;

	public TextBoxWidget() {

		textField = new JTextField();
		textField.setEditable(false);
		textField.setHorizontalAlignment(JTextField.CENTER);
		add(textField, BorderLayout.CENTER);

		decoratedWidgetLoaded();
	}

	private void displaySettingsDialog() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
		settingsDialog.setLayout(new BorderLayout(5, 5));

		settingsDialog.add(new JLabel("Value to Display:"), BorderLayout.NORTH);

		JTextField displayValueTextField = new JTextField(valueToDisplay, 20);
		settingsDialog.add(displayValueTextField, BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener((ActionEvent) -> {
			valueToDisplay = displayValueTextField.getText();
			titleLabel.setText(valueToDisplay);
			settingsDialog.dispose();
		});
		settingsDialog.add(closeButton, BorderLayout.SOUTH);

		settingsDialog.pack();
		settingsDialog.setVisible(true);
	}

	@Override
	protected void decoratedWidgetLoaded() {
		settingsButton.addActionListener((ActionEvent) -> displaySettingsDialog());

		new Timer(true).schedule(new TimerTask() {

			@Override
			public void run() {
				textField.setText(NetworkClient.getInstance().readString(valueToDisplay));
			}
		}, 0, 20);
	}
}

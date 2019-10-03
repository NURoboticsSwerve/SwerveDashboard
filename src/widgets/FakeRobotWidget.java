package widgets;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import network.NetworkClient;
import network.NetworkServer;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class FakeRobotWidget extends DecoratedWidget {

	private final ArrayList<JTextField[]> valuesTable;

	private final JPanel valuesToSendPanel;
	private final JButton addRowButton;

	public FakeRobotWidget() {
		valuesTable = new ArrayList<JTextField[]>();

		valuesToSendPanel = new JPanel(new GridLayout(0, 2));

		JScrollPane jsp = new JScrollPane(valuesToSendPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		add(jsp, BorderLayout.CENTER);

		addRowButton = new JButton("Add Row");
		valuesToSendPanel.add(addRowButton);

		decoratedWidgetLoaded();
	}

	private void createSettingsDialog() {
		JDialog settingsDialog = new JDialog();
		settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
		JTextField serverPortField = new JTextField(NetworkServer.getInstance().getTargetPort() + "");
		settingsDialog.add(serverPortField);

		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setVisible(true);

		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				NetworkServer.getInstance().setPort(Integer.parseInt(serverPortField.getText()));
			}
		}, 0);

	}

	@Override
	protected void decoratedWidgetLoaded() {

		NetworkServer.getInstance().setPort(12345);

		addRowButton.addActionListener((ActionEvent) -> {
			valuesToSendPanel.remove(addRowButton);
			JTextField nameTextField = new JTextField(10);
			JTextField valueTextField = new JTextField(10);
			valuesTable.add(new JTextField[] { nameTextField, valueTextField });
			valuesToSendPanel.add(nameTextField);
			valuesToSendPanel.add(valueTextField);
			valuesToSendPanel.add(addRowButton);
			revalidate();
		});

		settingsButton.addActionListener((ActionEvent) -> createSettingsDialog());

		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				for (JTextField[] curNameValuePair : valuesTable) {
					String curName = curNameValuePair[0].getText();
					String curValue = curNameValuePair[1].getText();

					if (curName != null && curValue != null) {
						NetworkServer.getInstance().writeString(curName, curValue);
					}
				}
			}
		}, 0, 100);
	}
}

package widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dashboard.Widget;
import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class ConnectionWidget extends Widget {

	public static final String NAME = "Connection Status";

	private final Timer timer;

	private JTextField field;

	public ConnectionWidget() {

		this.add(new JLabel("Connection Status", SwingConstants.CENTER), BorderLayout.NORTH);

		field = new JTextField(10);
		field.setEditable(false);
		this.add(field, BorderLayout.CENTER);

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (NetworkClient.getInstance().isConnected()) {
					field.setBackground(Color.GREEN);
				} else {
					field.setBackground(Color.RED);
				}

				field.setText("Ping: " + NetworkClient.getInstance().getPingTime() + "ms");
			}
		}, 0, 1000);
	}

	@Override
	protected void deconstruct() {
		timer.cancel();
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
	}
}

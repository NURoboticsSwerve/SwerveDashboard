package widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dashboard.Widget;
import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
abstract public class DecoratedWidget extends Widget {

	protected JButton settingsButton;
	protected JLabel titleLabel;
	protected JButton removeButton;

	public DecoratedWidget() {
		setLayout(new BorderLayout(5, 5));
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));

		settingsButton = new JButton("⚙");
		topPanel.add(settingsButton, BorderLayout.WEST);

		titleLabel = new JLabel();
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		topPanel.add(titleLabel, BorderLayout.CENTER);

		removeButton = new JButton("X");
		removeButton.addActionListener((ActionEvent) -> removeWidget());
		topPanel.add(removeButton, BorderLayout.EAST);

		add(topPanel, BorderLayout.NORTH);
	}

	@Override
	protected void widgetLoaded() {
		removeButton.addActionListener((ActionEvent) -> removeWidget());
		decoratedWidgetLoaded();
	}

	abstract protected void decoratedWidgetLoaded();
}

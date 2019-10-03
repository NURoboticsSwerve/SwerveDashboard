package widgets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import dashboard.Widget;
import network.NetworkClient;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class RedRectangleWidget extends Widget {

	public RedRectangleWidget() {
		this.setPreferredSize(new Dimension(100, 100));
		this.setMinimumSize(new Dimension(100, 100));
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.RED);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	protected void widgetLoaded() {
	}
}

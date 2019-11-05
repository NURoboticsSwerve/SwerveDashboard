package widgets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map;

import dashboard.Widget;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class BooleanBox extends Widget {

	public static final String NAME = "Boolean Box";
	
	public BooleanBox() {
		this.setPreferredSize(new Dimension(100, 100));
		this.setMinimumSize(new Dimension(100, 100));
	}
	
	public static String getDisplayName() {
		return "Boolean Box";
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.RED);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	protected void widgetLoaded(Map<String, String> args) {
	}

	@Override
	protected Map<String, String> widgetSaved() {
		return null;
	}
}

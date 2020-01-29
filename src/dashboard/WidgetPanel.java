package dashboard;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * Responsible for managing the various widgets in the dashboard as they are
 * added, removed, moved, and resized.
 * 
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class WidgetPanel extends JPanel {

	private final ArrayList<Widget> widgets;
	private final WidgetMoveResizeManager moveResizeManager;
	private final WidgetRemoveManager removeManager;

	WidgetPanel() {
		super(null);

		widgets = new ArrayList<Widget>();
		moveResizeManager = new WidgetMoveResizeManager();
		removeManager = new WidgetRemoveManager();

		this.addMouseListener(moveResizeManager);
		this.addMouseMotionListener(moveResizeManager);
	}

	/**
	 * Creates new widgets from a string that describes the type, location, size,
	 * and widget specific details of multiple widgets.
	 * 
	 * @param widgetsData A string describing any number of widgets.
	 * 
	 * @see WidgetPanel#toSaveForm()
	 */
	void addFromData(String widgetsData) {
		for (String curWidgetString : widgetsData.split(";")) {
			String[] curWidgetData = curWidgetString.split(",");
			try {
				// Create a new widget and load its basic information
				Widget newWidget = (Widget) Class.forName(curWidgetData[0]).newInstance();
				int x = Integer.parseInt(curWidgetData[1]);
				int y = Integer.parseInt(curWidgetData[2]);
				int width = Integer.parseInt(curWidgetData[3]);
				int height = Integer.parseInt(curWidgetData[4]);

				// The widget specific data is decoded from Base64, and then given to the newly
				// created widget as a map for it to parse. 
				Decoder decoder = Base64.getDecoder();
				Map<String, String> dataMap = new HashMap<String, String>();
				for (int i = 5; i < curWidgetData.length; i++) {
					String[] keyValueEncodedPair = curWidgetData[i].split(":");
					String key = new String(decoder.decode(keyValueEncodedPair[0]));
					String value = "";
					if (keyValueEncodedPair.length == 2) {
						value = new String(decoder.decode(keyValueEncodedPair[1]));
					}
					dataMap.put(key, value);
				}

				// Add the now correctly configured widget to the panel
				addWidget(newWidget);
				newWidget.getMoveResizePanel().setBounds(x, y, width, height);
				newWidget.widgetLoaded(dataMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds an already made widget object to the dashboard.
	 * 
	 * @param w The prepared widget object.
	 */
	void addWidget(Widget w) {
		this.add(w.getMoveResizePanel());
		w.getMoveResizePanel().addMouseListener(moveResizeManager);
		w.getMoveResizePanel().addMouseMotionListener(moveResizeManager);
		w.getMoveResizePanel().addKeyListener(removeManager);
		w.setup();
		widgets.add(w);
	}

	/**
	 * Converts all the widgets currently in the dashboard into a single string that
	 * describes their type, location, size, and any widget specific data.
	 * 
	 * @return A string that can be used to recreate the widgets.
	 * 
	 * @see WidgetPanel#addFromData(String)
	 */
	String toSaveForm() {
		String s = "";

		for (int i = 0; i < widgets.size(); i++) {
			// Widgets are responsible for formatting themselves
			// We simply append the individual widgets here
			s += ";" + widgets.get(i).toSaveForm();
		}

		return s.substring(1);
	}

	/**
	 * Removes all the widgets from the dashboard.
	 */
	void clear() {
		while (!widgets.isEmpty()) {
			widgets.get(0).removeWidget();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {

		for (int i = 0; i < widgets.size(); i++) {
			if (widgets.get(i).removeRequested()) {
				remove(widgets.get(i).getMoveResizePanel());
				widgets.get(i).deconstruct();
				widgets.remove(i);
				i--;
			}
		}

		super.paintComponent(g);
	}

	/**
	 * Responsible for managing the movement and resizing of widgets by the user.
	 * 
	 * @author Nicholas Contreras
	 */
	private class WidgetMoveResizeManager implements MouseInputListener {

		private int prevX, prevY;
		private boolean moving, resizingWidth, resizingHeight;

		@Override
		public void mouseClicked(MouseEvent event) {

			for (Widget w : widgets) {
				w.setSelected(false);
			}

			if (isEventOnWidget(event)) {
				Widget widgetClicked = getWidgetFromEvent(event);

				if (event.getButton() == MouseEvent.BUTTON3) {
					widgetClicked.showSettingsWindow();
				}

				widgetClicked.setSelected(true);
			}
		}

		@Override
		public void mouseEntered(MouseEvent event) {
		}

		@Override
		public void mouseExited(MouseEvent event) {
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if (isEventOnWidget(event)) {
				Widget curWidget = getWidgetFromEvent(event);

				if (curWidget.isSelected()) {
					if (event.getX() >= curWidget.getMoveResizePanel().getWidth()
							- curWidget.getSelectableBorderRegionWidth()) {
						resizingWidth = true;
					}

					if (event.getY() >= curWidget.getMoveResizePanel().getHeight()
							- curWidget.getSelectableBorderRegionWidth()) {
						resizingHeight = true;
					}

					if (!resizingWidth && !resizingHeight) {
						moving = true;
					}

					updateMouseCursor();
				}
			}

			prevX = event.getXOnScreen();
			prevY = event.getYOnScreen();
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			moving = false;
			resizingWidth = false;
			resizingHeight = false;
			updateMouseCursor();
		}

		@Override
		public void mouseDragged(MouseEvent event) {

			if (isEventOnWidget(event)) {
				Widget curWidget = getWidgetFromEvent(event);

				if (curWidget.isSelected()) {
					int deltaX = event.getXOnScreen() - prevX;
					int deltaY = event.getYOnScreen() - prevY;

					if (moving) {
						curWidget.moveWidget(deltaX, deltaY);
					} else {
						if (resizingWidth && resizingHeight) {
							curWidget.resizeWidget(deltaX, deltaY);
						} else if (resizingWidth) {
							curWidget.resizeWidget(deltaX, 0);
						} else if (resizingHeight) {
							curWidget.resizeWidget(0, deltaY);
						}
					}
				}
			}
			prevX = event.getXOnScreen();
			prevY = event.getYOnScreen();
		}

		@Override
		public void mouseMoved(MouseEvent event) {
		}

		private void updateMouseCursor() {
			Cursor cursorToSet = Cursor.getDefaultCursor();
			if (resizingWidth && resizingHeight) {
				cursorToSet = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
			} else if (resizingWidth) {
				cursorToSet = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			} else if (resizingHeight) {
				cursorToSet = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
			} else if (moving) {
				cursorToSet = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
			}
			setCursor(cursorToSet);
		}

		private boolean isEventOnWidget(MouseEvent event) {
			return event.getSource() instanceof Widget.MoveResizePanel;
		}

		private Widget getWidgetFromEvent(MouseEvent event) {
			return ((Widget.MoveResizePanel) event.getSource()).getWidget();
		}
	}

	private class WidgetRemoveManager implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				for (int i = 0; i < widgets.size(); i++) {
					if (widgets.get(i).getMoveResizePanel() == e.getComponent()) {
						if (widgets.get(i).isSelected()) {
							widgets.get(i).removeWidget();
						}
						break;
					}
				}
			}
		}
	}
}

package dashboard;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class WidgetPanel extends JPanel {

	private final ArrayList<Widget> widgets;
	private final WidgetMoveResizeManager moveResizeManager;

	WidgetPanel() {
		super(null);

		widgets = new ArrayList<Widget>();
		moveResizeManager = new WidgetMoveResizeManager();

		this.addMouseListener(moveResizeManager);
		this.addMouseMotionListener(moveResizeManager);
	}

	void addWidget(Widget w) {
		this.add(w.getMoveResizePanel());
		w.getMoveResizePanel().addMouseListener(moveResizeManager);
		w.getMoveResizePanel().addMouseMotionListener(moveResizeManager);
		w.setup();
		widgets.add(w);
	}

	@Override
	protected void paintComponent(Graphics g) {

		for (int i = 0; i < widgets.size(); i++) {
			if (widgets.get(i).removeRequested()) {
				remove(widgets.get(i).getMoveResizePanel());
				widgets.remove(i);
				i--;
			}
		}

		super.paintComponent(g);
	}

	void triggerWidgetLoad() {
		for (int i = 0; i < widgets.size(); i++) {
			widgets.get(i).widgetLoaded();
		}
	}

	private class WidgetMoveResizeManager implements MouseInputListener, Serializable {

		private int prevX, prevY;
		private boolean moving, resizingWidth, resizingHeight;

		@Override
		public void mouseClicked(MouseEvent event) {

			for (Widget w : widgets) {
				w.setSelected(false);
			}

			if (isEventOnWidget(event)) {
				Widget widgetClicked = getWidgetFromEvent(event);
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
}

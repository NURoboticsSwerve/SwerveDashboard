package dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
abstract public class Widget extends JPanel {

	private final MoveResizePanel moveResizePanel;

	private int selectableBorderRegionWidth;

	private boolean selected;

	private boolean removeRequested;

	public Widget() {
		this.setLayout(new BorderLayout());
		moveResizePanel = new MoveResizePanel(this);
		setSelectableBorderRegionWidth(10);
		moveResizePanel.add(this, BorderLayout.CENTER);
		moveResizePanel.setBounds(0, 0, 0, 0);
	}

	void setup() {
		moveWidget(25, 25);
		resizeWidget(getPreferredSize().width, getPreferredSize().height);
	}

	void setSelected(boolean selected) {
		this.selected = selected;

		if (selected) {
			moveResizePanel.requestFocusInWindow();
		}

		updateBorder();
	}

	boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the width of the region around the edge of the widget that is outside
	 * the widget itself but still trigger selection as if you had clicked on the
	 * widget.
	 * 
	 * @param width The width of the extra selectable region.
	 */
	void setSelectableBorderRegionWidth(int width) {
		if (width % 2 == 1) {
			width++;
		}

		selectableBorderRegionWidth = width + 1;
		updateBorder();
	}

	int getSelectableBorderRegionWidth() {
		return selectableBorderRegionWidth;
	}

	/**
	 * Gets the 'MoveResizePanel' which is the panel surrounding the widget
	 * responsible for its drag to move and resize behavior.
	 * 
	 * @return The MoveResizePanel
	 */
	MoveResizePanel getMoveResizePanel() {
		return moveResizePanel;
	}

	/**
	 * Moves this widget, by means of it's 'MoveResizePanel'
	 * 
	 * @param deltaX X distance to move.
	 * @param deltaY Y distance to move.
	 */
	void moveWidget(int deltaX, int deltaY) {
		int curX = moveResizePanel.getX();
		int curY = moveResizePanel.getY();
		moveResizePanel.setBounds(curX + deltaX, curY + deltaY, moveResizePanel.getWidth(),
				moveResizePanel.getHeight());
	}

	/**
	 * Resizes this widget, by means of it's 'MoveResizePanel' Note that this method
	 * does not allow the widget to be smaller than it's requested minimum size.
	 * 
	 * @param deltaWidth  Change in width to apply.
	 * @param deltaHeight Change in height to apply.
	 * 
	 * @see Component#setMinimumSize(java.awt.Dimension)
	 */
	void resizeWidget(int deltaWidth, int deltaHeight) {
		Insets is = moveResizePanel.getInsets();
		int insetWidth = is.left + is.right;
		int insetHeight = is.top + is.bottom;
		int newWidth = getWidth() + deltaWidth;
		int newHeight = getHeight() + deltaHeight;

		newWidth = Math.max(newWidth, getMinimumSize().width);
		newHeight = Math.max(newHeight, getMinimumSize().height);

		moveResizePanel.setBounds(moveResizePanel.getX(), moveResizePanel.getY(), newWidth + insetWidth,
				newHeight + insetHeight);
		this.setSize(newWidth, newHeight);
		revalidate();
	}

	/**
	 * Requests that a widget be removed. Note that this is the only correct way to
	 * remove a widget, as deconstruction tasks must occur before it is actually
	 * removed.
	 * 
	 * @see Widget#deconstruct()
	 */
	protected void removeWidget() {
		removeRequested = true;
	}

	boolean removeRequested() {
		return removeRequested;
	}

	/**
	 * Converts this widget into a string that can be used to recreate the widget
	 * later.
	 * 
	 * @return A string decribing this widget.
	 */
	String toSaveForm() {
		// Add the basic information about this widget
		String s = this.getClass().getCanonicalName() + ",";
		s += moveResizePanel.getX() + "," + moveResizePanel.getY() + "," + moveResizePanel.getWidth() + ","
				+ moveResizePanel.getHeight();

		// Add widget specific information. This data is provided by each widget type
		// individually as a map, and is then converted to a Base64 string. Base64
		// prevents any reserved delimiters from appearing in the arbitrary data
		// provided by each widget.
		Map<String, String> saveMap = widgetSaved();
		if (saveMap != null) {
			Encoder encoder = Base64.getEncoder();
			for (Entry<String, String> curEntry : saveMap.entrySet()) {
				s += "," + encoder.encodeToString(curEntry.getKey().getBytes());
				s += ":" + encoder.encodeToString(curEntry.getValue().getBytes());
			}
		}
		return s;
	}

	/**
	 * Called when this widget is being removed. This is a widget's last chance to
	 * perform any clean up operations before it is removed from the dashboard.
	 * 
	 * @see Widget#removeWidget()
	 */
	abstract protected void deconstruct();

	/**
	 * Called when this widget is loaded from a save file. A map holds key value
	 * pairs of data that was assocaited with this widget in the save file. These
	 * values should be used to set the state of this widget to the state it was at
	 * when it was saved.
	 * 
	 * @param args A map of values assocaited with this widget in the save file.
	 * 
	 * @see Widget#widgetSaved()
	 */
	abstract protected void widgetLoaded(Map<String, String> args);

	/**
	 * Called when this widget is saved to a file. This method must return a map
	 * holding key value pairs of data assocaited with this widget. These values
	 * will be read and loaded when the file is loaded in order to recreate the
	 * state of this widget.
	 * 
	 * @return A map of values to assocaited with this widget in the save file.
	 * 
	 * @see Widget#widgetLoaded()
	 */
	abstract protected Map<String, String> widgetSaved();

	/**
	 * Called when the user right clicks on the widget. Right clicking should show a
	 * pop-up window with options to configure the behavior of this widget.
	 */
	protected abstract void showSettingsWindow();

	private void updateBorder() {
		Border outerBorder = BorderFactory.createEmptyBorder(selectableBorderRegionWidth / 2,
				selectableBorderRegionWidth / 2, selectableBorderRegionWidth / 2, selectableBorderRegionWidth / 2);
		Border middleBorder = selected ? BorderFactory.createLineBorder(Color.BLACK, 1, true)
				: BorderFactory.createEmptyBorder(1, 1, 1, 1);
		Border innerBorder = BorderFactory.createEmptyBorder(selectableBorderRegionWidth / 2,
				selectableBorderRegionWidth / 2, selectableBorderRegionWidth / 2, selectableBorderRegionWidth / 2);
		moveResizePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder,
				BorderFactory.createCompoundBorder(middleBorder, innerBorder)));
	}

	public class MoveResizePanel extends JPanel {

		private final Widget widget;

		private MoveResizePanel(Widget widget) {
			super(new BorderLayout(0, 0));
			this.setOpaque(false);
			this.setFocusable(true);
			this.widget = widget;
		}

		Widget getWidget() {
			return widget;
		}
	}
}

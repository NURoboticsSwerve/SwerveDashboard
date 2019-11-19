package dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
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

	MoveResizePanel getMoveResizePanel() {
		return moveResizePanel;
	}

	void moveWidget(int deltaX, int deltaY) {
		int curX = moveResizePanel.getX();
		int curY = moveResizePanel.getY();
		moveResizePanel.setBounds(curX + deltaX, curY + deltaY, moveResizePanel.getWidth(),
				moveResizePanel.getHeight());
	}

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

	protected void removeWidget() {
		removeRequested = true;
	}

	boolean removeRequested() {
		return removeRequested;
	}

	String toSaveForm() {
		String s = this.getClass().getCanonicalName() + ",";
		s += this.getX() + "," + this.getY() + "," + this.getWidth() + "," + this.getHeight();

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

	abstract protected void widgetLoaded(Map<String, String> args);

	abstract protected Map<String, String> widgetSaved();

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

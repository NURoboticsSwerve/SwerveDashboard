package widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import dashboard.Widget;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class NyanWidget extends Widget {

	public static final String NAME = "Nyan";

	private static final int NUM_FRAMES = 12;

	private BufferedImage masterImage;
	private final JLabel imageLabel;
	
	private final Timer timer;

	private int curFrame;

	public NyanWidget() {

		imageLabel = new JLabel();
		imageLabel.setPreferredSize(new Dimension(400, 400));
		this.add(imageLabel, BorderLayout.CENTER);

		try {
			masterImage = ImageIO.read(this.getClass().getResourceAsStream("/nyan.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateNyan();
			}
		}, 0, 100);
	}

	private void updateNyan() {
		int frameWidth = masterImage.getWidth() / NUM_FRAMES;
		BufferedImage curFrameImage = masterImage.getSubimage(curFrame * frameWidth, 0, frameWidth,
				masterImage.getHeight());
		imageLabel.setIcon(new ImageIcon(curFrameImage));

		curFrame++;
		curFrame %= NUM_FRAMES;
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

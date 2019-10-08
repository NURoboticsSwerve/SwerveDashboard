package widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class NyanWidget extends DecoratedWidget {

	private static final int NUM_FRAMES = 12;

	private final JLabel imageLabel;

	private transient BufferedImage masterImage;

	private int curFrame;

	public NyanWidget() {

		imageLabel = new JLabel();
		imageLabel.setPreferredSize(new Dimension(400, 400));
		this.add(imageLabel, BorderLayout.CENTER);

		decoratedWidgetLoaded();
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
	protected void decoratedWidgetLoaded() {
		try {
			masterImage = ImageIO.read(this.getClass().getResourceAsStream("/nyan.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Timer(true).schedule(new TimerTask() {

			@Override
			public void run() {
				updateNyan();
			}
		}, 0, 100);
	}
}

package dashboard;

import java.awt.Dialog.ModalityType;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import network.NetworkClient;
import widgets.FakeRobotWidget;
import widgets.RedRectangleWidget;
import widgets.TextBoxWidget;

/**
 * @author Nicholas Contreras
 */

public class Dashboard {

	private static final File DATA_DIR = new File(System.getProperty("user.home"), "DashboardData");

	private final ArrayList<Class<? extends Widget>> widgetTypes;

	private JFrame frame;
	private WidgetPanel widgetPanel;

	private Dashboard() {
		widgetTypes = new ArrayList<Class<? extends Widget>>();
	}

	private void setWidgetPanel(WidgetPanel wp) {
		if (widgetPanel != null) {
			frame.remove(widgetPanel);
		}
		frame.add(wp);
		widgetPanel = wp;
		frame.revalidate();
	}

	private void loadBuiltInWidgets() {
		widgetTypes.add(RedRectangleWidget.class);
		widgetTypes.add(TextBoxWidget.class);
		widgetTypes.add(FakeRobotWidget.class);
	}

	@SuppressWarnings("unchecked")
	private void loadExternalWidgets() {

		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;

		Method method = null;
		try {
			method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (!DATA_DIR.exists()) {
			DATA_DIR.mkdir();
		}

		for (File curFile : DATA_DIR.listFiles()) {
			if (curFile.getName().endsWith(".jar")) {
				System.out.println("Found jarfile: " + curFile.getName());
				try {
					JarFile jarFile = new JarFile(curFile);
					Enumeration<JarEntry> e = jarFile.entries();

					method.invoke(sysloader, new Object[] { curFile.toURI().toURL() });

					while (e.hasMoreElements()) {
						JarEntry je = e.nextElement();

						System.out.println("Cur element: " + je.getName());

						if (je.isDirectory() || !je.getName().endsWith(".class")) {
							continue;
						}
						// -6 because of .class
						String className = je.getName().substring(0, je.getName().length() - 6);
						className = className.replace('/', '.');
						try {
							Class<?> c = sysloader.loadClass(className);
							System.out.println("Loaded class: " + c);
							System.out.println("is widget? " + Widget.class.isAssignableFrom(c));
							if (Widget.class.isAssignableFrom(c)) {
								widgetTypes.add((Class<? extends Widget>) c);
							}
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						}
					}
					jarFile.close();
				} catch (IOException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void init() {

		loadBuiltInWidgets();
		loadExternalWidgets();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		NetworkClient.getInstance().setAddress("localhost", 12345);

		frame = new JFrame("Dashboard");
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setMinimumSize(new Dimension(400, 300));

		loadSaveFile(new File(DATA_DIR, "default.dash"));

		if (widgetPanel == null) {
			System.out.println("Creating blank panel");
			widgetPanel = new WidgetPanel();
		}

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem clearMenuItem = new JMenuItem("Clear");
		clearMenuItem.addActionListener((ActionEvent) -> {
			setWidgetPanel(new WidgetPanel());
		});
		fileMenu.add(clearMenuItem);

		JMenuItem loadMenuItem = new JMenuItem("Load");
		loadMenuItem.addActionListener((ActionEvent) -> {
			JFileChooser jfc = new JFileChooser(DATA_DIR);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setMultiSelectionEnabled(false);
			jfc.setFileFilter(new FileNameExtensionFilter("Dashboard Layouts (.dash)", "dash"));
			int result = jfc.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File toLoad = jfc.getSelectedFile();
				loadSaveFile(toLoad);
			}
		});
		fileMenu.add(loadMenuItem);

		JMenuItem saveCustomMenuItem = new JMenuItem("Save as Custom Layout");
		saveCustomMenuItem.addActionListener((ActionEvent) -> {
			JFileChooser jfc = new JFileChooser(DATA_DIR);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setMultiSelectionEnabled(false);
			jfc.setFileFilter(new FileNameExtensionFilter("Dashboard Layouts (.dash)", "dash"));
			int result = jfc.showSaveDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File toSave = jfc.getSelectedFile();
				String fileName = toSave.getName();
				if (!fileName.endsWith(".dash")) {
					fileName += ".dash";
				}
				writeSaveFile(new File(toSave.getParentFile(), fileName));
			}
		});
		fileMenu.add(saveCustomMenuItem);

		JMenuItem saveDefaultMenuItem = new JMenuItem("Save as Default Layout");
		saveDefaultMenuItem.addActionListener((ActionEvent) -> {
			writeSaveFile(new File(DATA_DIR, "default.dash"));
		});
		fileMenu.add(saveDefaultMenuItem);

		JMenuItem settingsMenuItem = new JMenuItem("Network Settings");
		settingsMenuItem.addActionListener((ActionEvent) -> generateNetworkSettingsDialog());
		fileMenu.add(settingsMenuItem);

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener((ActionEvent) -> System.exit(0));
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		JMenu addMenu = new JMenu("Add");

		for (Class<? extends Widget> curWidgetType : widgetTypes) {
			System.out.println("Add to menu: " + curWidgetType);
			JMenuItem curAddMenuItem = new JMenuItem(curWidgetType.getSimpleName());
			curAddMenuItem.addActionListener((ActionEvent) -> {
				try {
					widgetPanel.addWidget(curWidgetType.newInstance());
					frame.revalidate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			addMenu.add(curAddMenuItem);
		}

		menuBar.add(addMenu);

		frame.setJMenuBar(menuBar);

		setWidgetPanel(widgetPanel);

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				frame.repaint();
			}
		}, 0, 20);
	}

	private void generateNetworkSettingsDialog() {
		JDialog networkSettingsDialog = new JDialog();
		networkSettingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);

		JPanel outerPanel = new JPanel(new BorderLayout(5, 5));

		outerPanel.add(new JLabel("Network Settings", JLabel.CENTER), BorderLayout.NORTH);

		JPanel panel = new JPanel(new GridLayout(0, 2));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		panel.add(new JLabel("Robot Address:"));
		JTextField addressTextField = new JTextField(NetworkClient.getInstance().getTargetAddress());
		panel.add(addressTextField);

		panel.add(new JLabel("Port Number:"));
		JTextField portNumberField = new JTextField(NetworkClient.getInstance().getTargetPort() + "");
		panel.add(portNumberField);

		outerPanel.add(panel, BorderLayout.CENTER);

		JButton reconnectButton = new JButton("Reconnect");
		reconnectButton.addActionListener((ActionEvent) -> {
			NetworkClient.getInstance().setAddress(addressTextField.getText(),
					Integer.parseInt(portNumberField.getText()));
		});
		outerPanel.add(reconnectButton, BorderLayout.SOUTH);

		networkSettingsDialog.add(outerPanel);
		networkSettingsDialog.pack();
		networkSettingsDialog.setLocationRelativeTo(frame);
		networkSettingsDialog.setVisible(true);
	}

	private void writeSaveFile(File saveFile) {
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream output = new ObjectOutputStream(bos);
			output.writeObject(widgetPanel);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadSaveFile(File saveFile) {
		if (saveFile.isFile() && saveFile.canRead()) {
			try {
				FileInputStream fis = new FileInputStream(saveFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream input = new ObjectInputStream(bis);
				Object read = input.readObject();
				input.close();
				setWidgetPanel((WidgetPanel) read);
				widgetPanel.triggerWidgetLoad();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Dashboard().init());
	}
}

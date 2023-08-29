package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main implements FileUtils
{
	static PrintStream stdout=System.out,stderr=System.err;
	static void initLogger(Consumer<String> cout,Consumer<String> cerr)
	{
		if(cout!=null)
			System.setOut(redirect(cout,stdout));
		else System.setOut(stdout);
		if(cerr!=null)
			System.setErr(redirect(cerr,stderr));
		else System.setErr(stderr);
	}
	static PrintStream redirect(Consumer<String> consumer,OutputStream out)
	{
		try
		{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			OutputStream os=new OutputStream()
			{
				@Override
				public synchronized void write(int b) throws IOException
				{
					out.write(b);
//					stderr.print((char)b);
//					stderr.println("accept");
					if(String.valueOf((char)b).matches("\r\n|[\n\r\u2028\u2029\u0085]"))
					{
						if(baos.size()!=0)
						{
							consumer.accept(new String(baos.toByteArray(), StandardCharsets.UTF_8));
							baos.reset();
						}
					}
					else baos.write(b);
//					stderr.println("accept2");
				}
			};
			return new PrintStream(os,true);
		}
		catch (Exception e)
		{
			System.exit(-1);
			return null;
		}
	}

	public static void main(String[] args)
	{
		try
		{
			initLogger((s)->{stdout.println("received "+s);},(s)->{stderr.println("received "+s);});
			System.out.println("stdout test");
			System.err.println("stderr test");
			initLogger(null,null);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JDialog dialog = new JDialog();
			dialog.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					System.exit(11);
				}
			});
			JLabel label = new JLabel(L10n.INITIALIZING.toString());
			label.setPreferredSize(new Dimension(160, 64));
			dialog.setContentPane(label);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
//			initLogger();
			System.out.println("test");
			//read config
			MainConfig config;
			if (!getConfigFile().isFile())
				config = new MainConfig();
			else config = Policy.gson.fromJson(Utils.readAllUTFString(getConfigFile()), MainConfig.class);
			while (config.aicDir == null || !config.aicDir.isDirectory())
			{
//				JDialog
				int id = JOptionPane.showConfirmDialog(null, L10n.NO_AIC_DIR, L10n.NO_AIC_DIR_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION);
				if (id == JOptionPane.OK_OPTION)
					config.aicDir = chooseDir();
				else System.exit(1);
			}
			int cid;
			do
			{
				cid = JOptionPane.showConfirmDialog(null, String.format(L10n.FOUND_AIC_DIR.toString(), config.aicDir.getAbsolutePath()), L10n.FOUND_AIC_DIR_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION);
				if (cid == JOptionPane.CLOSED_OPTION)
					System.exit(3);
				if (cid != JOptionPane.OK_OPTION)
					config.aicDir = chooseDir();
			}
			while (cid != JOptionPane.OK_OPTION);
			Utils.writeAllUTFString(getConfigFile(), Policy.gson.toJson(config));
			checkGit(config);
			RedirectHandler redirectHandler = new RedirectHandler(config.aicDir);
			redirectHandler.createDirectories();
			if (!new File(config.aicDir, "BepInEx/core/BepInEx.dll").isFile()
					|| !new File(config.aicDir, "winhttp.dll").isFile())
				//fix bepinex
				fixBepInEx(config);
			//check if theres aicutils
			updateAicUtils(config);
			//check if theres unpack
			checkNeedRefresh(config, redirectHandler);
			checkRedirect(config, redirectHandler);
			refreshGUI(config, redirectHandler);
			dialog.dispose();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	static JFrame mainFrame=null;
	static synchronized void refreshGUI(MainConfig config,RedirectHandler redirectHandler)
	{
		if(mainFrame!=null)
			mainFrame.dispose();

		mainFrame=initGUI(config,redirectHandler);
	}

	static JFrame initGUI(MainConfig config, RedirectHandler redirectHandler)
	{
		JFrame frame = new JFrame(L10n.MAINTITLE.toString());
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setPreferredSize(new Dimension(640, 360));
		panel.setLayout(null);
		frame.setLayout(new FlowLayout());
		frame.setContentPane(panel);
		panel.add(new JLabel(config.aicDir.getAbsolutePath()));

//		JRadioButton
		JPanel mods = new JPanel();
//		mods.setBorder(new LineBorder(Color.BLACK,1));
		Arrays.stream(redirectHandler.getWorkDir().listFiles())
				.filter(f -> f.isDirectory() && !f.getName().equalsIgnoreCase("redirect") && !f.getName().equalsIgnoreCase("unpack"))
				.map(f ->
				{
					return getModPanel(f,config,redirectHandler);
				})
				.filter(p -> p != null)
				.forEach(p -> {
					JPanel border=new JPanel();
					border.setSize(p.getWidth(),1);
					border.setPreferredSize(border.getSize());
					border.setMaximumSize(border.getSize());
					border.setMinimumSize(border.getSize());
					border.setBackground(new Color(0));
					mods.add(border);
					mods.add(p);
				});
		if(mods.getComponents().length==0)
		{
			JTextArea area=new JTextArea(L10n.NO_WORKING.toString());
			area.setEditable(false);
			mods.add(area);
		}
		else
			mods.remove(0);
		mods.setLayout(new BoxLayout(mods,BoxLayout.Y_AXIS));
//		mods.setSize(mods.getPreferredSize());
		System.out.println(Arrays.toString(mods.getComponents()));
		System.out.println("prefer "+mods.getPreferredSize());

		JScrollPane scrollPane = new JScrollPane(mods);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		panel.add(scrollPane);
		scrollPane.setBounds(16, 16 + 32, (int) (2+384 + scrollPane.getVerticalScrollBar().getPreferredSize().getWidth()), 300);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JLabel something = new JLabel(L10n.BORDER_WORK_MODS.toString());
		something.setFont(getFont(16));
		something.setLocation(scrollPane.getX(), scrollPane.getY() - 32);
		something.setSize(something.getPreferredSize());
		panel.add(something);

		JButton button = new JButton(L10n.CREATE_NEW_MOD.toString());
		button.setFont(getFont(16));
		button.setLocation(scrollPane.getX() + scrollPane.getWidth() + 32, scrollPane.getY());
		button.setSize(button.getPreferredSize());
		button.addActionListener(l ->
		{
			button.setEnabled(false);
			try
			{
				mainFrame.dispose();
				boolean ok=createNewMod(config, redirectHandler,frame);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
		panel.add(button);
		JButton runButton = new JButton(L10n.RUN_AIC.toString());
		runButton.setFont(getFont(16));
		runButton.setLocation(scrollPane.getX() + scrollPane.getWidth() + 32, button.getY()+button.getHeight()+32);
		runButton.setSize(runButton.getPreferredSize());
		runButton.addActionListener(l ->
		{
			runButton.setEnabled(false);
			try
			{
				mainFrame.dispose();
				runAliceInCradle(config,redirectHandler);
			} catch (Exception e)
			{
				e.printStackTrace();
				System.exit(13);
			}
		});
		panel.add(runButton);


		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		return frame;
	}
	static void runAliceInCradle(MainConfig config,RedirectHandler redirectHandler)
	{
		mainFrame.dispose();
		JDialog dialog=new JDialog();

		dialog.setLocationRelativeTo(null);
		JPanel panel=new JPanel();
		panel.setSize(640,320);
		panel.setPreferredSize(panel.getSize());
		panel.setMaximumSize(panel.getSize());
		dialog.setContentPane(panel);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(12);
			}
		});

		JLabel label=new JLabel(L10n.RUNNING_AIC.toString());
		label.setFont(getFont(16));
		panel.add(label);

		JTextArea area=new JTextArea("initializing\n");
		JScrollPane pane=new JScrollPane(area);
		pane.setPreferredSize(new Dimension(560,420/420*225));
		panel.add(pane);
		area.setBorder(new LineBorder(Color.BLACK,1));
		area.setEditable(false);
		area.getCaret().setVisible(true);
//		panel.add(area);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		dialog.setResizable(false);
		Consumer<String> consumer=new Consumer<String>()
		{
			@Override
			public synchronized void accept(String s)
			{
				area.append(s+"\n");
//				dialog.pack();
				area.setCaretPosition(area.getDocument().getLength());
			}
		};
		initLogger(
				(s)-> consumer.accept(String.format("(%s) [%s] <STDOUT> %s", Thread.currentThread().getName(),new SimpleDateFormat("HH:mm:ss.SSSS").format(new Date()),s)),
				(s)-> consumer.accept(String.format("(%s) [%s] <STDERR> %s", Thread.currentThread().getName(),new SimpleDateFormat("HH:mm:ss.SSSS").format(new Date()),s)));
		Thread t=new Thread(()->{
			try
			{
				AICLauncher.main(
						new String[]{
								"--dir", config.aicDir.getAbsolutePath(),
								"--force",
								"--run",
								"--block"
						}
				);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			refreshGUI(config,redirectHandler);
		});
		t.start();
	}

	static boolean createNewMod(MainConfig config, RedirectHandler redirectHandler,JFrame or)
	{
		String str = JOptionPane.showInputDialog(null, L10n.INPUT_MOD_NAME, L10n.INPUT_MOD_NAME_TITLE.toString(), JOptionPane.INFORMATION_MESSAGE);
		if (str == null || str.isEmpty()) return false;
		File target = new File(redirectHandler.getWorkDir(), str);
		if (!str.matches("\\w+") || target.exists())
		{
			JOptionPane.showMessageDialog(null, L10n.FALSE_MOD_PATH, L10n.INPUT_MOD_NAME_TITLE.toString(), JOptionPane.WARNING_MESSAGE);
			return false;
		}

//		AtomicBoolean end=new AtomicBoolean(false);

		JDialog dialog=new JDialog(or,false);
//		dialog.setUndecorated(true);
		dialog.setLocationRelativeTo(null);
		JPanel panel=new JPanel();
		panel.setSize(640,320);
		panel.setPreferredSize(panel.getSize());
		panel.setMaximumSize(panel.getSize());
		dialog.setContentPane(panel);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JLabel label=new JLabel(String.format(L10n.WORKING.toString(), str));
		label.setFont(getFont(16));
		panel.add(label);

		JTextArea area=new JTextArea("initializing\n");
		JScrollPane pane=new JScrollPane(area);
		pane.setPreferredSize(new Dimension(560,420/420*225));
		panel.add(pane);
		area.setBorder(new LineBorder(Color.BLACK,1));
		area.setEditable(false);
		area.getCaret().setVisible(true);
//		panel.add(area);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		dialog.setResizable(false);
		Consumer<String> consumer=new Consumer<String>()
		{
			@Override
			public synchronized void accept(String s)
			{
				area.append(s+"\n");
//				dialog.pack();
				area.setCaretPosition(area.getDocument().getLength());
			}
		};
		initLogger(
				(s)-> consumer.accept(String.format("(%s) [%s] <STDOUT> %s", Thread.currentThread().getName(),new SimpleDateFormat("HH:mm:ss.SSSS").format(new Date()),s)),
				(s)-> consumer.accept(String.format("(%s) [%s] <STDERR> %s", Thread.currentThread().getName(),new SimpleDateFormat("HH:mm:ss.SSSS").format(new Date()),s)));


		Thread thread=new Thread(()->
				{
					long time=System.currentTimeMillis();
					System.out.println("thread start");
					try
					{
						target.mkdirs();
						Mod mod = Mod.createMod(str, target);
						mod.initMod(redirectHandler.getUnpackDir(), new File(redirectHandler.getStreamingAssetsDir(), "localization"));
						System.out.println("finished task without exception..?");
					} catch (Exception e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(null,L10n.INIT_MOD_FAILED);
						dialog.dispose();
						refreshGUI(config,redirectHandler);
						return;
					}
//					end.getAndSet(true);
					initLogger(null,null);
					dialog.dispose();
//					dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					long interval=System.currentTimeMillis()-time;
					int chs=JOptionPane.showConfirmDialog(null, String.format(L10n.INIT_MOD_FINISH.toString(), str, String.format("%d:%02d.%03d", interval/60/1000,interval/1000%60,interval%1000)),L10n.INIT_MOD_FINISH_TITLE.toString(), JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
					if(chs==0)
					{
						try
						{
							Desktop.getDesktop().open(target);
						} catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					refreshGUI(config,redirectHandler);
				}
		);
		thread.start();
		return true;
	}

	static String getStates()
	{
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		while (group.getParent() != null)
			group = group.getParent();
		Thread[] threads = new Thread[group.activeCount()];
		while (group.enumerate(threads, true) == threads.length)
			threads = new Thread[threads.length + 1];
		StringJoiner joiner = new StringJoiner("\n");
		Arrays.stream(threads)
				.filter(t -> t != null)
				.sorted(Comparator.comparingLong(t -> t.getId()))
				.forEachOrdered(t ->
				{
					StackTraceElement[] stackTraceElements = t.getStackTrace();
					joiner.add(String.format("%d %s %s", t.getId(), t.getState(), stackTraceElements.length == 0 ? "null" : stackTraceElements[0]));
				});
		return joiner.toString();
	}

	static Font getFont(int size)
	{
		return new Font("Default", Font.PLAIN, size);
	}

	static JPanel getModPanel(File dir,MainConfig config,RedirectHandler redirectHandler)
	{
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setSize(384, 64);
		panel.setPreferredSize(panel.getSize());
		panel.setMaximumSize(panel.getSize());
		panel.setMinimumSize(panel.getSize());
		LineBorder lineBorder = new LineBorder(Color.BLACK, 2 / 2);
//		panel.setBorder(lineBorder);
		try
		{
			Mod mod = Mod.readMod(dir);

			panel.setToolTipText("<html>" + mod.info.replace("\n", "<br>") + "</html>");

			JLabel name = new JLabel(mod.name.equals(dir.getName())?mod.name:dir.getName()+" / "+mod.name);
			name.setFont(getFont(20));
			name.setLocation(8 + 64, 0);
			name.setSize(name.getPreferredSize());
			panel.add(name);

			JLabel version = new JLabel(mod.version + " " + "(" + mod.versionId + ")");
			version.setFont(getFont(12));
			version.setLocation(name.getX(), name.getY() + name.getHeight());
			version.setSize(version.getPreferredSize());
			panel.add(version);

			JLabel assetsVersion = new JLabel(mod.assetsVersion == null ? L10n.UNKNOWN_VERSION.toString() : mod.assetsVersion);
			assetsVersion.setFont(getFont(12));
			assetsVersion.setLocation(name.getX() + name.getWidth(), (int) (name.getY() + name.getHeight() - assetsVersion.getPreferredSize().getHeight()));
			assetsVersion.setSize(assetsVersion.getPreferredSize());
			panel.add(assetsVersion);

			StringJoiner joiner = new StringJoiner(", ");
			Arrays.stream(mod.author).forEachOrdered(a -> joiner.add(a));
			JLabel author = new JLabel(joiner.length() != 0 ? joiner.toString() : L10n.NO_AUTHOR.toString());
			author.setFont(getFont(12));
			author.setLocation(name.getX(), name.getY() + name.getHeight() + version.getHeight());
			author.setSize(author.getPreferredSize());
			panel.add(author);

			JButton build = new JButton(L10n.BUILD.toString());
			build.setFont(getFont(16));
			build.setSize(build.getPreferredSize());
			build.setLocation(panel.getWidth() - build.getWidth() - 2, 0 + 2);
			panel.add(build);

			JButton openDir = new JButton(L10n.OPEN_DIR.toString());
			openDir.setFont(getFont(16));
			openDir.setSize(openDir.getPreferredSize());
			openDir.setLocation(panel.getWidth() - openDir.getWidth() - 2, 0 + build.getHeight() + 2);
			panel.add(openDir);

			JLabel iconLabel = new JLabel();
//			iconLabel.setBorder(lineBorder);
			iconLabel.setLocation(0, 0);
			iconLabel.setSize(64, 64);
			iconLabel.setPreferredSize(iconLabel.getSize());
			try
			{
				BufferedImage image = ImageIO.read(new File(dir, "pack.png"));
				ImageIcon icon = new ImageIcon(image.getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_FAST));
				iconLabel.setIcon(icon);
			} catch (Exception e)
			{
				iconLabel.setFont(getFont(12));
				iconLabel.setText(L10n.NO_PACK_PNG.toString());
			}
			panel.add(iconLabel);

			openDir.addActionListener(l ->
			{
				try
				{
					Desktop.getDesktop().open(dir);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			});

			build.addActionListener(l->{
				build.setEnabled(false);
				try
				{
					mainFrame.dispose();
					buildMod(mod, config, redirectHandler);
				}
				catch (Exception e){}

			});

		} catch (Exception e)
		{
			StringWriter sw=new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			panel.setToolTipText("<html>" + sw.toString().replace("\n", "<br>") + "</html>");

			JLabel name = new JLabel(dir.getName());
			panel.setBackground(new Color(255,240,240));
			name.setFont(getFont(20));
			name.setLocation(8 + 64, 0);
			name.setSize(name.getPreferredSize());
			panel.add(name);

			JLabel assetsVersion = new JLabel(L10n.FAILED_READING.toString());
			assetsVersion.setFont(getFont(12));
			assetsVersion.setLocation(name.getX(), name.getY() + name.getHeight());
			assetsVersion.setSize(assetsVersion.getPreferredSize());
			panel.add(assetsVersion);

			JLabel failInfo = new JLabel(e.getLocalizedMessage());
			failInfo.setFont(getFont(12));
			failInfo.setLocation(name.getX(), assetsVersion.getY() + assetsVersion.getHeight());
			failInfo.setSize(panel.getWidth()-failInfo.getX(), (int) failInfo.getPreferredSize().getHeight());
			panel.add(failInfo);
			return panel;
		}
		return panel;
	}

	static void buildMod(Mod mod,MainConfig config,RedirectHandler redirectHandler)
	{
		JDialog dialog=new JDialog();
		JPanel panel=new JPanel();
		panel.setSize(320,240);
		panel.setPreferredSize(panel.getSize());
		dialog.setResizable(false);
		dialog.setContentPane(panel);
		dialog.pack();
		JLabel label=new JLabel(String.format(L10n.BUILDING_MOD.toString(), mod.name));
		label.setFont(getFont(16));
		panel.add(label);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		Thread t=new Thread(()->
		{
			File file = new File(new File(config.aicDir, "mods"), mod.name + ".zip");
			try
			{
				FileOutputStream fos = new FileOutputStream(file);
				mod.diffAll(fos);
				fos.close();
			} catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, L10n.BUILD_MOD_FAILED);
				refreshGUI(config,redirectHandler);
				return;
			}
			dialog.dispose();
			int chs = JOptionPane.showConfirmDialog(null,
					String.format(L10n.BUILD_MOD_FINISH.toString(), mod.name),
					L10n.BUILD_MOD_FINISH_TITLE.toString(), JOptionPane.YES_NO_OPTION);
			refreshGUI(config,redirectHandler);
			if (chs != 0)
				return;
			try
			{
				Desktop.getDesktop().open(file.getParentFile());
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
		t.start();
	}

	static void checkRedirect(MainConfig config, RedirectHandler redirectHandler)
	{
		if (redirectHandler.getRedirectInfoFile().isFile())
			return;
		int chs = JOptionPane.showConfirmDialog(null, L10n.REDIRECT, L10n.REDIRECT_TITLE.toString(), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (chs != 0)
			System.exit(10);
		System.out.println("rmdir");
		redirectHandler.rmdir(redirectHandler.getRedirectDir());
		System.out.println("initRedirect");
		redirectHandler.initRedirect();
	}

	static void checkNeedRefresh(MainConfig config, RedirectHandler redirectHandler)
	{
		if (new File(config.aicDir, "work/unpack/info.yml").isFile())
			return;
		int chs = JOptionPane.showConfirmDialog(null, L10n.UNPACK, L10n.UNPACK_TITLE.toString(), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (chs != 0)
			System.exit(9);
		redirectHandler.refreshOrigin();
	}

	static void updateAicUtils(MainConfig config)
	{
		File aicUtils = new File(config.aicDir, "BepInEx/plugins/AicUtils.dll");
		if (aicUtils.isFile())
			return;
		int chs = JOptionPane.showConfirmDialog(null, L10n.AICUTILS, L10n.AICUTILS_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chs != 0)
			System.exit(8);
		InputStream is = Utils.readFromResources("AicUtils.dll", false);
		Utils.writeAllBytes(aicUtils, Utils.readAllBytes(is));
	}

	static void fixBepInEx(MainConfig config) throws Exception
	{
		int chs = JOptionPane.showConfirmDialog(null, L10n.BEX, L10n.BEX_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chs != 0)
			System.exit(7);
		InputStream is = Utils.readFromResources("bepinex.zip", false);
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry e;
		while ((e = zis.getNextEntry()) != null)
		{
			if (e.isDirectory())
				new File(config.aicDir, e.getName()).mkdirs();
			else
			{
				Utils.writeAllBytes(new File(config.aicDir, e.getName()), Utils.readAllBytes(zis));
			}
		}
		zis.close();
	}

	static void checkGit(MainConfig config) throws Exception
	{
//		try
//		{
		File targetGit = new File(config.aicDir, "PortableGit/bin/git.exe");
		try
		{
			if (targetGit.isFile())
			{
				Process p = Runtime.getRuntime().exec(new String[]{targetGit.getAbsolutePath(), "version"});
				int rt = p.waitFor();
				if (rt != 0)
					throw new Exception();
				String msg = Utils.readAllUTFString(p.getInputStream()).trim();
				JOptionPane.showMessageDialog(null, String.format(L10n.FOUND_PGIT.toString(), rt, msg), L10n.FOUND_PGIT_TITLE.toString(), JOptionPane.INFORMATION_MESSAGE);
				Git.targetGit = targetGit;
				return;
			}
		} catch (Exception e)
		{
		}
		try
		{
			if (false)
				if (true)
					throw new Exception();
			Process p = Runtime.getRuntime().exec("git version");
			int rt = p.waitFor();
			if (rt != 0)
				throw new Exception();
			String msg = Utils.readAllUTFString(p.getInputStream()).trim();
			JOptionPane.showMessageDialog(null, String.format(L10n.FOUND_GIT.toString(), rt, msg), L10n.FOUND_GIT_TITLE.toString(), JOptionPane.INFORMATION_MESSAGE);
			return;
		} catch (Exception e)
		{
		}
		int cfm = JOptionPane.showConfirmDialog(null, L10n.NO_GIT, L10n.NO_GIT_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (cfm != 0)
			System.exit(4);
		HttpsURLConnection con = (HttpsURLConnection) new URL("https://mirrors.tuna.tsinghua.edu.cn/github-release/git-for-windows/git/LatestRelease/").openConnection();
		con.connect();
		String rt = Utils.readAllUTFString(con.getInputStream());
		con.disconnect();
		System.out.println(rt);
		String ver = rt.split("PortableGit-")[1].split("-")[0];
		System.out.println(ver);
		String fileName = "PortableGit-" + ver + "-64-bit.7z.exe";
		File targetFile = new File(config.aicDir, fileName);
		URL dl = new URL("https://mirrors.tuna.tsinghua.edu.cn/github-release/git-for-windows/git/LatestRelease/" + fileName);

		cfm = JOptionPane.showConfirmDialog(null, String.format(L10n.DL_GIT.toString(), ver, dl.getPath()), L10n.DL_GIT_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (cfm != 0)
			System.exit(5);

		JProgressBar bar = new JProgressBar();
		bar.setSize(320, 32);
		bar.setLocation(0, 0);
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setSize(320, 32);
		panel.setPreferredSize(panel.getSize());
		panel.setLocation(0, 0);
		panel.add(bar);
		JDialog dialog = new JDialog((Frame) null);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(6);
			}
		});
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		bar.setStringPainted(true);

		bar.setString(L10n.DLING.toString());
		dialog.setTitle(String.format(L10n.DLING_TITLE.toString(), fileName));

		HttpsURLConnection dlcon = (HttpsURLConnection) dl.openConnection();
		InputStream dis = dlcon.getInputStream();
		FileOutputStream fos = new FileOutputStream(targetFile);
		long cont = dlcon.getContentLengthLong();

		bar.setMaximum((int) cont);
		bar.setValue(0);

		long dled = 0;
		int len;
		byte[] buf = new byte[8192];
		while ((len = dis.read(buf)) != -1)
		{
			fos.write(buf, 0, len);
			dled += len;
			bar.setValue((int) dled);
			bar.setString(String.format("%.2f / %.2f MB    %d%%", dled / 1048576.0, cont / 1048576.0, 100L * dled / cont));
		}
		dlcon.disconnect();
		fos.close();
		dialog.setVisible(false);
		dialog.dispose();

		ProcessBuilder builder = new ProcessBuilder();
		builder.command(targetFile.getAbsolutePath(), "-o", new File(config.aicDir, "PortableGit").getAbsolutePath(), "-y");
		builder.inheritIO();
		Process unzip = builder.start();
		unzip.waitFor();
		checkGit(config);
		return;
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}

	static File chooseDir()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName().equalsIgnoreCase("AliceInCradle.exe");
			}

			@Override
			public String getDescription()
			{
				return "AliceInCradle executable (AliceInCradle.exe)";
			}
		});
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int rt = fileChooser.showDialog(null, null);
		if (rt == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile().getParentFile();
		}
		System.exit(2);
		return null;
	}

	static File getConfigFile()
	{
		return new File("config.json");
	}

	static class MainConfig
	{
		File aicDir;

		MainConfig()
		{

		}
	}
}

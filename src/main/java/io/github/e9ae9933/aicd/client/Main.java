package io.github.e9ae9933.aicd.client;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Constants;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.packets.ServerboundRequestPacket;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class Main
{
	public static Gson gson= Policy.gson;
	Handler handler;
	GUI gui;
	public static String host=Policy.serverHost;
	public static int port=Policy.serverPort;
	public static boolean integratedServerAsIntegrated=true;

	boolean ranIntegratedServer=false;
	void runIntegratedServer()
	{
		if(ranIntegratedServer)
			throw new RuntimeException("ran a server");
		ranIntegratedServer=true;
		try
		{
			if (integratedServerAsIntegrated)
				io.github.e9ae9933.aicd.server.Main.main(new String[]{"--port", "0", "--integrated"});
			else
				io.github.e9ae9933.aicd.server.Main.main(new String[]{"--port", "0"});
			host = "localhost";
			port = io.github.e9ae9933.aicd.server.Main.port;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	Main(String[] args) throws Exception
	{
		System.out.println("Encoding "+System.getenv("file.encoding"));
		System.out.println("Or "+ Charset.defaultCharset());
		System.out.println("Chinese support: "+"支持");
		OptionParser optionParser=new OptionParser();
		OptionSpec<Void> integrated=optionParser.accepts("integrated");
		ArgumentAcceptingOptionSpec<String> server=optionParser.accepts("server").availableUnless(integrated).withRequiredArg();
		OptionSet optionSet=optionParser.parse(args);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame initFrame=new JFrame("初始化中");
		initFrame.setSize(200,75);
//		initFrame.setLayout(new FlowLayout(FlowLayout.CENTER));
		initFrame.setLocationRelativeTo(null);
		initFrame.setResizable(false);
//		initFrame.setLayout(null);
		initFrame.setVisible(true);
		initFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JLabel label=new JLabel();
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
//		label.setBounds(0,0,200,75);
		initFrame.add(label);
		//should we run server?
		if(optionSet.has(integrated))
		{
			label.setText("正在启动内置服务端");
			runIntegratedServer();
		}
		if(optionSet.has(server))
		{
			try
			{
				String addr = optionSet.valueOf(server);
				host = addr.split(":")[0];
				port = Integer.parseInt(addr.split(":")[1]);
				new InetSocketAddress(host, port);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				label.setText("--server 指定的参数无效。");
				return;
			}
		}
		label.setText("正在获取游戏路径");
		Utils.getGamePath(true);

		while(handler==null)
		{
			label.setText("正在连接服务器");
			try
			{
				handler = new Handler(host, port);
			} catch (Exception e)
			{
				label.setText("连接服务器失败，请尝试 --server 启动参数");
				e.printStackTrace();
				int chs = JOptionPane.showOptionDialog(null, "连接服务器失败。\n点击“输入地址”来输入自定义的服务器地址。\n点击“离线模式”以启动内置服务端。", "连接服务器失败", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"输入地址", "离线模式", "重试", "取消"}, "离线模式");
				if (chs == JOptionPane.CLOSED_OPTION || chs == 3)
					System.exit(0);
				if (chs == 2)
					continue;
				if(chs==1)
				{
					label.setText("正在启动内置服务端");
					runIntegratedServer();
					continue;
				}
				if(chs==0)
				{
					String addr=JOptionPane.showInputDialog(null,"请以\"地址:端口\"的方式输入服务器地址。","输入地址",JOptionPane.INFORMATION_MESSAGE);
					try
					{
						host = addr.split(":")[0];
						port = Integer.parseInt(addr.split(":")[1]);
						new InetSocketAddress(host, port);
					}
					catch (Exception ex2)
					{
						ex2.printStackTrace();
						continue;
					}
				}
			}
		}
		label.setText("正在获取更新");
		ServerboundRequestPacket.LatestVersion latest=handler.getLatestVersion();
		if(latest.versionCode> Constants.versionCode)
		{
//			label.setVisible(false);
//			initFrame.remove(label);
//			initFrame.setSize(200,150);
			label.setText("点击下载新版本 "+latest.version);
//			update.setSize(200,75);
//			update.setBounds(0,0,200,75);
			label.addMouseListener(new MouseListener()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					Utils.ignoreExceptions(()->Desktop.getDesktop().browse(latest.url.toURI()));
					System.exit(0);
				}

				@Override
				public void mousePressed(MouseEvent e)
				{

				}

				@Override
				public void mouseReleased(MouseEvent e)
				{

				}

				@Override
				public void mouseEntered(MouseEvent e)
				{

				}

				@Override
				public void mouseExited(MouseEvent e)
				{

				}
			});
//			update.addActionListener((l)-> );
//			initFrame.add(update);
//			update.setVisible(true);
//			initFrame.repaint();
			return;
		}
		label.setText("正在初始化窗口");
		gui=new GUI(this,handler);
		if(ranIntegratedServer)
			gui.frame.setTitle("AliceInCradle Toolbox 离线模式 "+Constants.version);
		else
			gui.frame.setTitle("AliceInCradle Toolbox "+Constants.version);
		initFrame.setVisible(false);
		initFrame.dispose();
	}
	public static void main(String[] args) throws Exception
	{
		new Main(args);
	}
}

package io.github.e9ae9933.aicd.client;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Constants;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.packets.ClientboundResponcePacket;
import io.github.e9ae9933.aicd.packets.ServerboundRequestPacket;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main
{
	public static Gson gson= Policy.gson;
	Handler handler;
	GUI gui;
	public static String host=Policy.serverHost;
	public static int port=Policy.serverPort;
	public static boolean integratedServerAsIntegrated=true;
	Main(String[] args) throws Exception
	{
		OptionParser optionParser=new OptionParser();
		OptionSpec<Void> integrated=optionParser.accepts("integrated");
		OptionSet optionSet=optionParser.parse(args);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		//should we run server?
		if(optionSet.has(integrated))
		{
			AtomicBoolean initialized=new AtomicBoolean(false);
			JDialog dialog=new JDialog(){
				@Override
				public void dispose()
				{
					super.dispose();
					if(!initialized.get())
					{
						System.out.println("user stop initializing server");
						System.exit(0);
					}
//					System.out.println("normal dispose");
				}
			};
			dialog.setBounds(0,0,200,75);
			dialog.setLocationRelativeTo(null);
			JLabel label=new JLabel("正在启动内置服务端");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.CENTER);
			dialog.getContentPane().add(label);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			if(integratedServerAsIntegrated)
				io.github.e9ae9933.aicd.server.Main.main(new String[]{"--port","0","--integrated"});
			else
				io.github.e9ae9933.aicd.server.Main.main(new String[]{"--port","0"});
			host="localhost";
			port= io.github.e9ae9933.aicd.server.Main.port;
			initialized.set(true);
			dialog.setVisible(false);
			dialog.dispose();
		}
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
		label.setText("正在获取游戏路径");
		Utils.getGamePath(false);

		label.setText("正在连接服务器");
		try
		{
			handler = new Handler(host, port);
		}
		catch (Exception e)
		{
			label.setText("连接服务器失败");
			return;
		}
		label.setText("正在获取更新");
		ServerboundRequestPacket.LatestVersion latest=handler.getLatestVersion();
		if(latest.versionCode!= Constants.versionCode)
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
		initFrame.setVisible(false);
		initFrame.dispose();
	}
	public static void main(String[] args) throws Exception
	{
		new Main(args);
	}
}

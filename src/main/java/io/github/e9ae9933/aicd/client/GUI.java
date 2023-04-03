package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.UUID;

public class GUI
{
	Main main;
	Handler handler;
	JFrame frame;
	LoginGUI loginGUI;
	MainGUI mainGUI;
	UUID token;
	static Font middleFont=new Font("宋体",Font.PLAIN,16);
	static Font bigFont=new Font("宋体",Font.BOLD,32);
	static LineBorder border=new LineBorder(Color.BLACK);
	public GUI(Main main, Handler handler)
	{
		this.main = main;
		this.handler = handler;
		loginGUI=new LoginGUI(this,handler);
		mainGUI=new MainGUI(this,handler);


		frame=new JFrame();
		frame.setBounds(0,0,1280,720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setTitle("AicD");

		JTabbedPane pane=new JTabbedPane();
		pane.setFont(middleFont);
		pane.addTab("登录",loginGUI.panel);
		pane.addTab("下载",mainGUI.panel);
		pane.addTab("模组",ModGUI.create(this,handler));
		pane.addTab("设置",SettingsGUI.create());
		pane.setSelectedIndex(1);
		pane.setBounds(0,100,1280,720);

		frame.setContentPane(pane);
		frame.setVisible(true);
		System.out.println("Client GUI constructed");
	}
	void login(UUID token)
	{
		this.token=token;
	}
}

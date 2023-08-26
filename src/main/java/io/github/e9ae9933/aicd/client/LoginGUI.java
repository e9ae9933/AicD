package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.packets.ClientboundLoginPacket;
import io.github.e9ae9933.aicd.packets.ClientboundRejectPacket;
import io.github.e9ae9933.aicd.packets.Packet;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI
{
	JPanel panel;
	Handler handler;
	GUI gui;
	LoginGUI(GUI gui,Handler handler)
	{
		this.gui=gui;
		this.handler=handler;
		panel=new JPanel();
		panel.setLayout(null);
		panel.setSize(1280,720);
		JTextField username=new JTextField();
		username.setBounds(440,200,400,36);
		username.setFont(GUI.middleFont);
		panel.add(username);
		JPasswordField password=new JPasswordField();
		password.setBounds(440,250,400,36);
		password.setFont(GUI.middleFont);
		panel.add(password);
		JButton register=new JButton("注册");
		register.setBounds(440,300,100,36);
		register.setFont(GUI.middleFont);
		panel.add(register);
		JButton login=new JButton("登录");
		login.setBounds(840-100,300,100,36);
		login.setFont(GUI.middleFont);
		panel.add(login);
		JLabel field=new JLabel();
		field.setBounds(440,500,400,200);
		field.setFont(GUI.middleFont);
		panel.add(field);
		register.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Packet packet=handler.register(username.getText(),String.valueOf(password.getPassword()));
				if(packet instanceof ClientboundRejectPacket)
				{
					field.setText(((ClientboundRejectPacket) packet).text);
					System.out.println(((ClientboundRejectPacket) packet).text);
				}
				else
				{
					field.setText("注册成功");
					gui.login(((ClientboundLoginPacket)packet).token);
				}
			}
		});
		login.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Packet packet=handler.login(username.getText(),String.valueOf(password.getPassword()));
				if(packet instanceof ClientboundRejectPacket)
				{
					field.setText(((ClientboundRejectPacket) packet).text);
					System.out.println(((ClientboundRejectPacket) packet).text);
				}
				else
				{
					field.setText("登录成功");
					gui.login(((ClientboundLoginPacket)packet).token);
				}
			}
		});
	}
}

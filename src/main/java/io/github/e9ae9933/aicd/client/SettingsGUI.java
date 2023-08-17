package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class SettingsGUI
{
	public static JPanel create()
	{
		JPanel panel=new JPanel();
		panel.setBounds(0,0,1280,720);
		panel.setLayout(null);


		JLabel label=new JLabel("设置");
		label.setBounds(0,0,1280,100);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(GUI.bigFont);
		panel.add(label);
		JTextField path=new JTextField();
		path.setBounds(100,100,950,36);
		path.setFont(GUI.middleFont);
		try{path.setText(Utils.getGamePath(false).getAbsolutePath());}catch(Exception e){}
		panel.add(path);
		path.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				update();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				update();
			}

			public void update()
			{
				System.out.println(path.getText());
				File file=new File(path.getText());
				Utils.setGamePath(file);
				if(!file.isDirectory()||!file.exists())
					path.setForeground(Color.RED);
				else
					path.setForeground(null);
			}
		});

		JButton button=new JButton("浏览");
		button.setBounds(1108,100,72,36);
		button.setFont(GUI.middleFont);
		panel.add(button);
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setBounds(600,150,600,400);
		button.addActionListener(e ->
		{
			int id=fileChooser.showOpenDialog(fileChooser);
			if(id==JFileChooser.APPROVE_OPTION)
				path.setText(fileChooser.getSelectedFile().getAbsolutePath());
		});

		JLabel integrated=new JLabel();
		integrated.setBounds(100,150,400,36);
		if(io.github.e9ae9933.aicd.server.Main.integrated)
			integrated.setText("连接到 本地服务端");
		else
			integrated.setText("连接到 云端服务端");
		integrated.setFont(GUI.middleFont);
		panel.add(integrated);
		return panel;
	}
}

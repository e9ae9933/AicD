package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.Constants;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.modifier.NoelElement;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ModifierGUI
{
	public static Component debugCreate(Component parent)
	{
		JPanel panel= (JPanel) io.github.e9ae9933.aicd.modifier.Main.debugLoad().createGUI(parent);
		panel.setPreferredSize(panel.getSize());
//		panel.setBounds(0,0,panel.getWidth(),panel.getHeight());
		JScrollPane pane=new JScrollPane();
//		pane.setBounds(100,100,280,300);
		pane.setViewportView(panel);
		pane.getVerticalScrollBar().setUnitIncrement(36);
		return pane;
	}
	public static JPanel create(Main main,Component parent)
	{
		JPanel panel=new JPanel();
		panel.setBounds(0,0,1280,720);
		panel.setLayout(null);

		JLabel label=new JLabel("存档编辑");
		label.setBounds(0,0,1280,100);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(GUI.bigFont);
		panel.add(label);

		JScrollPane pane=new JScrollPane();
		pane.setBounds(100,150,1080,475);
		pane.getVerticalScrollBar().setUnitIncrement(36);
		panel.add(pane);

		JLabel path=new JLabel("未选择文件");
		path.setBounds(100,100,800,36);
		path.setFont(GUI.middleFont);
		panel.add(path);

		JButton choose=new JButton("浏览");
		choose.setBounds(908,100,72,36);
		choose.setFont(GUI.middleFont);
		panel.add(choose);

		JButton save=new JButton("保存");
		save.setBounds(1008,100,72,36);
		save.setFont(GUI.middleFont);
		panel.add(save);

		JButton cancel=new JButton("取消");
		cancel.setBounds(1108,100,72,36);
		cancel.setFont(GUI.middleFont);
		panel.add(cancel);

		File[] nowpath={null};
		NoelElement[] now = {null};
		choose.setEnabled(true);
		save.setEnabled(false);
		cancel.setEnabled(false);

		choose.addActionListener(l->{
			JFileChooser chooser=new JFileChooser(Utils.getSavePath());
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setSize(800,600);
			int rt=chooser.showOpenDialog(parent);
			if(rt==JFileChooser.APPROVE_OPTION)
			{
				try
				{
					File file = chooser.getSelectedFile();
					path.setText(file.getName());
					FileInputStream fis = new FileInputStream(file);
					byte[] data=Utils.readAllBytes(fis);
					fis.close();

					InputStream rsc=Utils.readFromResources("version18_refresh.yml",false);
					byte[] yml=Utils.readAllBytes(rsc);
					rsc.close();

					NoelElement e=io.github.e9ae9933.aicd.modifier.Main.load(new String(yml, StandardCharsets.UTF_8),data);
					nowpath[0]=file;
					now[0] =e;
					pane.setViewportView(e.createGUI(parent));

					cancel.setEnabled(true);
					save.setEnabled(true);
					choose.setEnabled(false);

				}
				catch (Exception e)
				{
					path.setText("读取失败");
					e.printStackTrace();
					StringWriter writer=new StringWriter();
					writer.write("读取失败。如果你想要寻求帮助或者自行解决，以下信息或许有用：\n");
					writer.write("如果您正在尝试寻求帮助，请您将存档文件和这些信息完整地发送给我们。\n");
					writer.write("\n");
					writer.write("version: "+ Constants.version+"\n");
					writer.write("versionCode: "+Constants.versionCode+"\n");
					writer.write("integrated: "+main.ranIntegratedServer+"\n");
					writer.write("timestamp: "+new Date().toString());
					writer.write("\n");
					PrintWriter pw=new PrintWriter(writer);
					e.printStackTrace(pw);
					pw.flush();
					writer.flush();
					StringBuffer sb= writer.getBuffer();
					String s=sb.toString();
					JTextArea area=new JTextArea();
					area.setText(s);
					area.setEditable(false);
					area.setCaretPosition(0);
					pane.setViewportView(area);
				}
			}
		});
		cancel.addActionListener(l->{
			pane.setViewportView(null);
			path.setText("已关闭文件 "+nowpath[0].getName());
			now[0]=null;
			System.gc();
			cancel.setEnabled(false);
			save.setEnabled(false);
			choose.setEnabled(true);
		});
		save.addActionListener(l->{
			try
			{
				int chs=JOptionPane.showConfirmDialog(parent,"存档编辑器正在测试，保存存档可能导致存档损坏。\n建议先做好备份工作。\n要继续吗？","警告",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if(chs!=JOptionPane.OK_OPTION)
					return;
				NoelElement e = now[0];
				File file = nowpath[0];
				NoelByteBuffer b = new NoelByteBuffer();
				e.writeTo(b);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(b.getNBytes(b.size()));
				fos.close();
//				pane.setViewportView(null);
				path.setText("已保存文件 "+file.getName()+" "+new Date().toString());
//				cancel.setEnabled(false);
//				save.setEnabled(false);
//				choose.setEnabled(true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});

		return panel;

	}
}

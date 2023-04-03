package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.ModInfo;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.packets.*;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModGUI
{
	public static JPanel create(GUI gui,Handler handler)
	{
		JPanel panel=new JPanel();
		panel.setBounds(0,0,1280,720);
		panel.setLayout(null);

		JLabel label=new JLabel("模组管理");
		label.setBounds(0,0,1280,100);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(GUI.bigFont);
		panel.add(label);


		JList<ModInfo> box=new JList<>(new Vector<>());
		box.setBounds(100,100,400,500);
		box.setFont(GUI.middleFont);
		box.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		box.setBorder(GUI.border);
		box.setFont(GUI.middleFont);
		panel.add(box);

		JButton bepinex=new JButton("安装 BepInEx");
		bepinex.setBounds(550,100,144,36);
		bepinex.setFont(GUI.middleFont);
		panel.add(bepinex);

		JButton swc=new JButton("启用/禁用模组");
		swc.setBounds(550,150,144,36);
		swc.setFont(GUI.middleFont);
		panel.add(swc);

		JButton cloud=new JButton("安装云端模组");
		cloud.setBounds(550,200,144,36);
		cloud.setFont(GUI.middleFont);
		panel.add(cloud);

		JButton refresh=new JButton("刷新");
		refresh.setBounds(550,250,144,36);
		refresh.setFont(GUI.middleFont);
		panel.add(refresh);

		JButton run=new JButton("启动游戏");
		run.setBounds(800,100,288,144);
		run.setFont(GUI.bigFont.deriveFont(Font.PLAIN));
		panel.add(run);

		JScrollPane pane=new JScrollPane();

		JTextArea area=new JTextArea(){
			@Override
			public void append(String str)
			{
				super.append(str);
//				pane.getVerticalScrollBar().setValue(pane.getVerticalScrollBar().getMaximum());
				this.setCaretPosition(this.getDocument().getLength());
			}
		};
//		area.setBounds(550,400,400,200);
//		area.setBorder(GUI.border);
		area.setEditable(false);
//		panel.add(area);

		pane.setViewportView(area);
		pane.setBounds(550,400,400,200);
//		pane.add(area);
//		pane.setAutoscrolls(true);
//		pane.getVerticalScrollBar().setAutoscrolls(true);
//		area.setAutoscrolls(true);
//		((DefaultCaret) area.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		//pane.getVerticalScrollBar().addAdjustmentListener(l-> l.getAdjustable().setValue(l.getAdjustable().getMaximum()));
		panel.add(pane);

		Supplier<Void> supplier= () ->
		{
			update(box,swc,bepinex,cloud,area,refresh);
			return null;
		};
		refresh.addActionListener(l->supplier.get());
		cloud.addActionListener(l->new Thread(()->{
			try
			{
				disableOthers(panel);
				area.append(fixed("获取模组路径"));
				File path=Utils.getModPath();
				area.append(fixed("尝试获取云端列表"));
				Packet p=handler.modInfos();
				if(p instanceof ClientboundRejectPacket)
					throw new RuntimeException(((ClientboundRejectPacket) p).text);
				List<ModInfo> infos= ((ClientboundResponcePacket<List<ModInfo>>) p).data;
				area.append(fixed("列表: "+infos.toString()));
				AtomicInteger index= new AtomicInteger(-1);
				new JDialog(gui.frame,true)
				{
					@Override
					protected void dialogInit()
					{
						super.dialogInit();
//						this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//						this.setLocationRelativeTo(panel);
						this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
						Container panel=this.getContentPane();
						panel.setLayout(null);
						panel.setBounds(0,0,800,400);
						this.setResizable(false);
						this.setBounds(0,0,800,400);
						JComboBox<String> box=new JComboBox<String>(new Vector<>(infos.stream().map(i->i.name+" "+i.version).collect(Collectors.toList())));
						box.setFont(GUI.middleFont);
						box.setBounds(150,50,500,36);
						panel.add(box);
						Runnable runnable=()->{
							index.set(box.getSelectedIndex());
							area.append(fixed("选择 "+index));
							setVisible(false);
							dispose();
						};
						JButton install=new JButton("安装");
						install.setFont(GUI.middleFont);
						install.setBounds(200,250,72,36);
						install.addActionListener(l->
						{
							runnable.run();
						});
						panel.add(install);
						this.setVisible(true);
					}
				};
//				System.gc();
				area.append(fixed("编号 "+index));
				if(index.get() ==-1)
				{
					area.append(fixed("操作取消"));
					return;
				}
				ModInfo info=infos.get(index.get());
				area.append(fixed("目标为 "+info.name+" "+info.url));
				File file= new File(path.getAbsolutePath() + "/" + info.name + ".dll");
				area.append(fixed("下载到 "+file.getAbsolutePath()));
				file.createNewFile();
				HttpsURLConnection conn= (HttpsURLConnection) info.url.openConnection();
				conn.addRequestProperty("user-agent","AicD");
				conn.connect();
				InputStream is=conn.getInputStream();
				FileOutputStream fos=new FileOutputStream(file);
				BufferedOutputStream bos=new BufferedOutputStream(fos);
				int b;
				while((b=is.read())!=-1)
					bos.write(b);
				is.close();
				bos.close();
				fos.close();
				conn.disconnect();
				area.append(fixed("下载完成"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				area.append(fixed("错误: "+e.toString()));
			}
			finally
			{
				supplier.get();
			}
		}).start());
		bepinex.addActionListener(l->new Thread(()->{
			try
			{
				disableOthers(panel);
				area.append(fixed("修复或安装 BepInEx"));
				area.append(fixed("获取下载路径……"));
				Packet packet= handler.bepInEx();
				if(!(packet instanceof ClientboundResponcePacket))
					throw new Exception("未返回链接");
				URL url=((ClientboundResponcePacket<URL>) packet).data;
				HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
				InputStream is=conn.getInputStream();
				ZipInputStream zis=new ZipInputStream(is);
				File path=Utils.getGamePath(false);
				area.append(fixed("目标路径: "+path.getAbsolutePath()));
				ZipEntry entry;
				String abs=path.getAbsolutePath();
				while((entry=zis.getNextEntry())!=null)
				{
					if(entry.isDirectory())
					{
						area.append(fixed("创建文件夹 "+abs+"/"+entry.getName()));
						new File(abs + "/" + entry.getName()).mkdirs();
					}
					else
					{
						area.append(fixed("创建文件 "+abs+"/"+entry.getName()));
						new File(abs+"/"+entry.getName()).getParentFile().mkdirs();
						FileOutputStream fos = new FileOutputStream(abs + "/" + entry.getName());
						BufferedOutputStream bos=new BufferedOutputStream(fos);
						int b;
						while((b=zis.read())!=-1)
							bos.write(b);
						bos.close();
						fos.close();
					}
				}
				zis.close();
				is.close();
				conn.disconnect();
				area.append(fixed("创建模组文件夹……"));
				new File(abs+"/BepInEx/plugins").mkdirs();
				area.append(fixed("完成，尝试刷新……"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				area.append(fixed("错误: "+e.toString()));
			}
			supplier.get();
		}).start());
		supplier.get();
		run.addActionListener(l->{
			try
			{
				File path=Utils.getGamePath(false);
				Runtime.getRuntime().exec(path.getAbsolutePath()+"/AliceInCradle.exe",new String[]{},Utils.getGamePath(false));
				run.setEnabled(false);
			}
			catch (Exception e)
			{
				area.append(fixed("错误: "+e.toString()));
			}
		});
		new Thread(()->
		{
			try
			{
				while (true)
				{
					Process p=Runtime.getRuntime().exec("tasklist /fi \"IMAGENAME eq AliceInCradle.exe\" /nh");
					while(p.isAlive())Thread.sleep(10);
					byte[] b=new byte[p.getInputStream().available()];
					p.getInputStream().read(b);
//					System.out.println(new String(b));
					if(new String(b).contains("AliceInCradle.exe"))
					{
						run.setText("正在运行");
						run.setEnabled(false);
					}
					else
					{
						run.setText("尝试启动");
						run.setEnabled(true);
					}
					Thread.sleep(1000);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}).start();
		return panel;
	}
	static synchronized void disableOthers(JPanel panel)
	{
		Arrays.stream(panel.getComponents()).filter(c->c instanceof JButton).forEach(c->c.setEnabled(false));
	}
	static synchronized String fixed(String s)
	{
		DateFormat format=new SimpleDateFormat("[HH:mm:ss.SSSS] ");
		return format.format(new Date())+s+"\n";
	}
	public synchronized static void update(JList<ModInfo> box, JButton swc, JButton bepinex, JButton cloud, JTextArea area, JButton refresh)
	{
		try
		{
			refresh.setEnabled(false);
			box.setListData(new Vector<>());
			box.setEnabled(false);
			bepinex.setEnabled(false);
			cloud.setEnabled(false);
			swc.setEnabled(false);
			area.append(fixed("尝试刷新"));


			File path = Utils.getGamePath(false);
			if (path == null || !path.exists())
				throw new IllegalArgumentException("路径错误");
			area.append(fixed("路径为 " + path.getAbsolutePath()));
			bepinex.setEnabled(true);
			if (!new File(path.getAbsolutePath() + File.separatorChar + "BepInEx" + File.separatorChar + "core").isDirectory())
			{
				bepinex.setText("安装 BepInEx");
				return;
			} else
				bepinex.setText("修复 BepInEx");
			File pluginsDir = new File(path.getAbsolutePath() + File.separatorChar + "BepInEx" + File.separatorChar + "plugins");
			if (!pluginsDir.exists() || !pluginsDir.isDirectory())
				throw new IllegalArgumentException(fixed("未找到文件夹 " + pluginsDir.getAbsolutePath()));
			File[] files=pluginsDir.listFiles();
			area.append(fixed("发现 "+files.length+" 个文件"));
			box.setListData(new Vector<>(Arrays.stream(files).filter(File::isFile).map(mod -> new ModInfo(mod.getName(), mod)).collect(Collectors.toList())));
			box.setEnabled(true);
			cloud.setEnabled(true);
			swc.setEnabled(true);
			area.append(fixed("刷新完成"));
		}
		catch (Exception e)
		{
			area.append(fixed("错误: "+e.toString()));
			e.printStackTrace();
		}
		refresh.setEnabled(true);
	}
}


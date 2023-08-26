package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.VersionInfo;
import io.github.e9ae9933.aicd.packets.ClientboundVersionListPacket;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainGUI
{
	GUI gui;
	Handler handler;
	JPanel panel;
	public MainGUI(GUI gui, Handler handler)
	{
		this.gui = gui;
		this.handler = handler;
		panel=new JPanel();
		panel.setSize(1280,720);
		panel.setLayout(null);


		JLabel label=new JLabel("下载管理");
		label.setBounds(0,0,1280,100);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(GUI.bigFont);
		panel.add(label);

		List<VersionInfo> versionList=((ClientboundVersionListPacket)handler.versionList()).versionList;
		JList<VersionInfo> box=new JList<VersionInfo>(new Vector<>(versionList));
		box.setBounds(100,100,400,500);
		box.setFont(GUI.middleFont);
		box.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		box.setBorder(GUI.border);
		panel.add(box);

		JCheckBox debug=new JCheckBox("保留 _debug.txt");
		debug.setSelected(true);
		debug.setBounds(550,100,300,32);
		debug.setFont(GUI.middleFont);
		panel.add(debug);

		JCheckBox download=new JCheckBox("重新下载");
		download.setSelected(true);
		download.setBounds(550,150,300,32);
		download.setFont(GUI.middleFont);
		panel.add(download);

//		panel.add(fileChooser);
//		fileChooser.showOpenDialog(fileChooser);


		JButton run=new JButton("运行");
		run.setFont(GUI.middleFont);
		run.setBounds(1108,250,72,36);
		panel.add(run);

		JLabel info=new JLabel("请先在左边选择要安装的版本");
		info.setFont(GUI.middleFont);
		info.setBounds(700,250,400,36);
		panel.add(info);

		JProgressBar total=new JProgressBar();
		total.setMinimum(0);
		total.setMaximum(3);
		total.setValue(0);
		total.setBounds(550,350,630,36);
		total.setStringPainted(true);
		total.setString("总进度");
		total.setFont(GUI.middleFont);
		panel.add(total);

		JProgressBar single=new JProgressBar();
		single.setMinimum(0);
		single.setMaximum(100);
		single.setValue(0);
		single.setBounds(550,400,630,36);
		single.setStringPainted(true);
		single.setString("阶段进度");
		single.setFont(GUI.middleFont);
		panel.add(single);


		run.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					total.setString("准备下载");
					run.setEnabled(false);
					Thread thread=new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								VersionInfo info = box.getSelectedValue();
								if(info==null)
									throw new NullPointerException("未选择版本");
								String name = info.name;
								URL url = info.url;
								String zipFileName = Utils.getGamePath(false).getAbsolutePath() + File.separatorChar + name;
								if(download.isSelected())
								{
									total.setString("正在下载文件 (第 1 项，共 3 项)");
									HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
									con.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");

									con.connect();
									System.out.println("length " + con.getContentLength());
									int length = con.getContentLength();
									InputStream is = con.getInputStream();
									System.out.println("available " + is.available());
									FileOutputStream fos = new FileOutputStream(zipFileName);
//									BufferedOutputStream bos = new BufferedOutputStream(fos, 16 * 1024 * 1024);
									byte[] b = new byte[1024 * 1024];
									int len;
									int downloaded = 0;
									single.setMaximum(length);
									Queue<Map.Entry<Long, Integer>> q = new LinkedList<>();
									q.add(new AbstractMap.SimpleEntry<>(System.currentTimeMillis(), 0));
									while ((len = is.read(b)) != -1)
									{
										fos.write(b, 0, len);
										downloaded += len;
										//System.out.println("downloaded " + len);
										long now = System.currentTimeMillis();
										while (q.size() > 1 && q.peek().getKey() <= now - 1000)
											q.poll();
										q.add(new AbstractMap.SimpleEntry<>(now, downloaded));
										double time = (now - q.peek().getKey()) / 1000.0;
										int sub = downloaded - q.peek().getValue();
										String s = String.format("%.3f/%.3f MB    %.3fMB/s", downloaded / 1048576.0, length / 1048576.0, (double) sub / time / 1048576.0);
										single.setString(s);
										single.setValue(downloaded);
									}
//									bos.close();
									fos.close();
									is.close();
									con.disconnect();
								}
								total.setString("正在打开并分析压缩文件 (第 2 项，共 3 项)");
								total.setValue(1);
								ZipFile zipFile=new ZipFile(zipFileName);
								Enumeration<? extends ZipEntry> ze=zipFile.entries();
								List<ZipEntry> entries=new ArrayList<>();
								while(ze.hasMoreElements())
									entries.add(ze.nextElement());

								String located=entries.stream().filter(s->s.getName().toLowerCase().endsWith("aliceincradle.exe")).limit(1).map(
										s->
										{
											String loc=s.getName();
											return loc.substring(0,loc.lastIndexOf('/')+1);
										}
								).findAny().orElse(null);
								if(located==null)
									throw new IllegalArgumentException("Failed to locate aic.");
								System.out.println("Located "+located);

								total.setString("正在解压文件 (第 3 项，共 3 项)");
								total.setValue(2);

								single.setMaximum(entries.size());
								int extr=0;

								for(ZipEntry entry:entries)
								{
									String entryName=entry.getName();
									single.setString(String.format("分析 %s (第 %d 项，共 %d 项)", entryName.substring(entryName.lastIndexOf('/')+1),extr++,entries.size()));
									single.setValue(extr);
									if(debug.isSelected()&&entryName.endsWith("/_debug.txt"))
									{
										System.out.println("skipped _debug.txt");
										continue;
									}
									if(!entryName.startsWith(located))
										continue;
									String fileName=Utils.getGamePath(false).getAbsolutePath()+File.separatorChar+entryName.substring(located.length());
									if(entry.isDirectory())
									{
										new File(fileName).mkdirs();
										continue;
									}
									InputStream is=zipFile.getInputStream(entry);
									FileOutputStream fos=new FileOutputStream(fileName);
									//System.out.println(String.format("%s to %s", entryName,fileName));
									byte[] b=new byte[1024];
									int len;
									while((len=is.read(b))!=-1)
									{
										fos.write(b,0,len);
									}
									is.close();
									fos.close();
								}

								/*
								ZipInputStream zip=new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFileName)));
								ZipEntry entry=null;
								while((entry=zip.getNextEntry())!=null)
								{
									System.out.println(entry.getName()+" "+entry.isDirectory()+" "+entry.getCrc());
								}
								zip.close();*/

								total.setString("已完成");
								total.setValue(3);
								single.setString("已完成");
								single.setValue(entries.size());
							}catch (Exception ex)
							{
								ex.printStackTrace();
								total.setString("运行失败: "+ex.toString());
							}
							finally
							{
								run.setEnabled(true);
							}

						}
					});
					thread.start();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
	}
}

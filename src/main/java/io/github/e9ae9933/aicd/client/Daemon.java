package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class Daemon
{
	public static JFrame daemon;

	public static Object lock=new Object();
	public static JFrame createDaemon()
	{
		synchronized (lock)
		{
			if(daemon!=null)
			{
				System.err.println("daemon not null...");
				return null;
			}
			daemon = new JFrame();
		}
		daemon.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		daemon.setLocationRelativeTo(null);
		daemon.setTitle("AliceInCradle Toolbox Daemon");
		daemon.setResizable(false);
		try{
			InputStream is= Utils.readFromResources("icon.png",false);
			BufferedImage bi= ImageIO.read(is);
			is.close();
			daemon.setIconImage(bi);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		daemon.setContentPane(panel);
		JLabel label=new JLabel("守护窗口 / Daemon frame",null,SwingConstants.CENTER);
//		label.setBorder(new LineBorder(Color.BLACK,1));
		panel.add(label);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		JTextArea area=new JTextArea();
		area.setEditable(false);
		area.setText("" +
				"" +
				"这是工具箱的“守护窗口”。\n" +
				"当该窗口被关闭，工具箱也将被一起关闭。\n" +
				"\n" +
				"This is the \"Daemon frame\".\n" +
				"The toolbox will be terminated as long as the frame is closed.");
		area.setBorder(new LineBorder(Color.BLACK,1));
		panel.add(area);

		Monitor monitor=new Monitor();
		monitor.setPreferredSize(new Dimension(320,80));
		monitor.setBorder(new LineBorder(Color.BLACK,1));
		panel.add(monitor);
		daemon.pack();
		daemon.setLocationRelativeTo(null);
		daemon.setVisible(true);
//		daemon.pack();
		daemon.toFront();
		return daemon;
	}
}
class Monitor extends JPanel
{
	class Slice
	{
		long time;
		long mem;

		public Slice(long time, long mem)
		{
			this.time = time;
			this.mem = mem;
		}
	}
	Deque<Slice> slices=new LinkedList<>();
	long leave=20*1000;
	Thread t;
	boolean overload=false;
	Monitor()
	{
//		slices.add(new Slice(System.currentTimeMillis()-1,0));
		t=new Thread(()->runLoop());
		t.start();
	}
	void runLoop()
	{
		long nextUpdate=0;
		while(true)
		{
			newSlice();
			if(System.currentTimeMillis()>=nextUpdate)
			{
				this.repaint();
				nextUpdate=System.currentTimeMillis()+40;
			}
			try
			{
				Thread.sleep(1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	}
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Rectangle r=g.getClipBounds();
		int w=getWidth();
		int h=getHeight();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
		long[] draws=new long[w];
		Arrays.fill(draws,-1);
		long now=System.currentTimeMillis();
		long low=now-leave;
		int slc;
		synchronized (slices)
		{
			slc=slices.size();
			slices.forEach(s ->
			{
				int pos = (int) ((s.time - low) * w / leave);
				if (pos < 0) return;
				draws[pos >= w ? w - 1 : pos] = s.mem;
			});
		}
		long nowmem=draws[w-1]=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		for(int i=1;i<w;i++)
			if(draws[i]==0)
				draws[i]=draws[i-1]>=0?-draws[i-1]:draws[i-1];
		long max=Runtime.getRuntime().maxMemory();
		long tot=Runtime.getRuntime().totalMemory();
		for(int i=0;i<w;i++)
		{
			g.setColor(draws[i]>=0?Color.GREEN:Color.YELLOW);
			long abs=Math.abs(draws[i]);
			double ratio=(double)abs/max;
			if(ratio>0.75)
				g.setColor(Color.YELLOW);
			if(ratio>0.9)
				g.setColor(Color.RED);
			if(ratio>0.98)
				g.setColor(new Color(191,0,191));
			if(ratio>0.995)
				overload=true;
			int height= (int) (ratio*h);
			g.fillRect(i,h-height,1,height);
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, (int) ((max-tot)*h/max),w,1);
		g.setColor(new Color(0x1f000000,true));
		for(int i=1;i<4;i++)
			g.fillRect(0,h*i/4,w,1);
		g.setColor(Color.BLACK);
//		g.drawString(String.format("%d / %d MB", nowmem>>20,max>>20),10,20);
//		g.drawString(String.format("%d slices", slc),10,40);
		g.drawString(String.format("Used: %d MiB / Allocated: %d MiB / Max: %d MiB", nowmem>>20,Runtime.getRuntime().totalMemory()>>20,max>>20),10,20);
		g.drawString(String.format("With %d slices of data", slc),10,40);
		g.drawString(String.format("All %d ms", leave),10,60);
		if(overload)
		{
			g.setColor(Color.RED);
			g.drawString("Overload detected!",10,80);
		}
		//System.out.println("repaint");
	}

	void newSlice()
	{
		long now=System.currentTimeMillis();
		synchronized (slices)
		{
			slices.addLast(new Slice(now, Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory()));
			while (!slices.isEmpty() && slices.getFirst().time + leave <= now)
				slices.poll();
		}
	}
}
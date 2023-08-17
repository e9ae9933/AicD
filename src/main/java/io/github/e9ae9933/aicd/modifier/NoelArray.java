package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class NoelArray extends NoelElement
{
	long len;
	int lenSize;
	List<NoelElement> data;
	transient Object valueType;
	transient Map<String,Class<? extends NoelElement>> primitives;
	Map<String,NoelElement> variables;
	protected NoelArray(){}
	public NoelArray(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new IllegalArgumentException("Array must have settings");
		len=Long.parseLong(settings.getOrDefault("len",-1).toString());
		long length=len;
		if(len==-1)
		{
			Object lensz=settings.get("lensize");
			if(lensz==null)
				throw new RuntimeException("null lensize");
			lenSize=Integer.parseInt(lensz.toString());
			length=0;
			for(int i=0;i<lenSize;i++)
				length=length*256+(b.getByte()&0xFF);
		}

		Object o=settings.get("value");
		valueType=o;
		this.primitives=primitives;
		this.variables=variables;
		data =new ArrayList<>();
		for(int i=0;i<length;i++)
			data.add(NoelElement.newInstance(o,b,primitives,variables));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		if(len==-1)
		{
			//write length
			int length= data.size();
			for(int i=lenSize-1;i>=0;i--)
				b.putByte((byte) ((length>>(i*8))&0xFF));
		}
		for(NoelElement e: data)
			e.writeTo(b);
	}
	private transient Map<NoelElement,Component> guiCache=new HashMap<>();
	private transient List<JButton> addButtonCache=new ArrayList<>();
	@Override
	public Component createGUI(Component parent)
	{
		JButton button=new JButton();
		button.setSize(200,36);
		button.setFont(middleFont);
		JPanel panel=new JPanel();
//		panel.setBounds(0,0,1280,720);
//		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.setLayout(null);
		JScrollPane pane=new JScrollPane(panel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		JFrame frame=new JFrame();
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.setResizable(false);
		frame.setContentPane(pane);
//		frame.setSize(1280,720);
		Consumer<Boolean> runnable= new Consumer<Boolean>()
		{
			@Override
			public void accept(Boolean first)
			{
				panel.removeAll();
				LinkedList<Pair<NoelElement, Component>> list = new LinkedList<>();
				//cache?
				Map<NoelElement, Component> newCache = new HashMap<>();
				data.forEach(d ->
				{
					Component c;
					if (guiCache.containsKey(d))
						c = guiCache.get(d);
					else
						c = d.createGUI(frame);
					newCache.put(d, c);
					list.add(new Pair<>(d, c));
				});
				guiCache = newCache;
				button.setText(String.format("数组长度 %d", list.size()));
				int w = list.stream().mapToInt(p -> p.second.getWidth()).max().orElse(0) + 72;
				int h = list.stream().mapToInt(p -> p.second.getHeight()).sum() + 36 * (list.size() + 1);
				if (w < 200)
					w = 200;
				frame.setSize(Math.min(1280, w + 50), Math.min(720, h + 50));
//				frame.setLocationRelativeTo(parent);
				if(first)
					frame.setLocationRelativeTo(parent);
				frame.setTitle("数组长度 "+list.size());
				panel.setSize(w, h);
				panel.setPreferredSize(panel.getSize());
				pane.setViewportView(panel);
				pane.getVerticalScrollBar().setUnitIncrement(16);
				Iterator<Pair<NoelElement, Component>> it = list.iterator();
				int w0 = 0;
				while (addButtonCache.size() < list.size()+1)
				{
					int n = addButtonCache.size();
					JButton add = new JButton(String.format("在第 %d 项之前添加", n));
					add.setSize(200, 36);
					add.setFont(middleFont);
					add.addActionListener(l ->
					{
						NoelElement toBeAdd = NoelElement.newInstance(valueType, new NoelZeroBuffer(), primitives, variables);
						data.add(n, toBeAdd);
						accept(false);
					});
					addButtonCache.add(add);
				}
				for (int i = 0; it.hasNext(); i++)
				{
					Pair<NoelElement, Component> p = it.next();
					NoelElement e = p.first;
					Component c = p.second;
					JLabel label = new JLabel("" + i);
					label.setHorizontalAlignment(SwingConstants.CENTER);
					label.setFont(middleFont);
					label.setBounds(0, w0 + 36, 72, c.getHeight());
					c.setLocation(72, w0 + 36);
					JButton add = addButtonCache.get(i);
					add.setLocation(0, w0);
					panel.add(add);
					panel.add(label);
					panel.add(c);
					w0 += c.getHeight() + 36;
				}
				JButton add = addButtonCache.get(list.size());
				add.setLocation(0, w0);
				panel.add(add);
			}
		};
		runnable.accept(true);
		button.addActionListener(l->frame.setVisible(true));
		return button;
	}
}

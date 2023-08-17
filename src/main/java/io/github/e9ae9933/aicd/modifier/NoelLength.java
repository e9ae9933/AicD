package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class NoelLength extends NoelElement
{
	private long data;
	private int size;
	public NoelLength(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new RuntimeException("NoelLength must have settings");
		size=Integer.parseInt(settings.get("size").toString());
		data=0;
		for(int i=0;i<size;i++)
			data=data*256+b.getByte()&0xFF;
	}
	void updateComponent()
	{
		if(component!=null)
			component.setText("数组长度标记 "+data);
	}
	public void set(long data)
	{
		if(data<0||data>=(1L<<(8*size)))
			throw new IllegalArgumentException(data+" is not in size "+size);
		this.data=data;
		updateComponent();
	}
	public long get()
	{
		return data;
	}
	public void increase()
	{
		set(get()+1);
	}
	public void decrease()
	{
		set(get()-1);
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		for(int i=size-1;i>=0;i--)
			b.putByte((byte)(0xFF&(data>>>(i*8))));
	}
	transient JLabel component=null;
	@Override
	public Component createGUI(Component parent)
	{
//		JLabel label=new JLabel("动态数组长度标记，请直接修改原数组");
		if(component!=null)
			throw new RuntimeException("duplicated component");
		JLabel label=new JLabel();
		component=label;
		label.setFont(middleFont);
		label.setSize(300,36);
		updateComponent();
		return label;
	}
}

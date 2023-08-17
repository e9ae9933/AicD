package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class NoelSegmentShifted extends NoelElement
{
	NoelElement data;
	byte shift;
	byte[] left;
	public NoelSegmentShifted(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new IllegalArgumentException("Segment must have settings");
		int len=b.getInt();
		shift=b.getByte();
		Object o=settings.get("value");
		NoelByteBuffer buf=new NoelByteBuffer(b.getNBytes(len));
		buf.addShift(shift);
		data=NoelElement.newInstance(o,buf,primitives,variables);
		if(buf.size()!=0)
			System.err.println("Warning: left "+buf.size()+" bytes");
		left=buf.getNBytes(buf.size());
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		NoelByteBuffer buf=new NoelByteBuffer();
		data.writeTo(buf);
		buf.putBytes(left);
		buf.addShift((byte) -shift);

		b.putInt(buf.size());
		b.putByte(shift);

		b.putBytes(buf.getNBytes(buf.size()));
	}
	@Override
	public Component createGUI(Component parent)
	{
		JPanel panel=new JPanel();
		panel.setLayout(null);
		JLabel label=new JLabel(String.format("偏移: %d / 0x%02X", shift&0xff,shift&0xff));
		label.setFont(middleFont);
		label.setBounds(1,1,200,36);
		JLabel label1=new JLabel(left.length!=0? String.format("尾部有 %d 字节未知数据 (存档格式表版本对得上吗?)", left.length):"尾部没有未知数据");
		label1.setFont(left.length!=0?middleFont.deriveFont(Font.BOLD):middleFont);
		Component c=data.createGUI(parent);
		panel.setSize(Math.max(c.getWidth(),500)+2,c.getHeight()+36+36+2);
		c.setLocation(1,36+1);
		label1.setBounds(1,c.getHeight()+36+1,500,36);
		label1.setForeground(left.length!=0?Color.RED:null);
		panel.add(label1);
		panel.add(label);
		panel.add(c);
		panel.setBorder(border);
		panel.setPreferredSize(panel.getSize());
		return panel;
	}
}

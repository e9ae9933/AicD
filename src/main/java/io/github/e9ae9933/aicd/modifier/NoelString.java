package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NoelString extends NoelElement
{
	int len;
	String data;
	public NoelString(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		if(settings!=null)
			len=Integer.parseInt(settings.getOrDefault("len",-1).toString());
		else
			len=-1;
		int length=len;
		if(length==-1)
			length=Short.toUnsignedInt(b.getShort());
		byte[] bytes=new byte[length];
		b.getBytes(bytes);
		data =new String(bytes, StandardCharsets.UTF_8);
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		byte[] bytes= data.getBytes(StandardCharsets.UTF_8);
		if(len==-1)
			b.putShort((short) bytes.length);
		b.putBytes(bytes);
	}
	@Override
	public String toString()
	{
		return data;
	}

	@Override
	public Component createGUI(Component parent)
	{
		JTextField field=new JTextField(data);
		field.setFont(middleFont);
		Runnable update=()->
		{
			field.setSize(field.getFontMetrics(middleFont).stringWidth(field.getText())+100,36);
		};
		update.run();
		field.getDocument().addDocumentListener(new DocumentListener()
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
			void update()
			{
				String s=field.getText();
				if(len==-1&&s.getBytes(StandardCharsets.UTF_8).length<65536||len==s.getBytes(StandardCharsets.UTF_8).length)
				{
					data=s;
					update.run();
					brilliant(field);
				}
				else
					blunder(field);
			}
		});
		return field;
	}
}

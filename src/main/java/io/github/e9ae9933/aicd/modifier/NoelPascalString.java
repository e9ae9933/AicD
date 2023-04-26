package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class NoelPascalString extends NoelElement
{
	String data;
	public NoelPascalString(NoelByteBuffer b)
	{
		int len=b.getByte()&0xFF;
		byte[] bytes=new byte[len];
		b.getBytes(bytes);
		data =new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		byte[] bytes= data.getBytes(StandardCharsets.UTF_8);
		b.putByte((byte) bytes.length);
		b.putBytes(bytes);
	}

	@Override
	public Component createGUI()
	{
		JTextField field=new JTextField(data);
		field.setFont(middleFont);
		Runnable update=()->
		{
			field.setSize(field.getFontMetrics(middleFont).stringWidth(field.getText())+50,36);
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
				if(s.getBytes(StandardCharsets.UTF_8).length<256)
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

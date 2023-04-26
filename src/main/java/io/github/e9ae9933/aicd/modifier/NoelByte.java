package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelByte extends NoelLongable
{
	byte data;
	public NoelByte(NoelByteBuffer b)
	{
		data=b.getByte();
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putByte(data);
	}

	@Override
	public Component createGUI()
	{
		JTextField field=new JTextField(Byte.toString(data));
		field.setSize(maxLength(4),36);
		field.setFont(middleFont);
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
				try
				{
					int result=Integer.parseInt(s);
					if(result<-128||result>127)
						throw new NumberFormatException();
					data= ((byte) result);
					brilliant(field);
				}
				catch (Exception e)
				{
					blunder(field);
				}
			}
		});
		return field;
	}

	@Override
	public long getLong()
	{
		return data;
	}

	@Override
	public void setLong(long l)
	{
		if(l<Byte.MIN_VALUE||l>Byte.MAX_VALUE)
			throw new IllegalArgumentException(l+" is not a byte");
		data= (byte) l;
	}
}

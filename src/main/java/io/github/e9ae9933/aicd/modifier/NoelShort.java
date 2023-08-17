package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelShort extends NoelLongable
{
	short data;
	public NoelShort(NoelByteBuffer b)
	{
		data=b.getShort();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putShort(data);
	}

	@Override
	public long getLong()
	{
		return data;
	}

	@Override
	public void setLong(long l)
	{
		if(l<Short.MIN_VALUE||l>Short.MAX_VALUE)
			throw new IllegalArgumentException(l+" is not a short");
		data= (short) l;
	}

	@Override
	public Component createGUI(Component parent)
	{
		JTextField field=new JTextField(Short.toString(data));
		field.setSize(maxLength(6),36);
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
					if(result<-32768||result>32767)
						throw new NumberFormatException();
					data= ((short) result);
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
}

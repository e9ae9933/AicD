package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelInt extends NoelLongable
{
	int data;
	public NoelInt(NoelByteBuffer b)
	{
		data=b.getInt();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putInt(data);
	}

	@Override
	public long getLong()
	{
		return data;
	}

	@Override
	public void setLong(long l)
	{
		if(l<Integer.MIN_VALUE||l>Integer.MAX_VALUE)
			throw new IllegalArgumentException(l+" is not an int");
		data= (int) l;
	}

	@Override
	public Component createGUI()
	{
		JTextField field=new JTextField(Integer.toString(data));
		field.setSize(maxLength(11),36);
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
					data=Integer.parseInt(s);
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

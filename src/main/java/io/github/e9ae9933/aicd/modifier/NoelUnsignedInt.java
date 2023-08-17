package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelUnsignedInt extends NoelInt
{
	public NoelUnsignedInt(NoelByteBuffer b)
	{
		super(b);
	}

	@Override
	public long getLong()
	{
		return Integer.toUnsignedLong(data);
	}

	@Override
	public void setLong(long l)
	{
		if(l<0||l>=1L<<Integer.SIZE)
			throw new IllegalArgumentException(l+" is not an unsigned integer");
		data= (int) l;
	}

	@Override
	public Component createGUI(Component parent)
	{
		JTextField field=new JTextField(Integer.toUnsignedString(data));
		field.setSize(maxLength(10),36);
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
					data=Integer.parseUnsignedInt(s);
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

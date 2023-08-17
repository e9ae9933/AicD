package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelLong extends NoelElement
{
	long data;
	public NoelLong(NoelByteBuffer b)
	{
		data=b.getLong();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putLong(data);
	}

	@Override
	public Component createGUI(Component parent)
	{
		JTextField field=new JTextField(Long.toString(data));
		field.setSize(maxLength(20),36);
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
					data=Long.parseLong(s);
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

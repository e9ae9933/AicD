package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelDouble extends NoelElement
{
	double data;
	public NoelDouble(NoelByteBuffer b)
	{
		data=b.getDouble();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putDouble(data);
	}

	@Override
	public Component createGUI(Component parent)
	{
		JTextField field=new JTextField(Double.toString(data));
		field.setSize(maxLength(25),36);
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
					data=Double.parseDouble(s);
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

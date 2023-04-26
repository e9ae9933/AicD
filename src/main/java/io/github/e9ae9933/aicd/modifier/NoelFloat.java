package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelFloat extends NoelElement
{
	float data;
	public NoelFloat(NoelByteBuffer b)
	{
		data=b.getFloat();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putFloat(data);
	}

	@Override
	public Component createGUI()
	{
		JTextField field=new JTextField(Float.toString(data));
		field.setSize(maxLength(15),36);
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
					data=Float.parseFloat(s);
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

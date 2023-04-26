package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelUnsignedLong extends NoelLong
{
	public NoelUnsignedLong(NoelByteBuffer b)
	{
		super(b);
	}
	@Override
	public Component createGUI()
	{
		JTextField field=new JTextField(Long.toUnsignedString(data));
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
					data=Long.parseUnsignedLong(s);
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

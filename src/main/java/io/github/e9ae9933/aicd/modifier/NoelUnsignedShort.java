package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelUnsignedShort extends NoelShort
{
	public NoelUnsignedShort(NoelByteBuffer b)
	{
		super(b);
	}

	@Override
	public long getLong()
	{
		return Short.toUnsignedLong(data);
	}

	@Override
	public void setLong(long l)
	{
		if(l<0||l>=1L<<Short.SIZE)
			throw new IllegalArgumentException(l+" is not an unsigned short");
		data= (short) l;
	}

	@Override
	public Component createGUI(Component parent)
	{
		JTextField field=new JTextField(Short.toUnsignedInt(data)+"");
		field.setSize(maxLength(5),36);
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
					if(result<0||result>65535)
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

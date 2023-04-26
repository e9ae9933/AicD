package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelUnsignedByte extends NoelByte
{
	public NoelUnsignedByte(NoelByteBuffer b)
	{
		super(b);
	}

	@Override
	public long getLong()
	{
		return Byte.toUnsignedLong(data);
	}

	@Override
	public void setLong(long l)
	{
		if(l<0||l>=1L<<Byte.SIZE)
			throw new IllegalArgumentException(l+" is not an unsigned byte");
		data= (byte) l;
	}
	@Override
	public Component createGUI()
	{
		JTextField field=new JTextField(Byte.toUnsignedInt(data)+"");
		field.setSize(maxLength(3),36);
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
					if(result<0||result>255)
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
}

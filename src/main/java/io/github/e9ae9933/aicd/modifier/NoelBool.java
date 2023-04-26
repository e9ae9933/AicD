package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class NoelBool extends NoelByte
{
	public NoelBool(NoelByteBuffer b)
	{
		super(b);
	}
	@Override
	public Component createGUI()
	{
		JButton button=new JButton();
		button.setSize(72,36);
		button.setFont(middleFont);
		Runnable update=()->button.setText(data!=0?"是":"否");
		button.addActionListener(l->{
			if(data!=0)
				data=0;
			else
				data=1;
			update.run();
		});
		update.run();
		return button;
	}
}

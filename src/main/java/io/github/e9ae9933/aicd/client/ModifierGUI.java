package io.github.e9ae9933.aicd.client;

import javax.swing.*;
import java.awt.*;

public class ModifierGUI
{
	public static Component create()
	{
		JPanel panel= (JPanel) io.github.e9ae9933.aicd.modifier.Main.debugLoad().createGUI();
		panel.setPreferredSize(panel.getSize());
//		panel.setBounds(0,0,panel.getWidth(),panel.getHeight());
		JScrollPane pane=new JScrollPane();
//		pane.setBounds(100,100,280,300);
		pane.setViewportView(panel);
		pane.getVerticalScrollBar().setUnitIncrement(36);
		return pane;
	}
}

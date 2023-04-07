package io.github.e9ae9933.aicd.client;

import io.github.e9ae9933.aicd.packets.ClientboundRejectPacket;
import io.github.e9ae9933.aicd.packets.ClientboundResponcePacket;
import io.github.e9ae9933.aicd.packets.Packet;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class AboutGUI
{
	public static JPanel create(Handler handler)
	{
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBounds(0,0,1280,720);
		JEditorPane pane=new JEditorPane();
		pane.setEditable(false);
		pane.setContentType("text/html; charset=utf-8");
		Packet packet=handler.aboutPage();
		if(packet instanceof ClientboundRejectPacket)
		{
			pane.setText("错误: "+((ClientboundRejectPacket) packet).text);
		}
		else
		{
			URL url= ((ClientboundResponcePacket<URL>) packet).data;
			System.out.println("获取到关于 URL: "+url.getPath());
			try
			{
				pane.setPage(url);
			}
			catch (Exception e)
			{
				StringWriter sw=new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				pane.setText(sw.toString());
			}
		}
		pane.addHyperlinkListener(l->{
			if(l.getEventType()== HyperlinkEvent.EventType.ACTIVATED)
				try
				{
					URL url = l.getURL();
					Desktop.getDesktop().browse(url.toURI());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
		});
		JScrollPane scrollPane=new JScrollPane(pane);
		panel.add(scrollPane);
		return panel;
	}
}

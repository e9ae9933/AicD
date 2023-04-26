package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoelObject extends NoelElement
{
	Map<String,NoelElement> data;
	public NoelObject(Map<String,NoelElement> data)
	{
		this.data=data;
	}
	public NoelObject(NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new IllegalArgumentException("osn");
		data=new LinkedHashMap<>();
		Map<String,Object> values= (Map<String, Object>) settings.get("types");
		Map<String,NoelElement> vars=new LinkedHashMap<>(variables);
		for(Map.Entry<String,Object> entry:values.entrySet())
		{
			String name=entry.getKey();
			Object o=entry.getValue();
			NoelElement e=NoelElement.newInstance(o,b,primitives,vars);
			data.put(name,e);
			vars.put(name,e);
		}
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		for(NoelElement e:data.values())
			e.writeTo(b);
	}

	@Override
	public Component createGUI()
	{
		List<Pair<String, Component>> str=data.entrySet().stream().map(s-> new Pair<>(s.getKey(), s.getValue().createGUI())).collect(Collectors.toList());
		int w=str.stream().mapToInt(c->c.second.getWidth()).max().orElse(0);
		int h=str.stream().mapToInt(c->c.second.getHeight()).sum();
		JPanel panel=new JPanel();
		FontMetrics metrics=panel.getFontMetrics(middleFont);
		int wfont=str.stream().mapToInt(c->metrics.stringWidth(c.first)).max().orElse(0);
		panel.setLayout(null);
		panel.setSize(wfont+w,h);
		final int[] hs = {0};
		str.forEach(c->{
			JLabel label=new JLabel(c.first);
			label.setBounds(0,hs[0],wfont,c.second.getHeight());
			label.setFont(middleFont);
			panel.add(label);
			c.second.setLocation(wfont, hs[0]);
			hs[0] +=c.second.getHeight();
			panel.add(c.second);
		});
		return panel;
	}
}

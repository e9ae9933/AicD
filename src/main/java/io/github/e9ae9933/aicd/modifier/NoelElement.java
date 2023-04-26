package io.github.e9ae9933.aicd.modifier;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.Map;

public abstract class NoelElement
{
	//public String objectType=getClass().getCanonicalName();
	protected static Font middleFont=new Font("宋体",Font.PLAIN,16);
	protected static LineBorder border=new LineBorder(Color.BLACK);
	public abstract void writeTo(NoelByteBuffer b);
	public static NoelElement newInstance(Object o,NoelByteBuffer b,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		if(o instanceof String)
			return newInstance(primitives.get(o.toString()),b,null,primitives,variables);
		else if(o instanceof Map)
		{
			Map<String,Object> map= (Map<String, Object>) o;
			String type=map.get("type").toString();
			return newInstance(primitives.get(type),b,map,primitives,variables);
		}
		throw new RuntimeException("a type must be a string or a map");
	}
	public static NoelElement newInstance(Class<? extends NoelElement> clazz,NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> knownTypes,Map<String,NoelElement> variables)
	{
		Constructor<? extends NoelElement> cons;
		try
		{
			cons=clazz.getDeclaredConstructor(NoelByteBuffer.class,Map.class,Map.class,Map.class);
			if(cons==null)
				throw new NoSuchMethodException();
			return cons.newInstance(b,settings,knownTypes,variables);
		}
		catch (NoSuchMethodException ignored)
		{
			try
			{
				cons = clazz.getDeclaredConstructor(NoelByteBuffer.class);
				return cons.newInstance(b);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		catch (Exception e)
		{
//			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}
//	public abstract Component createGUI();
	public Component createGUI(){return unsupportedGUI();}
	protected Component unsupportedGUI()
	{
		JLabel label=new JLabel(getClass().getSimpleName()+" 的编辑未被实装");
		label.setFont(middleFont);
		label.setSize(300,36);
		return label;
	}
	protected void blunder(Component component)
	{
		component.setForeground(Color.RED);
	}
	protected void brilliant(Component component)
	{
		component.setForeground(null);
	}
	protected int maxLength(int len)
	{
		return 8*len+8;
	}
}

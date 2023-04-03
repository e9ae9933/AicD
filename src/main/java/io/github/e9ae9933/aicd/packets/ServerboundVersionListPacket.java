package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.VersionInfo;
import io.github.e9ae9933.aicd.server.Main;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServerboundVersionListPacket extends Packet
{
	static List<VersionInfo> versionInfo;
	static long nextUpdate=0;
	static URL url;

	static
	{
		try
		{
			url = new URL("https://mua.sibaken.org/CN02/Public/AliceInCradle/Download");
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public static synchronized void update()
	{
		if(System.currentTimeMillis()>=nextUpdate)
		{
			nextUpdate=System.currentTimeMillis()+1000*60;
			try
			{
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.addRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
				con.connect();
				InputStream is=con.getInputStream();
				ByteArrayOutputStream bos=new ByteArrayOutputStream();
				int d;
				while((d=is.read())!=-1)
					bos.write(d);
				String s= bos.toString();
				//System.out.println(s);
				int i=0;
				List<VersionInfo> next=new ArrayList<>();
				while((i=s.indexOf("data-path=",i))!=-1)
				{
					i=i+11;
					int end=s.indexOf('\"',i);
					String u=s.substring(i,end);
					String name=u.substring(u.lastIndexOf('/')+1/*,u.lastIndexOf('.')*/);
					next.add(new VersionInfo(name,new URL("https://mua.sibaken.org"+u)));
				}
				System.out.println("updated "+ Main.gson.toJson(next));
				versionInfo=next;
			}catch (Exception e)
			{
				e.printStackTrace();
				if(versionInfo==null)
					versionInfo=new ArrayList<>();
			}
		}
	}
	@Override
	public Packet handle()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				update();
			}
		}).start();
		return new ClientboundVersionListPacket(versionInfo);
	}
}

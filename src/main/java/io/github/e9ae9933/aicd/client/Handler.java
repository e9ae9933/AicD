package io.github.e9ae9933.aicd.client;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.e9ae9933.aicd.ModInfo;
import io.github.e9ae9933.aicd.packets.*;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Handler
{
	Socket socket;
	void connect(String address,int port) throws Exception
	{
		socket=new Socket();
		socket.setKeepAlive(true);
		socket.connect(new InetSocketAddress(address,port));
	}
	Handler(String address,int port) throws Exception
	{
		connect(address,port);
	}
	/*
	For defend xiaotiancai
	 */
	private Packet sendPacket(Packet packet)
	{
		return sendPacket(packet,null);
	}
	private synchronized Packet sendPacket(Packet packet, TypeToken<?> typeToken)
	{
		try
		{
			socket.getOutputStream().write(Main.gson.toJson(packet).getBytes(StandardCharsets.UTF_8));
			InputStream is = socket.getInputStream();
			while(is.available()==0&&socket.isConnected()&&!socket.isClosed());
			byte[] b = new byte[is.available()];
			is.read(b);
			String s = new String(b, StandardCharsets.UTF_8);
			System.out.println("s"+s);
			JsonObject object = Main.gson.fromJson(s, JsonObject.class);
			String type = object.get("packet_type").getAsString();
			Class<?> clazz = Class.forName("io.github.e9ae9933.aicd.packets." + type);
			if(typeToken!=null&&clazz.equals(typeToken.getRawType()))
				return (Packet) Main.gson.fromJson(s,typeToken);
			else
				return (Packet) Main.gson.fromJson(s, clazz);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new ClientboundRejectPacket("网络问题: "+e.toString());
		}
	}
	Packet register(String username,String password)
	{
		Packet packet=sendPacket(new ServerboundRegisterPacket(username,password));
		return packet;
	}
	Packet login(String username,String password)
	{
		Packet packet=sendPacket(new ServerboundLoginPacket(username,password));
		return packet;
	}
	Packet versionList()
	{
		return sendPacket(new ServerboundVersionListPacket());
	}
	Packet modInfos()
	{
		return sendPacket(new ServerboundRequestPacket(ServerboundRequestPacket.Type.MODS),new TypeToken<ClientboundResponcePacket<List<ModInfo>>>(){});
	}
	Packet bepInEx()
	{
		return sendPacket(new ServerboundRequestPacket(ServerboundRequestPacket.Type.BEPINEX),new TypeToken<ClientboundResponcePacket<URL>>(){});
	}
}

package io.github.e9ae9933.aicd.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.e9ae9933.aicd.ModInfo;
import io.github.e9ae9933.aicd.SocketHandler;
import io.github.e9ae9933.aicd.Utils;
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
	String address;
	SocketHandler handler;
	int port;
	void connect() throws Exception
	{
		socket=new Socket();
		socket.setKeepAlive(true);
		System.out.println("连接到 "+address+":"+port);
		socket.connect(new InetSocketAddress(address,port));
		handler=new SocketHandler(socket,true);
	}
	Handler(String address,int port) throws Exception
	{
		this.address=address;
		this.port=port;
		connect();
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
			if(socket.isClosed()||!socket.isConnected()||socket.isOutputShutdown()||socket.isInputShutdown())
			{
				connect();
			}
			handler.sendPacket(packet);
			Packet rt=null;
			long end=System.currentTimeMillis();
			end+=1000*15;
			while((rt=handler.getPacket())==null)
			{
				if(System.currentTimeMillis()>end)
					return new ClientboundRejectPacket("网络超时");
				Thread.sleep(1);
			}
			// todo: shit-like casting
			if(typeToken==null)
				return rt;
			else
			{
				Gson gson=Main.gson;
				return (Packet) gson.fromJson(gson.toJson(rt),typeToken);
			}
		}
		catch (Exception e)
		{
			Utils.ignoreExceptions(()->socket.close());
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
	Packet aboutPage()
	{
		return sendPacket(new ServerboundRequestPacket(ServerboundRequestPacket.Type.ABOUT),new TypeToken<ClientboundResponcePacket<URL>>(){});
	}
}

package io.github.e9ae9933.aicd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.e9ae9933.aicd.packets.ClientboundKeepalivePacket;
import io.github.e9ae9933.aicd.packets.ClientboundRejectPacket;
import io.github.e9ae9933.aicd.packets.Packet;
import io.github.e9ae9933.aicd.server.Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketHandler implements Runnable
{
	Socket socket;
	boolean alive;
	ConcurrentLinkedQueue<Packet> pendingPackets;
	Thread thread;
	int len=-1;
	public SocketHandler(Socket socket,boolean run)
	{
		this.socket = socket;
		alive=true;
		pendingPackets=new ConcurrentLinkedQueue<>();
		if(run)
		{
			thread = new Thread(this);
			thread.start();
		}
	}
	public boolean isAlive()
	{
		return alive;
	}
	public void close()
	{
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		alive=false;
	}
	public /*synchronized*/ void tick()
	{
		Gson gson=Policy.gson;
//		while(alive)
		if(alive)
		{
			try
			{
				InputStream is=socket.getInputStream();
				OutputStream os=socket.getOutputStream();
				if(socket.isClosed())
					throw new IOException("socket closed");
				if(len==-1)
				{
					if(is.available()<2)
//						continue;
						return;
					int l1=is.read();int l2=is.read();len=l1<<8|l2;
//					continue;
					return;
				}
				else
				{
					if (is.available() >= len)
					{
						byte[] b = new byte[len];
						is.read(b);
						String s = new String(b, StandardCharsets.UTF_8);
						JsonObject object = gson.fromJson(s, JsonObject.class);
						String type = object.get("packet_type").getAsString();
						Class<?> clazz = Class.forName("io.github.e9ae9933.aicd.packets." + type);
						Packet packet = (Packet) gson.fromJson(s, clazz);
						System.out.println("Received:");
						System.out.println(s);
						if (!(packet instanceof ClientboundKeepalivePacket))
						{
							pendingPackets.add(packet);
						}
						len=-1;
					}
				}
			}
			catch (Exception e)
			{
				alive=false;
				e.printStackTrace();
				Utils.ignoreExceptions(socket::close);
			}
		}
	}
	@Override
	public void run()
	{
		while(alive)
		{
			tick();
			Utils.ignoreExceptions(()->Thread.sleep(50));
		}
	}
	/*
	public Packet getNextPacket()
	{
		return pendingPackets.poll();
	}
	public synchronized Packet getNextPacketUntilExists()
	{
		while(alive&&pendingPackets.isEmpty())
		{

		}
		if(!alive)
			return new ClientboundRejectPacket("网络断开");
		return pendingPackets.poll();
	}
	*/
	public Packet getPacket()
	{
		return pendingPackets.poll();
	}
	public /*synchronized*/ void sendPacket(Packet packet)
	{
		if(!alive)
			return;
		try
		{
			OutputStream os = socket.getOutputStream();
			Gson gson = Policy.gson;
			String s = gson.toJson(packet);
			byte[] b = s.getBytes(StandardCharsets.UTF_8);
			int len=b.length;
			if(len>=65536)
				throw new RuntimeException("len "+len+" size too big as "+packet.getClass().getName());
			os.write(len>>8);
			os.write(len&0xFF);
			os.write(b);
		}
		catch (Exception e)
		{
			alive=false;
			e.printStackTrace();
			try
			{
				socket.close();
			}
			catch (Exception ee)
			{
				ee.printStackTrace();
			}
		}
	}
}

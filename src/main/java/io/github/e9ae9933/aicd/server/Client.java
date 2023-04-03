package io.github.e9ae9933.aicd.server;

import com.google.gson.JsonObject;
import io.github.e9ae9933.aicd.packets.ClientboundRejectPacket;
import io.github.e9ae9933.aicd.packets.Packet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client
{
	Socket socket;
	InputStream is;
	OutputStream os;
	private boolean isAlive=true;
	Client(Socket socket)
	{
		try
		{
			this.socket = socket;
			is = socket.getInputStream();
			os = socket.getOutputStream();
		}
		catch (Exception e)
		{
			isAlive=false;
		}
	}
	void tick()
	{
		try
		{
			//We only accept ONE query.
			if(socket.isClosed()||!isAlive)
				throw new Exception("close");
			if(is.available()==0)
				return;
			try
			{
				byte[] b = new byte[is.available()];
				is.read(b);
				String s = new String(b, StandardCharsets.UTF_8);
				JsonObject object = Main.gson.fromJson(s, JsonObject.class);
				String type = object.get("packet_type").getAsString();
				Class<?> clazz = Class.forName("io.github.e9ae9933.aicd.packets." + type);
				Packet packet = (Packet) Main.gson.fromJson(s, clazz);
				Packet response = packet.handle();
				byte[] r = Main.gson.toJson(response).getBytes(StandardCharsets.UTF_8);
				os.write(r);
				os.flush();
			}catch (Exception e)
			{
				Packet response = new ClientboundRejectPacket(e.getMessage());
				byte[] r = Main.gson.toJson(response).getBytes(StandardCharsets.UTF_8);
				os.write(r);
				os.flush();
			}
		}
		catch (Exception e)
		{
			isAlive=false;
			System.out.println("close socket");
			e.printStackTrace(System.out);
			try
			{
				socket.close();
			}
			catch (Exception ee)
			{

			}
		}
	}

	public boolean isAlive()
	{
		return isAlive;
	}
}

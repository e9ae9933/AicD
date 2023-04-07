package io.github.e9ae9933.aicd.server;

import com.google.gson.JsonObject;
import io.github.e9ae9933.aicd.SocketHandler;
import io.github.e9ae9933.aicd.packets.ClientboundKeepalivePacket;
import io.github.e9ae9933.aicd.packets.ClientboundRejectPacket;
import io.github.e9ae9933.aicd.packets.Packet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client
{
	Socket socket;
	SocketHandler handler;
	Client(Socket socket)
	{
		try
		{
			this.socket = socket;
			handler=new SocketHandler(socket,false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	int ticks=0;
	void tick()
	{
		ticks++;
		handler.tick();
		if(ticks%(20*15)==0)
			handler.sendPacket(new ClientboundKeepalivePacket());
		Packet pending;
		while((pending=handler.getPacket())!=null)
		{
			try
			{
				Packet responce=pending.handle();
				handler.sendPacket(responce);
			}
			catch (Exception e)
			{
				handler.sendPacket(new ClientboundRejectPacket(e.getMessage()));
			}
		}
//		try
//		{
//			//We only accept ONE query.
//			if(socket.isClosed()||!socket.isConnected()||socket.isInputShutdown()||socket.isOutputShutdown()||!isAlive)
//				throw new Exception("close");
//			try
//			{
//				Packet packet=handler.getNextPacket();
//				if(packet)
//			}catch (Exception e)
//			{
//				Packet response = new ClientboundRejectPacket(e.getMessage());
//				byte[] r = Main.gson.toJson(response).getBytes(StandardCharsets.UTF_8);
//				os.write(r);
//				os.flush();
//			}
//		}
//		catch (Exception e)
//		{
//			isAlive=false;
//			System.out.println("close socket "+socket.getInetAddress());
//			e.printStackTrace(System.out);
//			try
//			{
//				socket.close();
//			}
//			catch (Exception ee)
//			{
//
//			}
//		}
	}

	public boolean isAlive()
	{
		return handler.isAlive();
	}
}

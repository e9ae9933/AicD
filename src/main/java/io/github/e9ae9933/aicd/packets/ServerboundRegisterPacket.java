package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.server.Database;
import io.github.e9ae9933.aicd.server.User;

public class ServerboundRegisterPacket extends Packet
{
	String username;
	String password;

	public ServerboundRegisterPacket(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	@Override
	public Packet handle()
	{
		User user= Database.instance.register(username,password);
		return new ClientboundLoginPacket(user.token,user.tokenExpireDate);
	}
}

package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.server.Database;
import io.github.e9ae9933.aicd.server.User;

public class ServerboundLoginPacket extends Packet
{
	String username;
	String password;

	public ServerboundLoginPacket(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	@Override
	public Packet handle()
	{
		User user= Database.instance.login(username,password);
		user.refreshToken();
		return new ClientboundLoginPacket(user.token,user.tokenExpireDate);
	}
}

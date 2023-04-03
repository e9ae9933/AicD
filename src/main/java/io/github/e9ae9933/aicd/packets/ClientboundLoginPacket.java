package io.github.e9ae9933.aicd.packets;

import java.util.UUID;

public class ClientboundLoginPacket extends Packet
{
	public UUID token;
	public long tokenExpireDate;
	public ClientboundLoginPacket(UUID token,long tokenExpireDate)
	{
		this.token = token;
		this.tokenExpireDate=tokenExpireDate;
	}
	@Override
	public Packet handle()
	{
		return null;
	}
}

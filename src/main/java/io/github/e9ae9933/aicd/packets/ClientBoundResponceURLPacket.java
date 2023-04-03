package io.github.e9ae9933.aicd.packets;

import java.net.URL;

@Deprecated
public class ClientBoundResponceURLPacket extends Packet
{
	public URL url;
	public ClientBoundResponceURLPacket(URL url)
	{
		this.url = url;
	}

	@Override
	public Packet handle()
	{
		return null;
	}
}

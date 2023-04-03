package io.github.e9ae9933.aicd.packets;

public class ClientboundRejectPacket extends Packet
{
	public String text;

	public ClientboundRejectPacket(String text)
	{
		this.text = text;
	}

	@Override
	public Packet handle()
	{
		return null;
	}
}

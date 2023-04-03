package io.github.e9ae9933.aicd.packets;

public class ClientboundResponcePacket<T> extends Packet
{
	public T data;

	public ClientboundResponcePacket(T data)
	{
		this.data = data;
	}

	@Override
	public Packet handle()
	{
		return null;
	}
}

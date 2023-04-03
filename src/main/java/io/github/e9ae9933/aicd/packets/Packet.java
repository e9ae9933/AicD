package io.github.e9ae9933.aicd.packets;

public abstract class Packet
{
	String packetType=getClass().getSimpleName();
	public abstract Packet handle();
}

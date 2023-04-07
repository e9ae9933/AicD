package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.Constants;

public abstract class Packet
{
	String packetType=getClass().getSimpleName();
	int versionCode=Constants.versionCode;
	public abstract Packet handle();
}

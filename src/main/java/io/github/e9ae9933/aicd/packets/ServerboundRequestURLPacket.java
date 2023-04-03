package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.Utils;

import java.net.URL;
import java.util.function.Supplier;

@Deprecated
public class ServerboundRequestURLPacket extends Packet
{
	Type type;
	String data;

	public ServerboundRequestURLPacket(Type type, String data)
	{
		this.type = type;
		this.data = data;
	}

	@Override
	public Packet handle()
	{
		switch (type)
		{
			case BEPINEX:
				return new ClientBoundResponceURLPacket(Utils.ignoreExceptions(()->new URL("https://github.com/BepInEx/BepInEx/releases/download/v5.4.21/BepInEx_x64_5.4.21.0.zip")));
		}
		return new ClientboundRejectPacket("Unknown type");
	}
	public enum Type
	{
		BEPINEX,
		MOD
	}
}

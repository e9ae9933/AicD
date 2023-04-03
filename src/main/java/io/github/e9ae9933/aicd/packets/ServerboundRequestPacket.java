package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.server.Database;

import java.net.URL;
import java.util.UUID;

public class ServerboundRequestPacket extends Packet
{
	Type type;
	String extra;
	UUID token;

	public ServerboundRequestPacket(Type type, String extra,UUID token)
	{
		this.type = type;
		this.extra = extra;
		this.token = token;
	}
	public ServerboundRequestPacket(Type type)
	{
		this(type,null,null);
	}

	@Override
	public Packet handle()
	{
		switch (type)
		{
			case MODS:
				return new ClientboundResponcePacket<>(Database.instance.getMods());
			case BEPINEX:
				return new ClientboundResponcePacket<>(Utils.ignoreExceptions(()->new URL("https://github.com/BepInEx/BepInEx/releases/download/v5.4.21/BepInEx_x64_5.4.21.0.zip")));
		}
		return new ClientboundRejectPacket("Unknown type: "+type);
	}

	public enum Type
	{
		MODS,
		BEPINEX,
	}
}

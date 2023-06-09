package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.Constants;
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
				return new ClientboundResponcePacket<>(Utils.ignoreExceptions(()->new URL("https://aicd-1259776053.cos.ap-beijing.myqcloud.com/BepInEx_x64_5.4.21.0.zip")));
			case ABOUT:
//				return new ClientboundResponcePacket<>(Utils.ignoreExceptions(()->new URL("https://aicd-1259776053.cos.ap-beijing.myqcloud.com/AicD%20%E8%AF%B4%E6%98%8E.html")));
				return new ClientboundResponcePacket<>(Utils.ignoreExceptions(()->new URL("https://aicd-1259776053.cos.ap-beijing.myqcloud.com/AicD_about.html")));
			case UPDATE:
				return new ClientboundResponcePacket<>(new LatestVersion(Constants.version,Constants.versionCode,Utils.ignoreExceptions(()->new URL(Constants.versionUrl))));
		}
		return new ClientboundRejectPacket("Unknown type: "+type);
	}

	public enum Type
	{
		MODS,
		BEPINEX,
		ABOUT,
		UPDATE,
	}
	public class LatestVersion
	{
		public String version;
		public int versionCode;
		public URL url;

		public LatestVersion(String version, int versionCode, URL url)
		{
			this.version = version;
			this.versionCode = versionCode;
			this.url = url;
		}
	}
}

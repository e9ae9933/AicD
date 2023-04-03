package io.github.e9ae9933.aicd.packets;

import io.github.e9ae9933.aicd.VersionInfo;

import java.util.List;

public class ClientboundVersionListPacket extends Packet
{
	public List<VersionInfo> versionList;

	public ClientboundVersionListPacket(List<VersionInfo> versionList)
	{
		this.versionList = versionList;
	}

	@Override
	public Packet handle()
	{
		return null;
	}
}

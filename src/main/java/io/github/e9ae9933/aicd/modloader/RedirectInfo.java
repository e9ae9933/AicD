package io.github.e9ae9933.aicd.modloader;

class RedirectInfo
{
	String modsMD5;
	PxlsUnpackedCache pxlsUnpackedCache;

	RedirectInfo(RedirectHandler handler)
	{
		modsMD5 = handler.md5Dir(handler.getModsDir());
		pxlsUnpackedCache = new PxlsUnpackedCache(handler.getRedirectAssets().getPxlsUnpackedDir());
	}
}

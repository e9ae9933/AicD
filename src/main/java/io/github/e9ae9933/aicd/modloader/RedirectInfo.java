package io.github.e9ae9933.aicd.modloader;

class RedirectInfo
{
	String modsMD5;
	PxlsUnpackedCache pxlsUnpackedCache;
	PxlsCache pxlsCache;
	boolean needUpdate;

	RedirectInfo(RedirectHandler handler)
	{
		modsMD5 = handler.md5Dir(handler.getModsDir());
		pxlsUnpackedCache = new PxlsUnpackedCache(handler.getRedirectAssets().getPxlsUnpackedDir());
		pxlsCache=new PxlsCache(handler.getRedirectPxlsPackedDir());
		needUpdate=true;
	}
}

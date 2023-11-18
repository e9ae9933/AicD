package io.github.e9ae9933.aicd.main;

import java.io.*;
import java.nio.channels.FileLock;

public class SystemLogger
{
	static FileOutputStream fos;
	static FileLock lock;
	static PrintStream stdout=System.out;
	static PrintStream stderr=System.err;
	static void logAll() throws Exception
	{
		fos=new FileOutputStream("atb.log");
		lock=fos.getChannel().tryLock();
		System.setOut(new PrintStream(new SplitOutputStream(stdout,fos),true,"UTF-8"));
		System.setErr(new PrintStream(new SplitOutputStream(stderr,fos),true,"UTF-8"));
	}
	static class SplitOutputStream extends OutputStream
	{
		private final OutputStream[] targets;

		public SplitOutputStream(OutputStream... targets)
		{
			this.targets=targets;
		}

		@Override
		public void write(int b) throws IOException
		{
			for(OutputStream os:targets)
				os.write(b);
		}
	}
}

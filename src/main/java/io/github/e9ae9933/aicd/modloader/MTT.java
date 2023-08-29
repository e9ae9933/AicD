package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Utils;

public class MTT
{
	public static void main(String[] args)
	{
		for(int i=0;i<10;i++)
			new Thread(()->new shit().hello2()).start();
	}
	static class shit
	{
		shit(){}
		synchronized void hello()
		{
			System.out.println("hello");
			Utils.ignoreExceptions(()->Thread.sleep(5000));
		}
		void hello2()
		{
			synchronized (this)
			{
				System.out.println("hello2");
				Utils.ignoreExceptions(()->Thread.sleep(5000));
			}
		}
	}
}

package io.github.e9ae9933.aicd.client;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import javax.swing.*;

public class Main
{
	public static Gson gson= Policy.gson;
	Handler handler;
	GUI gui;
	public static int port=10051;
	public static boolean integratedServerAsIntegrated=false;
	Main() throws Exception
	{
		//should we run server?
		if(true)
		{
			if(integratedServerAsIntegrated)
				io.github.e9ae9933.aicd.server.Main.main(new String[]{"--port","0","--integrated"});
			else
				io.github.e9ae9933.aicd.server.Main.main(new String[]{"--port","0"});
			port= io.github.e9ae9933.aicd.server.Main.port;
		}
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Utils.getGamePath(false);

		handler=new Handler("localhost",port);
		gui=new GUI(this,handler);
	}
	public static void main(String[] args) throws Exception
	{
		new Main();
	}
}

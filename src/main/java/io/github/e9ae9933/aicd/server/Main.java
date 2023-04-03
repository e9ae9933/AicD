package io.github.e9ae9933.aicd.server;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.packets.ServerboundVersionListPacket;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Main
{
	public static Gson gson= Policy.gson;
	public static Set<Client> clients= Collections.synchronizedSet(new HashSet<>());
	public static int port=10051;
	public static boolean integrated=false;
	public static void main(String[] args) throws Exception
	{
		OptionParser optionParser=new OptionParser();
		OptionSpec<Integer> portSpec=optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(port);
		OptionSpec<Void> integratedSpec=optionParser.accepts("integrated");
		OptionSet optionSet=optionParser.parse(args);

		port=optionSet.valueOf(portSpec);
		integrated=optionSet.has(integratedSpec);

		System.out.println(String.format("Running server on %d", port));
		if(integrated)
			System.out.println("Integrated server");
		Database.instance=new Database();

		ServerboundVersionListPacket.update();

		ServerSocket socket=new ServerSocket();
		socket.bind(new InetSocketAddress("localhost",port));
		port=socket.getLocalPort();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(!socket.isClosed())
				{
					clients.forEach(Client::tick);
					clients.removeIf(c->!c.isAlive());
				}
			}
		}).start();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(!socket.isClosed())
				{
					try
					{
						Socket c = socket.accept();
						c.setKeepAlive(true);
						clients.add(new Client(c));
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}).start();
		System.out.println("Server main end with port "+port);
	}
}

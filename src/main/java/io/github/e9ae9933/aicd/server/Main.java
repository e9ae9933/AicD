package io.github.e9ae9933.aicd.server;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.packets.ServerboundVersionListPacket;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
		if(integrated)
		{
			System.err.println("警告: 尝试放通所有证书");
			try
			{
				SSLContext context=SSLContext.getInstance("SSL");
				context.init(null,new TrustManager[]{
					new X509TrustManager()
					{
						@Override
						public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
						{

						}

						@Override
						public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
						{

						}

						@Override
						public X509Certificate[] getAcceptedIssuers()
						{
							return null;
						}
					}
				},null);
				HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier((url,ss)->true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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

		ServerSocket socket=new ServerSocket(port);
		port=socket.getLocalPort();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(!socket.isClosed())
				{
					long time=System.currentTimeMillis();
					clients.forEach(Client::tick);
					clients.stream().filter(c->!c.isAlive()).forEach(c->{
						System.out.println("Closed connection from "+c.socket.getRemoteSocketAddress());
					});
					clients.removeIf(c->!c.isAlive());
					long used=System.currentTimeMillis()-time;
					Utils.ignoreExceptions(()->Thread.sleep(50));
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
						System.out.println("Accepted connection from "+c.getRemoteSocketAddress());
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

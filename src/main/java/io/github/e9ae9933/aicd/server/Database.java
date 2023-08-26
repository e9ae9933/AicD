package io.github.e9ae9933.aicd.server;

import com.google.gson.reflect.TypeToken;
import io.github.e9ae9933.aicd.ModInfo;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.VersionInfo;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class Database
{
	public static Database instance;
	private Set<User> users;
	private Map<UUID,User> tokenToUser;
	private Map<String,User> usernameToUser;
	private Set<ModInfo> mods;
	private List<VersionInfo> versions;
	Database() throws Exception
	{
		versions=readFrom("versions.json",new TypeToken<List<VersionInfo>>(){});
		VersionInfo info;
		versions.add(info=new VersionInfo("等待自动更新……",null,"镜像站"));
		AtomicBoolean updatedFirst=new AtomicBoolean(false);
		Thread timer= new Thread(() ->
		{
			while(true)
			{
				try{
					URL url=new URL("https://aic.zip");
					HttpsURLConnection conn= ((HttpsURLConnection) url.openConnection());
					conn.addRequestProperty("User-Agent","AliceInCradle Toolbox on Windows");
					conn.addRequestProperty("Sec-Ch-Ua-Platform","\"Windows\"");
					conn.connect();
					System.out.println(conn.getResponseCode());
					InputStream is=conn.getInputStream();
					ByteArrayOutputStream b=new ByteArrayOutputStream();
					int ch;
					ArrayList<Byte> bb=new ArrayList<>();
					while((ch=is.read())!=-1)
						bb.add(((byte)ch));
					conn.disconnect();
					byte[] ba=new byte[bb.size()];
					for(int i=0;i<ba.length;i++)
						ba[i]=bb.get(i);
					String s=new String(ba,StandardCharsets.UTF_8);
					System.out.println(s);
					int pos=s.indexOf("\"blocklink");
					int r=s.lastIndexOf("\"",pos-1);
					int l=s.lastIndexOf("\"",r-1);
					String ans=s.substring(l+1,r);
					System.out.println(ans);
					String reboot=ans;

					Function<URL,URL> fun=(aurl)->{
//						URL aurl=new URL(ans);
						try
						{
							System.out.println("Connecting to "+aurl);
							HttpsURLConnection c = (HttpsURLConnection) aurl.openConnection();
							c.setInstanceFollowRedirects(false);
							c.addRequestProperty("User-Agent", "AliceInCradle Toolbox on Windows");
							c.connect();
							return new URL(c.getHeaderField("Location"));
						}
						catch (Exception e)
						{
							e.printStackTrace();
							return null;
						}
					};
					String fileName=fun.apply(fun.apply(new URL(reboot))).toString();
					String location=fileName;
					fileName=URLDecoder.decode(fileName);
//					c.disconnect();
					System.out.println(new URL(fileName).toExternalForm());
					fileName=fileName.substring(fileName.lastIndexOf('/')+1,fileName.lastIndexOf('.')-1+1);
					System.out.println(fileName);
					info.name=fileName+".zip";
					info.url=new URL(location);
				}catch (Exception e)
				{
					e.printStackTrace();
					if(!updatedFirst.get())
					{
						info.name = "自动更新最新版本失败";
					}
				}
				updatedFirst.getAndSet(true);
				Utils.ignoreExceptions(()->Thread.sleep(1000*600));
			}
		});
		timer.start();
		System.out.println("等待自动更新");
		while((!isIntegrated()||true)&&!updatedFirst.get());
		if(isIntegrated())
			return;
		users=readFrom("users.json",new TypeToken<HashSet<User>>(){});
		tokenToUser=new HashMap<>();
		usernameToUser=new HashMap<>();
		users.forEach(user->updateUserIntoMaps(user,false));
		mods=readFrom("mods.json",new TypeToken<HashSet<ModInfo>>(){});
//		mods.add(new ModInfo("test",null));
		save();
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		System.out.println("finalizing");
		save();
	}

	void save()
	{
		writeTo("users.json",users);
		writeTo("mods.json",mods);
	}
	void writeTo(String name,Object o)
	{
		try
		{
			FileOutputStream fos=new FileOutputStream(name);
			fos.write(Main.gson.toJson(o).getBytes(StandardCharsets.UTF_8));
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	<T> T readFrom(String name,TypeToken<T> typeToken)
	{
		try
		{
			InputStream is = Utils.readFromResources(name, !isIntegrated());
			byte[] b = Utils.readAllBytes(is);
			is.close();
			return Main.gson.fromJson(new String(b, StandardCharsets.UTF_8),typeToken);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	void updateUserIntoMaps(User user,boolean updateSet)
	{
		if(updateSet)
			users.add(user);
		tokenToUser.put(user.token,user);
		usernameToUser.put(user.username,user);
	}
	public void refreshToken(UUID oldToken,UUID newToken,User user)
	{
		tokenToUser.remove(oldToken);
		tokenToUser.put(newToken,user);
	}
	public User login(String username,String password)
	{
		checkIntegrated();
		User user=usernameToUser.get(username);
		if(user==null)
			throw new IllegalArgumentException("用户不存在");
		if(!user.password.equals(password))
			throw new IllegalArgumentException("密码错误");
		return user;
	}
	public User login(UUID uuid)
	{
		checkIntegrated();
		User user=tokenToUser.get(uuid);
		if(user==null)
			return null;
		if(System.currentTimeMillis()>=user.tokenExpireDate)
			return null;
		return user;
	}
	public User register(String username,String password)
	{
		checkIntegrated();
		if(!Policy.isUsernameValid(username))
			throw new IllegalArgumentException("用户名不合法");
		if(!Policy.isPasswordValid(password))
			throw new IllegalArgumentException("密码不合法？？？");
		if(usernameToUser.containsKey(username))
			throw new IllegalArgumentException("用户已存在");
		User user=new User(username,password);
		updateUserIntoMaps(user,true);
		System.out.println("trying to register "+username+" "+password);
		return user;
	}
	public List<ModInfo> getMods()
	{
		checkIntegrated();
		return new ArrayList<>(mods);
	}
	public List<VersionInfo> getVersions()
	{
		return versions;
	}
	boolean isIntegrated()
	{
		return Main.integrated;
	}
	void checkIntegrated()
	{
		if(isIntegrated())
			throw new UnsupportedOperationException("内置服务端不支持该操作");
	}
}

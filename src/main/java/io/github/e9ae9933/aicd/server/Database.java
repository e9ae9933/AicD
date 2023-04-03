package io.github.e9ae9933.aicd.server;

import com.google.gson.reflect.TypeToken;
import io.github.e9ae9933.aicd.ModInfo;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Database
{
	public static Database instance;
	private Set<User> users;
	private Map<UUID,User> tokenToUser;
	private Map<String,User> usernameToUser;
	private Set<ModInfo> mods;
	Database() throws Exception
	{
		if(isIntegrated())
			return;
		users=readFrom("users",new HashSet<>(),new TypeToken<HashSet<User>>(){});
		tokenToUser=new HashMap<>();
		usernameToUser=new HashMap<>();
		users.forEach(user->updateUserIntoMaps(user,false));
		mods=readFrom("mods",new HashSet<>(),new TypeToken<HashSet<ModInfo>>(){});
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
		writeTo("users",users);
		writeTo("mods",mods);
	}
	void writeTo(String name,Object o)
	{
		try
		{
			FileWriter fw = new FileWriter(new File(name + ".json"));
			fw.write(Main.gson.toJson(o));
			fw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	<T> T readFrom(String name,T def,TypeToken<T> typeToken)
	{
		File file=new File(name+".json");
		if(!file.exists())
			return def;
		FileReader fr= Utils.ignoreExceptions(()->new FileReader(file));
		T o=Main.gson.fromJson(fr,typeToken);
		Utils.ignoreExceptions(()->fr.close());
		if(o!=null)
		System.out.println("T "+o.getClass());
		return o==null?def:o;
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
		return new ArrayList<>(mods);
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

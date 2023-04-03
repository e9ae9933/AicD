package io.github.e9ae9933.aicd.server;

import io.github.e9ae9933.aicd.Policy;

import java.util.UUID;

public class User
{
	public String username;
	public String password;
	public UUID token;
	public long tokenExpireDate;

	User()
	{
	}
	User(String username,String password)
	{
		if(!Policy.isUsernameValid(username))
			throw new IllegalArgumentException("illegal username");
		if(!Policy.isPasswordValid(password))
			throw new IllegalArgumentException("illegal password");
		this.username=username;
		this.password=password;
		refreshToken();
	}
	public void refreshToken()
	{
		UUID old=token;
		token=UUID.randomUUID();
		tokenExpireDate=System.currentTimeMillis()+1000L*60*60*24*30;
		Database.instance.refreshToken(old,token,this);
	}
}

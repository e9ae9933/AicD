package io.github.e9ae9933.aicd;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.File;
import java.io.IOException;

public class Policy
{
	public static String serverHost="43.138.15.251";
	public static int serverPort=10051;
	public static Gson gson=new GsonBuilder().registerTypeAdapterFactory(TypeAdapters.newFactory(Class.class, new TypeAdapter<Class>()
	{
		@Override
		public void write(JsonWriter out, Class value) throws IOException
		{
			if(value==null)
				out.nullValue();
			else
				out.value(value.getName());
		}

		@Override
		public Class read(JsonReader in) throws IOException
		{
			try
			{
				return Class.forName(in.nextString());
			} catch (ClassNotFoundException e)
			{
				throw new IOException(e);
			}
		}
	})).registerTypeAdapterFactory(TypeAdapters.newFactory(File.class, new TypeAdapter<File>()
	{
		@Override
		public void write(JsonWriter out, File value) throws IOException
		{
			if(value==null)
				out.nullValue();
			else
				out.value(value.getPath());
		}

		@Override
		public File read(JsonReader in) throws IOException
		{
			return new File(in.nextString());
		}
	})).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)/*.serializeNulls()*/.setPrettyPrinting().serializeSpecialFloatingPointValues().disableHtmlEscaping().create();
	public static Load load= new Load(LoadSettings.builder().setParseComments(true).build());
	public static Dump dump=new Dump(DumpSettings.builder().setDumpComments(true).setUseUnicodeEncoding(true).build());
	public static boolean isUsernameValid(String s)
	{
		if(s==null)
			return false;
		if(s.length()<8||s.length()>15)
			return false;
		for(char c:s.toCharArray())
			if(!(c>='0'&&c<='9'||c>='a'&&c<='z'||c>='A'&&c<='Z'))
				return false;
		return true;
	}
	public static boolean isPasswordValid(String s)
	{
		//todo
		if(s==null)
			return false;
		if(s.length()==0)
			return false;
		return true;
	}
}

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import org.snakeyaml.engine.v2.api.Load;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Test01
{
	public static void main(String[] args) throws Exception
	{
		File file=new File("H:\\work\\LongLongCane\\translations\\zh-cn\\ev____Laevi_v_talk_cane.yml");
		Gson gson=io.github.e9ae9933.aicd.Policy.getGson();
		Load load= Policy.getLoad();

		Object o=load.loadFromInputStream(new FileInputStream(file));
		String s=gson.toJson(o);
		Object o2=load.loadFromString(s);
		System.out.println(o.equals(o2));
	}
}

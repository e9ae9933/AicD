import io.github.e9ae9933.aicd.l10nkiller.Family;
import io.github.e9ae9933.aicd.l10nkiller.RefreshedEventLoader;

import java.io.File;

public class Test02
{
	public static void main(String[] args)
	{
		Family a=RefreshedEventLoader.loadFamilyFromAIC(new File("H:\\AliceInCradle_Data\\StreamingAssets\\localization\\zh-cn\\ev_fatal_snake.txt"));
		System.out.println(a);
	}
}

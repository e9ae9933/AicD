using HarmonyLib;
using nel;

namespace SamplePlugin
{
	public class Listener
	{
		[HarmonyPatch(typeof(NEL),"version",MethodType.Getter)]
		[HarmonyPostfix]
		public static void patchVersion(ref string __result)
		{
			// TODO: PLEASE DISABLE THIS PATCHER BEFORE RELEASING
			__result += " with " + PluginInfo.PLUGIN_NAME;
		}
	}
}

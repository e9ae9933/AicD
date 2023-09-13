using BepInEx;
using HarmonyLib;

namespace SamplePlugin
{
    [BepInPlugin(PluginInfo.PLUGIN_GUID, PluginInfo.PLUGIN_NAME, PluginInfo.PLUGIN_VERSION)]
    [BepInDependency("AicUtils",BepInDependency.DependencyFlags.SoftDependency)]
    public class Plugin : BaseUnityPlugin
    {
        private void Awake()
        {
            // Plugin startup logic

            // 1. example to access Resources(in the dll)
            Logger.LogInfo($"TextTest.txt has length {Resource.TestText.Length}");

            // 2. example to listen to some events
            Harmony.CreateAndPatchAll(typeof(Listener));

            // 3. access to some AicUtils API (soft dependency)
            Logger.LogInfo($"If there is AicUtils, then it says, \"{AicUtils.API.hello()}\"");

            Logger.LogInfo($"Plugin {PluginInfo.PLUGIN_GUID} is loaded!");
        }
    }
}

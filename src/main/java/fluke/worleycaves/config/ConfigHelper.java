package fluke.worleycaves.config;

import net.minecraftforge.fml.config.ModConfig;

public class ConfigHelper
{

	private static ModConfig clientConfig;
	private static ModConfig serverConfig;

	public static void bakeClient(final ModConfig config)
	{
		clientConfig = config;

//		ModConfig.clientBoolean = ConfigHolder.CLIENT.clientBoolean.get();
//		ModConfig.clientStringList = ConfigHolder.CLIENT.clientStringList.get();
//		ModConfig.clientEnumDyeColor = ConfigHolder.CLIENT.clientEnumDyeColor.get();

	}

	public static void bakeServer(final ModConfig config)
	{
		serverConfig = config;

		WorleyConfig.lavaDepth = ConfigHolder.SERVER.lavaDepth.get();
	}

	private static void setValueAndSave(final ModConfig modConfig, final String path, final Object newValue)
	{
		modConfig.getConfigData().set(path, newValue);
		modConfig.save();
	}
}
	
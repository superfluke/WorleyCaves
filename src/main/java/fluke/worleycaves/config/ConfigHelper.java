package fluke.worleycaves.config;

import net.minecraftforge.fml.config.ModConfig;

public class ConfigHelper
{

	private static ModConfig clientConfig;
	private static ModConfig serverConfig;

	public static void bakeClient(final ModConfig config)
	{
		clientConfig = config;

	}

	public static void bakeServer(final ModConfig config)
	{
		serverConfig = config;
		
		//WorleyConfig.blackListedDims = (int[]) ConfigHolder.SERVER.blackListedDims.get().toArray(); //TODO fix dim blacklist
		WorleyConfig.lavaDepth = ConfigHolder.SERVER.lavaDepth.get();
		WorleyConfig.easeInDepth = ConfigHolder.SERVER.easeInDepth.get();
		WorleyConfig.lavaBlock = ConfigHolder.SERVER.lavaBlock.get();
		WorleyConfig.maxCaveHeight = ConfigHolder.SERVER.maxCaveHeight.get();
		WorleyConfig.minCaveHeight = ConfigHolder.SERVER.minCaveHeight.get();
		WorleyConfig.noiseCutoffValue = ConfigHolder.SERVER.noiseCutoffValue.get();
		WorleyConfig.surfaceCutoffValue = ConfigHolder.SERVER.surfaceCutoffValue.get();
		WorleyConfig.verticalCompressionMultiplier = ConfigHolder.SERVER.verticalCompressionMultiplier.get();
		WorleyConfig.horizonalCompressionMultiplier = ConfigHolder.SERVER.horizonalCompressionMultiplier.get();
		WorleyConfig.warpAmplifier = ConfigHolder.SERVER.warpAmplifier.get();

	}

	private static void setValueAndSave(final ModConfig modConfig, final String path, final Object newValue)
	{
		modConfig.getConfigData().set(path, newValue);
		modConfig.save();
	}
}
	
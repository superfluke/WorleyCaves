package fluke.worleycaves.config;

import net.minecraftforge.common.config.Configuration;

public class Configs 
{
	public static Configuration config;
  
	public static double noiseCutoffValue;
	public static double surfaceCutoffValue;
	public static double warpAmplifier;
	public static int easeInDepth;
	public static double verticalCompressionMultiplier;
	public static double horizonalCompressionMultiplier;
	public static int[] blackListedDims;
	public static int maxCaveHeight;
	
	public static boolean refreshConfig()
	{
		config.load();
		
		noiseCutoffValue = config.get("cavegen", "noiseCuttofValue", -0.18, "Controls size of caves. Smaller values = larger caves.", -1.0, 1.0).getDouble();
		surfaceCutoffValue = config.get("cavegen", "surfaceCutoffValue", -0.081, "Controls size of caves at the surface. Smaller values = more caves break through the surface.", -1.0, 1.0).getDouble(); //Default: -0.081 (45% of noiseCutoffValue)
		warpAmplifier = config.get("cavegen", "warpAmplifier", 8.0, "Controls how much to warp caves. Lower values = straighter caves").getDouble();
		easeInDepth = config.get("cavegen", "easeInDepth", 15, "Reduces number of caves at surface level, becoming more common until caves generate normally X number of blocks below the surface").getInt();
		verticalCompressionMultiplier = config.get("cavegen", "verticalCompressionMultiplier", 2.0, "Squishes caves on the Y axis. Lower values = taller caves and more steep drops").getDouble();
		horizonalCompressionMultiplier = config.get("cavegen", "horizonalCompressionMultiplier", 1.0, "Streches (when > 1.0) or compresses (when < 1.0) cave generation along X and Z axis").getDouble();
		blackListedDims = config.get("cavegen", "blackListedDims", new int[] {}, "Dimension IDs that will use Vanilla cave generation rather than Worley's Caves").getIntList();
		maxCaveHeight = config.get("cavegen", "maxCaveHeight", 128, "Caves will not attempt to generate above this y level.", 1, 256).getInt();
		
		config.save();
		
		return true;
	}
}

package fluke.worleycaves.config;

import fluke.worleycaves.util.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;

@Config(modid = Reference.MOD_ID, category = "")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class Configs 
{
	public static ConfigCaveGen cavegen = new ConfigCaveGen();
	
	public static class ConfigCaveGen
	{
		@Config.Comment({"Controls size of caves. Smaller values = larger caves. Between -1.0 and 1.0", "Default: -0.18"})
		@Config.RequiresWorldRestart
		public double noiseCutoffValue = -0.18;
		
		@Config.Comment({"Controls size of caves at the surface. Smaller values = more caves break through the surface. Between -1.0 and 1.0", "Default: -0.081 (45% of noiseCutoffValue)"})
		@Config.RequiresWorldRestart
		public double surfaceCutoffValue = -0.081;
		
		@Config.Comment({"Controls how much to warp caves. Lower values = straighter caves", "Default: 8.0"})
		@Config.RequiresWorldRestart
		public double warpAmplifier = 8.0;
		
		@Config.Comment({"Reduces number of caves at surface level, becoming more common until caves generate normally X number of blocks below the surface", "Default: 15"})
		@Config.RequiresWorldRestart
		public int easeInDepth = 15;
		
		@Config.Comment({"Squishes caves on the Y axis. Lower values = taller caves and more steep drops", "Default: 2.0"})
		@Config.RequiresWorldRestart
		public double verticalCompressionMultiplier = 2.0;
		
	    @Config.Comment({"Streches (when > 1.0) or compresses (when < 1.0) cave generation along X and Z axis", "Default: 1.0"}) 
	    @Config.RequiresWorldRestart 
	    public double horizonalCompressionMultiplier = 1.0; 
		
		@Config.Comment({"Dimension IDs that will use Vanilla cave generation rather than Worley's Caves", "Default:"})
		@Config.RequiresWorldRestart
		public int[] blackListedDims = {};
	}

}

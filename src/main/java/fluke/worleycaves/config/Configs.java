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
		
		@Config.Comment({"At this y-level and below air is replaced by lava", "Default: 10"})
		@Config.RequiresWorldRestart
		public int lavaDepth = 10;
		
		@Config.Comment({"Controls how much to warp caves. Lower values = straighter caves", "Default: 8.0"})
		@Config.RequiresWorldRestart
		public double warpAmplifier = 8.0;
	}

}

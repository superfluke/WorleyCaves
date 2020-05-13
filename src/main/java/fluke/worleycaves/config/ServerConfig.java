package fluke.worleycaves.config;

import fluke.worleycaves.util.Reference;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	final ForgeConfigSpec.ConfigValue<Integer> lavaDepth;
	final ForgeConfigSpec.ConfigValue<Integer> easeInDepth;
	final ForgeConfigSpec.ConfigValue<String> lavaBlock;
	final ForgeConfigSpec.ConfigValue<Integer> maxCaveHeight;
	final ForgeConfigSpec.ConfigValue<Integer> minCaveHeight;
	final ForgeConfigSpec.ConfigValue<Double> noiseCutoffValue;
	final ForgeConfigSpec.ConfigValue<Double> surfaceCutoffValue;
	final ForgeConfigSpec.ConfigValue<Double> verticalCompressionMultiplier;
	final ForgeConfigSpec.ConfigValue<Double> horizonalCompressionMultiplier;
	final ForgeConfigSpec.ConfigValue<Double> warpAmplifier;

	ServerConfig(final ForgeConfigSpec.Builder builder)
	{
		builder.push("general");
		
		lavaDepth = builder.comment("\nAir blocks at or below this y level will generate as lavaBlock \nDefault: 10")
				.translation(Reference.MOD_ID + ".config.lavaDepth")
				.worldRestart()
				.defineInRange("lavaDepth", 10, 0, 255);

		easeInDepth = builder.comment("\nReduces number of caves at surface level, becoming more common until caves generate normally X number of blocks below the surface \nDefault: 15")
				.translation(Reference.MOD_ID + ".config.easeInDepth")
				.worldRestart()
				.define("easeInDepth", 15);

		lavaBlock = builder.comment("\nBlock to use when generating large lava lakes below lavaDepth (usually y=10) \nDefault: minecraft:lava")
				.translation(Reference.MOD_ID + ".config.lavaBlock")
				.worldRestart()
				.define("lavaBlock", "minecraft:lava");

		maxCaveHeight = builder.comment("\nCaves will not attempt to generate above this y level. \nDefault: 128")
				.translation(Reference.MOD_ID + ".config.maxCaveHeight")
				.worldRestart()
				.defineInRange("maxCaveHeight", 128, 1, 256);
		
		minCaveHeight = builder.comment("\nCaves will not attempt to generate below this y level. \nDefault: 1")
				.translation(Reference.MOD_ID + ".config.minCaveHeight")
				.worldRestart()
				.defineInRange("minCaveHeight", 1, 1, 256);
		
		noiseCutoffValue = builder.comment("\nControls size of caves. Smaller values = larger caves. \nDefault: -0.18")
				.translation(Reference.MOD_ID + ".config.noiseCutoffValue")
				.worldRestart()
				.defineInRange("noiseCutoffValue", -0.18, -1.0, 1.0);
		
		surfaceCutoffValue = builder.comment("\nControls size of caves at the surface. Smaller values = more caves break through the surface. \nDefault: -0.081 (45% of noiseCutoffValue)")
				.translation(Reference.MOD_ID + ".config.noiseCutoffValue")
				.worldRestart()
				.defineInRange("surfaceCutoffValue", -0.081, -1.0, 1.0);
		
		verticalCompressionMultiplier = builder.comment("\nSquishes caves on the Y axis. Lower values = taller caves and more steep drops \nDefault: 2.0")
				.translation(Reference.MOD_ID + ".config.verticalCompressionMultiplier")
				.worldRestart()
				.define("verticalCompressionMultiplier", 2.0);
		
		horizonalCompressionMultiplier = builder.comment("\nStreches (when < 1.0) or compresses (when > 1.0) cave generation along X and Z axis \nDefault: 1.0")
				.translation(Reference.MOD_ID + ".config.horizonalCompressionMultiplier")
				.worldRestart()
				.define("horizonalCompressionMultiplier", 1.0);
		
		warpAmplifier = builder.comment("\nControls how much to warp caves. Lower values = straighter caves \nDefault: 8.0")
				.translation(Reference.MOD_ID + ".config.warpAmplifier")
				.worldRestart()
				.define("warpAmplifier", 8.0);
		
		builder.pop();
	}
}

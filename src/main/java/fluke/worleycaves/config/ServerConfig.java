package fluke.worleycaves.config;

import fluke.worleycaves.util.Reference;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	final ForgeConfigSpec.ConfigValue<Integer> lavaDepth;

	ServerConfig(final ForgeConfigSpec.Builder builder)
	{
		builder.push("general");
		lavaDepth = builder.comment("Air blocks at or below this y level will generate as lavaBlock")
				.translation(Reference.MOD_ID + ".config.lavaDepth")
				// ..worldRestart()
				// .defineInRange("lavaDepth", 10, 0, 255);
				.define("lavaDepth", 10);
		builder.pop();
	}
}

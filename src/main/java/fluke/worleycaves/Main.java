package fluke.worleycaves;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fluke.worleycaves.util.Reference;
import fluke.worleycaves.world.WorldCarverWorley;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Reference.MOD_ID)
public class Main {
	public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

	public Main() {
		LOGGER.info("WorleyCaves entry point");
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::cavesReplace);
	}

	private void cavesReplace(final FMLCommonSetupEvent event) {
		
		LOGGER.info("WorleyCaves replace carvers");
		
		WorldCarver<ProbabilityConfig> worleyCarver = new WorldCarverWorley(ProbabilityConfig::deserialize, 256);
		ConfiguredCarver<ProbabilityConfig> configuredWorleyCarver = Biome.createCarver(worleyCarver, new ProbabilityConfig(1));
		
		ForgeRegistries.BIOMES.forEach(new Consumer<Biome>() {
			@Override
			public void accept(Biome b) {
				// Exclude Nether and End biomes
				if (b == Biomes.NETHER || b == Biomes.THE_END || b == Biomes.END_BARRENS || b == Biomes.END_HIGHLANDS || b == Biomes.END_MIDLANDS || b == Biomes.SMALL_END_ISLANDS) return;
				
				b.getCarvers(GenerationStage.Carving.AIR).clear();
				b.getCarvers(GenerationStage.Carving.LIQUID).clear();
				b.getCarvers(GenerationStage.Carving.LIQUID).add(configuredWorleyCarver);
			}
		});
	}
}

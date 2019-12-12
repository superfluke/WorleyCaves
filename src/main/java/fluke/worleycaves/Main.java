package fluke.worleycaves;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fluke.worleycaves.proxy.ClientProxy;
import fluke.worleycaves.proxy.CommonProxy;
import fluke.worleycaves.util.Reference;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MOD_ID)
public class Main
{
	public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
	public static CommonProxy proxy;

	public Main()
	{
		LOGGER.info("WorleyCaves entry point");

		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::cavesReplace);
		proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		proxy.start();
	}

//	private void cavesReplace(final FMLCommonSetupEvent event)
//	{
//
//		LOGGER.info("WorleyCaves replace carvers");
//
//		WorldCarver<ProbabilityConfig> worleyCarver = new WorldCarverWorley(ProbabilityConfig::deserialize, 256);
//		ConfiguredCarver<ProbabilityConfig> configuredWorleyCarver = Biome.createCarver(worleyCarver, new ProbabilityConfig(1));
//
//		ForgeRegistries.BIOMES.forEach(new Consumer<Biome>()
//		{
//			@Override
//			public void accept(Biome b)
//			{
//				// Exclude Nether and End biomes
//				if (b.getCategory() == Biome.Category.NETHER || b.getCategory() == Biome.Category.THEEND)
//					return;
//
//				b.getCarvers(GenerationStage.Carving.AIR).clear();
//				b.getCarvers(GenerationStage.Carving.LIQUID).clear();
//				b.getCarvers(GenerationStage.Carving.LIQUID).add(configuredWorleyCarver);
//			}
//		});
//	}
}

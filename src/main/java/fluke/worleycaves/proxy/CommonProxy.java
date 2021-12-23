package fluke.worleycaves.proxy;

import fluke.worleycaves.config.ConfigHelper;
import fluke.worleycaves.config.ConfigHolder;
import fluke.worleycaves.util.Reference;
import fluke.worleycaves.world.WorldCarverWorley;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.*;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.function.Supplier;

public class CommonProxy
{
	private WorldCarverWorley worleyCarver;
	private ConfiguredCarver<ProbabilityConfig> configuredWorleyCarver;
	private long worldSeed;
	private boolean seedsSet = false;
	
	public void start()
	{
		IEventBus fmlBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;


		registerListeners(fmlBus, forgeBus);
	}

	public void registerCarvers(RegistryEvent.Register<WorldCarver<?>> event) {
		final WorldCarverWorley value = new WorldCarverWorley(ProbabilityConfig.CODEC, 256);
		event.getRegistry().register(value.setRegistryName(new ResourceLocation(Reference.MOD_ID, "worley_cave")));
		worleyCarver = value;
		configuredWorleyCarver = Registry.register(WorldGenRegistries.CONFIGURED_CARVER, new ResourceLocation(Reference.MOD_ID, "worley_cave"), worleyCarver.configured(new ProbabilityConfig(1)));
	}
	
	public void registerListeners(IEventBus fmlBus, IEventBus forgeBus) 
	{
		fmlBus.addListener(this::configChanged);

        forgeBus.addListener(this::biomeSetup);
        forgeBus.addListener(this::worldLoad);
        forgeBus.addListener(this::worldCreateSpawn);
        forgeBus.addListener(this::worldUnload);
		fmlBus.addGenericListener(WorldCarver.class, this::registerCarvers);
	}
	
	public void biomeSetup(BiomeLoadingEvent event)
	{

			// Exclude Nether and End biomes
			if (event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND || event.getCategory() == Biome.Category.NONE)
				return;

			//Remove vanilla cave carver
			List<Supplier<ConfiguredCarver<?>>> carversAir = event.getGeneration().getCarvers(GenerationStage.Carving.AIR);
			carversAir.removeIf(carver -> carver.get().worldCarver instanceof CaveWorldCarver);

			//Remove vanilla underwater cave carver
			List<Supplier<ConfiguredCarver<?>>> carversLiquid = event.getGeneration().getCarvers(GenerationStage.Carving.LIQUID);
			carversLiquid.removeIf(carver -> {
				final WorldCarver<?> worldCarver = carver.get().worldCarver;
				return worldCarver instanceof UnderwaterCaveWorldCarver || worldCarver instanceof UnderwaterCanyonWorldCarver;
			});

			event.getGeneration().getCarvers(GenerationStage.Carving.AIR).add(() -> configuredWorleyCarver);
//				b.getCarvers(GenerationStage.Carving.LIQUID).add(configuredWorleyCarver); //There is no underwater carver for Worley's

	}
	
	public void configChanged(ModConfig.ModConfigEvent event)
	{
		final ModConfig config = event.getConfig();
		// Rebake the configs when they change
		if (config.getSpec() == ConfigHolder.CLIENT_SPEC)
		{
			ConfigHelper.bakeClient(config);
		} else if (config.getSpec() == ConfigHolder.SERVER_SPEC)
		{
			ConfigHelper.bakeServer(config);
		}
	}
	
	public void worldLoad(WorldEvent.Load event)
	{
		setWorldSeed(event);
	}
	
	public void worldCreateSpawn(WorldEvent.CreateSpawnPosition event)
	{
		setWorldSeed(event);
	}
	
	public void worldUnload(WorldEvent.Unload event) 
	{
		//if player quits world make sure we reset seed
		DimensionType dimension = event.getWorld().dimensionType();
		if(dimension.hasSkyLight())
		{
			seedsSet = false;
			worldSeed = 0;
		}
	}
	
	public void setWorldSeed(WorldEvent event)
	{
		if(seedsSet)
			return;
		if(event.getWorld().isClientSide())
			return;
		
		// There's probably a better (and prettier) way to get the seed.
		// So far, this is the only way that I've found out.
		long seed = ((IServerWorld)event.getWorld()).getLevel().getSeed();
		if(seed != 0) 
		{
			worldSeed = seed;
			seedsSet = true;
		}

		worleyCarver.init(worldSeed);
	}

}

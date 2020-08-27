package fluke.worleycaves.proxy;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import fluke.worleycaves.config.ConfigHelper;
import fluke.worleycaves.config.ConfigHolder;
import fluke.worleycaves.world.WorldCarverWorley;
import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.UnderwaterCaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

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


		worleyCarver = new WorldCarverWorley(ProbabilityConfig.field_236576_b_, 256);
		configuredWorleyCarver = Biome.createCarver(worleyCarver, new ProbabilityConfig(1));
		
		registerListeners(fmlBus, forgeBus);
	}
	
	public void registerListeners(IEventBus fmlBus, IEventBus forgeBus) 
	{
		fmlBus.addListener(this::configChanged);
        fmlBus.addListener(this::commonSetup);

        forgeBus.addListener(this::worldLoad);
        forgeBus.addListener(this::worldCreateSpawn);
        forgeBus.addListener(this::worldUnload);
    }
	
	public void commonSetup(FMLCommonSetupEvent event)
	{
		ForgeRegistries.BIOMES.forEach(new Consumer<Biome>()
		{
			@Override
			public void accept(Biome b)
			{
				// Exclude Nether and End biomes
				if (b.getCategory() == Biome.Category.NETHER || b.getCategory() == Biome.Category.THEEND || b.getCategory() == Biome.Category.NONE)
					return;
				
				//Remove vanilla cave carver
				List<ConfiguredCarver<?>> carversAir = b.getCarvers(GenerationStage.Carving.AIR);
				Iterator<ConfiguredCarver<?>> iteratorCarversAir = carversAir.iterator();
				while(iteratorCarversAir.hasNext())
				{
					ConfiguredCarver<?> carver = iteratorCarversAir.next();
					if(carver.carver instanceof CaveWorldCarver)
					{
						iteratorCarversAir.remove();
					}
				}
				
				//Remove vanilla underwater cave carver
				List<ConfiguredCarver<?>> carversLiquid = b.getCarvers(GenerationStage.Carving.LIQUID);
				Iterator<ConfiguredCarver<?>> iteratorCarversLiquid = carversLiquid.iterator();
				while(iteratorCarversLiquid.hasNext())
				{
					ConfiguredCarver<?> carver = iteratorCarversLiquid.next();
					if(carver.carver instanceof UnderwaterCaveWorldCarver)
					{
						iteratorCarversLiquid.remove();
					}
				}
				
				b.getCarvers(GenerationStage.Carving.AIR).add(configuredWorleyCarver);
//				b.getCarvers(GenerationStage.Carving.LIQUID).add(configuredWorleyCarver); //There is no underwater carver for Worley's
			}
		});
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
		DimensionType dimension = event.getWorld().getWorld().func_230315_m_();
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
		if(event.getWorld().isRemote())
			return;
		
		// There's probably a better (and prettier) way to get the seed.
		// So far, this is the only way that I've found out.
		long seed = event.getWorld().getWorld().getServer().func_240793_aU_().func_230418_z_().func_236221_b_();
		if(seed != 0) 
		{
			worldSeed = seed;
			seedsSet = true;
		}

		worleyCarver.init(worldSeed);
	}

}

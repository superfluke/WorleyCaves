package fluke.worleycaves;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fluke.worleycaves.config.ConfigHolder;
import fluke.worleycaves.proxy.ClientProxy;
import fluke.worleycaves.proxy.CommonProxy;
import fluke.worleycaves.util.Reference;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Reference.MOD_ID)
public class Main
{
	public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
	public static CommonProxy proxy;

	public Main()
	{
		LOGGER.info("WorleyCaves entry point");
		
		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigHolder.SERVER_SPEC);

		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::cavesReplace);
		proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		proxy.start();
	}

}

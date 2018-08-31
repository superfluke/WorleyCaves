package fluke.worleycaves.proxy;

import fluke.worleycaves.compat.MystcraftCompat;
import fluke.worleycaves.event.CaveEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event)
    {
		MinecraftForge.TERRAIN_GEN_BUS.register(new CaveEvent());
//		if (Loader.isModLoaded("mystcraft"))
//		{
//			MystcraftCompat.doMystcraftIntegration();
//		}
    }
	
	public void init(FMLInitializationEvent event)
	{
		if (Loader.isModLoaded("mystcraft"))
		{
			MystcraftCompat.doMystcraftIntegration();
		}
	}
}

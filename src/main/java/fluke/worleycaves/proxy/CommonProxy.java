package fluke.worleycaves.proxy;

import fluke.worleycaves.event.CaveEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event)
    {
		MinecraftForge.TERRAIN_GEN_BUS.register(new CaveEvent());
    }
}

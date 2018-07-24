package fluke.worleycaves.proxy;

import fluke.worleycaves.event.CaveEvent;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event)
    {
		MinecraftForge.TERRAIN_GEN_BUS.register(new CaveEvent());
    }
	
	public void init(FMLInitializationEvent event) 
	{
		
	}

}

package fluke.worleycaves.event;

import fluke.worleycaves.Main;
import fluke.worleycaves.world.WorleyCaveGenerator;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CaveEvent 
{
	@SubscribeEvent
    public void onCaveEvent(InitMapGenEvent event) 
	{
		if (event.getType() == InitMapGenEvent.EventType.CAVE)
	    {
			Main.LOGGER.info("Replacing cave generation with Worley Caves");
	        event.setNewGen(new WorleyCaveGenerator());
	    }
	}

}

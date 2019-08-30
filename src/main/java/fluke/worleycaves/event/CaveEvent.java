package fluke.worleycaves.event;

import fluke.worleycaves.world.WorleyCaveGenerator;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CaveEvent 
{
	@SubscribeEvent(priority = EventPriority.LOWEST) 
    public void onCaveEvent(InitMapGenEvent event) 
	{
		//only replace cave gen if the original gen passed isn't a worley cave
		if (event.getType() == InitMapGenEvent.EventType.CAVE && !event.getOriginalGen().getClass().equals(WorleyCaveGenerator.class))
	    {
			//Main.LOGGER.info("Replacing cave generation with Worley Caves");
	        event.setNewGen(new WorleyCaveGenerator());
	    }
	}

}

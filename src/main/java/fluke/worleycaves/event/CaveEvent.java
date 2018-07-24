package fluke.worleycaves.event;

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
	        System.out.println("Imma cave gen now");
	        event.setNewGen(new WorleyCaveGenerator());
	    }
	}

}

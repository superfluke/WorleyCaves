package fluke.worleycaves.compat;

import com.xcompwiz.mystcraft.api.APIInstanceProvider;
import com.xcompwiz.mystcraft.api.MystObjects;
import com.xcompwiz.mystcraft.api.exception.APIUndefined;
import com.xcompwiz.mystcraft.api.exception.APIVersionRemoved;
import com.xcompwiz.mystcraft.api.exception.APIVersionUndefined;
import com.xcompwiz.mystcraft.api.hook.SymbolAPI;
import com.xcompwiz.mystcraft.api.hook.SymbolFactory;
import com.xcompwiz.mystcraft.api.hook.SymbolFactory.CategoryPair;
import com.xcompwiz.mystcraft.api.impl.InternalAPI;
import com.xcompwiz.mystcraft.api.symbol.BlockCategory;
import com.xcompwiz.mystcraft.api.symbol.IAgeSymbol;

import fluke.worleycaves.Main;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class MystcraftCompat 
{
	public static SymbolAPI  symbolApi;
	public static SymbolFactory  symbolFactory;
	public static IAgeSymbol worleysSymbol;
	
	public static void doMystcraftIntegration() 
	{

		APIInstanceProvider provider = MystObjects.entryPoint.getProviderInstance();
 		if (provider == null)
			return;
 		
		try 
		{
			Main.LOGGER.info("Trying to play nice with mystcraft API");
			//Object apiinst = provider.getAPIInstance("awesomeAPI-3");
			//useAPI(apiinst); //At this point, we've got an object of the right interface.
			symbolApi = (SymbolAPI)provider.getAPIInstance("symbol-1");
			symbolFactory = (SymbolFactory)provider.getAPIInstance("symbolfact-1");
		} catch(APIVersionRemoved e1) {
			Main.LOGGER.error("API version removed!");
        } catch(APIVersionUndefined e2) {
        	Main.LOGGER.error("API version undefined!");
        } catch(APIUndefined e3) {
            Main.LOGGER.error("API undefined!");
        }
		
		worleysSymbol = symbolFactory.createSymbol(Blocks.OBSIDIAN.getDefaultState(), "Terrain", 2, new CategoryPair(BlockCategory.TERRAIN, 4));
	}
}

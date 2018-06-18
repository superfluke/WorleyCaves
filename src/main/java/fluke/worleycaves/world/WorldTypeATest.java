package fluke.worleycaves.world;

import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldTypeATest extends WorldType
{
	
	public WorldTypeATest()
	{
		super("atest");
	}
	
	@Override
	public IChunkGenerator getChunkGenerator(World world, String genOptions)
	{
		return new ChunkGeneratorOverworldCustom(world);
	}
	
	@Override
    public boolean isCustomizable()
    {
        return false;
    }
	
	public net.minecraft.world.biome.BiomeProvider getBiomeProvider(World world)
    {
		return new net.minecraft.world.biome.BiomeProvider(world.getWorldInfo());
    }
}

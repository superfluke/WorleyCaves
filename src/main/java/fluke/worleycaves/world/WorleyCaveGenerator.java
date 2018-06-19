package fluke.worleycaves.world;

import fluke.worleycaves.util.FastNoise;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class WorleyCaveGenerator extends MapGenBase
{
	public FastNoise cellnoise = new FastNoise();
	public FastNoise perlin = new FastNoise();
	
	public WorleyCaveGenerator()
	{
		cellnoise.SetNoiseType(FastNoise.NoiseType.Cellular);
		cellnoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		//perlin.SetNoiseType(FastNoise.CellularDistanceFunction.)
	}
	
	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		if (chunkX == originalX && chunkZ == originalZ)
		{
			
			int startHeight = 1;
			int maxHeight = 90;
			float worleyscale = 1.0F;
			float worleyamp = 0.9F;
			int perlinscale = 20;
			float perlinamp = 0.6F;
			double cutoff = -0.35;
			for (int x=0; x<16; x++)
			{
				int realX = x + chunkX*16;
				for (int z=0; z<16; z++)
				{
					int realZ = z + chunkZ*16;
					for(int y=startHeight; y<maxHeight; y++)
						{
							float cellvalue = cellnoise.GetNoise((float) realX*worleyscale,  (float) y*worleyscale, (float) realZ*worleyscale) * worleyamp;
							float perlinvalue = ((perlin.GetNoise((float) realX*perlinscale,  (float) y*perlinscale, (float) realZ*perlinscale)+1)/2) * perlinamp;
							float noise = cellvalue - perlinvalue;
							if ( noise > cutoff)
							{
								chunkPrimerIn.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
							}
						}
				}
			}

		}
    }

}
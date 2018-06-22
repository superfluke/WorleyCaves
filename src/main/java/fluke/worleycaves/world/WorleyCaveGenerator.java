package fluke.worleycaves.world;

import fluke.worleycaves.util.FastNoise;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class WorleyCaveGenerator extends MapGenBase
{
	public FastNoise cellnoise = new FastNoise();
	public FastNoise cellnoise2 = new FastNoise();
	public FastNoise perlin = new FastNoise();
	
	public WorleyCaveGenerator()
	{
		cellnoise.SetNoiseType(FastNoise.NoiseType.Cellular);
		cellnoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
		cellnoise2.SetNoiseType(FastNoise.NoiseType.Cellular);
		cellnoise2.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
		cellnoise2.SetSeed(8008135);
		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		//perlin.SetNoiseType(FastNoise.CellularDistanceFunction.)
	}
	
	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		if (chunkX == originalX && chunkZ == originalZ)
		{

			

			int startHeight = 1;
			int maxHeight = 90;
			float worleyoffset = 20.0F;
			float worleyscale = 2.0F;
			float worleyamp = 1.0F;
			float perlinscale = 4;
			float perlinamp = 1.2F;
			double cutoff = -0.3;
			for (int x=0; x<16; x++)
			{
				int realX = x + chunkX*16;
				for (int z=0; z<16; z++)
				{
					int realZ = z + chunkZ*16;
					for(int y=startHeight; y<maxHeight; y++)
					{
//						float cellvalue = (cellnoise.GetNoise((float) realX*worleyscale,  (float) y*worleyscale, (float) realZ*worleyscale) * worleyamp)+1;
//						float perlinvalue = ((perlin.GetNoise((float) realX*perlinscale,  (float) y*perlinscale, (float) realZ*perlinscale)+1)/2) * perlinamp;
//						//float noise = Math.min(cellvalue, perlinvalue);
//						//float noise = (cellvalue + perlinvalue)/2;
//						float noise = cellvalue - perlinvalue;
						
						float worley1 = cellnoise.GetNoise(realX*worleyscale, y*worleyscale, realZ*worleyscale);

						int x2 = (int) (realX + worleyoffset * 0.5f);
						int y2 = (int) (y + worleyoffset * 0.5f);
						int z2 = (int) (realZ + worleyoffset * 0.5f);

					   float worley2 = cellnoise2.GetNoise(x2*worleyscale*2.2f, y2*worleyscale*2.2f, z2*worleyscale*2.2f);
					   float noise = Math.min(worley1, worley2);
						//System.out.println(noise);
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
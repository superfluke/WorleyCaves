package fluke.worleycaves.world;

import fluke.worleycaves.util.FastNoise;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class WorleyCaveGenerator extends MapGenBase
{
	public FastNoise cellnoise = new FastNoise();
	
	public WorleyCaveGenerator()
	{
		cellnoise.SetNoiseType(FastNoise.NoiseType.Cellular);
		cellnoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance2Div);
	}
	
	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		if (chunkX == originalX && chunkZ == originalZ)
		{

			int startHeight = 1;
			int maxHeight = 90;
			float amp = (float) 1.5;
			double cutoff = -0.25;
			for (int x=0; x<16; x++)
			{
				int realX = x + chunkX*16;
				for (int z=0; z<16; z++)
				{
					int realZ = z + chunkZ*16;
					for(int y=startHeight; y<maxHeight; y++)
						{
							//System.out.println(cellnoise.GetNoise((float) realX,  (float) y, (float) realZ));
							if (cellnoise.GetNoise((float) realX*amp,  (float) y*amp, (float) realZ*amp) > cutoff)
							{
								chunkPrimerIn.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
							}
						}
				}
			}

		}
    }

}
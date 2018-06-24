package fluke.worleycaves.world;

import fluke.worleycaves.util.FastNoise;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class WorleyCaveGenerator extends MapGenBase
{
	private FastNoise cellnoiseF1divF3 = new FastNoise();
//	private FastNoise perlin = new FastNoise();
	
	private int startHeight = 1;
	private int maxHeight = 90;
//	private float perlinscale = 4;
//	private float perlinamp = 1.2F;
	private float cutoff = -0.18F;
	
	public WorleyCaveGenerator()
	{
		cellnoiseF1divF3.SetNoiseType(FastNoise.NoiseType.Cellular);
		cellnoiseF1divF3.SetFrequency(0.02f);
		cellnoiseF1divF3.SetCellularReturnType(FastNoise.CellularReturnType.Distance1Div3);
		
//		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
//		perlin.SetNoiseType(FastNoise.CellularDistanceFunction.)
	}
	
	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		long millis = System.currentTimeMillis();
		
		if (chunkX == originalX && chunkZ == originalZ)
		{
			for (int x=0; x<16; x++)
			{
				int realX = x + chunkX*16;
				for (int z=0; z<16; z++)
				{
					int realZ = z + chunkZ*16;
					for(int y=startHeight; y<maxHeight; y++)
					{
						float noise = cellnoiseF1divF3.SingleCellular3Edge(realX, y, realZ);
						if (noise > cutoff)
						{
							chunkPrimerIn.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
		}
		
		System.out.println("Current cave gen time [ms]: " + (System.currentTimeMillis() - millis));
    }

}
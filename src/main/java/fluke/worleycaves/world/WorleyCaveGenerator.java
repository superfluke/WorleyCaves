package fluke.worleycaves.world;

import fluke.worleycaves.util.FastNoise;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class WorleyCaveGenerator extends MapGenBase
{
	private FastNoise worleyF1divF3 = new FastNoise();
	private FastNoise perlin = new FastNoise();
	
	private int startHeight = 1;
	private int maxHeight = 90;
	private float cutoff = -0.18F;
	
	public WorleyCaveGenerator()
	{
		worleyF1divF3.SetNoiseType(FastNoise.NoiseType.Cellular);
		worleyF1divF3.SetFrequency(0.02f);
		worleyF1divF3.SetCellularReturnType(FastNoise.CellularReturnType.Distance1Div3);
		worleyF1divF3.SetCellularJitter(0.3f);
		
		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin.SetFrequency(0.05f);
	}
	
	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		//Values accessible for hot-swapping in debug mode
		//TODO move good values to constructor
		cutoff = -0.92f;
		worleyF1divF3.SetCellularJitter(1.0f);
		worleyF1divF3.SetCellularReturnType(FastNoise.CellularReturnType.Distance3Sub1);
		worleyF1divF3.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Euclidean);
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
						float dispAmp = 15.0f;
						
						//Experiment making the cave system more chaotic the more you descend 
//						float dispAmp = 0.3f * (maxHeight-y);
						
						float xDisp = perlin.GetNoise(realX, y, realZ)*dispAmp;
						float yDisp = perlin.GetNoise(realX, y-256.0f, realZ)*dispAmp;
						float zDisp = perlin.GetNoise(realX, y-512.0f, realZ)*dispAmp;
						
						//Different experiment for displacement
//						float dispAmpNoise = perlin.GetNoise(realX, y, realZ) * dispAmp;
//						float dispDirNoise = perlin.GetNoise(realX, y+256.0f, realZ);
//						float xDisp = (float)Math.sin(dispDirNoise) * dispAmpNoise;
//						float yDisp = 0.0f;
//						float zDisp = (float)Math.cos(dispDirNoise) * dispAmpNoise;
						
						//Multiplying doubling the y frequency to get some more caves
						float noise = worleyF1divF3.SingleCellular3Edge(realX+xDisp, y*2.0f+yDisp, realZ+zDisp);
						
						if (noise < cutoff)
						{
							//Diggy diggy hole
							chunkPrimerIn.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
							
							//Give some headroom for the player
							chunkPrimerIn.setBlockState(x, y+1, z, Blocks.AIR.getDefaultState());
							chunkPrimerIn.setBlockState(x, y+2, z, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
		}
		
		System.out.println("Current cave gen time [ms]: " + (System.currentTimeMillis() - millis));
    }

}
package fluke.worleycaves.world;

import fluke.worleycaves.util.FastNoise;
import fluke.worleycaves.util.WorleyUtil;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class WorleyCaveGenerator extends MapGenBase
{
	long[] genTime = new long[300];
	int currentTimeIndex = 0;
	
//	private FastNoise worleyF1divF3 = new FastNoise();
	private WorleyUtil worleyF1divF3 = new WorleyUtil();
	private FastNoise perlin = new FastNoise();
	private FastNoise perlin2 = new FastNoise();
	
	private int startHeight = 1;
	private int maxHeight = 90;
	private float cutoff = -0.18F;
	
	
	
	public WorleyCaveGenerator()
	{
//		worleyF1divF3.SetNoiseType(FastNoise.NoiseType.Cellular);
//		worleyF1divF3.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Euclidean);
//		worleyF1divF3.SetCellularReturnType(FastNoise.CellularReturnType.Distance1Div3);
		worleyF1divF3.SetFrequency(0.016f);
		
		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin.SetFrequency(0.05f);
		
		perlin2.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin2.SetFrequency(0.05f);
		
	}
	
	@Override
	public void generate(World worldIn, int x, int z, ChunkPrimer primer)
	{
		System.out.println("Generate worley caves");
		long millis = System.currentTimeMillis();

		this.world = worldIn;
		this.recursiveGenerate(worldIn, x, z, x, z, primer);

		genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
		System.out.println("Current cave gen time [ms]: " + genTime[currentTimeIndex]);
		currentTimeIndex++;
		if (currentTimeIndex == genTime.length)
		{
			currentTimeIndex = 0;
		}
	}

	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		//Values accessible for hot-swapping in debug mode
		//TODO move good values to constructor
//		cutoff = -0.20f;
		
		perlin2.SetFrequency(0.1f);
//		worleyF1divF3.SetFrequency(0.016f);
		
		for (int x=0; x<16; x++)
		{
			int realX = x + chunkX*16;
			for (int z=0; z<16; z++)
			{
				int realZ = z + chunkZ*16;
				for(int y = startHeight; y < maxHeight; y++)
				{
					float funAdjuster = (2 * perlin.GetNoise(realX-2, y+256.0f, realZ/2))/10;
					cutoff = -0.18f + funAdjuster;
//					cutoff = -0.18f;
					float dispAmp = 8.0f;
					
					//Experiment making the cave system more chaotic the more you descend 
					dispAmp *= ((maxHeight-y)/(maxHeight*0.7));
//					
					float xDisp = 0f;
					float yDisp = 0f;
					float zDisp = 0f;
					
					xDisp = perlin.GetNoise(realX, y, realZ)*dispAmp;
					yDisp = perlin.GetNoise(realX, y-256.0f, realZ)*dispAmp;
					zDisp = perlin.GetNoise(realX, y-512.0f, realZ)*dispAmp;
					
					//Multiplying doubling the y frequency to get some more caves
					float noise = worleyF1divF3.SingleCellular3Edge(realX+xDisp, y*2.0f+yDisp, realZ+zDisp);
					
					if (noise > cutoff)
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
}
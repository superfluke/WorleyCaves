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

	private WorleyUtil worleyF1divF3 = new WorleyUtil();
	private FastNoise perlin = new FastNoise();
	private FastNoise perlin2 = new FastNoise();
	
	private int startHeight = 1;
	private int maxHeight = 90;
	private float cutoff = -0.18F;
	
	
	public WorleyCaveGenerator()
	{	
		worleyF1divF3.SetFrequency(0.016f);
		
		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin.SetFrequency(0.05f);
		
		perlin2.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin2.SetFrequency(0.05f);
		
	}
	
	@Override
	public void generate(World worldIn, int x, int z, ChunkPrimer primer)
	{
//		System.out.println("Generate worley caves");
		long millis = System.currentTimeMillis();

		this.world = worldIn;
		this.recursiveGenerate(worldIn, x, z, x, z, primer);

		genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
		System.out.println("chunk " + currentTimeIndex + ":" + genTime[currentTimeIndex]);
		currentTimeIndex++;
		if (currentTimeIndex == genTime.length)
		{
			currentTimeIndex = 0;
		}
	}

	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		float[][][] samples = sampleNoise(chunkX, chunkZ);
		float oneEighth = 0.125F;
        float oneQuarter = 0.25F;

		//Values accessible for hot-swapping in debug mode
		//TODO move good values to constructor
//		cutoff = -0.20f;
		
		perlin2.SetFrequency(0.1f);

		//each chunk divided into 4 subchunks along X axis
		for (int x=0; x<4; x++)
		{
			//each chunk divided into 4 subchunks along Z axis
			for (int z=0; z<4; z++)
			{
				//loop through all y points, only generating from 0 to 128 y levels
				for(int y = 0; y < 128; y++)
				{
					//grab the 4 sample points needed from the noise values
					float x0y0z0 = samples[x][y][z];
                    float x0y0z1 = samples[x][y][z+1];
                    float x1y0z0 = samples[x+1][y][z];
                    float x1y0z1 = samples[x+1][y][z+1];
                    
                    //noise values of 4 corners at y=0
                    float noiseStartX0 = x0y0z0;
                    float noiseStartX1 = x0y0z1;
                    float noiseEndX0 = x1y0z0;
                    float noiseEndX1 = x1y0z1;

                	int localY = y;
                    float noiseStartZ = noiseStartX0;
                    float noiseEndZ = noiseStartX1;
                    
                    //how much to increment X values, linear interpolation
                    float noiseStepX0 = (noiseEndX0 - noiseStartX0) * oneQuarter;
                    float noiseStepX1 = (noiseEndX1 - noiseStartX1) * oneQuarter;

                    // loop through 4 blocks of the X subchunk
                    for (int subx = 0; subx < 4; subx++)
                    {
                    	int localX = subx + x*4;
                    	int realX = localX + chunkX*16;
                    	
                    	//how much to increment Z values, linear interpolation
                        float noiseStepZ = (noiseEndZ - noiseStartZ) * oneQuarter;
                        
                        //Y and X already interpolated, just need to interpolate final 4 Z block to get final noise value
                        float noiseVal = noiseStartZ;

                        // loop through 4 blocks of the Z subchunk
                        for (int subz = 0; subz < 4; subz++)
                        {
                        	int localZ = subz + z*4;
                        	int realZ = localZ + chunkZ*16;
                        	float cutoffAdjuster = (2 * perlin.GetNoise(realX-2, localY+256.0f, realZ/2))/10;
            				cutoff = -0.16f + cutoffAdjuster;

        					if (noiseVal > cutoff)
        					{
        						//Diggy diggy hole
        						chunkPrimerIn.setBlockState(localX, localY, localZ, Blocks.AIR.getDefaultState());
        						
        						//Give some headroom for the player
        						chunkPrimerIn.setBlockState(localX, localY+1, localZ, Blocks.AIR.getDefaultState());
        						chunkPrimerIn.setBlockState(localX, localY+2, localZ, Blocks.AIR.getDefaultState());
        					}
                            
                            noiseVal += noiseStepZ;
                        }

                        noiseStartZ += noiseStepX0;
                        noiseEndZ += noiseStepX1;
                    }                    
				}
			}	
		}
    }
	
	public float[][][] sampleNoise(int chunkX, int chunkZ) 
	{	
		float[][][] noiseSamples = new float[5][129][5];
		for (int x=0; x<5; x++)
		{
			int realX = x*4 + chunkX*16;
			for (int z=0; z<5; z++)
			{
				int realZ = z*4 + chunkZ*16;
				for(int y=0; y<129; y++)
				{
					int realY = y;
					float dispAmp = 8.0f;
					
					//Experiment making the cave system more chaotic the more you descend 
					dispAmp *= ((maxHeight-y)/(maxHeight*0.7));
				
					float xDisp = 0f;
					float yDisp = 0f;
					float zDisp = 0f;
					
					xDisp = perlin.GetNoise(realX, realY, realZ)*dispAmp;
					yDisp = perlin.GetNoise(realX, realY-256.0f, realZ)*dispAmp;
					zDisp = perlin.GetNoise(realX, realY-512.0f, realZ)*dispAmp;
					
					//Multiplying doubling the y frequency to get some more caves
					noiseSamples[x][y][z] = worleyF1divF3.SingleCellular3Edge(realX+xDisp, realY*2.0f+yDisp, realZ+zDisp);
				}
			}
		}
		return noiseSamples;
	}
}
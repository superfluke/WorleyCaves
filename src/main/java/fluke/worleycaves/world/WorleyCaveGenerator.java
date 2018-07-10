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
		float[][][] samples = sampleNoise(chunkX, chunkZ);
		float oneEighth = 0.125F;
        float oneQuarter = 0.25F;
        float oneHalf = 0.5F;
		//Values accessible for hot-swapping in debug mode
		//TODO move good values to constructor
//		cutoff = -0.20f;
		
		perlin2.SetFrequency(0.1f);
//		worleyF1divF3.SetFrequency(0.016f);
		//each chunk divided into 4 subchunks along X axis
		for (int x=0; x<4; x++)
		{
			//each chunk divided into 4 subchunks along Z axis
			for (int z=0; z<4; z++)
			{
				//each chunk divided into 32 subchunks along Y axis
				for(int y = 0; y < 64; y++)
				{
					//grab the 8 sample points needed from the noise values
					float x0y0z0 = samples[x][y][z];
                    float x0y0z1 = samples[x][y][z+1];
                    float x1y0z0 = samples[x+1][y][z];
                    float x1y0z1 = samples[x+1][y][z+1];
                    float x0y1z0 = samples[x][y+1][z];
                    float x0y1z1 = samples[x][y+1][z+1];
                    float x1y1z0 = samples[x+1][y+1][z];
                    float x1y1z1 = samples[x+1][y+1][z+1];
                    
                    //how much to increment noise along y value
                    //linear interpolation from start y and end y
                    float noiseStepY00 = (x0y1z0 - x0y0z0) * oneHalf;
                    float noiseStepY01 = (x0y1z1 - x0y0z1) * oneHalf;
                    float noiseStepY10 = (x1y1z0 - x1y0z0) * oneHalf;
                    float noiseStepY11 = (x1y1z1 - x1y0z1) * oneHalf;
                    
                    //noise values of 4 corners at y=0
                    float noiseStartX0 = x0y0z0;
                    float noiseStartX1 = x0y0z1;
                    float noiseEndX0 = x1y0z0;
                    float noiseEndX1 = x1y0z1;
                    
                    // loop through 4 blocks of the Y subchunk
                    for (int suby = 0; suby < 2; suby++)
                    {
                    	int localY = suby + y*2;
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
                            	float funAdjuster = (2 * perlin.GetNoise(realX-2, localY+256.0f, realZ/2))/10;
//            					cutoff = -0.18f + funAdjuster;
                            	cutoff = -0.18f;

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

                        noiseStartX0 += noiseStepY00;
                        noiseStartX1 += noiseStepY01;
                        noiseEndX0 += noiseStepY10;
                        noiseEndX1 += noiseStepY11;
                    }
				}
			}	
		}
		
		/*
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
					float dispAmp = 8.0f;
					
					//Experiment making the cave system more chaotic the more you descend 
					dispAmp *= ((maxHeight-y)/(maxHeight*0.7));
				
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
		*/
    }
	
	public float[][][] sampleNoise(int chunkX, int chunkZ) 
	{
		float[][][] noiseSamples = new float[5][65][5];
		for (int x=0; x<5; x++)
		{
			int realX = x*4 + chunkX*16;
			for (int z=0; z<5; z++)
			{
				int realZ = z*4 + chunkZ*16;
				for(int y=0; y<65; y++)
				{
					int realY = y*2;
					//Multiplying doubling the y frequency to get some more caves
					noiseSamples[x][y][z] = worleyF1divF3.SingleCellular3Edge(realX, realY*2.0f, realZ);
				}
			}
		}
		return noiseSamples;
	}
}
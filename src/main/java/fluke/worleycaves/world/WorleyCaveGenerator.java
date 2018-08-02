package fluke.worleycaves.world;

import com.google.common.base.MoreObjects;

import fluke.worleycaves.config.Configs;
import fluke.worleycaves.util.FastNoise;
import fluke.worleycaves.util.WorleyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

public class WorleyCaveGenerator extends MapGenBase
{
	long[] genTime = new long[300];
	int currentTimeIndex = 0;
	double sum = 0;

	private WorleyUtil worleyF1divF3 = new WorleyUtil();
	private FastNoise perlin = new FastNoise();
	private FastNoise perlin2 = new FastNoise();
	private MapGenCaves vanillaCaveGen;
	
	private int maxHeight = 128;
	
	private static final IBlockState LAVA = Blocks.LAVA.getDefaultState();
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	private static int lavaDepth;
	private static float noiseCutoff;
	private static float warpAmplifier;
	private static float easeInDepth;
	private static float yCompression;
	
	
	public WorleyCaveGenerator()
	{
		worleyF1divF3.SetFrequency(0.016f);
		
		perlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin.SetFrequency(0.05f);
		
		perlin2.SetNoiseType(FastNoise.NoiseType.Perlin);
		perlin2.SetFrequency(0.05f);
		
		lavaDepth = Configs.cavegen.lavaDepth;
		noiseCutoff = (float) Configs.cavegen.noiseCutoffValue;
		warpAmplifier = (float) Configs.cavegen.warpAmplifier;
		easeInDepth = (float) Configs.cavegen.easeInDepth;
		yCompression = (float) Configs.cavegen.verticalCompressionMultiplier;
		
		vanillaCaveGen = new MapGenCaves();
	}
	
	private void debugValueAdjustments()
	{
		//lavaDepth = 10;
		//noiseCutoff = 0.18F;
		//warpAmplifier = 8.0F;
		//easeInDepth = 15;
	}
	
	@Override
	public void generate(World worldIn, int x, int z, ChunkPrimer primer)
	{
		
		int currentDim = worldIn.provider.getDimension();
		//revert to vanilla cave generation for blacklisted dims
		for(int blacklistedDim: Configs.cavegen.blackListedDims)
		{
			if(currentDim == blacklistedDim)
			{
				this.vanillaCaveGen.generate(worldIn, x, z, primer);
				return;
			} 
		}
		
		debugValueAdjustments();
		boolean logTime = false;
		if(logTime)
		{
			long millis = System.currentTimeMillis();
	
			this.world = worldIn;
			this.recursiveGenerate(worldIn, x, z, x, z, primer);
	
			genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
	//		System.out.println("chunk " + currentTimeIndex + ":" + genTime[currentTimeIndex]);
			sum += genTime[currentTimeIndex];
			currentTimeIndex++;
			if (currentTimeIndex == genTime.length)
			{
				System.out.printf("300 chunk average: %.2f ms per chunk\n", sum/300.0);
				sum = 0;
				currentTimeIndex = 0;
			}
		}
	}

	protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn)
    {
		float[][][] samples = sampleNoise(chunkX, chunkZ);
		float oneEighth = 0.125F;
        float oneQuarter = 0.25F;
        float oneHalf = 0.5F;
        float cutoffAdjuster = 0F;
        IBlockState holeFiller;

		
		perlin2.SetFrequency(0.1f);
		//each chunk divided into 4 subchunks along X axis
		for (int x=0; x<4; x++)
		{
			//each chunk divided into 4 subchunks along Z axis
			for (int z=0; z<4; z++)
			{
				int depth = 0;
				//each chunk divided into 64 subchunks along Y axis. Need lots of y sample points to not break things
				for(int y = 63; y >= 0; y--)
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
                    float noiseStepY00 = (x0y1z0 - x0y0z0) * -oneHalf;
                    float noiseStepY01 = (x0y1z1 - x0y0z1) * -oneHalf;
                    float noiseStepY10 = (x1y1z0 - x1y0z0) * -oneHalf;
                    float noiseStepY11 = (x1y1z1 - x1y0z1) * -oneHalf;
                    
                    //noise values of 4 corners at y=0
                    float noiseStartX0 = x0y0z0;
                    float noiseStartX1 = x0y0z1;
                    float noiseEndX0 = x1y0z0;
                    float noiseEndX1 = x1y0z1;
                    
                    // loop through 2 blocks of the Y subchunk
                    for (int suby = 1; suby >= 0; suby--)
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
                            	
                            	if(depth == 0)
                            	{
                            		//only checks depth once per 4x4 subchunk
                            		if(subx == 0 && subz == 0)
                            		{
	                            		IBlockState currentBlock = chunkPrimerIn.getBlockState(localX, localY, localZ);
	                            		//use isDigable to skip leaves/wood getting counted as surface
	            						if(isDigable(currentBlock, AIR)) 
	            						{
	            							depth++;
	            						}
                            		}
            						else
            						{
            							continue;
            						}
                            	}
                            	else if(subx == 0 && subz == 0)
                            	{
                            		//already hit surface, simply increment depth counter
                            		depth++;
                            	}
                            	
//                            	if(y == 63)
//                            		cutoffAdjuster = (2*perlin.GetNoise(realX, realZ))/10;
//            					noiseCutoff = -0.18f + cutoffAdjuster;

                            	float adjustedNoiseCutoff = noiseCutoff + cutoffAdjuster;
                            	if(depth < easeInDepth)
                            	{
                            		//higher threshold at surface, normal threshold below easeInDepth
                            		float surfaceNoiseCutoff = noiseCutoff+(Math.abs(noiseCutoff)*0.55F);
                            		adjustedNoiseCutoff = (float) MathHelper.clampedLerp(noiseCutoff, surfaceNoiseCutoff, (easeInDepth-(float)depth)/easeInDepth);

                            	}
                            	
            					if (noiseVal > adjustedNoiseCutoff)
            					{
            						IBlockState currentBlock = chunkPrimerIn.getBlockState(localX, localY, localZ);
            						IBlockState aboveBlock = (IBlockState) MoreObjects.firstNonNull(chunkPrimerIn.getBlockState(localX, localY+1, localZ), Blocks.AIR.getDefaultState());
            						//make sure we only dig earthy blocks and dont dig into the ocean
            						if(isDigable(currentBlock, aboveBlock))
            						{
	            						if(localY <= lavaDepth)
	            						{
	                						holeFiller = LAVA;
	            						}
	            						else
	            						{
	            							holeFiller = AIR;
	            						}
	            						//Diggy diggy hole
	            						chunkPrimerIn.setBlockState(localX, localY, localZ, holeFiller);
            						}
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
    }
	
	public float[][][] sampleNoise(int chunkX, int chunkZ) 
	{
		float[][][] noiseSamples = new float[5][65][5];
		float noise;
		for (int x=0; x<5; x++)
		{
			int realX = x*4 + chunkX*16;
			for (int z=0; z<5; z++)
			{
				int realZ = z*4 + chunkZ*16;
				
				//loop from top down for y values so we can adjust noise above current y later on
				for(int y=64; y>=0; y--)
				{
					int realY = y*2;
					
					//Experiment making the cave system more chaotic the more you descend 
					///TODO might be too dramatic down at lava level
					float dispAmp = (float) (warpAmplifier * ((maxHeight-y)/(maxHeight*0.85)));
					
					float xDisp = 0f;
					float yDisp = 0f;
					float zDisp = 0f;
					
					xDisp = perlin.GetNoise(realX, realY, realZ)*dispAmp;
					yDisp = perlin.GetNoise(realX, realY-256.0f, realZ)*dispAmp;
					zDisp = perlin.GetNoise(realX, realY-512.0f, realZ)*dispAmp;
					
					//doubling the y frequency to get some more caves
					noise = worleyF1divF3.SingleCellular3Edge(realX+xDisp, realY*yCompression+yDisp, realZ+zDisp);
					noiseSamples[x][y][z] = noise;
					
					if (noise > noiseCutoff)
					{
						//if noise is below cutoff, adjust values of neighbors
						//helps prevent caves fracturing during interpolation
						
						if(x > 0)
							noiseSamples[x-1][y][z] = (noise * 0.2f) + (noiseSamples[x-1][y][z] * 0.8f);
						if(z > 0)
							noiseSamples[x][y][z-1] = (noise * 0.2f) + (noiseSamples[x][y][z-1] * 0.8f);
						
						//more heavily adjust y above 'air block' noise values to give players more headroom
						if(y < 64)
						{
							float noiseAbove = noiseSamples[x][y+1][z];
							if(noise > noiseAbove)
								noiseSamples[x][y+1][z] = (noise * 0.8F) + (noiseAbove * 0.2F);
							if(y < 63)
							{
								float noiseTwoAbove = noiseSamples[x][y+2][z];
								if(noise > noiseTwoAbove)
									noiseSamples[x][y+2][z] = (noise * 0.35F) + (noiseTwoAbove * 0.65F);
							}
						}
						
					}
				}
			}
		}
		return noiseSamples;
	}
	
	private boolean isDigable(IBlockState block, IBlockState blockAbove) 
	{
		Block blockType = block.getBlock();
		if (blockType == Blocks.STONE)
        {
            return true;
        }
        else if (blockType == Blocks.DIRT)
        {
            return true;
        }
        else if (blockType == Blocks.GRASS)
        {
            return true;
        }
        else if (blockType == Blocks.HARDENED_CLAY)
        {
            return true;
        }
        else if (blockType == Blocks.STAINED_HARDENED_CLAY)
        {
            return true;
        }
        else if (blockType == Blocks.SANDSTONE)
        {
            return true;
        }
        else if (blockType == Blocks.RED_SANDSTONE)
        {
            return true;
        }
        else if (blockType == Blocks.MYCELIUM)
        {
            return true;
        }
        else if (blockType == Blocks.SNOW_LAYER)
        {
            return true;
        }
		
		// Check if not under ocean or river
	    if ((blockType == Blocks.SAND || blockType == Blocks.GRAVEL) && blockAbove.getMaterial() != Material.WATER) 
	    {
	      return true;
	    }
	    
	    return false;
	}
}

package fluke.worleycaves.world;

import com.google.common.base.MoreObjects;

import fluke.worleycaves.config.Configs;
import fluke.worleycaves.util.FastNoise;
import fluke.worleycaves.util.WorleyUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

public class WorleyCaveGenerator extends MapGenCaves
{
	long[] genTime = new long[300];
	int currentTimeIndex = 0;
	double sum = 0;

	private WorleyUtil worleyF1divF3 = new WorleyUtil();
	private FastNoise displacementNoisePerlin = new FastNoise();
	private MapGenBase replacementCaves;
	private MapGenBase moddedCaveGen;
	
	
	
	private static final IBlockState LAVA = Blocks.LAVA.getDefaultState();
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	private static int maxCaveHeight;
	private static float noiseCutoff;
	private static float warpAmplifier;
	private static float easeInDepth;
	private static float yCompression;
	private static float xzCompression;
	private static float surfaceCutoff;

	
	
	public WorleyCaveGenerator()
	{
		worleyF1divF3.SetFrequency(0.016f);
		
		displacementNoisePerlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		displacementNoisePerlin.SetFrequency(0.05f);
		
		maxCaveHeight = Configs.cavegen.maxCaveHeight;
		noiseCutoff = (float) Configs.cavegen.noiseCutoffValue;
		warpAmplifier = (float) Configs.cavegen.warpAmplifier;
		easeInDepth = (float) Configs.cavegen.easeInDepth;
		yCompression = (float) Configs.cavegen.verticalCompressionMultiplier;
		xzCompression = (float) Configs.cavegen.horizonalCompressionMultiplier;
		surfaceCutoff = (float) Configs.cavegen.surfaceCutoffValue;
		
		//try and grab other modded cave gens, like swiss cheese caves or Quark big caves
		//our replace cavegen event will ignore cave events when the original cave class passed in is a Worley cave
		moddedCaveGen = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(this, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
		if(moddedCaveGen != this)
			replacementCaves = moddedCaveGen;
		else
			replacementCaves = new MapGenCaves(); //default to vanilla caves if there are no other modded cave gens
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
				this.replacementCaves.generate(worldIn, x, z, primer);
				return;
			} 
		}
		
		debugValueAdjustments();
		boolean logTime = false;
		long millis = 0;
		if(logTime)
		{
			millis = System.currentTimeMillis();
		}
		
		this.world = worldIn;
		this.generateWorleyCaves(worldIn, x, z, primer);
	
		if(logTime)
		{
			genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
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

	protected void generateWorleyCaves(World worldIn, int chunkX, int chunkZ, ChunkPrimer chunkPrimerIn)
    {
		int chunkMaxHeight = getMaxSurfaceHeight(chunkPrimerIn);
		int seaLevel = worldIn.getSeaLevel();
		float[][][] samples = sampleNoise(chunkX, chunkZ, chunkMaxHeight+1);
        float oneQuarter = 0.25F;
        float oneHalf = 0.5F;
        //float cutoffAdjuster = 0F; //TODO one day, perlin adjustments to cutoff
        IBlockState holeFiller;
        
		//each chunk divided into 4 subchunks along X axis
		for (int x = 0; x < 4; x++)
		{
			//each chunk divided into 4 subchunks along Z axis
			for (int z = 0; z < 4; z++)
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
	            						if(canReplaceBlock(currentBlock, AIR)) 
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

                            	float adjustedNoiseCutoff = noiseCutoff;// + cutoffAdjuster;
                            	if(depth < easeInDepth)
                            	{
                            		//higher threshold at surface, normal threshold below easeInDepth
                            		adjustedNoiseCutoff = (float) MathHelper.clampedLerp(noiseCutoff, surfaceCutoff, (easeInDepth-(float)depth)/easeInDepth);

                            	}
                            	
            					if (noiseVal > adjustedNoiseCutoff)
            					{
            						IBlockState aboveBlock = (IBlockState) MoreObjects.firstNonNull(chunkPrimerIn.getBlockState(localX, localY+1, localZ), Blocks.AIR.getDefaultState());
            						if(aboveBlock.getMaterial() != Material.WATER)
            						{
            							//if we are in the easeInDepth range or above sea level, do some extra checks for water before digging
            							if(depth < easeInDepth || localY > (seaLevel - 8)) 
            							{
            								if(localX < 15)
            									if(chunkPrimerIn.getBlockState(localX+1, localY, localZ).getMaterial() == Material.WATER)
            										continue;
            								if(localX > 0)
            									if(chunkPrimerIn.getBlockState(localX-1, localY, localZ).getMaterial() == Material.WATER)
            										continue;
            								if(localZ < 15)
            									if(chunkPrimerIn.getBlockState(localX, localY, localZ+1).getMaterial() == Material.WATER)
            										continue;
            								if(localZ > 0)
            									if(chunkPrimerIn.getBlockState(localX, localY, localZ-1).getMaterial() == Material.WATER)
            										continue;
            							}
	            						IBlockState currentBlock = chunkPrimerIn.getBlockState(localX, localY, localZ);
	            						boolean flag1 = false;
	            						if (isTopBlock(chunkPrimerIn, localX, localY, localZ, chunkX, chunkZ))
	                                    {
	                                        flag1 = true;
	                                    }
	            						digBlock(chunkPrimerIn, localX, localY, localZ, chunkX, chunkZ, flag1, currentBlock, aboveBlock);
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
	
	public float[][][] sampleNoise(int chunkX, int chunkZ, int maxSurfaceHeight) 
	{
		int originalMaxHeight = 128;
		float[][][] noiseSamples = new float[5][65][5];
		float noise;
		for (int x = 0; x < 5; x++)
		{
			int realX = x*4 + chunkX*16;
			for (int z = 0; z < 5; z++)
			{
				int realZ = z*4 + chunkZ*16;
				
				//loop from top down for y values so we can adjust noise above current y later on
				for(int y = 64; y >= 0; y--)
				{
					float realY = y*2;
					if(realY > maxSurfaceHeight || realY > maxCaveHeight)
					{
						noiseSamples[x][y][z] = -1.1F;
					}
					else
					{
						//Experiment making the cave system more chaotic the more you descend 
						///TODO might be too dramatic down at lava level
						float dispAmp = (float) (warpAmplifier * ((originalMaxHeight-y)/(originalMaxHeight*0.85)));
						
						float xDisp = 0f;
						float yDisp = 0f;
						float zDisp = 0f;
						
						xDisp = displacementNoisePerlin.GetNoise(realX, realY, realZ)*dispAmp;
						yDisp = displacementNoisePerlin.GetNoise(realX, realY-256.0f, realZ)*dispAmp;
						zDisp = displacementNoisePerlin.GetNoise(realX, realY-512.0f, realZ)*dispAmp;
						
						//doubling the y frequency to get some more caves
						noise = worleyF1divF3.SingleCellular3Edge(realX*xzCompression+xDisp, realY*yCompression+yDisp, realZ*xzCompression+zDisp);
						noiseSamples[x][y][z] = noise;
						
						if (noise > noiseCutoff)
						{
							//if noise is below cutoff, adjust values of neighbors
							//helps prevent caves fracturing during interpolation
							
							if(x > 0)
								noiseSamples[x-1][y][z] = (noise*0.2f) + (noiseSamples[x-1][y][z]*0.8f);
							if(z > 0)
								noiseSamples[x][y][z-1] = (noise*0.2f) + (noiseSamples[x][y][z-1]*0.8f);
							
							//more heavily adjust y above 'air block' noise values to give players more headroom
							if(y < 64)
							{
								float noiseAbove = noiseSamples[x][y+1][z];
								if(noise > noiseAbove)
									noiseSamples[x][y+1][z] = (noise*0.8F) + (noiseAbove*0.2F);
								if(y < 63)
								{
									float noiseTwoAbove = noiseSamples[x][y+2][z];
									if(noise > noiseTwoAbove)
										noiseSamples[x][y+2][z] = (noise*0.35F) + (noiseTwoAbove*0.65F);
								}
							}
							
						}
					}
				}
			}
		}
		return noiseSamples;
	}
	
	private int getSurfaceHeight(ChunkPrimer chunkPrimerIn, int localX, int localZ) 
	{
		for(int y = maxCaveHeight; y > 0; y--)
		{
			IBlockState currentBlock = chunkPrimerIn.getBlockState(localX, y, localZ);
			if(canReplaceBlock(currentBlock, AIR)) 
			{
				return y;
			}
		}	
		return 0;
	}
	
	//tests 8 edge points and center of chunk to get max height
	private int getMaxSurfaceHeight(ChunkPrimer primer)
	{
		int max = 0;
		int[] testcords = {0, 7, 15};
		
		for(int n = 0; n < testcords.length; n++)
		{
			for(int m = 0; m < testcords.length; m++)
			{
				int testmax = getSurfaceHeight(primer, testcords[n], testcords[m]);
				if(testmax > max)
					max = testmax;
			}
		}
		return max;
	}
	
	//Because it's private in MapGenCaves this is reimplemented
	//Determine if the block at the specified location is the top block for the biome, we take into account
    //Vanilla bugs to make sure that we generate the map the same way vanilla does.
    private boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ)
    {
        net.minecraft.world.biome.Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState state = data.getBlockState(x, y, z);
        return (isExceptionBiome(biome) ? state.getBlock() == Blocks.GRASS : state.getBlock() == biome.topBlock);
    }
    
    //Exception biomes to make sure we generate like vanilla
    private boolean isExceptionBiome(net.minecraft.world.biome.Biome biome)
    {
        if (biome == net.minecraft.init.Biomes.BEACH) return true;
        if (biome == net.minecraft.init.Biomes.DESERT) return true;
        return false;
    }
    
    /**
     * Digs out the current block, default implementation removes stone, filler, and top block
     * Sets the block to lava if y is less then 10, and air other wise.
     * If setting to air, it also checks to see if we've broken the surface and if so
     * tries to make the floor the biome's top block
     *
     * @param data Block data array
     * @param index Pre-calculated index into block data
     * @param x local X position
     * @param y local Y position
     * @param z local Z position
     * @param chunkX Chunk X position
     * @param chunkZ Chunk Y position
     * @param foundTop True if we've encountered the biome's top block. Ideally if we've broken the surface.
     */
    protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState state, IBlockState up)
    {
        net.minecraft.world.biome.Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState top = biome.topBlock;
        IBlockState filler = biome.fillerBlock;

        if (this.canReplaceBlock(state, up) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock())
        {
            if (y - 1 < 10)
            {
                data.setBlockState(x, y, z, BLK_LAVA);
            }
            else
            {
                data.setBlockState(x, y, z, BLK_AIR);

                if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock())
                {
                    data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
                }
                
                //replace floating sand with sandstone
                if(up == Blocks.SAND.getDefaultState())
                {
                	data.setBlockState(x, y+1, z, BLK_SANDSTONE);
                }
                else if(up == Blocks.SAND.getStateFromMeta(1))
                {
                	data.setBlockState(x, y+1, z, BLK_RED_SANDSTONE);
                }
            }
        }
    }
}

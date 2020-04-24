package fluke.worleycaves.world;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.mojang.datafixers.Dynamic;

import fluke.worleycaves.config.WorleyConfig;
import fluke.worleycaves.util.BlockUtil;
import fluke.worleycaves.util.FastNoise;
import fluke.worleycaves.util.WorleyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fluids.IFluidBlock;

public class WorldCarverWorley extends WorldCarver<ProbabilityConfig>
{

	long[] genTime = new long[300];
	int currentTimeIndex = 0;
	double sum = 0;

	private WorleyUtil worleyF1divF3;
	private FastNoise displacementNoisePerlin;

	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private static BlockState lavaBlock;
	private static int maxCaveHeight;
	private static int minCaveHeight;
	private static float noiseCutoff;
	private static float warpAmplifier;
	private static float easeInDepth;
	private static float yCompression;
	private static float xzCompression;
	private static float surfaceCutoff;
	private static int lavaDepth;
	private static boolean additionalWaterChecks = false;

	public WorldCarverWorley(Function<Dynamic<?>, ? extends ProbabilityConfig> p_i49929_1_, int p_i49929_2_)
	{
		super(p_i49929_1_, p_i49929_2_);

	}

	private void debugValueAdjustments()
	{
		// lavaDepth = 10;
		// noiseCutoff = 0.18F;
		// warpAmplifier = 8.0F;
		// easeInDepth = 15;
	}
	
	public void init(long worldSeed)
	{
		worleyF1divF3 = new WorleyUtil((int) worldSeed);
		worleyF1divF3.SetFrequency(0.016f);
		
		displacementNoisePerlin = new FastNoise((int) worldSeed);
		displacementNoisePerlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		displacementNoisePerlin.SetFrequency(0.05f);

		maxCaveHeight = WorleyConfig.maxCaveHeight;
		minCaveHeight = WorleyConfig.minCaveHeight;
		noiseCutoff = (float)WorleyConfig.noiseCutoffValue;
		warpAmplifier = (float)WorleyConfig.warpAmplifier;
		easeInDepth = (float)WorleyConfig.easeInDepth;
		yCompression = (float)WorleyConfig.verticalCompressionMultiplier;
		xzCompression = (float)WorleyConfig.horizonalCompressionMultiplier;
		surfaceCutoff = (float)WorleyConfig.surfaceCutoffValue;
		lavaDepth = WorleyConfig.lavaDepth;
		
		lavaBlock = BlockUtil.getStateFromString(WorleyConfig.lavaBlock, Blocks.LAVA.getDefaultState());

	}
	
	@Override
	public boolean func_225555_a_(IChunk chunkIn, Function<BlockPos, Biome> getBiomeFunction, Random rand, int seaLevel, int chunkXOffset, int chunkZOffset, int chunkX, int chunkZ, BitSet carvingMask, ProbabilityConfig config)
	{
		return carve(chunkIn, getBiomeFunction, rand, seaLevel, chunkX, chunkZ, chunkXOffset, chunkZOffset, carvingMask, config);
	}

	public boolean carve(IChunk chunkIn, Function<BlockPos, Biome> getBiomeFunction, Random rand, int seaLevel, int chunkX, int chunkZ, int chunkXOffset, int chunkZOffset, BitSet carvingMask, ProbabilityConfig config)
	{
		if (chunkXOffset != chunkX || chunkZOffset != chunkZ)
		{
			return false;
		}

		debugValueAdjustments();
		boolean logTime = false;
		long millis = 0;
		if (logTime)
		{
			millis = System.currentTimeMillis();
		}

		this.carveWorleyCaves(chunkIn, getBiomeFunction, seaLevel, chunkX, chunkZ);

		if (logTime)
		{
			genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
			sum += genTime[currentTimeIndex];
			currentTimeIndex++;
			if (currentTimeIndex == genTime.length)
			{
				System.out.printf("300 chunk average: %.2f ms per chunk\n", sum / 300.0);
				sum = 0;
				currentTimeIndex = 0;
			}
		}

		return true;
	}

	protected void carveWorleyCaves(IChunk chunk, Function<BlockPos, Biome> getBiomeFunction, int seaLevel, int chunkX, int chunkZ)
	{
		int chunkMaxHeight = getMaxSurfaceHeight(chunk);
		float[][][] samples = sampleNoise(chunkX, chunkZ, chunkMaxHeight + 1);
		float oneQuarter = 0.25F;
		float oneHalf = 0.5F;
		// float cutoffAdjuster = 0F; //TODO one day, perlin adjustments to cutoff

		// each chunk divided into 4 subchunks along X axis
		for (int x = 0; x < 4; x++)
		{
			// each chunk divided into 4 subchunks along Z axis
			for (int z = 0; z < 4; z++)
			{
				int depth = 0;
				// each chunk divided into 128 subchunks along Y axis. Need lots of y sample points to not break things
				for (int y = (maxCaveHeight / 2) - 1; y >= 0; y--)
				{
					// grab the 8 sample points needed from the noise values
					float x0y0z0 = samples[x][y][z];
					float x0y0z1 = samples[x][y][z + 1];
					float x1y0z0 = samples[x + 1][y][z];
					float x1y0z1 = samples[x + 1][y][z + 1];
					float x0y1z0 = samples[x][y + 1][z];
					float x0y1z1 = samples[x][y + 1][z + 1];
					float x1y1z0 = samples[x + 1][y + 1][z];
					float x1y1z1 = samples[x + 1][y + 1][z + 1];

					// how much to increment noise along y value linear interpolation from start y and end y
					float noiseStepY00 = (x0y1z0 - x0y0z0) * -oneHalf;
					float noiseStepY01 = (x0y1z1 - x0y0z1) * -oneHalf;
					float noiseStepY10 = (x1y1z0 - x1y0z0) * -oneHalf;
					float noiseStepY11 = (x1y1z1 - x1y0z1) * -oneHalf;

					// noise values of 4 corners at y=0
					float noiseStartX0 = x0y0z0;
					float noiseStartX1 = x0y0z1;
					float noiseEndX0 = x1y0z0;
					float noiseEndX1 = x1y0z1;

					// loop through 2 blocks of the Y subchunk
					for (int suby = 1; suby >= 0; suby--)
					{
						int localY = suby + y * 2;
						float noiseStartZ = noiseStartX0;
						float noiseEndZ = noiseStartX1;

						// how much to increment X values, linear interpolation
						float noiseStepX0 = (noiseEndX0 - noiseStartX0) * oneQuarter;
						float noiseStepX1 = (noiseEndX1 - noiseStartX1) * oneQuarter;

						// loop through 4 blocks of the X subchunk
						for (int subx = 0; subx < 4; subx++)
						{
							int localX = subx + x * 4;

							// how much to increment Z values, linear interpolation
							float noiseStepZ = (noiseEndZ - noiseStartZ) * oneQuarter;

							// Y and X already interpolated, just need to interpolate final 4 Z block to get final noise value
							float noiseVal = noiseStartZ;

							// loop through 4 blocks of the Z subchunk
							for (int subz = 0; subz < 4; subz++)
							{
								int localZ = subz + z * 4;

								if (depth == 0)
								{
									// only checks depth once per 4x4 subchunk
									if (subx == 0 && subz == 0)
									{
										BlockState currentBlock = chunk.getBlockState(new BlockPos(localX, localY, localZ));
										// use isDigable to skip leaves/wood getting counted as surface
										if (canReplaceBlock(currentBlock, AIR) || isBiomeBlock(chunk, getBiomeFunction, new BlockPos(localX, localY, localZ)))
										{
											depth++;
										}
									} else
									{
										continue;
									}
								} else if (subx == 0 && subz == 0)
								{
									// already hit surface, simply increment depth counter
									depth++;
								}

								float adjustedNoiseCutoff = noiseCutoff;// + cutoffAdjuster;
								if (depth < easeInDepth)
								{
									// higher threshold at surface, normal threshold below easeInDepth
									adjustedNoiseCutoff = (float) MathHelper.clampedLerp(noiseCutoff, surfaceCutoff, (easeInDepth - (float) depth) / easeInDepth);

								}

								// increase cutoff as we get closer to the minCaveHeight so it's not all flat floors
								if (localY < (minCaveHeight + 5))
								{
									adjustedNoiseCutoff += ((minCaveHeight + 5) - localY) * 0.05;
								}

								if (noiseVal > adjustedNoiseCutoff)
								{
									BlockState aboveBlock = (BlockState) MoreObjects.firstNonNull(chunk.getBlockState(new BlockPos(localX, localY + 1, localZ)), Blocks.AIR.getDefaultState());
									if (!isFluidBlock(aboveBlock) || localY <= lavaDepth)
									{
										// if we are in the easeInDepth range or near sea level, do some extra checks for water before digging
										if ((depth < easeInDepth || localY > (seaLevel - 8) || additionalWaterChecks) && localY > lavaDepth)
										{
											if (localX < 15)
												if (isFluidBlock(chunk.getBlockState(new BlockPos(localX + 1, localY, localZ))))
													continue;
											if (localX > 0)
												if (isFluidBlock(chunk.getBlockState(new BlockPos(localX - 1, localY, localZ))))
													continue;
											if (localZ < 15)
												if (isFluidBlock(chunk.getBlockState(new BlockPos(localX, localY, localZ + 1))))
													continue;
											if (localZ > 0)
												if (isFluidBlock(chunk.getBlockState(new BlockPos(localX, localY, localZ - 1))))
													continue;
										}
										BlockState currentBlock = chunk.getBlockState(new BlockPos(localX, localY, localZ));
										boolean foundTopBlock = false;
										if (isTopBlock(chunk, getBiomeFunction, localX, localY, localZ, chunkX, chunkZ))
										{
											foundTopBlock = true;
										}
										digBlock(chunk, getBiomeFunction, new BlockPos(localX, localY, localZ), chunkX, chunkZ, foundTopBlock, currentBlock, aboveBlock);
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
		float[][][] noiseSamples = new float[5][129][5];
		float noise;
		for (int x = 0; x < 5; x++)
		{
			int realX = x * 4 + chunkX * 16;
			for (int z = 0; z < 5; z++)
			{
				int realZ = z * 4 + chunkZ * 16;

				// loop from top down for y values so we can adjust noise above current y later on
				for (int y = 128; y >= 0; y--)
				{
					float realY = y * 2;
					if (realY > maxSurfaceHeight || realY > maxCaveHeight || realY < minCaveHeight)
					{
						// if outside of valid cave range set noise value below normal minimum of -1.0
						noiseSamples[x][y][z] = -1.1F;
					} else
					{
						// Experiment making the cave system more chaotic the more you descend
						float dispAmp = (float) (warpAmplifier * ((originalMaxHeight - y) / (originalMaxHeight * 0.85)));

						float xDisp = 0f;
						float yDisp = 0f;
						float zDisp = 0f;

						xDisp = displacementNoisePerlin.GetNoise(realX, realY, realZ) * dispAmp;
						yDisp = displacementNoisePerlin.GetNoise(realX, realY - 256.0f, realZ) * dispAmp;
						zDisp = displacementNoisePerlin.GetNoise(realX, realY - 512.0f, realZ) * dispAmp;

						// doubling the y frequency to get some more caves
						noise = worleyF1divF3.SingleCellular3Edge(realX * xzCompression + xDisp, realY * yCompression + yDisp, realZ * xzCompression + zDisp);
						noiseSamples[x][y][z] = noise;

						if (noise > noiseCutoff)
						{
							// if noise is below cutoff, adjust values of neighbors helps prevent caves fracturing during interpolation
							if (x > 0)
								noiseSamples[x - 1][y][z] = (noise * 0.2f) + (noiseSamples[x - 1][y][z] * 0.8f);
							if (z > 0)
								noiseSamples[x][y][z - 1] = (noise * 0.2f) + (noiseSamples[x][y][z - 1] * 0.8f);

							// more heavily adjust y above 'air block' noise values to give players more head room
							if (y < 128)
							{
								float noiseAbove = noiseSamples[x][y + 1][z];
								if (noise > noiseAbove)
									noiseSamples[x][y + 1][z] = (noise * 0.8F) + (noiseAbove * 0.2F);
								if (y < 127)
								{
									float noiseTwoAbove = noiseSamples[x][y + 2][z];
									if (noise > noiseTwoAbove)
										noiseSamples[x][y + 2][z] = (noise * 0.35F) + (noiseTwoAbove * 0.65F);
								}
							}

						}
					}
				}
			}
		}
		return noiseSamples;
	}

	private int getSurfaceHeight(IChunk chunk, int localX, int localZ)
	{
		// Using a recursive binary search to find the surface
		return recursiveBinarySurfaceSearch(chunk, localX, localZ, 255, 0);
	}

	// Recursive binary search, this search always converges on the surface in 8 in cycles for the range 255 >= y >= 0
	private int recursiveBinarySurfaceSearch(IChunk chunk, int localX, int localZ, int searchTop, int searchBottom)
	{
		int top = searchTop;
		if (searchTop > searchBottom)
		{
			int searchMid = (searchBottom + searchTop) / 2;
			if (canReplaceBlock(chunk.getBlockState(new BlockPos(localX, searchMid, localZ)), AIR))
			{
				top = recursiveBinarySurfaceSearch(chunk, localX, localZ, searchTop, searchMid + 1);
			} else
			{
				top = recursiveBinarySurfaceSearch(chunk, localX, localZ, searchMid, searchBottom);
			}
		}
		return top;
	}

	// tests 8 edge points and center of chunk to get max height
	private int getMaxSurfaceHeight(IChunk chunk)
	{
		int max = 0;
		int[] testcords = { 0, 7, 15 };

		for (int n = 0; n < testcords.length; n++)
		{
			for (int m = 0; m < testcords.length; m++)
			{
				int testmax = getSurfaceHeight(chunk, testcords[n], testcords[m]);
				if (testmax > max)
					max = testmax;
			}
		}
		return max;
	}

	// returns true if block matches the top or filler block of the location biome
	private boolean isBiomeBlock(IChunk chunk, Function<BlockPos, Biome> getBiomeFunction, BlockPos blockPos)
	{
		Biome biome = getBiomeFunction.apply(blockPos);
//		Biome biome = chunk.getBiome(blockPos);
		BlockState blockState = chunk.getBlockState(blockPos);
		return blockState == biome.getSurfaceBuilderConfig().getTop() || blockState == biome.getSurfaceBuilderConfig().getUnder();
	}

	// returns true if block is fluid, trying to play nice with modded liquid
	private boolean isFluidBlock(BlockState state)
	{
		Block blocky = state.getBlock();
		return blocky instanceof IFluidBlock;
	}

	// Because it's private in MapGenCaves this is reimplemented
	// Determine if the block at the specified location is the top block for the biome, we take into account
	private boolean isTopBlock(IChunk chunk, Function<BlockPos, Biome> getBiomeFunction, int x, int y, int z, int chunkX, int chunkZ)
	{
		Biome biome = getBiomeFunction.apply(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
//		Biome biome = chunk.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
		BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
		return (isExceptionBiome(biome) ? state.getBlock() == Blocks.GRASS
				: state == biome.getSurfaceBuilderConfig().getTop());
	}

	// Exception biomes to make sure we generate like vanilla
	private boolean isExceptionBiome(Biome biome)
	{
		if (biome == Biomes.BEACH)
			return true;
		if (biome == Biomes.DESERT)
			return true;
		return false;
	}

	@Override
	public boolean shouldCarve(Random rand, int chunkX, int chunkZ, ProbabilityConfig config)
	{
		return true;
	}

	@Override
	protected boolean func_222708_a(double p_222708_1_, double p_222708_3_, double p_222708_5_, int p_222708_7_)
	{
		return false;
	}

	private boolean canReplaceBlock(BlockState state, BlockState stateUp)
	{
		// Replace anything that's made of rock which should hopefully work for most modded type stones (and maybe not break everything)
		return state.getMaterial() == Material.ROCK || super.canCarveBlock(state, stateUp);
	}

	/**
	 * Digs out the current block, default implementation removes stone, filler, and
	 * top block Sets the block to lava if y is less then 10, and air other wise. If
	 * setting to air, it also checks to see if we've broken the surface and if so
	 * tries to make the floor the biome's top block
	 *
	 * @param chunk    Block data array
	 * @param index    Pre-calculated index into block data
	 * @param x        local X position
	 * @param y        local Y position
	 * @param z        local Z position
	 * @param chunkX   Chunk X position
	 * @param chunkZ   Chunk Y position
	 * @param foundTop True if we've encountered the biome's top block. Ideally if
	 *                 we've broken the surface.
	 */
	private void digBlock(IChunk chunk, Function<BlockPos, Biome> getBiomeFunction, BlockPos blockPos, int chunkX, int chunkZ, boolean foundTop, BlockState state, BlockState blockStateUp)
	{
		Biome biome = getBiomeFunction.apply(blockPos);
//		Biome biome = chunk.getBiome(blockPos);
		BlockState top = biome.getSurfaceBuilderConfig().getTop();
		BlockState filler = biome.getSurfaceBuilderConfig().getUnder();

		if (this.canReplaceBlock(state, blockStateUp) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock())
		{
			if (blockPos.getY() <= lavaDepth)
			{
				chunk.setBlockState(blockPos, lavaBlock, false);
			} else
			{
				chunk.setBlockState(blockPos, AIR, false);

				if (foundTop && chunk.getBlockState(blockPos.down()).getBlock() == filler.getBlock())
				{
					chunk.setBlockState(blockPos.down(), top, false);
				}

				// replace floating sand with sandstone
				if (blockStateUp == Blocks.SAND.getDefaultState())
				{
					chunk.setBlockState(blockPos.up(), Blocks.SANDSTONE.getDefaultState(), false);
				} else if (blockStateUp == Blocks.RED_SAND.getDefaultState())
				{
					chunk.setBlockState(blockPos.up(), Blocks.RED_SANDSTONE.getDefaultState(), false);
				}
			}
		}
	}
}

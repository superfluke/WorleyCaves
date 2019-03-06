package fluke.worleycaves.world;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fluke.worleycaves.config.Configs;
import fluke.worleycaves.util.FastNoise;
import fluke.worleycaves.util.WorleyUtil;

public class ThreadedCaveNoise implements Runnable
{
	private static WorleyUtil worleyF1divF3 = new WorleyUtil();
	private static FastNoise displacementNoisePerlin = new FastNoise();
	private static ExecutorService executor;
	
	private static float[][][] noiseSamples;
	private static final int X_SAMPLE_SIZE = 5;
	private static final int Y_SAMPLE_SIZE = 129;
	private static final int Z_SAMPLE_SIZE = 5;
	//private static final int Y_LAYERS_PER_THREAD = 4;
	
	private static int maxCaveHeight;
	private static float noiseCutoff;
	private static float warpAmplifier;
	private static float yCompression;
	private static float xzCompression;
	
	private int chunkX;
	private int chunkZ;
	private int maxSurfaceHeight;
	private int yLevel;
	
	static 
	{
		worleyF1divF3.SetFrequency(0.016f);
		displacementNoisePerlin.SetNoiseType(FastNoise.NoiseType.Perlin);
		displacementNoisePerlin.SetFrequency(0.05f);
		executor = java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		maxCaveHeight = Configs.cavegen.maxCaveHeight;
		noiseCutoff = (float) Configs.cavegen.noiseCutoffValue;
		warpAmplifier = (float) Configs.cavegen.warpAmplifier;
		yCompression = (float) Configs.cavegen.verticalCompressionMultiplier;
		xzCompression = (float) Configs.cavegen.horizonalCompressionMultiplier;
	}
	
	public ThreadedCaveNoise(int chunkX, int chunkZ, int maxSurfaceHeight, int yLevel)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.maxSurfaceHeight = maxSurfaceHeight;
		this.yLevel = yLevel;
	}

	@Override
	public void run() 
	{
		float[][] layerSamples = new float[X_SAMPLE_SIZE][Z_SAMPLE_SIZE];
		int originalMaxHeight = 128;
		float noise;
		for (int x = 0; x < X_SAMPLE_SIZE; x++)
		{
			int realX = x*4 + chunkX*16;
			for (int z = 0; z < Z_SAMPLE_SIZE; z++)
			{
				int realZ = z*4 + chunkZ*16;
				
				//loop from top down for y values so we can adjust noise above current y later on
				//for(int y = 128; y >= 0; y--)
//				for(int y = 0; y < Y_LAYERS_PER_THREAD; y++)
//				{
//					if(yLevel + Y_LAYERS_PER_THREAD >= Y_SAMPLE_SIZE)
//						break;
//					
				float realY = (yLevel)*2;
				if(realY > maxSurfaceHeight || realY > maxCaveHeight)
				{
					//if outside of valid cave range set noise value below normal minimum of -1.0
					layerSamples[x][z] = -1.1F;
				}
				else
				{
					//Experiment making the cave system more chaotic the more you descend 
					///TODO might be too dramatic down at lava level
					float dispAmp = (float) (warpAmplifier * ((originalMaxHeight-(yLevel))/(originalMaxHeight*0.85)));
					
					float xDisp = 0f;
					float yDisp = 0f;
					float zDisp = 0f;
					
					xDisp = displacementNoisePerlin.GetNoise(realX, realY, realZ)*dispAmp;
					yDisp = displacementNoisePerlin.GetNoise(realX, realY-256.0f, realZ)*dispAmp;
					zDisp = displacementNoisePerlin.GetNoise(realX, realY-512.0f, realZ)*dispAmp;
					
					//doubling the y frequency to get some more caves
					noise = worleyF1divF3.SingleCellular3Edge(realX*xzCompression+xDisp, realY*yCompression+yDisp, realZ*xzCompression+zDisp);
					layerSamples[x][z] = noise;
					
					if (noise > noiseCutoff)
					{
						//if noise is below cutoff, adjust values of neighbors
						//helps prevent caves fracturing during interpolation
						
						if(x > 0)
							layerSamples[x-1][z] = (noise*0.2f) + (layerSamples[x-1][z]*0.8f);
						if(z > 0)
							layerSamples[x][z-1] = (noise*0.2f) + (layerSamples[x][z-1]*0.8f);
						
						//TODO
//							//more heavily adjust y above 'air block' noise values to give players more headroom
//							if(y < 128)
//							{
//								float noiseAbove = noiseSamples[x][y+1][z];
//								if(noise > noiseAbove)
//									noiseSamples[x][y+1][z] = (noise*0.8F) + (noiseAbove*0.2F);
//								if(y < 127)
//								{
//									float noiseTwoAbove = noiseSamples[x][y+2][z];
//									if(noise > noiseTwoAbove)
//										noiseSamples[x][y+2][z] = (noise*0.35F) + (noiseTwoAbove*0.65F);
//								}
//							}
						
					}
				}
//				}
			}
		}
		
		addArrayLayer(layerSamples, yLevel);
	}
	
	public static synchronized void addArrayLayer(float[][] noiseVals, int yLevel)
	{
		for(int x = 0; x < X_SAMPLE_SIZE; x++)
		{
			for(int z = 0; z < Z_SAMPLE_SIZE; z++)
			{
//				for(int y = 0; y < Y_LAYERS_PER_THREAD; y++)
//				{
//					if(yLevel + Y_LAYERS_PER_THREAD >= Y_SAMPLE_SIZE)
//						break;
					
					noiseSamples[x][yLevel][z] = noiseVals[x][z];
//				}
			}
		}
	}
	
	public static float[][][] sampleNoise(int chunkX, int chunkZ, int maxSurfaceHeight) 
	{
		if(noiseSamples == null)
			noiseSamples = new float[X_SAMPLE_SIZE][Y_SAMPLE_SIZE][Z_SAMPLE_SIZE];
		
		List threads = new ArrayList();
		
		//int numThreads = (int)(128 / Y_LAYERS_PER_THREAD);
		for(int y = 0; y <= 128; y++)
		{
			threads.add(Executors.callable(new ThreadedCaveNoise(chunkX, chunkZ, maxSurfaceHeight, y)));
		}
		
		try
	    {
			executor.invokeAll(threads);
	    } catch (InterruptedException e) 
		{
	      e.printStackTrace();
	    }
		
		return noiseSamples;
		
		
		
	}

}

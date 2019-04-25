package fluke.worleycaves.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fluke.worleycaves.config.Configs;

public class ThreadedNoiseManager 
{
	
	private static ExecutorService executor;
//	public static WorleyUtil worleyF1divF3 = new WorleyUtil();
//	public static FastNoise displacementNoisePerlin = new FastNoise();
	public static final int X_SAMPLE_SIZE = 5;
	public static final int Y_SAMPLE_SIZE = 129;
	public static final int Z_SAMPLE_SIZE = 5;
	
	private float[][][] noiseSamples = new float[X_SAMPLE_SIZE][Y_SAMPLE_SIZE][Z_SAMPLE_SIZE];
	
	static 
	{
//		worleyF1divF3.SetFrequency(0.016f);
//		displacementNoisePerlin.SetNoiseType(FastNoise.NoiseType.Perlin);
//		displacementNoisePerlin.SetFrequency(0.05f);
		executor = java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}
	
	public float[][][] getNoiseSamples(int chunkX, int chunkZ, int maxSurfaceHeight) 
	{
		List<Callable<?>> threads = new ArrayList<Callable<?>>();
		int layersPerThread = 16;

		int numThreads = (int)Math.ceil(Y_SAMPLE_SIZE/(double)layersPerThread);
		for(int n = 0; n < numThreads; n++)
			threads.add(Executors.callable(new ThreadedNoiseSegment(chunkX, chunkZ, n * layersPerThread, layersPerThread, maxSurfaceHeight, this)));

		try
		{
			executor.invokeAll(threads);
		} catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		
		addHeadRoom();
		//System.out.println(Arrays.deepToString(noiseSamples));
		return noiseSamples;
	}
	
	public synchronized void mergeSegmentNoise(float[][][] threadSamples, int yLevel, int numLayers)
	{
		for(int x = 0; x < X_SAMPLE_SIZE; x++)
		{
			for(int z = 0; z < Z_SAMPLE_SIZE; z++)
			{
				for(int y = 0; y < numLayers; y++)
				{
					if(y + yLevel >= Y_SAMPLE_SIZE)
						continue;
					
					noiseSamples[x][y + yLevel][z] = threadSamples[x][y][z];
				}
			}
		}
	}
	
	private void addHeadRoom()
	{
		for(int x = 0; x < X_SAMPLE_SIZE; x++)
		{
			for(int z = 0; z < Z_SAMPLE_SIZE; z++)
			{
				for(int y = Y_SAMPLE_SIZE - 2; y >= 0; y--)
				{
					float noise = noiseSamples[x][y][z];
					if(noise > Configs.cavegen.noiseCutoffValue)
					{
					//more heavily adjust y above 'air block' noise values to give players more headroom
						float noiseAbove = noiseSamples[x][y+1][z];
						if(noise > noiseAbove)
							noiseSamples[x][y+1][z] = (noise*0.8F) + (noiseAbove*0.2F);
						if(y < Y_SAMPLE_SIZE - 3)
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

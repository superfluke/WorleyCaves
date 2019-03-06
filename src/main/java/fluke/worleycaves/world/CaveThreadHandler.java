//package fluke.worleycaves.world;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class CaveThreadHandler 
//{
//	
//	public float[][][] noiseSamples;
//	
//	private static ExecutorService executor;
//	private static final int X_SAMPLE_SIZE = 5;
//	private static final int Y_SAMPLE_SIZE = 129;
//	private static final int Z_SAMPLE_SIZE = 5;
//	private static final int Y_LAYERS_PER_THREAD = 1;
//	
//	static
//	{
//		executor = java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//	}
//	
//	public CaveThreadHandler()
//	{
//		this.noiseSamples = new float[X_SAMPLE_SIZE][Y_SAMPLE_SIZE][Z_SAMPLE_SIZE];
//	}
//	
//	public synchronized void addArrayLayer(float[][][] noiseVals, int yLevel)
//	{
//		for(int x = 0; x < X_SAMPLE_SIZE; x++)
//		{
//			for(int z = 0; z < Z_SAMPLE_SIZE; z++)
//			{
//				for(int y = 0; y < Y_LAYERS_PER_THREAD; y++)
//				{
//					if(yLevel + Y_LAYERS_PER_THREAD >= Y_SAMPLE_SIZE)
//						break;
//					
//					noiseSamples[x][yLevel+y][z] = noiseVals[x][y][z];
//				}
//			}
//		}
//	}
//	
//	public static float[][][] sampleNoise(int chunkX, int chunkZ, int maxSurfaceHeight) 
//	{
////		if(noiseSamples == null)
////			noiseSamples = new float[X_SAMPLE_SIZE][Y_SAMPLE_SIZE][Z_SAMPLE_SIZE];
//		
//		List threads = new ArrayList();
//		CaveThreadHandler handler = new CaveThreadHandler();
//		
//		int numThreads = (int)(128 / Y_LAYERS_PER_THREAD);
//		for(int y = 0; y <= numThreads; y++)
//		{
//			threads.add(Executors.callable(new ThreadedCaveNoise(chunkX, chunkZ, maxSurfaceHeight, y*Y_LAYERS_PER_THREAD, handler)));
//		}
//		
//		try
//	    {
//			executor.invokeAll(threads);
//	    } catch (InterruptedException e) 
//		{
//	      e.printStackTrace();
//	    }
//		
//		return handler.noiseSamples;
//	}
//
//
//}

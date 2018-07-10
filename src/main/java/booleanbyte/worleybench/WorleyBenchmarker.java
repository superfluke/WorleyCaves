package booleanbyte.worleybench;

import fluke.worleycaves.util.FastNoise;
import fluke.worleycaves.util.WorleyUtil;
import fluke.worleycaves.util.WorleyUtil2;

public class WorleyBenchmarker {
	
	long[] genTime = new long[40*40];
	int currentTimeIndex = 0;
	
	private FastNoise worleyF1divF3_FastNoise = new FastNoise();
	private WorleyUtil worleyF1divF3_WorleyUtil = new WorleyUtil();
	private WorleyUtil2 worleyF1divF3_WorleyUtil2 = new WorleyUtil2();
	
	private int startHeight = 1;
	private int maxHeight = 90;
	private float cutoff = -0.18F;
	
	public static void main(String[] args) {
		new WorleyBenchmarker();
	}
	
	public WorleyBenchmarker() {
		worleyF1divF3_FastNoise.SetNoiseType(FastNoise.NoiseType.Cellular);
		worleyF1divF3_FastNoise.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Euclidean);
		worleyF1divF3_FastNoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance1Div3);
		worleyF1divF3_FastNoise.SetFrequency(0.016f);
		
		worleyF1divF3_WorleyUtil.SetFrequency(0.016f);
		
		worleyF1divF3_WorleyUtil2.SetFrequency(0.016f);
		
		benchFastWorleyutil();
		benchFastWorleyutil2();
		benchFastNoise();
	}
	
	public void benchFastNoise() {
		for(int chunkX = -30; chunkX < 30; chunkX++) {
			for(int chunkZ = -30; chunkZ < 30; chunkZ++) {
				long millis = System.currentTimeMillis();
				
				buildChunk_FastNoise(chunkX, chunkZ);
				
				genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
				System.out.print(";" + genTime[currentTimeIndex]);
				currentTimeIndex++;
				if (currentTimeIndex == genTime.length)
				{
					currentTimeIndex = 0;
				}
			}
		}
		
		double avg = 0.0;
		for(int i = 0; i < genTime.length; i++) {
			avg += genTime[i];
		}
		
		avg /= (double) genTime.length;
		
		System.out.println("\nFastNoise AVG time: " + avg);
	}
	
	public void benchFastWorleyutil() {
		for(int chunkX = -30; chunkX < 30; chunkX++) {
			for(int chunkZ = -30; chunkZ < 30; chunkZ++) {
				long millis = System.currentTimeMillis();
				
				buildChunk_WorelyUtil(chunkX, chunkZ);
				
				genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
				System.out.print(";" + genTime[currentTimeIndex]);
				currentTimeIndex++;
				if (currentTimeIndex == genTime.length)
				{
					currentTimeIndex = 0;
				}
			}
		}
		
		double avg = 0.0;
		for(int i = 0; i < genTime.length; i++) {
			avg += genTime[i];
		}
		
		avg /= (double) genTime.length;
		
		System.out.println("\nWorleyUtil AVG time: " + avg);
	}
	
	public void benchFastWorleyutil2() {
		for(int chunkX = -30; chunkX < 30; chunkX++) {
			for(int chunkZ = -30; chunkZ < 30; chunkZ++) {
				long millis = System.currentTimeMillis();
				
				buildChunk_Worleyutil2(chunkX, chunkZ);
				
				genTime[currentTimeIndex] = System.currentTimeMillis() - millis;
				System.out.print(";" + genTime[currentTimeIndex]);
				currentTimeIndex++;
				if (currentTimeIndex == genTime.length)
				{
					currentTimeIndex = 0;
				}
			}
		}
		
		double avg = 0.0;
		for(int i = 0; i < genTime.length; i++) {
			avg += genTime[i];
		}
		
		avg /= (double) genTime.length;
		
		System.out.println("\nWorleyUtil2 AVG time: " + avg);
	}
	
	public void buildChunk_FastNoise(int chunkX, int chunkZ) {
		float[][][] noise = new float[16][90][16];
		for (int x=0; x<16; x++)
		{
			int realX = x + chunkX*16;
			for (int z=0; z<16; z++)
			{
				int realZ = z + chunkZ*16;
				for(int y = startHeight; y < maxHeight; y++)
				{
					//Multiplying doubling the y frequency to get some more caves
					noise[x][y][z] = worleyF1divF3_FastNoise.SingleCellular3Edge(realX, y*2.0f, realZ);
				}
			}
		}
	}
	
	public void buildChunk_WorelyUtil(int chunkX, int chunkZ) {
		float[][][] noise = new float[16][90][16];
		for (int x=0; x<16; x++)
		{
			int realX = x + chunkX*16;
			for (int z=0; z<16; z++)
			{
				int realZ = z + chunkZ*16;
				for(int y = startHeight; y < maxHeight; y++)
				{
					//Multiplying doubling the y frequency to get some more caves
					noise[x][y][z] = worleyF1divF3_WorleyUtil.SingleCellular3Edge(realX, y*2.0f, realZ);
				}
			}
		}
	}
	
	public void buildChunk_Worleyutil2(int chunkX, int chunkZ) {
		float[] noise = new float[16*90*16];
		
		noise = worleyF1divF3_WorleyUtil2.SingleCellular3Edge(chunkX*16, chunkZ*16);
	}
}

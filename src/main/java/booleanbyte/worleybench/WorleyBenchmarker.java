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
		
//		benchFastWorleyutil();
//		benchFastWorleyutil2();
//		benchFastNoise();
		benchInterpWorleyutil();
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
	
	public void benchInterpWorleyutil() {
		for(int chunkX = -30; chunkX < 30; chunkX++) {
			for(int chunkZ = -30; chunkZ < 30; chunkZ++) {
				long millis = System.currentTimeMillis();
				
				interpWorleyNoise(chunkX, chunkZ);
				
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
		
		System.out.println("\nInterpWorleyUtil AVG time: " + avg);
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
	
	public float[][][] sampleNoise(int chunkX, int chunkZ) 
	{
		float[][][] noiseSamples = new float[5][17][5];
		for (int x=0; x<5; x++)
		{
			int realX = x*4 + chunkX*16;
			for (int z=0; z<5; z++)
			{
				int realZ = z*4 + chunkZ*16;
				for(int y=0; y<17; y++)
				{
					int realY = y*8;
					//Multiplying doubling the y frequency to get some more caves
					noiseSamples[x][y][z] = worleyF1divF3_WorleyUtil.SingleCellular3Edge(realX, realY*2.0f, realZ);
				}
			}
		}
		return noiseSamples;
	}
	
	public void interpWorleyNoise(int chunkX, int chunkZ)
	{
		float[][][] samples = sampleNoise(chunkX, chunkZ);
		float[][][] noiseFull = new float[16][128][16];
		float oneEighth = 0.125F;
        float oneQuarter = 0.25F;
        
        //each chunk divided into 4 subchunks along X axis
		for (int x=0; x<4; x++)
		{
			//each chunk divided into 4 subchunks along Z axis
			for (int z=0; z<4; z++)
			{
				//each chunk divided into 16 subchunks along Y axis
				for(int y = 0; y < 16; y++)
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
                    float noiseStepY00 = (x0y1z0 - x0y0z0) * oneEighth;
                    float noiseStepY01 = (x0y1z1 - x0y0z1) * oneEighth;
                    float noiseStepY10 = (x1y1z0 - x1y0z0) * oneEighth;
                    float noiseStepY11 = (x1y1z1 - x1y0z1) * oneEighth;
                    
                    //noise values of 4 corners at y=0
                    float noiseStartX0 = x0y0z0;
                    float noiseStartX1 = x0y0z1;
                    float noiseEndX0 = x1y0z0;
                    float noiseEndX1 = x1y0z1;
                    
                    // loop through 8 blocks of the Y subchunk
                    for (int suby = 0; suby < 8; suby++)
                    {
                        float noiseStartZ = noiseStartX0;
                        float noiseEndZ = noiseStartX1;
                        
                        //how much to increment X values, linear interpolation
                        float noiseStepX0 = (noiseEndX0 - noiseStartX0) * oneQuarter;
                        float noiseStepX1 = (noiseEndX1 - noiseStartX1) * oneQuarter;

                        // loop through 4 blocks of the X subchunk
                        for (int subx = 0; subx < 4; subx++)
                        {
                        	//how much to increment Z values, linear interpolation
                            float noiseStepZ = (noiseEndZ - noiseStartZ) * oneQuarter;
                            
                            //Y and X already interpolated, just need to interpolate final 4 Z block to get final noise value
                            float noiseVal = noiseStartZ;

                            // loop through 4 blocks of the Z subchunk
                            for (int subz = 0; subz < 4; subz++)
                            {
                                noiseFull[x*4+subx][y*8+suby][z*4+subz] = noiseVal;
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
}

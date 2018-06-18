package fluke.worleycaves.world;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

public class ChunkGeneratorOverworldCustom implements IChunkGenerator
{
    final Random rand = new Random();
    final World world;
    private MapGenBase caveGenerator = new WorleyCaveGenerator();
    Biome[] biomesForGeneration;

    
    public ChunkGeneratorOverworldCustom(final World world)
    {
        this.world = world;
        caveGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(caveGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);


    }

    @Override
    public Chunk generateChunk(int x, int z)
    {
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.generateTerrain(x, z, chunkprimer);
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.caveGenerator.generate(this.world, x, z, chunkprimer);
        

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
    
    public void generateTerrain(final int chunkX, final int chunkZ, final ChunkPrimer primer)
    {
        for (int x = 0; x < 16; x++)
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
                final int realZ = z + chunkZ * 16;

                final int height = 89;
                
                for (int y = 0; y <= height; y++)
                {

                    primer.setBlockState(x, y, z, Blocks.STONE.getDefaultState());

                }
            }
        }
    }

    @Override
    public void populate(int x, int z)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
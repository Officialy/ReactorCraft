package reika.reactorcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import reika.dragonapi.instantiable.data.maps.TileEntityCache;
import reika.dragonapi.instantiable.data.immutable.WorldLocation;
import reika.reactorcraft.auxiliary.ReactorControlLayout;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;

final class ReactorCacheGameTests {
    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        ReactorGameTests.register(event,environment,"dragonapi_cache_type_and_interface",40,ReactorCacheGameTests::types);
        ReactorGameTests.register(event,environment,"dragonapi_cache_cpu_wrong_subtype",40,ReactorCacheGameTests::controls);
        ReactorGameTests.register(event,environment,"dragonapi_cache_location_resolution",40,ReactorCacheGameTests::locations);
        ReactorGameTests.register(event,environment,"dragonapi_cache_location_original_behaviour",40,ReactorCacheGameTests::originalBehaviour);
    }
    private static void types(GameTestHelper h) {
        var chestPos=new BlockPos(2,1,2);var otherPos=new BlockPos(4,1,2);
        h.setBlock(chestPos,Blocks.CHEST);h.setBlock(otherPos,ReactorBlocks.FUEL.get());
        var chest=h.getBlockEntity(chestPos,ChestBlockEntity.class);
        var entries=new ListTag();entries.add(new WorldLocation(chest).writeToTag());
        entries.add(new WorldLocation(h.getLevel(),h.absolutePos(otherPos)).writeToTag());
        var bad=new CompoundTag();bad.putString("dim","INVALID DIM");entries.add(bad);
        var tag=new CompoundTag();tag.put("locs",entries);
        var cache=new TileEntityCache<>(ChestBlockEntity.class);cache.readFromNBT(tag,h.getLevel());
        h.assertTrue(cache.size()==1 && cache.values().iterator().next()==chest,"wrong block entities must be filtered inside cache loading");
        var interfaces=new TileEntityCache<>(Container.class);interfaces.readFromNBT(tag,h.getLevel());
        h.assertTrue(interfaces.values().contains(chest),"runtime tokens must also support BE interfaces");
        h.succeed();
    }
    private static void controls(GameTestHelper h) {
        var cpuPos=new BlockPos(2,1,4);var rodPos=new BlockPos(3,1,4);var wrongPos=new BlockPos(4,1,4);
        h.setBlock(cpuPos,ReactorBlocks.CPU.get());h.setBlock(rodPos,ReactorBlocks.CONTROL.get());h.setBlock(wrongPos,Blocks.CHEST);
        var cpu=h.getBlockEntity(cpuPos,TileEntityCPU.class);var rod=h.getBlockEntity(rodPos,TileEntityControlRod.class);
        var layout=new ReactorControlLayout(cpu);layout.addControlRod(rod);
        var tag=new CompoundTag();layout.writeToNBT(tag);
        tag.getListOrEmpty("locs").add(new WorldLocation(h.getLevel(),h.absolutePos(wrongPos)).writeToTag());
        var loaded=new ReactorControlLayout(cpu);loaded.readFromNBT(tag,h.getLevel());
        h.assertTrue(loaded.getNumberRods()==1 && loaded.getAllRods().iterator().next()==rod,"actual CPU layout must retain only control rods");
        h.assertTrue(loaded.countLoweredRods()>=0,"typed consumer iteration must not surface a deferred ClassCastException");
        h.succeed();
    }
    private static void originalBehaviour(GameTestHelper h) {
        var absolute=h.absolutePos(new BlockPos(6,1,6));
        var hit=new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(absolute.getX()+1,absolute.getY()+0.5,absolute.getZ()+0.5),Direction.EAST,absolute,false);
        var location=new WorldLocation(h.getLevel(),hit);
        h.assertTrue(location.pos.equals(absolute),"boundary hit must use the hit block, not its neighbour");
        var stack=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3);
        location.dropItem(stack,0);
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,location.asAABB());
        h.assertTrue(drops.size()==1 && drops.getFirst().getItem().getCount()==3,"original randomized drop must land inside its block");
        h.assertTrue(drops.getFirst().getDeltaMovement().lengthSqr()==0 && stack.getCount()==3,"velocity scale and copied item stack must survive");
        drops.getFirst().discard();
        h.succeed();
    }
    private static void locations(GameTestHelper h) {
        var pos=new BlockPos(2,1,6);var state=Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST);
        h.setBlock(pos,state);var location=new WorldLocation(h.getLevel(),h.absolutePos(pos));
        h.assertTrue(location.getBlockKey(h.getLevel()).blockID.equals(state),"BlockKey must retain actual state properties");
        var missing=new WorldLocation(ResourceKey.create(Registries.DIMENSION,Identifier.parse("dragonapi:missing")),location.pos.getX(),location.pos.getY(),location.pos.getZ());
        h.assertTrue(missing.getBlockEntity(h.getLevel())==null && missing.getBlockKey(h.getLevel())==null,"missing dimension must not fall back to the supplied level");
        h.assertTrue(!missing.canSeeTheSky(h.getLevel()) && !missing.isChunkLoaded(h.getLevel()),"unresolved helpers must be safe and never load a chunk");
        var far=new WorldLocation(h.getLevel().dimension(),30000000,5,30000000);
        h.assertTrue(!far.isChunkLoaded(h.getLevel()) && far.getBlockState(h.getLevel())==null,"read helper must not force chunk generation");
        h.succeed();
    }
}

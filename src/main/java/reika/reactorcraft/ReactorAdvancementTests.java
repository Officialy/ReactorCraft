package reika.reactorcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;

import java.util.List;

final class ReactorAdvancementTests {
    private ReactorAdvancementTests() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> env) {
        ReactorGameTests.register(event, env, "advancement_catalog", 40, ReactorAdvancementTests::catalog);
        ReactorGameTests.register(event, env, "advancement_first_login_books", 40, ReactorAdvancementTests::login);
        ReactorGameTests.register(event, env, "advancement_read_handbook", 40, ReactorAdvancementTests::book);
        ReactorGameTests.register(event, env, "advancement_ore_harvest", 40, ReactorAdvancementTests::ore);
        ReactorGameTests.register(event, env, "advancement_creative_harvest_ignored", 40, ReactorAdvancementTests::creative);
        ReactorGameTests.register(event, env, "advancement_reactor_event_award", 40, ReactorAdvancementTests::action);
        ReactorGameTests.register(event, env, "advancement_plutonium_hazard", 40, ReactorAdvancementTests::plutonium);
        ReactorGameTests.register(event, env, "advancement_canister_compatibility", 40, ReactorAdvancementTests::canister);
        ReactorGameTests.register(event, env, "advancement_fifty_gw_connected", 40, ReactorAdvancementTests::fiftyGw);
        ReactorGameTests.register(event, env, "advancement_fifty_gw_below_threshold", 40, ReactorAdvancementTests::belowFiftyGw);
        ReactorGameTests.register(event, env, "advancement_fifty_gw_separate_plants", 40, ReactorAdvancementTests::separatePlants);
    }

    private static boolean done(ServerPlayer player, ReactorAchievements entry) {
        var advancement = player.level().getServer().getAdvancements().get(entry.getId());
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static void catalog(GameTestHelper h) {
        h.assertTrue(ReactorAchievements.list.length == 28, "all original ReactorCraft achievements");
        for (var entry : ReactorAchievements.list) {
            var advancement = h.getLevel().getServer().getAdvancements().get(entry.getId());
            h.assertTrue(advancement != null && advancement.value().display().isPresent(), "missing advancement " + entry);
            if (entry.dependency != null)
                h.assertTrue(advancement.value().parent().orElseThrow().equals(entry.dependency.getId()), "original dependency " + entry);
        }
        h.succeed();
    }

    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (player.getInventory().getItem(i).is(item)) total += player.getInventory().getItem(i).getCount();
        return total;
    }

    private static void login(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        h.assertTrue(count(player, ReactorItems.REACTOR_BOOK.get()) == 1, "first login ReactorCraft book");
        h.assertTrue(count(player, reika.electricraft.registry.ElectriItems.BOOK.get()) == 1, "first login ElectriCraft book");
        reika.dragonapi.auxiliary.trackers.PlayerFirstTimeTracker.checkPlayer(player);
        h.assertTrue(count(player, ReactorItems.REACTOR_BOOK.get()) == 1, "ReactorCraft delivery is once only");
        h.succeed();
    }

    private static void book(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        h.assertTrue(!done(player, ReactorAchievements.RECUSEBOOK), "owning a book is not using it");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ReactorItems.REACTOR_BOOK.get()));
        ReactorItems.REACTOR_BOOK.get().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(done(player, ReactorAchievements.RECUSEBOOK), "reading awards knowledge");
        h.succeed();
    }

    private static void harvest(GameTestHelper h, ServerPlayer player, net.minecraft.world.level.block.Block block) {
        NeoForge.EVENT_BUS.post(new BlockDropsEvent(h.getLevel(), h.absolutePos(BlockPos.ZERO),
                block.defaultBlockState(), null, List.of(), player, new ItemStack(Items.IRON_PICKAXE)));
    }

    private static void ore(GameTestHelper h) {
        var player = survivalPlayer(h);
        harvest(h, player, ReactorBlocks.END_PITCHBLENDE_ORE.get());
        h.assertTrue(done(player, ReactorAchievements.MINEURANIUM), "End pitchblende counts as uranium mining");
        harvest(h, player, ReactorBlocks.CADMIUM_ORE.get());
        h.assertTrue(done(player, ReactorAchievements.MINECADMIUM), "cadmium harvest awards its distinct milestone");
        h.assertTrue(!done(player, ReactorAchievements.FISSION), "ore harvest cannot award fission");
        h.succeed();
    }

    private static void creative(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        harvest(h, player, ReactorBlocks.PITCHBLENDE_ORE.get());
        h.assertTrue(!done(player, ReactorAchievements.MINEURANIUM), "creative mining preserves upstream exclusion");
        h.succeed();
    }

    private static void action(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        h.assertTrue(!done(player, ReactorAchievements.FISSION), "fission starts unearned");
        ReactorAchievements.FISSION.triggerAchievement(player);
        h.assertTrue(done(player, ReactorAchievements.FISSION), "reactor runtime callback awards generated data");
        ReactorAchievements.FISSION.triggerAchievement(player);
        // GameTest's connected mocks all share a profile name; resolve the online owner selected
        // by PlayerList, rather than assuming the newest duplicate-name mock is that owner.
        var owner = h.getLevel().getServer().getPlayerList().getPlayerByName(player.getName().getString());
        ReactorAchievements.SCRAM.triggerAchievement(player.getName().getString());
        h.assertTrue(owner != null && done(owner, ReactorAchievements.SCRAM), "name-based server-owner callback resolves online player");
        h.succeed();
    }

    private static ServerPlayer survivalPlayer(GameTestHelper h) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "test-mock-player"), false);
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override public GameType gameMode() { return GameType.SURVIVAL; }
        };
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    private static void plutonium(GameTestHelper h) {
        var player = survivalPlayer(h);
        ReactorItems.PLUTONIUM_ROD.get().inventoryTick(new ItemStack(ReactorItems.PLUTONIUM_ROD.get()),
                h.getLevel(), player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        h.assertTrue(done(player, ReactorAchievements.PUPOISON), "the registered plutonium item must execute its radiation/award behavior");
        h.succeed();
    }

    private static void canister(GameTestHelper h) {
        var fluid = reika.reactorcraft.registry.ReactorFluids.UF6.get();
        var old = net.neoforged.neoforge.fluids.SimpleFluidContent.copyOf(new net.neoforged.neoforge.fluids.FluidStack(fluid, 1000));
        var ops = h.getLevel().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var json = net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC.encodeStart(ops, old).getOrThrow();
        var restored = reika.reactorcraft.auxiliary.CanisterContents.CODEC.parse(ops, json).getOrThrow();
        h.assertTrue(restored.getFluid() == fluid && restored.getAmount() == 1000, "existing saved fluid contents survive");
        h.assertTrue(reika.reactorcraft.auxiliary.CanisterContents.CODEC.encodeStart(ops, restored).getOrThrow().equals(json), "retain the saved JSON format");
        h.assertTrue(reika.reactorcraft.auxiliary.CanisterContents.CODEC.parse(ops, new com.google.gson.JsonObject()).getOrThrow().isEmpty(), "preserve an explicitly empty saved component");
        var icon = ReactorAchievements.UF6.getIcon().create();
        h.assertTrue(reika.reactorcraft.auxiliary.ReactorStacks.isCanisterOf(icon, fluid), "the displayed canister is genuinely filled with UF6");
        h.succeed();
    }

    private static void fixtureField(Object target, String name, Object value) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
            } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
        }
        throw new IllegalArgumentException("Missing fixture field " + name);
    }

    private static reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine turbinePlant(
            GameTestHelper h, ServerPlayer owner, int turbines, boolean joined) {
        reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine tail = null;
        for (int row = 0; row < turbines; row++) {
            int z = 1 + row * 2;
            h.setBlock(new BlockPos(0, 2, z), ReactorBlocks.STEAMLINE.get());
            if (joined && row > 0) h.setBlock(new BlockPos(0, 2, z - 1), ReactorBlocks.STEAMLINE.get());
            for (int stage = 0; stage < 7; stage++) {
                var relative = new BlockPos(1 + stage, 2, z);
                h.setBlock(relative, ReactorBlocks.BIGTURBINE.get().defaultBlockState()
                        .setValue(reika.reactorcraft.blocks.BlockReactorMachine.FACING, net.minecraft.core.Direction.EAST));
                var tile = h.getBlockEntity(relative, reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine.class);
                tile.setPlacer(owner);
                // Formed, fully spun-up seven-stage ammonia turbines at their real native limits.
                fixtureField(tile, "hasMultiBlock", true);
                fixtureField(tile, "readPos", tile.getBlockPos().west());
                fixtureField(tile, "writePos", tile.getBlockPos().east());
                fixtureField(tile, "omega", tile.getMaxSpeed());
                fixtureField(tile, "steam", 5000);
                fixtureField(tile, "fluid", reika.reactorcraft.registry.WorkingFluid.AMMONIA);
                tail = tile;
            }
        }
        return tail;
    }

    private static void fiftyGw(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var turbine = turbinePlant(h, player, 4, true);
        h.assertTrue(turbine.getCurrentPower() < reika.reactorcraft.auxiliary.ReactorPowerMilestones.FIFTY_GW,
                "one turbine cannot reach 50 GW; count actual shared-circuit outlets");
        h.assertTrue(reika.reactorcraft.auxiliary.ReactorPowerMilestones.circuitPower(h.getLevel(), turbine.getBlockPos())
                >= reika.reactorcraft.auxiliary.ReactorPowerMilestones.FIFTY_GW, "four real outputs reach 50 GW in one circuit");
        reika.reactorcraft.auxiliary.ReactorPowerMilestones.check(turbine);
        h.assertTrue(done(player, ReactorAchievements.FIFTYGW), "a producing connected reactor plant awards Permanent Surplus");
        h.succeed();
    }

    private static void belowFiftyGw(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var turbine = turbinePlant(h, player, 2, true);
        reika.reactorcraft.auxiliary.ReactorPowerMilestones.check(turbine);
        h.assertTrue(!done(player, ReactorAchievements.FIFTYGW), "serial turbine stages cannot be double-counted as independent outputs");
        h.succeed();
    }

    private static void separatePlants(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var turbine = turbinePlant(h, player, 4, false);
        reika.reactorcraft.auxiliary.ReactorPowerMilestones.check(turbine);
        h.assertTrue(!done(player, ReactorAchievements.FIFTYGW), "unconnected reactors cannot pool their output for this milestone");
        h.succeed();
    }
}

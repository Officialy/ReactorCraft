package reika.reactorcraft;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import reika.reactorcraft.blocks.BlockFluorite;
import reika.reactorcraft.container.CPUViewPayload;
import reika.reactorcraft.container.MenuCPU;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.items.ItemRemoteControl;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorDataComponents;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorPackets;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;
import reika.rotarycraft.registry.RotaryItems;

/** Survival content, charge boundaries, saved remote links and menu-owned reactor controls. */
public final class ReactorContentGameTests {
    private static final BlockPos CPU = new BlockPos(3, 2, 4);
    private ReactorContentGameTests() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var color : FluoriteTypes.colorList)
            ReactorGameTests.register(event, environment, "decorative_fluorite_" + color.getColorName(), 40, h -> fluorite(h, color));
        ReactorGameTests.register(event, environment, "remote_charge_and_range_boundaries", 40, ReactorContentGameTests::charge);
        ReactorGameTests.register(event, environment, "remote_crafting_binding_and_persistence", 40, ReactorContentGameTests::binding);
        ReactorGameTests.register(event, environment, "remote_opens_cpu_and_spends_one_charge", 60, ReactorContentGameTests::opening);
        ReactorGameTests.register(event, environment, "remote_missing_cpu_preserves_charge", 40, ReactorContentGameTests::missingCPU);
        ReactorGameTests.register(event, environment, "cpu_menu_controls_only_its_own_rods", 80, ReactorContentGameTests::controls);
        ReactorGameTests.register(event, environment, "remote_monitor_link_and_chunk_reload", 40, ReactorContentGameTests::monitor);
        ReactorGameTests.register(event, environment, "cpu_view_roundtrip_without_local_blocks", 40, ReactorContentGameTests::view);
    }

    private static CraftingRecipe recipe(GameTestHelper h, String id) {
        return (CraftingRecipe) h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,
                Identifier.fromNamespaceAndPath(ReactorCraft.MODID, id))).orElseThrow().value();
    }

    private static void fluorite(GameTestHelper h, FluoriteTypes color) {
        BlockFluorite block = (BlockFluorite) ReactorBlocks.fluoriteBlock(color);
        String id = color.getColorName() + "_fluorite_block";
        ItemStack gem = new ItemStack(ReactorItems.fluorite(color));
        CraftingInput grid = CraftingInput.of(3, 3, Collections.nCopies(9, gem));
        var compact = recipe(h, id);
        h.assertTrue(compact.matches(grid, h.getLevel()) && compact.assemble(grid).is(block.asItem()), "nine matching gems must craft their colour's block");
        var reverse = recipe(h, id + "_uncraft");
        CraftingInput single = CraftingInput.of(1, 1, List.of(new ItemStack(block)));
        ItemStack gems = reverse.assemble(single);
        h.assertTrue(reverse.matches(single, h.getLevel()) && gems.is(gem.getItem()) && gems.getCount() == 9, "uncrafting must return nine gems of the same colour");
        h.setBlock(CPU, block);
        BlockPos pos = h.absolutePos(CPU);
        h.assertTrue(h.getLevel().getBlockState(pos).getLightEmission(h.getLevel(), pos) == 0, "placed fluorite starts unexcited");
        var neutron = new EntityNeutron(h.getLevel(), pos.west(), Direction.EAST, EntityNeutron.NeutronType.FISSION);
        neutron.setPos(Vec3.atCenterOf(pos));
        neutron.tick();
        var glowing = h.getLevel().getBlockState(pos);
        h.assertTrue(glowing.getValue(BlockFluorite.ACTIVATED) && glowing.getLightEmission(h.getLevel(), pos) == 15, "a neutron must excite the block to full light");
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        h.assertTrue(pick.isCorrectToolForDrops(glowing), "wooden pickaxes must harvest decorative fluorite");
        var drops = Block.getDrops(glowing, h.getLevel(), pos, null, null, pick);
        h.assertTrue(drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1, "excited fluorite must drop its normal colour block");
        glowing.randomTick(h.getLevel(), pos, h.getLevel().getRandom());
        h.assertTrue(!h.getLevel().getBlockState(pos).getValue(BlockFluorite.ACTIVATED), "random ticks must end neutron excitation");
        h.succeed();
    }

    private static TileEntityCPU cpu(GameTestHelper h) {
        h.setBlock(CPU, ReactorBlocks.CPU.get());
        return h.getBlockEntity(CPU, TileEntityCPU.class);
    }

    private static void charge(GameTestHelper h) {
        var cpu = cpu(h);
        ItemRemoteControl remote = ReactorItems.REMOTE_CONTROL.get();
        ItemStack stack = new ItemStack(remote);
        remote.setLinkedCPU(stack, cpu);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = cpu.getBlockPos();
        player.setPos(Vec3.atCenterOf(pos));
        h.assertTrue(remote.getRange(stack) == 0 && !remote.canUse(stack, h.getLevel(), player), "uncharged remotes must be unusable");
        remote.setCharged(stack, 8192, false);
        h.assertTrue(remote.getRange(stack) == 52 && !remote.canWorkInterdimensionally(stack), "8192 kJ means 52m and same-dimension only");
        player.setPos(Vec3.atCenterOf(pos.east(52)));
        h.assertTrue(remote.canUse(stack, h.getLevel(), player), "52m boundary must be usable");
        player.setPos(Vec3.atCenterOf(pos.east(53)));
        h.assertTrue(!remote.canUse(stack, h.getLevel(), player), "53m must be out of range");
        stack.set(ReactorDataComponents.REMOTE_CPU.get(), GlobalPos.of(Level.NETHER, pos));
        player.setPos(Vec3.atCenterOf(pos));
        h.assertTrue(!remote.canUse(stack, h.getLevel(), player), "cross-dimension use requires more than 8192 kJ");
        remote.setCharged(stack, 8193, false);
        h.assertTrue(remote.canWorkInterdimensionally(stack) && remote.canUse(stack, h.getLevel(), player), "8193 kJ enables cross-dimension control");
        h.assertTrue(remote.setCharged(stack, 50000, true) == 8193 && stack.getDamageValue() == 32000, "charging returns previous charge and caps at 32000");
        h.succeed();
    }

    private static void binding(GameTestHelper h) {
        var cpu = cpu(h);
        var remote = ReactorItems.REMOTE_CONTROL.get();
        ItemStack stack = new ItemStack(remote);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(cpu.getBlockPos()), Direction.UP, cpu.getBlockPos(), false);
        h.assertTrue(remote.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)) == InteractionResult.SUCCESS,
                "using an uncharged remote on a CPU must bind it");
        remote.setCharged(stack, 16384, false);
        FriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, stack);
            var copy = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
            h.assertTrue(copy.getDamageValue() == 16384 && remote.getLinkedCPU(copy, h.getLevel().getServer()) == cpu, "charge and linked CPU must survive network serialization");
            var ops = h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
            var saved = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
            var loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();
            h.assertTrue(remote.getLinkedCPU(loaded, h.getLevel().getServer()) == cpu && loaded.getDamageValue() == 16384, "link and charge must survive disk serialization");
        } finally { buffer.release(); }
        CraftingInput grid = CraftingInput.of(3, 3, List.of(new ItemStack(RotaryItems.HSLA_STEEL_INGOT.get()), new ItemStack(Items.ENDER_PEARL), new ItemStack(RotaryItems.HSLA_STEEL_INGOT.get()), new ItemStack(Items.STONE_BUTTON), new ItemStack(RotaryItems.CIRCUIT_BOARD.get()), new ItemStack(Items.STONE_BUTTON), new ItemStack(Items.STONE_BUTTON), new ItemStack(RotaryItems.HSLA_PLATE.get()), new ItemStack(Items.STONE_BUTTON)));
        var recipe = recipe(h, "remote_control");
        h.assertTrue(recipe.matches(grid, h.getLevel()) && recipe.assemble(grid).is(remote) && recipe.assemble(grid).getDamageValue() == 0, "the original remote recipe must craft an uncharged remote");
        h.succeed();
    }

    private static void opening(GameTestHelper h) {
        var cpu = cpu(h);
        var remote = ReactorItems.REMOTE_CONTROL.get();
        var stack = new ItemStack(remote);
        remote.setLinkedCPU(stack, cpu);
        remote.setCharged(stack, 16000, false);
        var player = (net.minecraft.server.level.ServerPlayer) h.makeMockServerPlayer(GameType.SURVIVAL);
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        var channel = new io.netty.channel.embedded.EmbeddedChannel(connection);
        var openScreen = Identifier.fromNamespaceAndPath("neoforge", "advanced_open_screen");
        net.neoforged.neoforge.network.registration.ChannelAttributes.setPayloadSetup(connection,
                new net.neoforged.neoforge.network.registration.NetworkPayloadSetup(Map.of(net.minecraft.network.ConnectionProtocol.PLAY,
                        Map.of(openScreen, new net.neoforged.neoforge.network.registration.NetworkChannel(openScreen, "1"),
                                CPUViewPayload.TYPE.id(), new net.neoforged.neoforge.network.registration.NetworkChannel(CPUViewPayload.TYPE.id(), "1")))));
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player,
                net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
        try {
            player.setPos(Vec3.atCenterOf(cpu.getBlockPos()));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            h.assertTrue(remote.use(h.getLevel(), player, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS
                    && player.containerMenu instanceof MenuCPU menu && menu.tile == cpu && stack.getDamageValue() == 15999,
                    "a charged remote must open the bound CPU and spend exactly one kJ");
            player.closeContainer();
        } finally { channel.finishAndReleaseAll(); }
        h.succeed();
    }

    private static void missingCPU(GameTestHelper h) {
        var cpu = cpu(h);
        var remote = ReactorItems.REMOTE_CONTROL.get();
        var stack = new ItemStack(remote);
        remote.setLinkedCPU(stack, cpu);
        remote.setCharged(stack, 16000, false);
        h.setBlock(CPU, Blocks.AIR);
        var player = h.makeMockServerPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setPos(Vec3.atCenterOf(cpu.getBlockPos()));
        h.assertTrue(remote.getLinkedCPU(stack, h.getLevel().getServer()) == null && remote.use(h.getLevel(), player, InteractionHand.MAIN_HAND) == InteractionResult.PASS && stack.getDamageValue() == 16000, "a deleted CPU must not open a menu or spend charge");
        stack.set(ReactorDataComponents.REMOTE_CPU.get(), GlobalPos.of(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "missing")), cpu.getBlockPos()));
        h.assertTrue(!remote.canUse(stack, h.getLevel(), player) && remote.getLinkedCPU(stack, h.getLevel().getServer()) == null, "missing dimensions must fail safely");
        h.succeed();
    }

    private static void controls(GameTestHelper h) {
        var cpu = cpu(h);
        h.setBlock(CPU.east(), ReactorBlocks.CONTROL.get());
        h.setBlock(CPU.east(5), ReactorBlocks.CONTROL.get());
        var owned = h.getBlockEntity(CPU.east(), TileEntityControlRod.class);
        var other = h.getBlockEntity(CPU.east(5), TileEntityControlRod.class);
        h.startSequence().thenIdle(4).thenExecute(() -> {
            cpu.getLayout().clear();
            cpu.getLayout().addControlRod(owned);
            var player = h.makeMockPlayer(GameType.SURVIVAL);
            MenuCPU menu = new MenuCPU(7, player.getInventory(), cpu);
            player.containerMenu = menu;
            h.assertTrue(!menu.handleControl(player, ReactorPackets.CPUTOGGLE, other.getBlockPos()), "the CPU menu must reject rods outside its layout");
            h.assertTrue(!menu.handleControl(player, ReactorPackets.CPURAISE, other.getBlockPos()), "raise/lower packets must address the menu's CPU");
            h.assertTrue(menu.handleControl(player, ReactorPackets.CPURAISE, cpu.getBlockPos()), "the menu must route raise-all to its bound CPU");
            owned.updateEntity(h.getLevel(), owned.getBlockPos());
            h.assertTrue(!owned.isActive() && other.isActive(), "raising bound rods must leave unrelated rods inserted");
            player.containerMenu = player.inventoryMenu;
            h.assertTrue(!menu.handleControl(player, ReactorPackets.CPULOWER, cpu.getBlockPos()), "closed menus must reject controls");
            h.setBlock(CPU, Blocks.AIR);
            h.assertTrue(!menu.stillValid(player), "a removed CPU must invalidate its menu");
        }).thenSucceed();
    }

    private static void monitor(GameTestHelper h) {
        var cpu = cpu(h);
        h.setBlock(CPU.east(), ReactorBlocks.FUEL.get());
        var core = h.getBlockEntity(CPU.east(), reika.reactorcraft.tileentities.fission.TileEntityFuelRod.class);
        var remote = ReactorItems.REMOTE_CONTROL.get();
        var stack = new ItemStack(remote);
        remote.setLinkedCPU(stack, cpu);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(core.getBlockPos()), Direction.UP, core.getBlockPos(), false));
        h.assertTrue(remote.useOn(context) == InteractionResult.SUCCESS && remote.useOn(context) == InteractionResult.SUCCESS,
                "remote must link core monitoring without charge, including repeated clicks");
        var saved = cpu.saveWithoutMetadata(h.getLevel().registryAccess());
        var checks = saved.getListOrEmpty("checks");
        h.assertTrue(checks.size() == 1 && checks.getCompoundOrEmpty(0).getStringOr("dimension", "").equals(h.getLevel().dimension().identifier().toString()), "monitor links must deduplicate and persist their dimension");
        var loaded = new TileEntityCPU(cpu.getBlockPos(), cpu.getBlockState());
        loaded.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
        loaded.setLevel(h.getLevel());
        h.assertTrue(loaded.saveWithoutMetadata(h.getLevel().registryAccess()).getListOrEmpty("checks").size() == 1, "CPU monitor links must load before the chunk assigns a level");
        checks.getCompoundOrEmpty(0).remove("dimension");
        var legacy = new TileEntityCPU(cpu.getBlockPos(), cpu.getBlockState());
        legacy.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
        legacy.setLevel(h.getLevel());
        legacy.addTemperatureCheck(core);
        h.assertTrue(legacy.saveWithoutMetadata(h.getLevel().registryAccess()).getListOrEmpty("checks").size() == 1, "old coordinate-only links must resolve to the owning CPU's dimension and remain deduplicated");
        legacy.removeTemperatureCheck(core);
        h.assertTrue(legacy.saveWithoutMetadata(h.getLevel().registryAccess()).getListOrEmpty("checks").isEmpty(), "legacy links must also unlink correctly");
        h.succeed();
    }

    private static void view(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        BlockPos distant = h.absolutePos(CPU).offset(800, 0, 800);
        GlobalPos target = GlobalPos.of(Level.NETHER, distant);
        var original = new CPUViewPayload(3, target, Map.of(distant.east(), 0x00ff00, distant.above().west(), 0xff0000));
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            CPUViewPayload.STREAM_CODEC.encode(data, original);
            var decoded = CPUViewPayload.STREAM_CODEC.decode(data);
            h.assertTrue(original.equals(decoded), "CPU location, dimension and rod colours must survive packet encoding");
            original.write(data.clear());
            MenuCPU menu = new MenuCPU(3, player.getInventory(), data);
            h.assertTrue(menu.getController().equals(target) && menu.getRods().equals(original.rods()) && menu.minY() == 0 && menu.maxY() == 1, "client menus must show a remote CPU without looking up local block entities");
            menu.applyView(new CPUViewPayload(3, target, Map.of(distant.east(), 0xa0a0a0)));
            h.assertTrue(menu.getRods().get(distant.east()) == 0xa0a0a0 && menu.maxY() == 0, "live snapshots must refresh colours and remove stale rods");
        } finally { data.release(); }
        h.succeed();
    }
}

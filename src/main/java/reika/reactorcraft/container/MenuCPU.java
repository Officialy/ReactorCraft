/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.container;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import reika.dragonapi.base.CoreContainer;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorMenus;
import reika.reactorcraft.registry.ReactorPackets;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import net.neoforged.neoforge.network.PacketDistributor;

/** CPU controls and live rod states remain available when the CPU is outside client chunk tracking. */
public class MenuCPU extends CoreContainer<TileEntityCPU> {
    private final GlobalPos controller;
    private Map<BlockPos, Integer> rods = Map.of();

    public MenuCPU(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, CPUViewPayload.read(data));
    }

    private MenuCPU(int id, Inventory inventory, CPUViewPayload view) {
        this(id, inventory, new TileEntityCPU(view.controller().pos(), ReactorBlocks.CPU.get().defaultBlockState()), view.controller());
        tile.setLevel(inventory.player.level());
        applyView(view);
    }

    public MenuCPU(int id, Inventory inventory, TileEntityCPU cpu) {
        this(id, inventory, cpu, GlobalPos.of(cpu.getLevel().dimension(), cpu.getBlockPos()));
        rods = capture(cpu);
    }

    private MenuCPU(int id, Inventory inventory, TileEntityCPU cpu, GlobalPos controller) {
        super(ReactorMenus.CPU.get(), id, inventory, cpu);
        this.controller = controller;
    }

    public static void writeOpeningData(FriendlyByteBuf data, TileEntityCPU cpu) {
        new CPUViewPayload(0, GlobalPos.of(cpu.getLevel().dimension(), cpu.getBlockPos()), capture(cpu)).write(data);
    }

    private static Map<BlockPos, Integer> capture(TileEntityCPU cpu) {
        Map<BlockPos, Integer> view = new HashMap<>();
        var layout = cpu.getLayout();
        if (layout != null) {
            boolean powered = cpu.getPower() >= layout.getMinPower();
            for (var rod : layout.getAllRods())
                if (rod != null && !rod.isRemoved())
                    view.put(rod.getBlockPos(), powered ? (rod.isActive() ? 0x00ff00 : 0xff0000) : 0xa0a0a0);
        }
        return Map.copyOf(view);
    }

    public GlobalPos getController() { return controller; }
    public Map<BlockPos, Integer> getRods() { return rods; }
    public void applyView(CPUViewPayload view) { rods = view.rods(); }
    public int minY() { return rods.keySet().stream().mapToInt(p -> p.getY() - controller.pos().getY()).min().orElse(0); }
    public int maxY() { return rods.keySet().stream().mapToInt(p -> p.getY() - controller.pos().getY()).max().orElse(0); }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (epInv.player instanceof ServerPlayer player) {
            Map<BlockPos, Integer> next = capture(tile);
            if (!next.equals(rods)) {
                rods = next;
                PacketDistributor.sendToPlayer(player, new CPUViewPayload(containerId, controller, rods));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().isClientSide() || !tile.isRemoved()
                && tile.getLevel() != null && tile.getLevel().hasChunkAt(controller.pos())
                && tile.getLevel().getBlockEntity(controller.pos()) == tile;
    }

    /** Only the CPU owned by this open menu and rods in its discovered layout may receive controls. */
    public boolean handleControl(Player player, ReactorPackets action, BlockPos target) {
        if (player.containerMenu != this || player.level().isClientSide() || !stillValid(player))
            return false;
        if (action == ReactorPackets.CPURAISE && controller.pos().equals(target)) {
            tile.raiseAllRods();
            return true;
        }
        if (action == ReactorPackets.CPULOWER && controller.pos().equals(target)) {
            tile.lowerAllRods();
            return true;
        }
        if (action == ReactorPackets.CPUTOGGLE && tile.getPower() >= tile.getMinPower()) {
            var rod = tile.getLayout().getControlRodAtAbsolutePosition(tile.getLevel(), target.getX(), target.getY(), target.getZ());
            if (rod != null && !rod.isRemoved()) {
                rod.toggle(true, true);
                return true;
            }
        }
        return false;
    }
}

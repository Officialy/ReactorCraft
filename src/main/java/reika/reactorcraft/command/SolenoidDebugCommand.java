/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.command;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import reika.reactorcraft.blocks.multi.BlockSolenoidCasing;
import reika.reactorcraft.blocks.multi.BlockSolenoidCasing.CasingSpec;

/**
 * Debug command: places the full solenoid casing shell floating 5 blocks above the player, leaving TWO
 * blocks for the player to place by hand -- the central core ({@code solenoid_magnet}) and one wall of
 * the outer ring. Everything is enumerated from {@link BlockSolenoidCasing#layout(BlockPos)} (the same
 * source of truth the validator uses), so the debug structure can never drift from what actually
 * validates. The wall block must be placed LAST (after the core): its placement is what triggers the
 * assembly scan, which requires the core already present at centre.
 */
public final class SolenoidDebugCommand {

    private SolenoidDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("solenoiddebug").executes(ctx -> {
            Player player = ctx.getSource().getPlayerOrException();
            Level level = ctx.getSource().getLevel();
            // Floating above the player so the structure doesn't overwrite terrain underfoot or bury
            // the player in the core.
            BlockPos mid = player.blockPosition().offset(0, 5, 0);

            Map<BlockPos, CasingSpec> layout = BlockSolenoidCasing.layout(mid);
            // The front-centre wall block at eye level -- an obvious gap in the ring for the player.
            BlockPos ringGap = new BlockPos(mid.getX(), mid.getY(), mid.getZ() + 8);
            CasingSpec gapSpec = layout.get(ringGap);

            int placed = 0;
            for (Map.Entry<BlockPos, CasingSpec> e : layout.entrySet()) {
                if (e.getKey().equals(ringGap))
                    continue; // player places this one
                placeCasing(level, e.getKey(), e.getValue());
                placed++;
            }
            // The core (solenoid_magnet) is intentionally NOT placed -- the player places it.

            final int count = placed;
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Solenoid casing placed (" + count + " blocks). Two blocks left for you:\n"
                            + " 1. Core (solenoid_magnet) at " + mid.getX() + ", " + mid.getY() + ", " + mid.getZ() + "\n"
                            + " 2. Final " + gapSpec.part().getSerializedName() + " casing at "
                            + ringGap.getX() + ", " + ringGap.getY() + ", " + ringGap.getZ() + "\n"
                            + "Place the CORE first, then the casing block LAST -- the casing's placement "
                            + "triggers the assembly check."), true);
            return 1;
        }));
    }

    private static void placeCasing(Level level, BlockPos pos, CasingSpec spec) {
        Block b = BlockSolenoidCasing.blockFor(spec.part());
        BlockState st = b.defaultBlockState();
        if (spec.axis() != null)
            st = st.setValue(BlockSolenoidCasing.AXIS, spec.axis());
        level.setBlock(pos, st, 3);
    }
}

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

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import reika.reactorcraft.blocks.multi.BlockSolenoidMulti;
import reika.reactorcraft.blocks.multi.BlockSolenoidMulti.SolenoidPart;
import reika.reactorcraft.registry.ReactorBlocks;

/**
 * Debug command: places the full solenoid multiblock (core + every casing position
 * {@link BlockSolenoidMulti}'s {@code check*} validators expect) centred 5 blocks above the player,
 * skipping ONE casing block, so the structure can be completed and {@code checkForFullMultiBlock}
 * tested by manually placing the reported final block. The placement offsets below are a direct
 * mirror of {@code checkUpper}/{@code checkLower}/{@code checkMiddle}/{@code checkCorners}/
 * {@code checkSpokes}/{@code checkCore} -- keep them in sync if those change.
 */
public final class SolenoidDebugCommand {

    private record Placement(BlockPos pos, SolenoidPart part) {}

    private SolenoidDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("solenoiddebug").executes(ctx -> {
            Player player = ctx.getSource().getPlayerOrException();
            Level level = ctx.getSource().getLevel();
            // Floating above the player rather than centred on them, so the structure doesn't
            // overwrite terrain/builds underfoot or bury the player inside the core block.
            BlockPos mid = player.blockPosition().offset(0, 5, 0);

            List<Placement> placements = buildPlacements(mid);

            // Skip ONE casing block of the outer wall ring so the player can place it themselves.
            // Deliberately the front-centre WALL block at eye level (midZ+8) -- an obvious gap in the
            // ring rather than a hard-to-spot corner. Its onPlace is what triggers the assembly scan,
            // so it must be the LAST block placed (with the core already present).
            BlockPos ringGap = new BlockPos(mid.getX(), mid.getY(), mid.getZ() + 8);
            Placement skip = null;
            for (int i = 0; i < placements.size(); i++) {
                if (placements.get(i).pos().equals(ringGap)) {
                    skip = placements.remove(i);
                    break;
                }
            }

            // Do NOT place the core -- the player places it themselves (per request). Everything else
            // except the one ring gap is placed for them.
            BlockState casingDefault = ReactorBlocks.SOLENOIDMULTI.get().defaultBlockState();
            for (Placement p : placements) {
                level.setBlock(p.pos(), casingDefault.setValue(BlockSolenoidMulti.PART, p.part()), 3);
            }

            final Placement gap = skip;
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Solenoid casing placed (" + placements.size() + " blocks). Two blocks left for you:\n"
                            + " 1. Core (solenoid_magnet) at " + mid.getX() + ", " + mid.getY() + ", " + mid.getZ() + "\n"
                            + " 2. Final " + gap.part().getSerializedName() + " casing at "
                            + gap.pos().getX() + ", " + gap.pos().getY() + ", " + gap.pos().getZ() + "\n"
                            + "Place the CORE first, then the casing block LAST -- the casing's placement "
                            + "triggers the assembly check."), true);
            return 1;
        }));
    }

    private static List<Placement> buildPlacements(BlockPos mid) {
        int midX = mid.getX(), midY = mid.getY(), midZ = mid.getZ();
        List<Placement> list = new ArrayList<>();

        // checkCore: 3x2x3 shell around the magnet TE (centre excluded -- that's the SOLENOID block itself)
        for (int i = -1; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i != 0 || j != 0 || k != 0) {
                        list.add(new Placement(new BlockPos(midX + i, midY + j, midZ + k), SolenoidPart.CORE));
                    }
                }
            }
        }

        // checkSpokes: radial arms out to distance 7, plus diagonals out to distance 5
        for (int i = 2; i <= 7; i++) {
            list.add(new Placement(new BlockPos(midX + i, midY, midZ), SolenoidPart.SPOKE));
            list.add(new Placement(new BlockPos(midX - i, midY, midZ), SolenoidPart.SPOKE));
            list.add(new Placement(new BlockPos(midX, midY, midZ + i), SolenoidPart.SPOKE));
            list.add(new Placement(new BlockPos(midX, midY, midZ - i), SolenoidPart.SPOKE));

            if (i < 6) {
                list.add(new Placement(new BlockPos(midX + i, midY, midZ + i), SolenoidPart.SPOKE));
                list.add(new Placement(new BlockPos(midX - i, midY, midZ + i), SolenoidPart.SPOKE));
                list.add(new Placement(new BlockPos(midX + i, midY, midZ - i), SolenoidPart.SPOKE));
                list.add(new Placement(new BlockPos(midX - i, midY, midZ - i), SolenoidPart.SPOKE));
            }
        }

        // checkCorners
        int c = 6;
        list.add(new Placement(new BlockPos(midX - c, midY + 1, midZ - c), SolenoidPart.EDGE));
        list.add(new Placement(new BlockPos(midX - c, midY - 1, midZ - c), SolenoidPart.EDGE));

        // checkMiddle / checkLower / checkUpper: same ring layout at three Y levels
        addRing(list, midX, midY, midZ, SolenoidPart.WALL, SolenoidPart.WALL_EDGE);
        addRing(list, midX, midY - 1, midZ, SolenoidPart.FACE, SolenoidPart.EDGE);
        addRing(list, midX, midY + 1, midZ, SolenoidPart.FACE, SolenoidPart.EDGE);

        return list;
    }

    private static void addRing(List<Placement> list, int midX, int dy, int midZ, SolenoidPart inner, SolenoidPart outer) {
        for (int i = -5; i <= 5; i++) {
            int d = Math.abs(i) >= 4 ? 7 : 8;
            SolenoidPart part = Math.abs(i) >= 3 ? outer : inner;
            list.add(new Placement(new BlockPos(midX - d, dy, midZ + i), part));
            list.add(new Placement(new BlockPos(midX + d, dy, midZ + i), part));
            list.add(new Placement(new BlockPos(midX + i, dy, midZ + d), part));
            list.add(new Placement(new BlockPos(midX + i, dy, midZ - d), part));
        }
    }
}

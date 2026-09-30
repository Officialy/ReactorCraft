/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.items;

import java.util.function.Consumer;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import reika.reactorcraft.auxiliary.LinkableReactorCore;
import reika.reactorcraft.base.ItemReactorTool;
import reika.reactorcraft.container.MenuCPU;
import reika.reactorcraft.registry.ReactorDataComponents;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.rotarycraft.api.interfaces.ChargeableTool;

/** Coil-powered CPU remote: linking is free, opening costs one kJ, range is 4 floor(log2(charge)). */
public class ItemRemoteControl extends ItemReactorTool implements ChargeableTool {
    public static final int MAX_CHARGE = 32000;

    public ItemRemoteControl(Properties properties) { super(properties); }

    public void setLinkedCPU(ItemStack stack, TileEntityCPU cpu) {
        stack.set(ReactorDataComponents.REMOTE_CPU.get(), GlobalPos.of(cpu.getLevel().dimension(), cpu.getBlockPos()));
    }

    public TileEntityCPU getLinkedCPU(ItemStack stack, MinecraftServer server) {
        GlobalPos target = stack.get(ReactorDataComponents.REMOTE_CPU.get());
        if (!stack.is(this) || target == null || server == null)
            return null;
        ServerLevel level = server.getLevel(target.dimension());
        return level != null && level.getBlockEntity(target.pos()) instanceof TileEntityCPU cpu ? cpu : null;
    }

    public boolean canWorkInterdimensionally(ItemStack stack) { return stack.getDamageValue() > 8192; }

    public int getRange(ItemStack stack) {
        int charge = stack.getDamageValue();
        return charge > 0 ? 4 * (31 - Integer.numberOfLeadingZeros(charge)) : 0;
    }

    public boolean canUse(ItemStack stack, Level level, Player player) {
        GlobalPos target = stack.get(ReactorDataComponents.REMOTE_CPU.get());
        return stack.is(this) && stack.getDamageValue() > 0 && target != null
                && (target.dimension().equals(level.dimension()) || canWorkInterdimensionally(stack))
                && player.blockPosition().distSqr(target.pos()) <= Math.pow(getRange(stack) + 0.5, 2)
                && (level.isClientSide() || level.getServer().getLevel(target.dimension()) != null);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUse(stack, level, player))
            return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) {
            TileEntityCPU cpu = getLinkedCPU(stack, level.getServer());
            if (cpu == null)
                return InteractionResult.PASS;
            if (serverPlayer.openMenu(cpu, data -> MenuCPU.writeOpeningData(data, cpu)).isPresent())
                stack.setDamageValue(stack.getDamageValue() - 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        var target = level.getBlockEntity(context.getClickedPos());
        if (target instanceof TileEntityCPU cpu) {
            if (!level.isClientSide()) {
                setLinkedCPU(context.getItemInHand(), cpu);
                context.getPlayer().sendSystemMessage(Component.literal("Linked to reactor CPU at " + cpu.getBlockPos().toShortString()));
            }
            return InteractionResult.SUCCESS;
        }
        if (target instanceof LinkableReactorCore core) {
            if (!level.isClientSide()) {
                TileEntityCPU cpu = getLinkedCPU(context.getItemInHand(), level.getServer());
                if (cpu == null)
                    return InteractionResult.PASS;
                cpu.addTemperatureCheck(core);
                context.getPlayer().sendSystemMessage(Component.literal("Linked reactor temperature monitor to CPU at " + cpu.getBlockPos().toShortString()));
            }
            return context.getItemInHand().has(ReactorDataComponents.REMOTE_CPU.get()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public int setCharged(ItemStack stack, int charge, boolean strongcoil) {
        int previous = stack.getDamageValue();
        stack.setDamageValue(Math.clamp(charge, 0, MAX_CHARGE));
        return previous;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return stack.getDamageValue() > 0; }
    @Override
    public int getBarWidth(ItemStack stack) { return Math.round(13F * stack.getDamageValue() / MAX_CHARGE); }
    @Override
    public int getBarColor(ItemStack stack) { return 0x00ff00; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        GlobalPos target = stack.get(ReactorDataComponents.REMOTE_CPU.get());
        lines.accept(Component.literal(target == null ? "No linked CPU" : "Linked to CPU in " + target.dimension().identifier() + " at " + target.pos().toShortString()));
        lines.accept(Component.literal("Charge: " + stack.getDamageValue() + " kJ"));
        lines.accept(Component.literal("Range: " + getRange(stack) + " m"));
        if (canWorkInterdimensionally(stack))
            lines.accept(Component.literal("Cross-dimension control enabled"));
    }
}

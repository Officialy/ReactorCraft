package reika.reactorcraft.modinterface.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import reika.dragonapi.interfaces.blockentity.HasItemHandler;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.auxiliary.Temperatured;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.rotarycraft.api.interfaces.TemperatureTile;
import reika.rotarycraft.auxiliary.interfaces.ConditionalOperation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Server-backed diagnostics for every registered ReactorCraft machine block. */
@WailaPlugin
public final class ReactorJadePlugin implements IWailaPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "machine_state");
    private static final String PREFIX = "reactorcraft_jade_";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ServerData.INSTANCE, BlockReactorMachine.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, BlockReCMultiBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Tooltip.INSTANCE, BlockReactorMachine.class);
        registration.registerBlockComponent(Tooltip.INSTANCE, BlockReCMultiBlock.class);
    }

    private enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override public Identifier getUid() { return UID; }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BlockEntity blockEntity = accessor.getBlockEntity();
            if (!(blockEntity instanceof TileEntityReactorBase machine))
                return;
            data.putBoolean(key("present"), true);

            if (machine instanceof ConditionalOperation operation) {
                data.putBoolean(key("operational"), operation.areConditionsMet());
                String status = operation.getOperationalStatus();
                if (status != null && !status.isBlank())
                    data.putString(key("status"), status);
            }
            if (machine instanceof Temperatured thermal) {
                data.putInt(key("temperature"), thermal.getTemperature());
                data.putInt(key("max_temperature"), thermal.getMaxTemperature());
            } else if (machine instanceof TemperatureTile thermal) {
                data.putInt(key("temperature"), thermal.getTemperature());
                data.putInt(key("max_temperature"), thermal.getMaxTemperature());
            }
            if (machine instanceof ReactorPowerReceiver receiver) {
                data.putLong(key("power"), receiver.getPower());
                data.putLong(key("min_power"), receiver.getMinPower());
                data.putInt(key("speed"), receiver.getOmega());
                data.putInt(key("min_speed"), receiver.getMinSpeed());
                data.putInt(key("torque"), receiver.getTorque());
                data.putInt(key("min_torque"), receiver.getMinTorque());
            }
            if (machine instanceof HasItemHandler inventory) {
                var handler = inventory.getItemHandler();
                int occupied = 0;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    if (!handler.getStackInSlot(slot).isEmpty())
                        occupied++;
                }
                data.putInt(key("occupied"), occupied);
                data.putInt(key("slots"), handler.getSlots());
            }
            var handler = accessor.getLevel().getCapability(Capabilities.Fluid.BLOCK, accessor.getPosition(), null);
            if (handler != null) {
                ListTag fluids = new ListTag();
                for (int tank = 0; tank < Math.min(handler.size(), 8); tank++) {
                    FluidResource resource = handler.getResource(tank);
                    if (resource.isEmpty())
                        continue;
                    CompoundTag fluid = new CompoundTag();
                    fluid.putString("id", BuiltInRegistries.FLUID.getKey(resource.getFluid()).toString());
                    fluid.putInt("amount", handler.getAmountAsInt(tank));
                    fluid.putInt("capacity", handler.getCapacityAsInt(tank, resource));
                    fluids.add(fluid);
                }
                if (!fluids.isEmpty())
                    data.put(key("fluids"), fluids);
            }
            int comparator = machine.getRedstoneOverride();
            if (comparator > 0 || data.size() == 1)
                data.putInt(key("comparator"), comparator);
        }
    }

    private enum Tooltip implements IBlockComponentProvider {
        INSTANCE;

        @Override public Identifier getUid() { return UID; }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.getBooleanOr(key("present"), false))
                return;
            if (data.contains(key("status")))
                tooltip.add(Component.translatable("jade.reactorcraft.status", data.getStringOr(key("status"), ""))
                        .withStyle(data.getBooleanOr(key("operational"), false)
                                ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
            if (data.contains(key("temperature")))
                tooltip.add(Component.translatable("jade.reactorcraft.temperature",
                        data.getIntOr(key("temperature"), 0), data.getIntOr(key("max_temperature"), 0)));
            if (data.contains(key("power"))) {
                tooltip.add(Component.translatable("jade.reactorcraft.power",
                        data.getLongOr(key("power"), 0), data.getLongOr(key("min_power"), 0)));
                tooltip.add(Component.translatable("jade.reactorcraft.shaft",
                        data.getIntOr(key("torque"), 0), data.getIntOr(key("min_torque"), 0),
                        data.getIntOr(key("speed"), 0), data.getIntOr(key("min_speed"), 0)));
            }
            if (data.contains(key("slots")))
                tooltip.add(Component.translatable("jade.reactorcraft.inventory",
                        data.getIntOr(key("occupied"), 0), data.getIntOr(key("slots"), 0)));
            for (var entry : data.getListOrEmpty(key("fluids"))) {
                if (!(entry instanceof CompoundTag fluid))
                    continue;
                Identifier id = Identifier.tryParse(fluid.getStringOr("id", ""));
                var type = id == null ? null : BuiltInRegistries.FLUID.getValue(id);
                if (type != null)
                    tooltip.add(Component.translatable("jade.reactorcraft.fluid",
                            Component.translatable(type.getFluidType().getDescriptionId()),
                            fluid.getIntOr("amount", 0), fluid.getIntOr("capacity", 0))
                            .withStyle(ChatFormatting.AQUA));
            }
            if (data.contains(key("comparator")))
                tooltip.add(Component.translatable("jade.reactorcraft.comparator",
                        data.getIntOr(key("comparator"), 0)).withStyle(ChatFormatting.GRAY));
        }

    }

    private static String key(String suffix) {
        return PREFIX + suffix;
    }
}

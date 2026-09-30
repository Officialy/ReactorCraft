package reika.reactorcraft.container;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import reika.reactorcraft.ReactorCraft;

/** A menu-owned rod display, independent of the client's loaded chunks and dimension. */
public record CPUViewPayload(int containerId, GlobalPos controller, Map<BlockPos, Integer> rods) implements CustomPacketPayload {
    public static final Type<CPUViewPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "cpu_view"));
    public static final StreamCodec<FriendlyByteBuf, CPUViewPayload> STREAM_CODEC = StreamCodec.of(
            (data, view) -> view.write(data), CPUViewPayload::read);

    public CPUViewPayload { rods = Map.copyOf(rods); }
    @Override
    public Type<CPUViewPayload> type() { return TYPE; }

    public void write(FriendlyByteBuf data) {
        data.writeVarInt(containerId);
        GlobalPos.STREAM_CODEC.encode(data, controller);
        data.writeVarInt(rods.size());
        rods.forEach((pos, color) -> { data.writeBlockPos(pos); data.writeInt(color); });
    }

    public static CPUViewPayload read(FriendlyByteBuf data) {
        int id = data.readVarInt();
        GlobalPos cpu = GlobalPos.STREAM_CODEC.decode(data);
        int count = data.readVarInt();
        if (count < 0 || count > 8192)
            throw new IllegalArgumentException("Invalid CPU rod count: " + count);
        Map<BlockPos, Integer> rods = new HashMap<>();
        for (int i = 0; i < count; i++)
            rods.put(data.readBlockPos(), data.readInt());
        return new CPUViewPayload(id, cpu, rods);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(ReactorCraft.MODID).versioned("1").playToClient(TYPE, STREAM_CODEC, (view, context) -> {
            if (context.player().containerMenu instanceof MenuCPU menu && menu.containerId == view.containerId()
                    && menu.getController().equals(view.controller()))
                menu.applyView(view);
        });
    }
}

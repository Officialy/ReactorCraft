/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.dragonapi.auxiliary.PacketTypes;
import reika.dragonapi.interfaces.PacketHandler;
import reika.dragonapi.libraries.io.ReikaChatHelper;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.dragonapi.libraries.io.ReikaPacketHelper.PacketObj;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.registry.ReactorPackets;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;

/**
 * 26.2 port of the legacy ReactorCraft packet handler. Mirrors RotaryCraft's {@code PacketHandlerCore}:
 * decodes the channel byte-stream by {@link PacketTypes}, then dispatches by {@link ReactorPackets}.
 * Registered against {@link ReactorCraft#packetChannel} via {@code ReikaPacketHelper.registerPacketHandler}.
 *
 * Drives the control-rod GUI (OFF-49): CPUTOGGLE toggles one rod, CPURAISE/CPULOWER move all rods on
 * the CPU; ORERADIATION irradiates an ore on harvest.
 */
public class ReactorPacketCore implements PacketHandler {

    protected ReactorPackets pack;

    @Override
    public void handleData(PacketObj packet, Level world, Player ep) {
        DataInputStream inputStream = packet.getDataIn();
        int control;
        int len;
        int[] data = new int[0];
        long longdata = 0;
        float floatdata = 0;
        int x = 0;
        int y = 0;
        int z = 0;
        double dx = 0;
        double dy = 0;
        double dz = 0;
        boolean readinglong;
        String stringdata = null;
        UUID id = null;
        try {
            PacketTypes packetType = packet.getType();
            switch (packetType) {
                case FULLSOUND:
                    break;
                case SOUND:
                    return;
                case STRING:
                    stringdata = packet.readString();
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    break;
                case DATA:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    len = pack.getNumberDataInts();
                    data = new int[len];
                    readinglong = pack.isLongPacket();
                    if (!readinglong) {
                        for (int i = 0; i < len; i++)
                            data[i] = inputStream.readInt();
                    } else
                        longdata = inputStream.readLong();
                    break;
                case POS:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    dx = inputStream.readDouble();
                    dy = inputStream.readDouble();
                    dz = inputStream.readDouble();
                    len = pack.getNumberDataInts();
                    if (len > 0) {
                        data = new int[len];
                        for (int i = 0; i < len; i++)
                            data[i] = inputStream.readInt();
                    }
                    break;
                case UPDATE:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    break;
                case FLOAT:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    floatdata = inputStream.readFloat();
                    break;
                case SYNC:
                    String name = packet.readString();
                    x = inputStream.readInt();
                    y = inputStream.readInt();
                    z = inputStream.readInt();
                    ReikaPacketHelper.updateBlockEntityData(world, x, y, z, name, inputStream);
                    return;
                case TANK:
                    String tank = packet.readString();
                    x = inputStream.readInt();
                    y = inputStream.readInt();
                    z = inputStream.readInt();
                    int level = inputStream.readInt();
                    String fluid = ReikaPacketHelper.readString(inputStream);
                    ReikaPacketHelper.updateBlockEntityTankData(world, x, y, z, tank, level, fluid);
                    return;
                case RAW:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    len = pack.getNumberDataInts();
                    data = new int[len];
                    readinglong = pack.isLongPacket();
                    if (!readinglong) {
                        for (int i = 0; i < len; i++)
                            data[i] = inputStream.readInt();
                    } else
                        longdata = inputStream.readLong();
                    break;
                case PREFIXED:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    len = inputStream.readInt();
                    data = new int[len];
                    for (int i = 0; i < len; i++)
                        data[i] = inputStream.readInt();
                    break;
                case NBT:
                    break;
                case STRINGINT:
                case STRINGINTLOC:
                    stringdata = packet.readString();
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    data = new int[pack.getNumberDataInts()];
                    for (int i = 0; i < data.length; i++)
                        data[i] = inputStream.readInt();
                    break;
                case UUID:
                    control = inputStream.readInt();
                    pack = ReactorPackets.getEnum(control);
                    long l1 = inputStream.readLong(); //most
                    long l2 = inputStream.readLong(); //least
                    id = new UUID(l1, l2);
                    break;
            }
            if (packetType.hasCoordinates()) {
                x = inputStream.readInt();
                y = inputStream.readInt();
                z = inputStream.readInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            switch (pack) {
                case CPUTOGGLE:
                case CPURAISE:
                case CPULOWER:
                    if (ep.containerMenu instanceof reika.reactorcraft.container.MenuCPU menu)
                        menu.handleControl(ep, pack, new BlockPos(x, y, z));
                    break;
                case ORERADIATION:
                    RadiationEffects.instance.doOreIrradiation(world, x, y, z, ep);
                    break;
            }
        } catch (Exception e) {
            ReactorCraft.LOGGER.error("Machine/item was deleted before its packet could be received!");
            ReikaChatHelper.writeString("Machine/item was deleted before its packet could be received!");
            e.printStackTrace();
        }
    }
}

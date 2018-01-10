package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyTagCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage implements IMessage, IMessageHandler<UpdateChunkCapabilityMessage, IMessage>
{
    /**
     * The colony.
     */
    private int owningColonyId;

    /**
     * X Position of the chunk.
     */
    private int x;

    /**
     * Z Position of the chunk.
     */
    private int z;

    /**
     * The list of the close colonies.
     */
    private List<Integer> closeColonies;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Creates a message to handle chunk compatabilities..
     * @param colonyId the colony.
     * @param pos the chunk pos.
     */
    public UpdateChunkCapabilityMessage(@NotNull final IColonyTagCapability tagCapability, final int x, final int z)
    {
        this.x = x;
        this.z = z;
        this.owningColonyId = tagCapability.getOwningColony();
        this.closeColonies = tagCapability.getAllCloseColonies();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        x = buf.readInt();
        z = buf.readInt();
        owningColonyId = buf.readInt();
        int size = buf.readInt();
        closeColonies = new ArrayList<>();
        for(int i = 0; i < size; i++)
        {
            closeColonies.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(owningColonyId);
        buf.writeInt(closeColonies.size());
        for(final int id: closeColonies)
        {
            buf.writeInt(id);
        }
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final UpdateChunkCapabilityMessage message, final MessageContext ctx)
    {
        if(ctx.getClientHandler().world != null)
        {
            final Chunk chunk = ctx.getClientHandler().world.getChunkFromChunkCoords(message.x, message.z);
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);
            cap.setOwningColony(message.owningColonyId);
            for (final int id : message.closeColonies)
            {
                cap.addColony(id);
            }
        }
        return null;
    }
}
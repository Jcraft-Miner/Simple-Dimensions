package lumien.simpledimensions.network.messages;

import io.netty.buffer.ByteBuf;
import lumien.simpledimensions.util.WorldInfoSimple;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageCreateDimension implements IMessage
{
	WorldInfoSimple worldInfo;

	public MessageCreateDimension()
	{

	}

	public MessageCreateDimension(WorldInfoSimple worldInfo)
	{
		this.worldInfo = worldInfo;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.worldInfo = new WorldInfoSimple(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, worldInfo.cloneNBTCompound(null));
	}

	public WorldInfoSimple getWorldInfo()
	{
		return worldInfo;
	}

}

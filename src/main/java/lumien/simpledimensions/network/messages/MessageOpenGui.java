package lumien.simpledimensions.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageOpenGui implements IMessage
{
	int guiID;

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.guiID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(guiID);
	}

	public int getGuiID()
	{
		return guiID;
	}

	public MessageOpenGui setGuiID(int guiID)
	{
		this.guiID = guiID;

		return this;
	}

}

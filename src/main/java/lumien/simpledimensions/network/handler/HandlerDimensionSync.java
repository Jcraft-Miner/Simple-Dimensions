package lumien.simpledimensions.network.handler;

import java.util.HashMap;

import lumien.simpledimensions.client.ClientHandler;
import lumien.simpledimensions.network.messages.MessageDimensionSync;
import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerDimensionSync implements IMessageHandler<MessageDimensionSync, IMessage>
{

	@Override
	public IMessage onMessage(MessageDimensionSync message, MessageContext ctx)
	{
		final HashMap<Integer, DimensionType> dimensions = message.getDimensions();
		Minecraft.getMinecraft().addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				ClientHandler.getInstance().sync(dimensions);
			}
		});

		return null;
	}

}

package lumien.simpledimensions.network.handler;

import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.network.messages.MessageCreateDimension;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerCreateDimension implements IMessageHandler<MessageCreateDimension, IMessage>
{

	@Override
	public IMessage onMessage(final MessageCreateDimension message, final MessageContext ctx)
	{
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				NetHandlerPlayServer netHandler = (NetHandlerPlayServer) ctx.netHandler;

				if (netHandler.player.canUseCommand(3, "simpledimensions"))
				{
					DimensionHandler.getInstance().createDimension(((NetHandlerPlayServer) ctx.netHandler).player, message.getWorldInfo());
				}
			}
		});

		return null;
	}

}

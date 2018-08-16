package lumien.simpledimensions.network.handler;

import lumien.simpledimensions.client.gui.GuiCreateDimension;
import lumien.simpledimensions.network.messages.MessageOpenGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandlerOpenGui implements IMessageHandler<MessageOpenGui, IMessage>
{

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageOpenGui message, MessageContext ctx)
	{
		final int id = message.getGuiID();

		Minecraft.getMinecraft().addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				switch (id)
				{
					case 0:
						Minecraft.getMinecraft().displayGuiScreen(new GuiCreateDimension(Minecraft.getMinecraft().ingameGUI));
						break;
				}
			}

		});

		return null;
	}

}

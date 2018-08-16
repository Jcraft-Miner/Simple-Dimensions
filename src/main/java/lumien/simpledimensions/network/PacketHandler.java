package lumien.simpledimensions.network;

import lumien.simpledimensions.lib.Reference;
import lumien.simpledimensions.network.handler.HandlerCreateDimension;
import lumien.simpledimensions.network.handler.HandlerDimensionSync;
import lumien.simpledimensions.network.handler.HandlerOpenGui;
import lumien.simpledimensions.network.messages.MessageCreateDimension;
import lumien.simpledimensions.network.messages.MessageDimensionSync;
import lumien.simpledimensions.network.messages.MessageOpenGui;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
	public static SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Reference.MOD_ID);

	public static void init()
	{
		INSTANCE.registerMessage(HandlerCreateDimension.class, MessageCreateDimension.class, 0, Side.SERVER);
		INSTANCE.registerMessage(HandlerOpenGui.class, MessageOpenGui.class, 1, Side.CLIENT);
		INSTANCE.registerMessage(HandlerDimensionSync.class, MessageDimensionSync.class, 2, Side.CLIENT);
	}
}

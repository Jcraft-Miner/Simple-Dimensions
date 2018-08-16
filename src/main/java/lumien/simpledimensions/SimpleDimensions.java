package lumien.simpledimensions;

import lumien.simpledimensions.client.ClientHandler;
import lumien.simpledimensions.config.SimpleDimensionsConfig;
import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.lib.Reference;
import lumien.simpledimensions.network.PacketHandler;
import lumien.simpledimensions.server.commands.CommandSimpleDimensions;
import lumien.simpledimensions.server.commands.CommandTeleportD;
import lumien.simpledimensions.server.commands.CommandTimeD;
import lumien.simpledimensions.server.commands.CommandWeatherD;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class SimpleDimensions
{
	@Instance(value = Reference.MOD_ID)
	public static SimpleDimensions INSTANCE;

	@SidedProxy(clientSide = "lumien.simpledimensions.client.ClientProxy", serverSide = "lumien.simpledimensions.CommonProxy")
	public static CommonProxy proxy;

	public SimpleDimensionsConfig config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);

		config = new SimpleDimensionsConfig();

		config.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		PacketHandler.init();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandSimpleDimensions());
		event.registerServerCommand(new CommandWeatherD());
		event.registerServerCommand(new CommandTimeD());
		event.registerServerCommand(new CommandTeleportD());

		DimensionHandler.getInstance().loadDimensions();
	}

	@SubscribeEvent
	public void clientConnect(ServerConnectionFromClientEvent event)
	{
		event.getManager().sendPacket(PacketHandler.INSTANCE.getPacketFrom(DimensionHandler.getInstance().constructSyncMessage()));
	}

	@SubscribeEvent
	public void clientDisconnect(ClientDisconnectionFromServerEvent event)
	{
		if (!event.getManager().isLocalChannel())
		{
			ClientHandler.getInstance().cleanUp();
		}
	}
}

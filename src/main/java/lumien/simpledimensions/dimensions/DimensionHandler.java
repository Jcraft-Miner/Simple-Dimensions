package lumien.simpledimensions.dimensions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;

import lumien.simpledimensions.SimpleDimensions;
import lumien.simpledimensions.network.PacketHandler;
import lumien.simpledimensions.network.messages.MessageDimensionSync;
import lumien.simpledimensions.server.WorldCustom;
import lumien.simpledimensions.util.TeleporterSimple;
import lumien.simpledimensions.util.WorldInfoSimple;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class DimensionHandler extends WorldSavedData
{
	static String NAME = "SimpleDimensionsHandler";

	HashMap<Integer, WorldInfoSimple> dimensionInfo;

	public DimensionHandler(String name)
	{
		super(name);

		dimensionInfo = new HashMap<>();
	}

	public DimensionHandler()
	{
		super(NAME);

		dimensionInfo = new HashMap<>();
	}

	@Override
	public boolean isDirty()
	{
		return true;
	}
	
	public List<Integer> getDimensionIds()
	{
		ArrayList<Integer> dimensionIDs = new ArrayList<Integer>();
		
		dimensionIDs.addAll(dimensionInfo.keySet());
		return dimensionIDs;
	}

	public void createDimension(EntityPlayerMP playerEntity, WorldInfoSimple worldInfo)
	{
		int dimensionID = findFreeDimensionID();

		dimensionInfo.put(dimensionID, worldInfo);

		DimensionType dimensionType = worldInfo.getDimensionType();

		DimensionManager.registerDimension(dimensionID, dimensionType);

		loadDimension(dimensionID, worldInfo);

		playerEntity.sendMessage(new TextComponentString(String.format("Created %s using id %s", worldInfo.getWorldName(), dimensionID)).setStyle(new Style().setColor(TextFormatting.GREEN)));

		syncWithClients();
	}

	private int findFreeDimensionID()
	{
		HashSet<Integer> ids = new HashSet<>();
		ids.addAll(Arrays.asList(DimensionManager.getStaticDimensionIDs()));

		int currentID = SimpleDimensions.INSTANCE.config.startDimensionID();
		while (true)
		{
			if (!ids.contains(currentID))
			{
				return currentID;
			}
			else
			{
				currentID++;
			}
		}
	}

	private static String getDisplayableName(String input)
	{
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;

		for (char c : input.replace("_", " ").toCharArray())
		{
			if (Character.isSpaceChar(c))
			{
				nextTitleCase = true;
			}
			else if (nextTitleCase)
			{
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}

			titleCase.append(c);
		}

		return titleCase.toString();
	}

	public ITextComponent generateList()
	{
		StringBuilder stringBuilder = new StringBuilder();

		if (dimensionInfo.isEmpty())
		{
			return new TextComponentTranslation("simpleDimensions.nodimensions");
		}
		else
		{
			int counter = 0;
			for (Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
			{
				DimensionType dimensionType = entry.getValue().getDimensionType();

				stringBuilder.append(String.format("%s %s", "DIM " + entry.getKey(), "(" + entry.getValue().getWorldName() + ") (" + getDisplayableName(dimensionType.getName()) + ")"));
				counter++;
				if (counter < dimensionInfo.size())
				{
					stringBuilder.append("\n");
				}
			}

			return new TextComponentString(stringBuilder.toString());
		}
	}

	public static DimensionHandler getInstance()
	{
		DimensionHandler INSTANCE;
		INSTANCE = (DimensionHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().getOrLoadData(DimensionHandler.class, NAME);

		if (INSTANCE == null)
		{
			INSTANCE = new DimensionHandler();
			FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().setData(NAME, INSTANCE);
		}

		return INSTANCE;
	}

	public String getDimensionName(int dimensionId)
	{
		return dimensionInfo.get(dimensionId).getWorldName();
	}

	public HashMap<Integer, WorldInfoSimple> getDimensionInfo()
	{
		return dimensionInfo;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList nbtList = nbt.getTagList("dimensionInfo", 10);

		for (int i = 0; i < nbtList.tagCount(); i++)
		{
			NBTTagCompound compound = nbtList.getCompoundTagAt(i);

			dimensionInfo.put(compound.getInteger("dimensionID"), new WorldInfoSimple(compound.getCompoundTag("worldInfo")));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList nbtList = new NBTTagList();

		for (Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
		{
			NBTTagCompound compound = new NBTTagCompound();

			compound.setInteger("dimensionID", entry.getKey());
			compound.setTag("worldInfo", entry.getValue().cloneNBTCompound(null));

			nbtList.appendTag(compound);
		}

		nbt.setTag("dimensionInfo", nbtList);

		return nbt;
	}

	public void loadDimensions()
	{
		for (Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
		{
			int dimensionID = entry.getKey();
			WorldInfoSimple worldInfo = entry.getValue();

			DimensionManager.registerDimension(dimensionID, worldInfo.getDimensionType());

			System.out.println(worldInfo.getDimensionType());

			loadDimension(dimensionID, worldInfo);
		}
	}

	private void loadDimension(int dimensionID, WorldInfo worldInfo)
	{
		WorldServer overworld = (WorldServer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
		if (overworld == null)
		{
			throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");
		}
		try
		{
			DimensionManager.getProviderType(dimensionID);
		}
		catch (Exception e)
		{
			System.err.println("Cannot Hotload Dim: " + e.getMessage());
			return;
		}

		MinecraftServer mcServer = overworld.getMinecraftServer();
		ISaveHandler savehandler = overworld.getSaveHandler();
		EnumDifficulty difficulty = mcServer.getEntityWorld().getDifficulty();

		WorldServer world = (WorldServer) (new WorldCustom(worldInfo, mcServer, savehandler, dimensionID, overworld, mcServer.profiler).init());
		world.addEventListener(new ServerWorldEventHandler(mcServer, world));
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

		if (!mcServer.isSinglePlayer())
		{
			world.getWorldInfo().setGameType(mcServer.getGameType());
		}

		mcServer.setDifficultyForAllWorlds(difficulty);
	}

	public void deleteDimension(ICommandSender sender, int dimensionID)
	{
		WorldServer w = DimensionManager.getWorld(dimensionID);

		if (!dimensionInfo.containsKey(dimensionID))
		{
			if (w == null)
			{
				sender.sendMessage(new TextComponentString("No dimension with that id exists").setStyle(new Style().setColor(TextFormatting.RED)));
			}
			else
			{
				sender.sendMessage(new TextComponentString("The dimension associated with that id is not from the SimpleDimensions mod").setStyle(new Style().setColor(TextFormatting.RED)));
			}
			return;
		}

		if (w == null)
		{
			if (DimensionManager.isDimensionRegistered(dimensionID))
			{
				loadDimension(dimensionID, dimensionInfo.get(dimensionID));
				w = DimensionManager.getWorld(dimensionID);
				if (w == null)
				{
					sender.sendMessage(new TextComponentString("Failed to load dimension").setStyle(new Style().setColor(TextFormatting.RED)));
					return;
				}
			}
		}

		if (!w.playerEntities.isEmpty())
		{
			WorldServer overworld = DimensionManager.getWorld(0);
			MinecraftServer mcserver = overworld.getMinecraftServer();
			PlayerList plist = mcserver.getPlayerList();
			BlockPos defaultspawnpoint = overworld.getSpawnPoint();
			ArrayList<EntityPlayer> currentPlayers = new ArrayList<>();
			currentPlayers.addAll(w.playerEntities);
			for (EntityPlayer player : currentPlayers)
			{
				BlockPos spawnpoint = new BlockPos(defaultspawnpoint.getX(), overworld.getHeight(defaultspawnpoint.getX(), defaultspawnpoint.getZ()), defaultspawnpoint.getZ());
				BlockPos bedlocation = player.getBedLocation();

				if (bedlocation != null)
				{
					BlockPos bedspawnlocation = EntityPlayer.getBedSpawnLocation(overworld, bedlocation, false);
					if (bedspawnlocation != null)
					{
						spawnpoint = bedspawnlocation;
					}
				}

				EnumSet enumset = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

				player.dismountRidingEntity();
				((EntityPlayerMP) player).connection.setPlayerLocation(spawnpoint.getX(), spawnpoint.getY(), spawnpoint.getZ(), 0, 0, enumset);

				plist.transferPlayerToDimension((EntityPlayerMP) player, 0, new TeleporterSimple(overworld));

				if (sender != player)
				{
					player.sendMessage(new TextComponentString("The dimension you were in was deleted").setStyle(new Style().setColor(TextFormatting.RED)));
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(w));
		w.flush();
		DimensionManager.setWorld(dimensionID, null, w.getMinecraftServer());
		DimensionManager.unregisterDimension(dimensionID);

		dimensionInfo.remove(dimensionID);
		w.flush();

		File dimensionFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), "DIM" + dimensionID);

		try
		{
			FileUtils.deleteDirectory(dimensionFolder);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			sender.sendMessage(new TextComponentString("Error deleting dimension folder of " + dimensionID + ". Has to be removed manually.").setStyle(new Style().setColor(TextFormatting.RED)));
		}
		finally
		{
			sender.sendMessage(new TextComponentString("Completely deleted dimension " + dimensionID).setStyle(new Style().setColor(TextFormatting.GREEN)));
		}

		syncWithClients();
	}

	private void syncWithClients()
	{
		MessageDimensionSync message = new MessageDimensionSync();

		for (Map.Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
		{
			message.addDimension(entry.getKey(), entry.getValue().getDimensionType());
		}

		PacketHandler.INSTANCE.sendToAll(message);
	}

	public IMessage constructSyncMessage()
	{
		MessageDimensionSync message = new MessageDimensionSync();

		for (Map.Entry<Integer, WorldInfoSimple> entry : dimensionInfo.entrySet())
		{
			message.addDimension(entry.getKey(), entry.getValue().getDimensionType());
		}

		return message;
	}
}

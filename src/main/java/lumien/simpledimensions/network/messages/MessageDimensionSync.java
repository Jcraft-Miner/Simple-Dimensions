package lumien.simpledimensions.network.messages;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageDimensionSync implements IMessage
{
	HashMap<Integer, DimensionType> simpleDimensions;

	public MessageDimensionSync()
	{
		simpleDimensions = new HashMap<>();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		for (String key : tag.getKeySet())
		{
			int dimID;
			DimensionType dimType = DimensionType.OVERWORLD;
			try
			{
				dimID = Integer.parseInt(key);
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			String dimTypeS = tag.getString(key);
			if (dimTypeS != null)
			{
				try
				{
					dimType = DimensionType.byName(dimTypeS);
				}
				catch (IllegalArgumentException e)
				{
				}
			}

			simpleDimensions.put(dimID, dimType);
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		NBTTagCompound tag = new NBTTagCompound();
		for (Map.Entry<Integer, DimensionType> entry : simpleDimensions.entrySet())
		{
			tag.setString(Integer.toString(entry.getKey()), entry.getValue().getName());
		}
		ByteBufUtils.writeTag(buf, tag);
	}

	public void addDimension(int id, DimensionType type)
	{
		if (type == null)
		{
			type = DimensionType.OVERWORLD;
		}
		simpleDimensions.put(id, type);
	}

	public HashMap<Integer, DimensionType> getDimensions()
	{
		return simpleDimensions;
	}
}

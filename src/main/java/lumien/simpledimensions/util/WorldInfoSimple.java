package lumien.simpledimensions.util;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldInfoSimple extends WorldInfo
{
	WorldInfo superInfo;
	DimensionType dimensionType;

	public WorldInfoSimple(NBTTagCompound nbt)
	{
		super(nbt);

		this.dimensionType = DimensionType.OVERWORLD;

		if (nbt.hasKey("dimType"))
		{
			String dimTypeS = nbt.getString("dimType");
			if (dimTypeS != null)
			{
				try
				{
					DimensionType dimType = DimensionType.byName(dimTypeS);
					this.dimensionType = dimType;
				}
				catch (IllegalArgumentException e)
				{
				}
			}
		}

		superInfo = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo();
	}

	public WorldInfoSimple(WorldSettings settings, String name, DimensionType dimType)
	{
		super(settings, name);

		if (dimType == null)
		{
			this.dimensionType = DimensionType.OVERWORLD;
		}
		else
		{
			this.dimensionType = dimType;
		}
	}

	@Override
	public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt)
	{
		NBTTagCompound superNbt = super.cloneNBTCompound(nbt);
		superNbt.setString("dimType", this.dimensionType.getName());
		return superNbt;
	}

	@Override
	public NBTTagCompound getPlayerNBTTagCompound()
	{
		return superInfo.getPlayerNBTTagCompound();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public long getLastTimePlayed()
	{
		return superInfo.getLastTimePlayed();
	}

	@Override
	public GameType getGameType()
	{
		return superInfo.getGameType();
	}

	@Override
	public boolean isHardcoreModeEnabled()
	{
		return superInfo.isHardcoreModeEnabled();
	}

	@Override
	public boolean areCommandsAllowed()
	{
		return superInfo.areCommandsAllowed();
	}

	@Override
	public GameRules getGameRulesInstance()
	{
		return superInfo.getGameRulesInstance();
	}

	@Override
	public EnumDifficulty getDifficulty()
	{
		return superInfo.getDifficulty();
	}

	@Override
	public boolean isDifficultyLocked()
	{
		return superInfo.isDifficultyLocked();
	}

	public DimensionType getDimensionType()
	{
		return dimensionType;
	}
}

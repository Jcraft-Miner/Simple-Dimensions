package lumien.simpledimensions.server.commands;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;

import lumien.simpledimensions.util.TeleporterSimple;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CommandTeleportD extends CommandBase
{
	static Method copyDataFromOld;

	static
	{
		try
		{
			copyDataFromOld = Entity.class.getDeclaredMethod((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") ? "copyDataFromOld" : "func_180432_n", Entity.class);
			copyDataFromOld.setAccessible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String getName()
	{
		return "tpd";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "simpleDimensions.commands.tpd.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 1)
		{
			throw new WrongUsageException("simpleDimensions.commands.tpd.usage", new Object[0]);
		}
		else
		{
			byte b0 = 2;
			Object object = null;

			boolean dimensionThere = false;
			int dimensionID = 0;
			if (args.length > 0)
			{
				try
				{
					dimensionID = Integer.parseInt(args[0]);
					dimensionThere = true;
				}
				catch (NumberFormatException exception)
				{

				}
			}

			if (dimensionThere)
			{
				if (args.length == 1)
				{
					object = sender;
					if (object instanceof EntityPlayerMP)
					{
						EntityPlayerMP player = (EntityPlayerMP) object;

						if (player.dimension != dimensionID)
						{
							teleportEntityToDimension(server, player, dimensionID);
						}

						return;
					}
				}

				if (args.length == 5 || args.length == 7)
				{
					object = getEntity(server, sender, args[1]);
					b0 = 2;
				}
				else
				{
					object = getCommandSenderAsPlayer(sender);
					b0 = 1;
				}
			}
			else
			{
				if (args.length == 1)
				{
					object = getCommandSenderAsPlayer(sender);
				}
				else if (args.length == 2)
				{
					object = getEntity(server, sender, args[0]);
				}
			}

			if (args.length != 1 && args.length != 2)
			{
				if (args.length < b0 + 3 || !dimensionThere)
				{
					throw new WrongUsageException("simpleDimensions.commands.tpd.usage", new Object[0]);
				}
				else if (((Entity) object).world != null)
				{
					int i = b0 + 1;
					CommandBase.CoordinateArg coordinatearg = parseCoordinate(((Entity) object).posX, args[b0], true);
					CommandBase.CoordinateArg coordinatearg1 = parseCoordinate(((Entity) object).posY, args[i++], 0, 0, false);
					CommandBase.CoordinateArg coordinatearg2 = parseCoordinate(((Entity) object).posZ, args[i++], true);
					CommandBase.CoordinateArg coordinatearg3 = parseCoordinate(((Entity) object).rotationYaw, args.length > i ? args[i++] : "~", false);
					CommandBase.CoordinateArg coordinatearg4 = parseCoordinate(((Entity) object).rotationPitch, args.length > i ? args[i] : "~", false);
					float f;

					if (((Entity) object).dimension != dimensionID)
					{
						object = teleportEntityToDimension(server, (Entity) object, dimensionID);
					}

					if (object instanceof EntityPlayerMP)
					{
						EnumSet enumset = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

						if (coordinatearg.isRelative())
						{
							enumset.add(SPacketPlayerPosLook.EnumFlags.X);
						}

						if (coordinatearg1.isRelative())
						{
							enumset.add(SPacketPlayerPosLook.EnumFlags.Y);
						}

						if (coordinatearg2.isRelative())
						{
							enumset.add(SPacketPlayerPosLook.EnumFlags.Z);
						}

						if (coordinatearg4.isRelative())
						{
							enumset.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
						}

						if (coordinatearg3.isRelative())
						{
							enumset.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
						}

						f = (float) coordinatearg3.getAmount();

						if (!coordinatearg3.isRelative())
						{
							f = MathHelper.wrapDegrees(f);
						}

						float f1 = (float) coordinatearg4.getAmount();

						if (!coordinatearg4.isRelative())
						{
							f1 = MathHelper.wrapDegrees(f1);
						}

						if (f1 > 90.0F || f1 < -90.0F)
						{
							f1 = MathHelper.wrapDegrees(180.0F - f1);
							f = MathHelper.wrapDegrees(f + 180.0F);
						}

						((Entity) object).dismountRidingEntity();
						((EntityPlayerMP) object).connection.setPlayerLocation(coordinatearg.getAmount(), coordinatearg1.getAmount(), coordinatearg2.getAmount(), f, f1, enumset);
						((Entity) object).setRotationYawHead(f);
					}
					else
					{
						float f2 = (float) MathHelper.wrapDegrees(coordinatearg3.getResult());
						f = (float) MathHelper.wrapDegrees(coordinatearg4.getResult());

						if (f > 90.0F || f < -90.0F)
						{
							f = MathHelper.wrapDegrees(180.0F - f);
							f2 = MathHelper.wrapDegrees(f2 + 180.0F);
						}

						((Entity) object).setLocationAndAngles(coordinatearg.getResult(), coordinatearg1.getResult(), coordinatearg2.getResult(), f2, f);
						((Entity) object).setRotationYawHead(f2);

						((Entity) object).world.updateEntityWithOptionalForce((Entity) object, false);
					}

					notifyCommandListener(sender, this, "commands.tp.success.coordinates", new Object[] { ((Entity) object).getName(), Double.valueOf(coordinatearg.getResult()), Double.valueOf(coordinatearg1.getResult()), Double.valueOf(coordinatearg2.getResult()) });
				}
			}
			else
			{
				Entity entity = getEntity(server, sender, args[args.length - 1]);

				if (entity.world != ((Entity) object).world)
				{
					Entity toTeleport = (Entity) object;
					if (entity.dimension != toTeleport.dimension)
					{
						object = teleportEntityToDimension(server, toTeleport, entity.dimension);
					}
				}

				((Entity) object).dismountRidingEntity();
				;

				if (object instanceof EntityPlayerMP)
				{
					((EntityPlayerMP) object).connection.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				}
				else
				{
					((Entity) object).setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				}

				notifyCommandListener(sender, this, "commands.tp.success", new Object[] { ((Entity) object).getName(), entity.getName() });
			}
		}
	}

	private Entity teleportEntityToDimension(MinecraftServer server, Entity entity, int dimension) throws CommandException
	{
		World world = server.getWorld(dimension);

		if (world == null)
		{
			throw new CommandException("Couldn't find dimension " + dimension);
		}

		if (entity instanceof EntityPlayerMP)
		{
			server.getPlayerList().transferPlayerToDimension((EntityPlayerMP) entity, dimension, new TeleporterSimple((WorldServer) server.getEntityWorld()));
			return entity;
		}
		else
		{
			return travelEntity(server, entity, dimension);
		}
	}

	private Entity travelEntity(MinecraftServer server, Entity entity, int dimensionId)
	{
		if (!entity.world.isRemote && !entity.isDead)
		{
			entity.world.profiler.startSection("changeDimension");
			int j = entity.dimension;
			WorldServer worldserver = server.getWorld(j);
			WorldServer worldserver1 = server.getWorld(dimensionId);
			entity.dimension = dimensionId;

			Entity newEntity = EntityList.createEntityByIDFromName(EntityList.getKey(entity.getClass()), worldserver1);

			if (newEntity != null)
			{
				try
				{
					copyDataFromOld.invoke(newEntity, entity);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				entity.world.removeEntity(entity);

				newEntity.forceSpawn = true;

				worldserver1.spawnEntity(newEntity);
			}

			worldserver1.updateEntityWithOptionalForce(newEntity, true);

			entity.isDead = true;
			entity.world.profiler.endSection();
			worldserver.resetUpdateEntityTick();
			worldserver1.resetUpdateEntityTick();
			entity.world.profiler.endSection();
			return newEntity;
		}

		return entity;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length != 1 && args.length != 2 ? null : getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		return index == 0 || index == 1;
	}
}
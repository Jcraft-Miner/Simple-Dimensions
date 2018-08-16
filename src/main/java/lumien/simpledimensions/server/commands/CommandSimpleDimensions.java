package lumien.simpledimensions.server.commands;

import java.util.ArrayList;
import java.util.List;

import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.network.PacketHandler;
import lumien.simpledimensions.network.messages.MessageOpenGui;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.FakePlayer;

public class CommandSimpleDimensions extends CommandBase
{

	@Override
	public String getName()
	{
		return "simpledimensions";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "Usage: /simpledimensions <create:delete:list>";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 3;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0)
		{
			sender.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		}

		if (args[0].equals("create"))
		{
			if (sender instanceof EntityPlayerMP && !(sender instanceof FakePlayer))
			{
				EntityPlayerMP player = (EntityPlayerMP) sender;

				PacketHandler.INSTANCE.sendTo(new MessageOpenGui().setGuiID(0), player);
			}
		}
		else if (args[0].equals("delete"))
		{
			if (args.length == 1)
			{
				sender.sendMessage(new TextComponentString("Usage: /simpledimensions delete <id>"));
			}
			if (args.length == 2)
			{
				int dimensionID = Integer.parseInt(args[1]);

				DimensionHandler.getInstance().deleteDimension(sender, dimensionID);
			}
		}
		else if (args[0].equals("list"))
		{
			sender.sendMessage(DimensionHandler.getInstance().generateList());
		}
		else
		{
			sender.sendMessage(new TextComponentString(getUsage(sender)));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, "create", "delete", "list");
		}
		else
		{
			return new ArrayList<>();
		}
	}
}

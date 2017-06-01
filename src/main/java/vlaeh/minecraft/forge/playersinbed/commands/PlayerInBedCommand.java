package vlaeh.minecraft.forge.playersinbed.commands;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import vlaeh.minecraft.forge.playersinbed.PlayerInBed;

public class PlayerInBedCommand extends CommandBase {
	public static final String COMMAND = "playerInBed";
	public static final String USAGE = "/" + COMMAND + " enabled|ratio|clearWeather|sendMessageToChat|sendMessageToStatus|setSpawnDuringDay [new_value]";
	public static final String NOT_OPPED = "You must be opped to use this command";

	@Override
	public String getName() {
		return "playerInBed";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}

	public void usage(final ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Usage: " + USAGE).setStyle(new Style().setColor(TextFormatting.RED)));
	}
	
	public final boolean playerIsAllowed(final ICommandSender sender) {
        if ((! sender.getEntityWorld().isRemote) && (FMLCommonHandler.instance().getSide() == Side.CLIENT)) 
        	return true;
        if (! (sender instanceof EntityPlayer))
        	return true;
        for (String player : sender.getServer().getPlayerList().getOppedPlayerNames())
        	if (player.equals(sender.getName()))
                    return true;
        return false;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
        if (args.length == 0 || args.length >= 3)
        {
        	usage(sender);
            return;
        }
        if (args.length == 1) {
        	if (args[0].equalsIgnoreCase("enabled"))
                sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + PlayerInBed.skipEnabled));
        	else if (args[0].equalsIgnoreCase("ratio"))
                sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + PlayerInBed.ratio));
        	else if (args[0].equalsIgnoreCase("clearWeather"))
                sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + PlayerInBed.clearWeather));
        	else if (args[0].equalsIgnoreCase("sendMessageToChat"))
                sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + PlayerInBed.messageEnabled));
        	else if (args[0].equalsIgnoreCase("sendMessageToStatus"))
                sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + PlayerInBed.statusEnabled));
        	else if (args[0].equalsIgnoreCase("setSpawnDuringDay"))
        		sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + PlayerInBed.setSpawnDuringDayEnabled));
        	else
        		usage(sender);
        	return;
        }
        // args.length == 2
        if (! playerIsAllowed(sender)) {
        	sender.sendMessage(new TextComponentString(NOT_OPPED).setStyle(new Style().setColor(TextFormatting.RED)));
        	return;
        }
        if (args[0].equalsIgnoreCase("enabled")) { 
        	PlayerInBed.config.get(Configuration.CATEGORY_GENERAL, "1.enabled", PlayerInBed.skipEnabled).set(Boolean.parseBoolean(args[1]));
        	PlayerInBed.syncConfig();
        	sender.sendMessage(new TextComponentString("PlayerInBed enabled set to " + TextFormatting.GREEN + PlayerInBed.skipEnabled));
        	return;
        } else if (args[0].equalsIgnoreCase("ratio")) { 
        	try {
        		PlayerInBed.config.get(Configuration.CATEGORY_GENERAL, "2.ratio", PlayerInBed.ratio).set(Math.min(100, Math.max(0, Integer.parseInt(args[1]))));
            	PlayerInBed.syncConfig();
            	sender.sendMessage(new TextComponentString("PlayerInBed ratio set to " + TextFormatting.GREEN + PlayerInBed.ratio));
        		return;
        	} catch (NumberFormatException e) {
        	}
        } else if (args[0].equalsIgnoreCase("clearWeather")) { 
        	try {
        		PlayerInBed.config.get(Configuration.CATEGORY_GENERAL, "3.clearWeather", PlayerInBed.clearWeather).set(Boolean.parseBoolean(args[1]));
            	PlayerInBed.syncConfig();
            	sender.sendMessage(new TextComponentString("PlayerInBed clearWeather set to " + TextFormatting.GREEN + PlayerInBed.clearWeather));
        		return;
        	} catch (NumberFormatException e) {
        	}
        } else if (args[0].equalsIgnoreCase("sendMessageToChat")) { 
        	try {
        		PlayerInBed.config.get(Configuration.CATEGORY_GENERAL, "4.sendMessageToChat", PlayerInBed.messageEnabled).set(Boolean.parseBoolean(args[1]));
            	PlayerInBed.syncConfig();
            	sender.sendMessage(new TextComponentString("PlayerInBed sendMessageToChat set to " + TextFormatting.GREEN + PlayerInBed.messageEnabled));
        		return;
        	} catch (NumberFormatException e) {
        	}
        } else if (args[0].equalsIgnoreCase("sendMessageToStatus")) { 
        	try {
        		PlayerInBed.config.get(Configuration.CATEGORY_GENERAL, "5.sendMessageToStatus", PlayerInBed.statusEnabled).set(Boolean.parseBoolean(args[1]));
            	PlayerInBed.syncConfig();
            	sender.sendMessage(new TextComponentString("PlayerInBed sendMessageToStatus set to " + TextFormatting.GREEN + PlayerInBed.statusEnabled));
        		return;
        	} catch (NumberFormatException e) {
        	}
        } else if (args[0].equalsIgnoreCase("setSpawnDuringDay")) { 
        	try {
        		PlayerInBed.config.get(Configuration.CATEGORY_GENERAL, "5.setSpawnDuringDay", PlayerInBed.setSpawnDuringDayEnabled).set(Boolean.parseBoolean(args[1]));
            	PlayerInBed.syncConfig();
            	sender.sendMessage(new TextComponentString("PlayerInBed setSpawnDuringDay set to " + TextFormatting.GREEN + PlayerInBed.setSpawnDuringDayEnabled));
        		return;
        	} catch (NumberFormatException e) {
        	}
        }
        sender.sendMessage(new TextComponentString("Invalid argument " + args[0] + " " + args[1]));
	}

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "enabled", "ratio", "clearWeather", "sendMessageToChat", "sendMessageToStatus");
        }
        if (args[0].equalsIgnoreCase("enabled") || args[0].equalsIgnoreCase("clearWeather")
        		|| args[0].equalsIgnoreCase("sendMessageToChat") || args[0].equalsIgnoreCase("sendMessageToStatus") || args[0].equalsIgnoreCase("setSpawnDuringDay"))
        {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        return Collections.emptyList();
    }
}

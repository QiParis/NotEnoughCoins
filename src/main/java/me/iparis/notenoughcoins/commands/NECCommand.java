package me.iparis.notenoughcoins.commands;

import gg.essential.api.EssentialAPI;
import me.iparis.notenoughcoins.Main;
import me.iparis.notenoughcoins.Reference;
import me.iparis.notenoughcoins.commands.subcommands.Subcommand;
import me.iparis.notenoughcoins.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class NECCommand extends CommandBase {
    private final Subcommand[] subcommands;

    public NECCommand(Subcommand[] subcommands) {
        this.subcommands = subcommands;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("notenoughcoins", "notenoughcoin");
    }

    @Override
    public String getCommandName() {
        return "nec";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/nec <subcommand> <arguments>";
    }

    public void sendHelp(ICommandSender sender) {
        List<String> commandUsages = new LinkedList<>();
        for (Subcommand subcommand : this.subcommands) {
            if (!subcommand.isHidden()) {
                commandUsages.add(EnumChatFormatting.AQUA + "/nec " + subcommand.getCommandName() + " "
                    + subcommand.getCommandUsage() + EnumChatFormatting.DARK_AQUA + " - " + subcommand.getCommandDescription());
            }
        }
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "NEC " + EnumChatFormatting.GREEN
            + Reference.VERSION + "\n" + String.join("\n", commandUsages)));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            EssentialAPI.getGuiUtil().openScreen(Main.config.gui());
            return;
        }
        for (Subcommand subcommand : this.subcommands) {
            if (Objects.equals(args[0], subcommand.getCommandName())) {
                if (!subcommand.processCommand(sender, Arrays.copyOfRange(args, 1, args.length))) {
                    // processCommand returned false
                    Utils.sendMessageWithPrefix("&cÉchec de l'exécution de la commande, utilisation de la commande: /nec "
                        + subcommand.getCommandName() + " " + subcommand.getCommandUsage());
                }
                return;
            }
        }
        Utils.sendMessageWithPrefix(EnumChatFormatting.RED
            + "La sous-commande n'a pas été trouvée, veuillez vous référer au message d'aide ci-dessous pour la liste des sous-commandes");
        sendHelp(sender);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> possibilities = new LinkedList<>();
        for (Subcommand subcommand : subcommands) {
            possibilities.add(subcommand.getCommandName());
        }
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, possibilities);
        }
        return null;
    }
}

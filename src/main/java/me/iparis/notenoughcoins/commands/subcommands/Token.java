package me.iparis.notenoughcoins.commands.subcommands;

import me.iparis.notenoughcoins.Config;
import me.iparis.notenoughcoins.Main;
import me.iparis.notenoughcoins.utils.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;

public class Token implements Subcommand {
    @Override
    public String getCommandName() {
        return "token";
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public String getCommandUsage() {
        return "";
    }

    @Override
    public String getCommandDescription() {
        return "Affiche le jeton de session actuel à des fins de débogage de l'API, ne le partagez PAS avec qui que ce soit !";
    }

    @Override
    public boolean processCommand(ICommandSender sender, String[] args) {
        if (Config.debug) {
            Utils.sendMessageWithPrefix("&7Cliquez pour copier le jeton", new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, Main.authenticator.getToken()));
        }
        return true;
    }
}

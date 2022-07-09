package me.iparis.notenoughcoins.commands.subcommands;

import me.iparis.notenoughcoins.Config;
import me.iparis.notenoughcoins.Main;
import me.iparis.notenoughcoins.utils.Utils;
import net.minecraft.command.ICommandSender;

public class Toggle implements Subcommand {
    public Toggle() {
    }

    public static void updateConfig() {
        if (Config.enabled) {
            Utils.sendMessageWithPrefix("&aFlipper activé.");
        } else {
            Utils.sendMessageWithPrefix("&cFlipper désactivé.");
        }
    }

    @Override
    public String getCommandName() {
        return "toggle";
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public String getCommandUsage() {
        return "";
    }

    @Override
    public String getCommandDescription() {
        return "Active ou désactive le flipper";
    }

    @Override
    public boolean processCommand(ICommandSender sender, String[] args) {
        Config.enabled = !Config.enabled;
        Main.config.writeData();
        updateConfig();
        return true;
    }
}

package me.iparis.notenoughcoins.commands.subcommands;

import net.minecraft.command.ICommandSender;

public interface Subcommand {
    String getCommandName();

    /**
     * Renvoie un booléen qui indique s'il faut afficher la sous-commande dans le message d'aide
     */
    boolean isHidden();

    /**
     * Renvoie une chaîne qui sera affichée après le nom de la sous-commande dans le message d'aide et le message d'erreur.
     * Cela ressemblera à : /nec sous-commande (utilisation)
     */
    String getCommandUsage();

    /**
     * Renvoie une chaîne qui sera affichée après l'utilisation de la sous-commande dans le message d'aide.
     * Cela ressemblera à : /nec sous-commande (utilisation) (description [dans une couleur différente])
     */
    String getCommandDescription();

    /**
     * Cette fonction sera appelée lors de l'exécution de la sous-commande et devrait
     * renvoie un booléen déterminant si la commande a réussi ou non, ce qui
     * envoyer un message d'erreur sinon. Remarque : Les arguments n'incluront pas le
     * sous-commande elle-même, exemple : /nec sous-commande arg1 arg2 aura {"arg1",
     * "arg2"} comme ses arguments
     */
    boolean processCommand(ICommandSender sender, String[] args);
}

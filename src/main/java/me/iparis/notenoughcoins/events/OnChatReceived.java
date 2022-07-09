package me.iparis.notenoughcoins.events;

import me.iparis.notenoughcoins.Config;
import me.iparis.notenoughcoins.Main;
import me.iparis.notenoughcoins.Reference;
import me.iparis.notenoughcoins.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class OnChatReceived {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void chat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        if (message.startsWith("Votre nouvelle clé API est ")) {
            String key = message.split("la clé est ")[1];
            Config.apiKey = key;
            Utils.sendMessageWithPrefix("§aClé API définie sur " + key);
        }
        for (String filter : Main.chatFilters) {
            if (message.toLowerCase(Locale.ROOT).contains(filter) && message.contains(": ")) { // Contenant deux-points signifie qu'il s'agit d'un message utilisateur
                event.setCanceled(true);
                Reference.logger.info("Le message suivant a été ignoré en raison d'un filtre de discussion: " + message);
                return;
            }
        }
    }
}

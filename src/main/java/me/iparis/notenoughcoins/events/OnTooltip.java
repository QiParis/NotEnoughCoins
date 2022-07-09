package me.iparis.notenoughcoins.events;

import gg.essential.universal.UKeyboard;
import me.iparis.notenoughcoins.Config;
import me.iparis.notenoughcoins.objects.BestSellingMethod;
import me.iparis.notenoughcoins.utils.Utils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class OnTooltip {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!Utils.isOnSkyblock()) return;
        String id = Utils.getIDFromItemStack(event.itemStack);
        if (Config.debug && id != null) {
            event.toolTip.add(EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD.toString() + "Item ID: " +
                EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + id);
        }
        if (Config.bestSellingMethod) {
            Map.Entry<BestSellingMethod, Long> result = Utils.getBestSellingMethod(id);
            if (result.getKey() == BestSellingMethod.NONE) return;
            boolean shifted = UKeyboard.isShiftKeyDown();
            if (event.itemStack.stackSize > 1 && !shifted &&
                !event.toolTip.contains(EnumChatFormatting.DARK_GRAY + "[MAJ afficher x" + event.itemStack.stackSize + "]")) { // Compatible avec NEU
                event.toolTip.add(EnumChatFormatting.DARK_GRAY + "[MAJ afficher x" + event.itemStack.stackSize + "]");
            }

            event.toolTip.add(EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD.toString() + "Meilleure méthode de vente: " +
                EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + result.getKey().toString() + " ($" + Utils.formatValue(
                    shifted ? result.getValue() * event.itemStack.stackSize : result.getValue()) + ")");
        }
    }
}

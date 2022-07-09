package me.iparis.notenoughcoins.utils.updater;

import gg.essential.universal.UDesktop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import java.net.URI;

public class UpdateAvailableScreen extends GuiScreen {
    private final String text;

    public UpdateAvailableScreen() {
        text = "Une nouvelle mise à jour est disponible " + EnumChatFormatting.YELLOW + GitHub.getLatestVersion();
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 84, 200, 20, "Voir la log des modifications"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 108, 98, 20, "Mettez à jour maintenant"));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height / 4 + 108, 98, 20, "Mise à jour à la sortie"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 4 + 132, 200, 20, "Annuler"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int yOffset = Math.min(this.height / 2, this.height / 4 + 80 - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2);
        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, text, this.width / 2, yOffset - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT - 2, 0xFFFFFFFF);
        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, "Mettre à jour maintenant ou en quittant Minecraft ?", this.width / 2, yOffset, 0xFFFFFFFF);
        drawCenteredString(Minecraft.getMinecraft().fontRendererObj, "(La mise à jour maintenant quittera Minecraft après le téléchargement de la mise à jour)", this.width / 2, yOffset + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1 || button.id == 2) {
            // Mise à jour
            GitHub.showChangelog = true;
            Minecraft.getMinecraft().displayGuiScreen(new UpdatingScreen(button.id == 1));
        } else if (button.id == 3) {
            // Annuler
            Minecraft.getMinecraft().displayGuiScreen(null);
        } else if (button.id == 0) {
            // Voir la log des modifications
            UDesktop.browse(URI.create("https://github.com/QiParis/NotEnoughCoins/releases"));
        }
    }
}

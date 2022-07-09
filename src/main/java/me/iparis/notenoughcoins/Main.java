package me.iparis.notenoughcoins;

import me.iparis.notenoughcoins.commands.NECCommand;
import me.iparis.notenoughcoins.commands.subcommands.Help;
import me.iparis.notenoughcoins.commands.subcommands.Subcommand;
import me.iparis.notenoughcoins.commands.subcommands.Toggle;
import me.iparis.notenoughcoins.commands.subcommands.Token;
import me.iparis.notenoughcoins.events.OnChatReceived;
import me.iparis.notenoughcoins.events.OnGuiOpen;
import me.iparis.notenoughcoins.events.OnTick;
import me.iparis.notenoughcoins.events.OnTooltip;
import me.iparis.notenoughcoins.events.OnWorldJoin;
import me.iparis.notenoughcoins.objects.AverageItem;
import me.iparis.notenoughcoins.utils.ApiHandler;
import me.iparis.notenoughcoins.utils.Utils;
import me.iparis.notenoughcoins.utils.updater.GitHub;
import me.iparis.notenoughcoins.websocket.Client;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION)
public class Main {
    public static Config config = new Config();
    public static Authenticator authenticator;
    public static boolean checkedForUpdate = false;
    public static NECCommand commandManager = new NECCommand(new Subcommand[]{
        new Toggle(),
        new Help(),
        new Token()
    });
    public static Map<String, AverageItem> averageItemMap = new HashMap<>();
    public static Map<String, Date> processedItem = new HashMap<>(); // La date est l'heure d'expiration, indique quand l'enchère se termine et doit être purgée pour économiser de la mémoire à long terme
    public static Map<String, Integer> lbinItem = new HashMap<>();
    public static Map<String, Integer> bazaarItem = new HashMap<>(); // Long est le prix de vente instantané de l'article
    public static Map<String, Integer> npcItem = new HashMap<>();
    public static List<String> chatFilters = new LinkedList<>();
    public static double balance = 0;
    public static boolean justPlayedASound = false; // Ceci afin d'éviter que plusieurs flips n'entrent en même temps et ne fassent exploser l'utilisateur.
    public static File jarFile;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        jarFile = event.getSourceFile();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Not Enough Coins", 4);
        authenticator = new Authenticator(progressBar);
        try {
            authenticator.authenticate(true);
        } catch (Exception e) {
            while (progressBar.getStep() < (progressBar.getSteps() - 1))
                progressBar.step("loading-failed-" + progressBar.getStep());
            e.printStackTrace();
            Reference.logger.error("NotEnoughCoins a été désactivé en raison d'une erreur lors de l'authentification. Veuillez consulter les journaux pour plus d'informations.");
            return;
        }
        progressBar.step("Enregistrement d'événements, de commandes, de crochets et de tâches");
        config.preload();
        ClientCommandHandler.instance.registerCommand(commandManager);
        GitHub.downloadDeleteTask();
        MinecraftForge.EVENT_BUS.register(new OnWorldJoin());
        MinecraftForge.EVENT_BUS.register(new OnTick());
        MinecraftForge.EVENT_BUS.register(new OnTooltip());
        MinecraftForge.EVENT_BUS.register(new OnChatReceived());
        MinecraftForge.EVENT_BUS.register(new OnGuiOpen());
        Tasks.updateBalance.start();
        Tasks.updateBazaarItem.start();
        Tasks.updateFilters.start();
        Utils.runInAThread(ApiHandler::updateNPC);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Reference.logger.info("Déconnecter...");
            try {
                authenticator.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        progressBar.step("Établissement de la connexion WebSocket");
        Client.connectWithToken();
        ProgressManager.pop(progressBar);
    }
}

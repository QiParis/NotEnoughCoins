package me.iparis.notenoughcoins.websocket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.essential.universal.USound;
import me.iparis.notenoughcoins.Authenticator;
import me.iparis.notenoughcoins.Config;
import me.iparis.notenoughcoins.Main;
import me.iparis.notenoughcoins.Reference;
import me.iparis.notenoughcoins.objects.AverageItem;
import me.iparis.notenoughcoins.utils.Utils;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ResourceLocation;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Client extends WebSocketClient {
    private Date lastPing;
    public long latency = -1; // En ms

    public Client(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lastPing = new Date();
                send("{\"type\":\"ping\"}");
            }
        }, 1000, 30000);
    }

    public static void connectWithToken() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Main.authenticator.getToken());
        try {
            new Client(new URI("wss://nec.robothanzo.dev/ws"), headers).connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Reference.logger.info("Connexion Websocket établie");
    }

    @Override
    public void onMessage(String message) {
        Date start = new Date();
        JsonObject json = new JsonParser().parse(message).getAsJsonObject();
        if (json.has("type")) {
            switch (json.get("type").getAsString()) {
                case "profit":
                    if (Config.enabled && ((!Config.onlySkyblock) || Utils.isOnSkyblock())) {
                        for (JsonElement element : json.getAsJsonArray("result")) {
                            JsonObject item = element.getAsJsonObject();
                            String auctionID = item.get("auction_id").getAsString();
                            String itemID = item.get("id").getAsString();
                            if (!Main.processedItem.containsKey(auctionID)) { // N'ont pas été traitées
                                Main.processedItem.put(auctionID, new Date(item.get("end").getAsLong()));
                                if (!Config.categoryFilter.contains(item.get("category").getAsString().toUpperCase(Locale.ROOT)) && !Arrays.asList(Config.blacklistedIDs.split("\n")).contains(item.get("id").getAsString())) { // Vérifications de la liste noire
                                    int price = item.get("price").getAsInt();
                                    int profit = Utils.getTaxedProfit(price, item.get("profit").getAsInt());
                                    int demand;
                                    try {
                                        demand = Main.averageItemMap.get(itemID).demand;
                                    } catch (NullPointerException e) {
                                        Main.processedItem.remove(auctionID);
                                        continue;
                                    }
                                    double profitPercentage = ((double) profit / (double) price);
                                    if (price <= Main.balance && profit >= Config.minProfit && profitPercentage >= Config.minProfitPercentage && demand >= Config.minDemand) { // Recherche de profit minimum, etc.
                                        if ((!Config.manipulationCheck) || (!((price + item.get("profit").getAsInt()) * 0.6 > Main.averageItemMap.get(itemID).ahAvgPrice))) { // Contrôles de manipulation
                                            if (!Authenticator.myUUID.toLowerCase(Locale.ROOT).replaceAll("-", "").equals(item.get("auctioneer").getAsString())) { // Pas protéger
                                                Utils.sendMessageWithPrefix(Utils.getColorCodeFromRarity(item.get("rarity").getAsString()) + item.get("item_name").getAsString() + "&e " + // Nom de l'article
                                                        Utils.getProfitText(profit) + " " + // Profit
                                                        "&eP: &a" + Utils.formatValue(price) + " " + // Le prix
                                                        "&ePP: &a" + (int) Math.floor(profitPercentage * 100) + "% " + // Profit %
                                                        "&eSPD: &a" + demand + " " + // Demande
                                                        (Config.debug ? "\n&eCL: &a" + item.get("cache_latency").getAsInt() + "ms" : "") + " " + // Débogage : latence du cache
                                                        (Config.debug ? "&eAL: &a" + item.get("api_latency").getAsInt() + "ms" : "") + " " + // Débogage : latence de l'API
                                                        (Config.debug ? "&eWL: &a" + latency + "ms" : "") + " " + // Débogage: latence websocket
                                                        (Config.debug ? "&ePL: &a" + (new Date().getTime() - start.getTime()) + "ms" : "") + " " + // Débogage : latence de traitement.
                                                        (Config.debug ? "&eAA: &a" + Utils.formatValue(Main.averageItemMap.get(itemID).ahAvgPrice) : "") + " " + // Débogage : moyenne des enchères.
                                                        (Config.debug ? "&eLBIN: &a" + Utils.formatValue(Main.lbinItem.get(itemID)) : "") + " " + // Débogage : achat immédiat le plus bas.
                                                        ((profit >= 100000) ? "\n" : ""), // Mettre l'accent sur les flips avec un gros profit en envoyant une nouvelle ligne.
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction " + auctionID));
                                                if (Config.alertSounds && !Main.justPlayedASound) {
                                                    Main.justPlayedASound = true;
                                                    USound.INSTANCE.playSoundStatic(new ResourceLocation("note.pling"), 2F, 1.0F);
                                                }
                                            }
                                        } else {
                                            Reference.logger.info("Échec de la vérification de la manipulation pour " + item.get("item_name").getAsString() + " le prix " + price + " profit " + profit + " moyenne " + Main.averageItemMap.get(item.get("id").getAsString()).ahAvgPrice);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Main.justPlayedASound = false;
                    return;
                case "lowest_bin":
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("result").entrySet()) {
                        Main.lbinItem.put(entry.getKey(), entry.getValue().getAsInt());
                    }
                    return;
                case "average":
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("result").entrySet()) {
                        String item = entry.getKey();
                        JsonObject itemDetails = entry.getValue().getAsJsonObject();
                        int sampledDays = itemDetails.getAsJsonPrimitive("sampled_days").getAsInt();
                        int ahSales = Math.floorDiv(itemDetails.getAsJsonObject("auction").getAsJsonPrimitive("sales").getAsInt(), sampledDays);
                        int ahAvgPrice = (int) Math.floor(itemDetails.getAsJsonObject("auction").getAsJsonPrimitive("average_price").getAsDouble());
                        int binSales = Math.floorDiv(itemDetails.getAsJsonObject("bin").getAsJsonPrimitive("sales").getAsInt(), sampledDays);
                        // int binAvgPrice = (int)Math.floor(itemDetails.getAsJsonObject("bin").getAsJsonPrimitive("average_price").getAsDouble());
                        Main.averageItemMap.put(item, new AverageItem(item, ahSales + binSales, ahAvgPrice));
                    }
                    return;
                case "pong":
                    if (lastPing!=null) {
                        latency = (new Date().getTime() - lastPing.getTime()) / 2;
                    }
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Reference.logger.warn("Connexion Websocket fermée par " + (remote ? "remote peer" : "us") + " Code: " + code + " Raison: " + reason);
        if (reason.contains("418")) {
            Utils.sendMessageWithPrefix("&cÉchec de la récupération depuis le backend NEC, cela peut être dû à :\n" +
                "&cVous avez été mis sur liste noire par le mod pour avoir utilisé des scripts de macro\n" +
                "&cVeuillez rejoindre notre serveur discord pour plus d'informations (dans /nec > liens)");
        }
        if (reason.contains("401")) {
            Reference.logger.warn("Jeton expiré, récupération d'un nouveau jeton");
            try {
                Main.authenticator.authenticate(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (code != 1001) { // Clôture anormale
            connectWithToken();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Reference.logger.warn("Redémarrage de la connexion en raison d'une fermeture anormale");
        }
    }

    @Override
    public void onError(Exception ex) {
        Reference.logger.error("Erreur WebSocket", ex);
    }
}

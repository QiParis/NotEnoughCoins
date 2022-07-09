package me.iparis.notenoughcoins;

import com.google.gson.JsonElement;
import me.iparis.notenoughcoins.utils.ApiHandler;
import me.iparis.notenoughcoins.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Tasks {
    public static Thread updateBalance = new Thread(() -> {
        while (true) {
            if (Config.enabled) {
                try {
                    ApiHandler.updatePurse();
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    try {
                        Utils.sendMessageWithPrefix("&cÉchec de la mise à jour du solde, veuillez vérifier si vous avez correctement défini votre clé API.");
                        Thread.sleep(60000); // Attendre que l'utilisateur définisse la clé API.
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }, "Tâche de mise à jour du solde de pièces insuffisante");
    public static Thread updateBazaarItem = new Thread(() -> {
        while (true) {
            if (Config.enabled || Config.bestSellingMethod) {
                try {
                    ApiHandler.updateBazaar();
                    Thread.sleep(2500);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(60000); // Dormir 60s si l'API est en panne ou a été mise sur liste noire
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }, "Tâche de mise à jour du bazar pas assez de pièces");

    public static Thread updateFilters = new Thread(() -> {
        while (true) {
            if (Config.hideSpam) {
                List<String> filters = new LinkedList<>();
                try {
                    for (Map.Entry<String, JsonElement> f : Utils.getJson("https://nec.robothanzo.dev/filter").getAsJsonObject().getAsJsonObject("result").entrySet()) {
                        for (JsonElement filter : f.getValue().getAsJsonArray()) {
                            filters.add(filter.getAsString().toLowerCase(Locale.ROOT));
                        }
                    }
                    Main.chatFilters = filters;
                    Thread.sleep(60000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }, "Pas assez de pièces Filtres Tâche de mise à jour");
}

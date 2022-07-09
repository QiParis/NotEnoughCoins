package me.iparis.notenoughcoins;

import gg.essential.universal.UDesktop;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Category;
import gg.essential.vigilance.data.JVMAnnotationPropertyCollector;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyData;
import gg.essential.vigilance.data.PropertyType;
import gg.essential.vigilance.data.SortingBehavior;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Config extends Vigilant {
    public static final File CONFIG_FILE = new File("config/nec.toml");
    public static ArrayList<String> categoryFilter = new ArrayList<>(
        Arrays.asList("TRAVEL_SCROLL", "COSMETIC", "DUNGEON_PASS", "ARROW_POISON", "PET_ITEM"));
    @Property(
        type = PropertyType.SWITCH,
        category = "Flipping",
        subcategory = "Basic",
        name = "Activé",
        description = "Si le mod doit vérifier et envoyer des flips"
    )
    public static boolean enabled = false;
    @Property(
        type = PropertyType.SWITCH,
        category = "Flipping",
        subcategory = "Basic",
        name = "Activé uniquement dans skyblock",
        description = "Si le mod doit désactiver l'envoi de flips lorsqu'il n'est pas dans SkyBlock"
    )
    public static boolean onlySkyblock = true;
    @Property(
        type = PropertyType.NUMBER,
        category = "Flipping",
        subcategory = "Basic",
        name = "Bénéfice minimal",
        description = "Le montant minimum de profit requis pour que le mod vous envoie le flip",
        max = Integer.MAX_VALUE,
        increment = 10000
    )
    public static int minProfit = 50000;
    @Property(
        type = PropertyType.NUMBER,
        category = "Flipping",
        subcategory = "Basic",
        name = "Demande minimale",
        description = "Le minimum de ventes par jour requis pour que le mod vous envoie le flip",
        max = Integer.MAX_VALUE,
        increment = 5
    )
    public static int minDemand = 10;
    @Property(
        type = PropertyType.PERCENT_SLIDER,
        category = "Flipping",
        subcategory = "Basic",
        name = "Pourcentage de profit minimum",
        description = "Le pourcentage minimum de profit requis pour que le mod vous envoie le flip"
    )
    public static float minProfitPercentage = 0F;
    @Property(
        type = PropertyType.SWITCH,
        category = "Flipping",
        subcategory = "Basic",
        name = "Sons d'alerte",
        description = "Si un son doit être joué lors de l'envoi d'un flip"
    )
    public static boolean alertSounds = true;
    @Property(
        type = PropertyType.PARAGRAPH,
        category = "Flipping",
        subcategory = "Avancé",
        name = "Liste noire d'articles",
        description = "Exclure les articles de l'envoi en tant que flip (veuillez le configurer via le site Web fourni ci-dessous)"
    )
    public static String blacklistedIDs = "";
    @Property(
        type = PropertyType.SWITCH,
        category = "Flipping",
        subcategory = "Avancé",
        name = "Vérification des manipulations",
        description = "Si le mod doit vérifier si l'article a été manipulé avant d'envoyer le flip, DÉSACTIVEZ CELA À VOS PROPRES RISQUES CAR VOUS POUVEZ PERDRE VOTRE ARGENT AUPRÈS DES MANIPULATEURS DU MARCHÉ"
    )
    public static boolean manipulationCheck = true;
    @Property(
        type = PropertyType.TEXT,
        category = "Confidentiel",
        name = "API Key", protectedText = true,
        description = "Exécutez /api new pour le définir automatiquement, ou collez-en un si vous ne souhaitez pas le renouveler"
    )
    public static String apiKey = "";
    @Property(
        type = PropertyType.SWITCH,
        category = "Confidentiel",
        name = "Debug",
        description = "Afficher ou non les informations de débogage, telles que la latence"
    )
    public static boolean debug = false;
    @Property(
        type = PropertyType.SWITCH,
        category = "Économie d'argent",
        name = "Meilleure méthode de vente",
        description = "Montre la meilleure façon de vendre un objet sur sa tradition, celui qui se vend le plus sera affiché"
    )
    public static boolean bestSellingMethod = true;
    @Property(
        type = PropertyType.SWITCH,
        category = "Économie d'argent",
        name = "Superposition d'articles de la meilleure méthode de vente",
        description = "Affiche une superposition verte sur l'élément si le menu ouvert actuel est la meilleure méthode de vente pour l'élément"
    )
    public static boolean bestSellingOverlay = true;
    @Property(
        type = PropertyType.SWITCH,
        category = "Qualité de vie",
        name = "Masquer les spams",
        description = "Masquer les messages contenant des mots-clés prédéfinis de publicités frauduleuses, etc."
    )
    public static boolean hideSpam = true;

    public Config() {
        super(CONFIG_FILE, "NEC Configuration", new JVMAnnotationPropertyCollector(), new CustomSorting());
        initialize();
    }

    @Property(
        type = PropertyType.BUTTON,
        category = "Flipping",
        subcategory = "Avancé",
        name = "Site Web de configuration de la liste noire",
        description = "Configurer la liste noire via ce site Web"
    )
    public static void blacklistConfigure() {
        UDesktop.browse(URI.create("https://nec.robothanzo.dev/panel/#/itemBlacklist"));
    }

    @Property(
        type = PropertyType.BUTTON,
        category = "Liens",
        name = "Patreon",
        description = "Faites un don pour couvrir les frais d'hébergement du serveur !"
    )
    public static void patreon() {
        UDesktop.browse(URI.create("https://www.patreon.com/QiParis"));
    }

    @Property(
        type = PropertyType.BUTTON,
        category = "Liens",
        name = "GitHub",
        description = "Aide au développement !"
    )
    public static void github() {
        UDesktop.browse(URI.create("https://github.com/QiParis/NotEnoughCoins"));
    }

    @Property(
        type = PropertyType.BUTTON,
        category = "Liens",
        name = "Discord",
        description = "Rejoignez notre serveur Discord !"
    )
    public static void discord() {
        UDesktop.browse(URI.create("https://discord.gg/"));
    }
}

class CustomSorting extends SortingBehavior { // Nous comparons par l'ordre des propriétés dans le code
    @NotNull
    @Override
    public Comparator<? super Category> getCategoryComparator() {
        return (Comparator<Category>) (o1, o2) -> 0;
    }

    @NotNull
    @Override
    public Comparator<? super Map.Entry<String, ? extends List<PropertyData>>> getSubcategoryComparator() {
        return (Comparator<Map.Entry<String, ? extends List<PropertyData>>>) (o1, o2) -> 0;
    }

    @NotNull
    @Override
    public Comparator<? super PropertyData> getPropertyComparator() {
        return (Comparator<PropertyData>) (o1, o2) -> 0;
    }
}

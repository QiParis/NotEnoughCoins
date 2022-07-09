package me.iparis.notenoughcoins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;


// Crédits à DungeonsGuide @ https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide/blob/master/src/main/java/kr/syeyoung/dungeonsguide/Authenticator.java
public class Authenticator {
    private static final String BASE_URL = "https://nec.robothanzo.dev/auth";
    public static String myUUID;
    private final ProgressManager.ProgressBar progressBar;
    private String token;

    public Authenticator(ProgressManager.ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public static JsonElement getAuthenticatedJson(String jsonUrl) throws IOException {
        if (Main.authenticator.getToken() == null) {
            Reference.logger.warn("Le jeton d'authentification est nul, j'essaie d'en obtenir un");
            try {
                Main.authenticator.authenticate(false);
            } catch (Exception e) {
                Reference.logger.error(e.getMessage(), e);
                return null;
            }
            if (Main.authenticator.getToken() == null) {
                return null;
            }
        }
        HttpsURLConnection connection;
        try {
            connection = (HttpsURLConnection) new URL(jsonUrl).openConnection();
        } catch (IOException e) {
            Reference.logger.error(e.getMessage(), e);
            return null;
        }
        connection.setRequestProperty("User-Agent", "NotEnoughCoins/1.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", Main.authenticator.getToken());
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        int code = connection.getResponseCode(); // Le code de réponse doit d'abord être récupéré, sinon un IOE sera lancé
        String payload = String.join("\n", IOUtils.readLines(connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream()));
        if (code >= 400) {
            Reference.logger.error(jsonUrl + " :: Reçu " + connection.getResponseCode() + " de même que\n" + payload);
            if (code == 401) {
                try {
                    Main.authenticator.authenticate(true);
                } catch (AuthenticationException e) {
                    Main.authenticator.token = null;
                    return null;
                }
                return getAuthenticatedJson(jsonUrl);
            }
            return null;
        }
        return new JsonParser().parse(payload);
    }

    public String getToken() {
        return token;
    }

    public void authenticate(boolean withProgress) throws IOException, AuthenticationException, NullPointerException {
        Session session = Minecraft.getMinecraft().getSession();
        String sessionToken = session.getToken(); // Au cas où vous vous poseriez la question, ceci est à des fins d'autorisation et n'est jamais envoyé au serveur NEC
        if (withProgress)
            progressBar.step("Authentification (1/2)");
        String tempToken = Objects.requireNonNull(requestAuth(session.getProfile()));
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        JsonObject d = getJwtPayload(tempToken);
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), sessionToken, d.get("server_id").getAsString());
        if (withProgress)
            progressBar.step("Authentification (2/2)");
        this.token = Objects.requireNonNull(verifyAuth(tempToken));
    }

    public JsonObject getJwtPayload(String jwt) {
        String midPart = jwt.split("\\.")[1].replace("+", "-").replace("/", "_");
        String base64Decode = new String(Base64.decodeBase64(midPart)); // Rembourrage
        return (JsonObject) new JsonParser().parse(base64Decode);
    }

    private String requestAuth(GameProfile profile) throws IOException {
        myUUID = profile.getId().toString().replaceAll("-", "").toLowerCase(Locale.ROOT);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(BASE_URL + "/request").openConnection();
        connection.setRequestProperty("User-Agent", "NotEnoughCoins Authentication Service/1.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(("{\"uuid\":\"" + myUUID + "\",\"username\":\"" + profile.getName() + "\"}").getBytes());
        int code = connection.getResponseCode();
        String payload = String.join("\n", IOUtils.readLines(connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream()));
        if (code >= 400) {
            Reference.logger.error(BASE_URL + "/request :: Reçu " + connection.getResponseCode() + " de même que\n" + payload);
            return null;
        }

        JsonObject json = (JsonObject) new JsonParser().parse(payload);

        if (!json.get("success").getAsBoolean()) {
            return null;
        }
        return json.get("jwt").getAsString();
    }

    private String verifyAuth(String tempToken) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(BASE_URL + "/verify").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "NotEnoughCoins Authentication Service/1.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\":\"" + tempToken + "\"}").getBytes());
        String payload = String.join("\n", IOUtils.readLines(urlConnection.getErrorStream() == null ? urlConnection.getInputStream() : urlConnection.getErrorStream()));
        int code = urlConnection.getResponseCode();
        if (code >= 400) {
            Reference.logger.error(BASE_URL + "/verify :: Reçu " + urlConnection.getResponseCode() + " de même que\n" + payload);
            return null;
        }

        JsonObject json = (JsonObject) new JsonParser().parse(payload);
        if (!json.get("success").getAsBoolean()) {
            return null;
        }
        return json.get("token").getAsString();
    }

    public void logout() throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(BASE_URL + "/logout").openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent", "NotEnoughCoins Authentication Service/1.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", this.token);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        int code = urlConnection.getResponseCode();
        String payload = String.join("\n", IOUtils.readLines(urlConnection.getErrorStream() == null ? urlConnection.getInputStream() : urlConnection.getErrorStream()));
        if (code >= 400)
            Reference.logger.error(BASE_URL + "/logout :: Reçu " + urlConnection.getResponseCode() + " de même que\n" + payload);
    }
}

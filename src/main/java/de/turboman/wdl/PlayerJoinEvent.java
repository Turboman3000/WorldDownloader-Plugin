package de.turboman.wdl;

import com.google.gson.JsonParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;

public class PlayerJoinEvent implements Listener {
    private final OkHttpClient client = new OkHttpClient();
    private final MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onEvent(org.bukkit.event.player.PlayerJoinEvent event) throws IOException {
        if (!event.getPlayer().isOp()) return;

        final var request = new Request.Builder().url(WorldDownloader.backendURL + "/status").build();

        try (final Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            final var status = JsonParser.parseString(response.body().string()).getAsJsonObject();

            if (status.get("code").getAsInt() >= 300)
                event.getPlayer().sendMessage(mm.deserialize("<red>WDL Backend is not reachable, so no Worlds can't be downloaded!"));
        } catch (RuntimeException e) {
            event.getPlayer().sendMessage(mm.deserialize("<red>WDL Backend is not reachable, so no Worlds can't be downloaded!"));
        }
    }
}

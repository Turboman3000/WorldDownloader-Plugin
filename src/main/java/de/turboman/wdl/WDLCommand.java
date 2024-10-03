package de.turboman.wdl;

import com.google.gson.JsonParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.UUID;

public class WDLCommand implements CommandExecutor {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final String tmpdir = System.getProperty("java.io.tmpdir");
    private final short maxSteps = 5;
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType MEDIA_TYPE_ZIP = MediaType.parse("application/zip");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player)
            if (!sender.isOp()) {
                sender.sendMessage(mm.deserialize("<red>You need to be OP to use this command!"));
                return false;
            }

        if (WorldDownloader.isRunning) {
            sender.sendMessage(mm.deserialize("<red>Another download is already running!"));
            return false;
        }

        if (WorldDownloader.lastRunned * 60 * 5 >= System.currentTimeMillis() / 1000) {
            sender.sendMessage(mm.deserialize("<red>You need to wait 5 minutes before you can download again!"));
            return false;
        }

        WorldDownloader.isRunning = true;

        File zipFile;
        final var uuid = UUID.randomUUID();

        try {
            zipFile = File.createTempFile("wdl_" + uuid, ".zip");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Player p : Bukkit.getOnlinePlayers())
            p.showBossBar(WorldDownloader.progressBossbar);

        WorldDownloader.progressBossbar.progress(0);
        WorldDownloader.progressBossbar.name(mm.deserialize("<green>Gettings Worlds"));

        for (World w : Bukkit.getWorlds()) {
            w.save();

            ArrayList<File> files = new ArrayList<>();

            WorldDownloader.progressBossbar.progress((float) 1 / maxSteps);
            WorldDownloader.progressBossbar.name(mm.deserialize("<green>Preparing World (" + w.getName() + ")"));

            for (var f : Utils.parseDirectoryRecursive(Path.of(w.getName()).toString())) {
                var ff = w.getName() + f.replace(Path.of(w.getName()).toAbsolutePath().toString(), "");

                if (ff.contains(".lock")) continue;
                if (files.contains(new File(ff))) continue;

                files.add(new File(ff));
            }

            WorldDownloader.progressBossbar.progress((float) 2 / maxSteps);
            WorldDownloader.progressBossbar.name(mm.deserialize("<green>Gettings Files (" + w.getName() + ")"));

            for (var f : files) {
                Path.of(tmpdir, uuid + "", f.getPath()).toFile().mkdirs();

                try {
                    Files.copy(Path.of(f.getPath()).toAbsolutePath(), Path.of(tmpdir, uuid + "", f.getPath()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            WorldDownloader.progressBossbar.progress((float) 3 / maxSteps);
            WorldDownloader.progressBossbar.name(mm.deserialize("<green>Packing Files (" + w.getName() + ")"));
            ZipUtil.pack(Path.of(tmpdir, uuid + "").toFile(), zipFile);
        }

        WorldDownloader.progressBossbar.progress((float) 4 / maxSteps);
        WorldDownloader.progressBossbar.name(mm.deserialize("<green>Uploading to server..."));

        final var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "world.zip", RequestBody.create(MEDIA_TYPE_ZIP, zipFile))
                .build();

        final var request = new Request.Builder().post(body).url(WorldDownloader.backendURL + "/upload").build();

        try (final Response response = client.newCall(request).execute()) {
            assert response.body() != null;

            final var link = "https://rcurl.de/wdl?c=" + JsonParser.parseString(response.body().string()).getAsJsonObject().get("id").getAsString();
            sender.sendMessage(mm.deserialize("<gold>WDL ⇒ <green> Click here to download the world: <click:open_url:" + link + "><gold>" + link));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        WorldDownloader.progressBossbar.progress(1);
        WorldDownloader.progressBossbar.name(mm.deserialize("<green>Cleaning Up..."));
        Path.of(tmpdir, uuid + "").toFile().delete();

        for (Player p : Bukkit.getOnlinePlayers())
            p.hideBossBar(WorldDownloader.progressBossbar);

        WorldDownloader.isRunning = false;
        WorldDownloader.lastRunned = (int) (System.currentTimeMillis() / 1000);

        return true;
    }
}

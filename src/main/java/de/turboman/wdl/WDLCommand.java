package de.turboman.wdl;

import net.kyori.adventure.text.minimessage.MiniMessage;
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player)
            if (!sender.isOp()) {
                sender.sendMessage(mm.deserialize("<red>You need to be OP to use this command!"));
                return false;
            }

        File zipFile;
        final var uuid = UUID.randomUUID();

        try {
            zipFile = File.createTempFile("wdl_" + uuid, ".zip");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Player p : Bukkit.getOnlinePlayers())
            p.showBossBar(WorldDownloader.progressBossbar);

        for (World w : Bukkit.getWorlds()) {
            ArrayList<File> files = new ArrayList<>();

            for (var f : Utils.parseDirectoryRecursive(Path.of(w.getName()).toString())) {
                var ff = w.getName() + f.replace(Path.of(w.getName()).toAbsolutePath().toString(), "");

                if (ff.contains(".lock")) continue;
                if (files.contains(new File(ff))) continue;

                files.add(new File(ff));
            }

            for (var f : files) {
                Path.of(tmpdir, uuid + "", f.getPath()).toFile().mkdirs();

                try {
                    Files.copy(Path.of(f.getPath()).toAbsolutePath(), Path.of(tmpdir, uuid + "", f.getPath()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            ZipUtil.pack(Path.of(tmpdir, uuid + "").toFile(), zipFile);
        }

        System.out.println(zipFile.getAbsolutePath());
        Path.of(tmpdir, uuid + "").toFile().delete();

        return true;
    }
}

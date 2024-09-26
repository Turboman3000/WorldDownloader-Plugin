package de.turboman.wdl;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WDLCommand implements CommandExecutor {
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player)
            if (!sender.isOp()) {
                sender.sendMessage(mm.deserialize("<red>You need to be OP to use this command!"));
                return false;
            }

        for (Player p : Bukkit.getOnlinePlayers())
            p.showBossBar(WorldDownloader.progressBossbar);



        return true;
    }
}

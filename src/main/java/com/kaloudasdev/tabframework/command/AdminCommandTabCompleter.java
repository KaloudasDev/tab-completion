package com.kaloudasdev.tabframework.command;
import com.kaloudasdev.tabframework.TabFrameworkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public final class AdminCommandTabCompleter implements TabCompleter {
    private static final List<String> COMMANDS = Arrays.asList("ban", "unban", "kick", "reload");
    private static final List<String> DURATIONS = Arrays.asList("10s","30s","1m","5m","10m","30m","1h","2h","6h","12h","1d","2d","7d","30d");
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (!s.hasPermission("tabframework.admin")) return Collections.emptyList();
        if (args.length == 1) {
            String p = args[0].toLowerCase();
            return COMMANDS.stream().filter(cmd -> cmd.startsWith(p)).collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("kick")) {
                String p = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(p)).collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("unban")) {
                String p = args[1].toLowerCase();
                return TabFrameworkPlugin.getInstance().getBanManager().getActiveBans().stream().map(e -> e.playerName).filter(n -> n != null && n.toLowerCase().startsWith(p)).collect(Collectors.toList());
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("ban")) {
            String p = args[2].toLowerCase();
            return DURATIONS.stream().filter(d -> d.startsWith(p)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

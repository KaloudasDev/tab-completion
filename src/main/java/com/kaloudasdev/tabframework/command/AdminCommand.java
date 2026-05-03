package com.kaloudasdev.tabframework.command;
import com.kaloudasdev.tabframework.TabFrameworkPlugin;
import com.kaloudasdev.tabframework.manager.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public final class AdminCommand implements CommandExecutor {
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("tabframework.admin")) { s.sendMessage(ChatColor.RED + "No permission"); return true; }
        if (a.length == 0) { this.help(s); return true; }
        String sub = a[0].toLowerCase();
        BanManager bm = TabFrameworkPlugin.getInstance().getBanManager();
        switch (sub) {
            case "reload": TabFrameworkPlugin.getInstance().reloadConfig(); s.sendMessage(ChatColor.GREEN + "Reloaded"); break;
            case "ban": this.ban(s, a, bm); break;
            case "unban": this.unban(s, a, bm); break;
            case "kick": this.kick(s, a); break;
            default: this.help(s); break;
        }
        return true;
    }
    private void ban(CommandSender s, String[] a, BanManager bm) {
        if (a.length < 3) { s.sendMessage(ChatColor.RED + "Usage: /tab ban <player> <reason> [duration]"); return; }
        Player t = Bukkit.getPlayer(a[1]);
        if (t == null) { s.sendMessage(ChatColor.RED + "Player not found"); return; }
        StringBuilder r = new StringBuilder();
        long d = 0;
        for (int i = 2; i < a.length; i++) {
            if (a[i].matches("^\\d+[smhdw]$") && i == 2) { d = this.parse(a[i]); continue; }
            r.append(a[i]).append(" ");
        }
        bm.ban(t, r.length() > 0 ? r.toString().trim() : "Cheating", d);
        s.sendMessage(ChatColor.GREEN + "Banned " + t.getName());
    }
    private void unban(CommandSender s, String[] a, BanManager bm) {
        if (a.length < 2) { s.sendMessage(ChatColor.RED + "Usage: /tab unban <player>"); return; }
        s.sendMessage(bm.unban(a[1]) ? ChatColor.GREEN + "Unbanned " + a[1] : ChatColor.RED + "Not banned");
    }
    private void kick(CommandSender s, String[] a) {
        if (a.length < 2) { s.sendMessage(ChatColor.RED + "Usage: /tab kick <player> [reason]"); return; }
        Player t = Bukkit.getPlayer(a[1]);
        if (t == null) { s.sendMessage(ChatColor.RED + "Player not found"); return; }
        StringBuilder r = new StringBuilder();
        for (int i = 2; i < a.length; i++) r.append(a[i]).append(" ");
        t.kickPlayer(ChatColor.RED + (r.length() > 0 ? r.toString().trim() : "Kicked by staff"));
        s.sendMessage(ChatColor.GREEN + "Kicked " + t.getName());
    }
    private long parse(String t) {
        char u = t.charAt(t.length() - 1);
        int v = Integer.parseInt(t.substring(0, t.length() - 1));
        switch (u) { case 's': return v; case 'm': return v * 60L; case 'h': return v * 3600L; case 'd': return v * 86400L; case 'w': return v * 604800L; default: return 0; }
    }
    private void help(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "/tab ban <player> <reason> [duration] - Ban player");
        s.sendMessage(ChatColor.GOLD + "/tab unban <player> - Unban player");
        s.sendMessage(ChatColor.GOLD + "/tab kick <player> [reason] - Kick player");
        s.sendMessage(ChatColor.GOLD + "/tab reload - Reload config");
    }
}

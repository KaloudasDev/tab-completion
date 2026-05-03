package com.kaloudasdev.tabframework.manager;
import com.kaloudasdev.tabframework.TabFrameworkPlugin;
import org.bukkit.entity.Player;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public final class BanManager {
    private final Map<UUID, BanEntry> banRegistry = new ConcurrentHashMap<>();
    private final File banFile;
    public BanManager(TabFrameworkPlugin plugin) {
        plugin.getDataFolder().mkdirs();
        this.banFile = new File(plugin.getDataFolder(), "bans.dat");
        this.load();
    }
    public static class BanEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        public final UUID uuid;
        public final String playerName;
        public final String reason;
        public final long timestamp;
        public final long expiry;
        public BanEntry(UUID uuid, String name, String reason, long seconds) {
            this.uuid = uuid;
            this.playerName = name;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
            this.expiry = seconds == 0L ? Long.MAX_VALUE : this.timestamp + (seconds * 1000L);
        }
        public boolean isExpired() { return System.currentTimeMillis() > this.expiry; }
        public String getDuration() {
            if (this.expiry == Long.MAX_VALUE) return "Permanent";
            long diff = this.expiry - System.currentTimeMillis();
            long d = diff / 86400000L; if (d > 0) return d + "d";
            long h = diff / 3600000L; if (h > 0) return h + "h";
            return (diff / 60000L) + "m";
        }
    }
    @SuppressWarnings("unchecked")
    private void load() {
        if (!this.banFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.banFile))) {
            Map<UUID, BanEntry> loaded = (Map<UUID, BanEntry>) ois.readObject();
            loaded.forEach((uuid, e) -> { if (!e.isExpired()) this.banRegistry.put(uuid, e); });
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.banFile))) {
            oos.writeObject(this.banRegistry);
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void ban(Player p, String reason, long seconds) {
        this.banRegistry.put(p.getUniqueId(), new BanEntry(p.getUniqueId(), p.getName(), reason, seconds));
        this.save();
        p.kickPlayer("§cYou are banned\nReason: " + reason + "\nDuration: " + (seconds == 0L ? "Permanent" : new BanEntry(p.getUniqueId(), "", "", seconds).getDuration()));
    }
    public boolean unban(String name) {
        UUID target = null;
        for (Map.Entry<UUID, BanEntry> e : this.banRegistry.entrySet()) {
            if (e.getValue().playerName.equalsIgnoreCase(name)) { target = e.getKey(); break; }
        }
        if (target == null) return false;
        this.banRegistry.remove(target);
        this.save();
        return true;
    }
    public List<BanEntry> getActiveBans() {
        List<BanEntry> list = new ArrayList<>();
        for (BanEntry e : this.banRegistry.values()) if (!e.isExpired()) list.add(e);
        list.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return list;
    }
}

package com.kaloudasdev.tabframework;
import com.kaloudasdev.tabframework.command.AdminCommand;
import com.kaloudasdev.tabframework.command.AdminCommandTabCompleter;
import com.kaloudasdev.tabframework.manager.BanManager;
import org.bukkit.plugin.java.JavaPlugin;
public final class TabFrameworkPlugin extends JavaPlugin {
    private static TabFrameworkPlugin instance;
    private BanManager banManager;
    @Override
    public void onEnable() {
        instance = this;
        this.banManager = new BanManager(this);
        java.util.Objects.requireNonNull(this.getCommand("tab")).setExecutor(new AdminCommand());
        java.util.Objects.requireNonNull(this.getCommand("tab")).setTabCompleter(new AdminCommandTabCompleter());
        this.getLogger().info("TabCompletionFramework enabled");
    }
    @Override
    public void onDisable() {
        if (this.banManager != null) this.banManager.save();
        this.getLogger().info("TabCompletionFramework disabled");
    }
    public static TabFrameworkPlugin getInstance() { return instance; }
    public BanManager getBanManager() { return this.banManager; }
}

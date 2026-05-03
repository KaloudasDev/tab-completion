# Tab Completion Framework for Spigot/Paper

[![Minecraft API](https://img.shields.io/badge/Minecraft-1.16.5--1.21-5f9ea0?logo=spigotmc&logoColor=white)](https://www.spigotmc.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?logo=opensourceinitiative&logoColor=yellow)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-339933?logo=java&logoColor=white)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Spigot](https://img.shields.io/badge/Spigot-1.16.5--1.21-orange?logo=spigotmc&logoColor=white)](https://www.spigotmc.org/)

Reference implementation demonstrating contextual argument suggestion for Minecraft Spigot/Paper plugins. Implements Bukkit TabCompleter with multi-level parsing, dynamic providers, and permission-aware filtering.

## Features

- **Multi-Level State Machine** - Depth-aware suggestion generation based on current parsing state
- **Dynamic Player Enumeration** - Real-time online player name completion with prefix filtering
- **Permission-Based Filtering** - Contextual suggestions respect permission boundaries
- **Temporal Token Parsing** - Duration specification with support for s/m/h/d/w units
- **Ban Registry Integration** - Banned player suggestions for unban commands
- **Zero Dependencies** - Uses only native Spigot/Paper API
- **No Configuration Required** - Works out of the box

## Supported Suggestion Levels

| Depth | State | Provider Strategy |
|-------|-------|-------------------|
| 1 | Primary command selection | Static enumeration with prefix filtering |
| 2 | Target resolution | Dynamic player enumeration via Bukkit API |
| 2 (unban) | Ban registry lookup | Persistent storage query with expiration filtering |
| 3 | Duration specification | Predefined token set with temporal unit parsing |

## Supported Commands

| Command | Description | Suggestion Levels |
|---------|-------------|-------------------|
| `/tab ban <player> <reason> [duration]` | Ban a player with optional duration | Player → Duration |
| `/tab unban <player>` | Remove an existing ban | Banned player list |
| `/tab kick <player> [reason]` | Kick a player from the server | Online players |
| `/tab reload` | Reload plugin configuration | None |

## Temporal Token Format

| Unit | Suffix | Example | Conversion |
|------|--------|---------|------------|
| Seconds | `s` | `10s`, `30s` | Direct value |
| Minutes | `m` | `1m`, `5m`, `15m` | value × 60 |
| Hours | `h` | `1h`, `2h`, `12h` | value × 3600 |
| Days | `d` | `1d`, `7d`, `30d` | value × 86400 |
| Weeks | `w` | `1w`, `2w`, `4w` | value × 604800 |

## Installation

### 1. Build the Plugin

```bash
git clone https://github.com/KaloudasDev/tab-completion.git
cd tab-completion
mvn clean package
```

### 2. Deploy to Server

```bash
cp target/tab-completion-1.0.0.jar /path/to/server/plugins/
```

### 3. Restart Server

```bash
# Restart your Spigot/Paper server
# Or use plugman for dynamic loading: plugman load tab-completion
```

## Architecture

### Command Registration Lifecycle

```java
@Override
public void onEnable() {
    getCommand("tab").setExecutor(new AdminCommand());
    getCommand("tab").setTabCompleter(new AdminCommandTabCompleter());
}
```

### Tab Completion State Machine

```java
public List<String> onTabComplete(CommandSender sender, Command command, 
                                  String alias, String[] args) {
    int depth = args.length;
    String token = args[depth - 1].toLowerCase();
    
    if (depth == 1) {
        return this.filterByPrefix(PRIMARY_COMMANDS.stream(), token);
    }
    if (depth == 2) {
        return this.handleSecondLevelCompletion(args[0].toLowerCase(), token);
    }
    if (depth == 3 && args[0].equalsIgnoreCase("ban")) {
        return this.filterByPrefix(DURATION_TOKENS.stream(), token);
    }
    return Collections.emptyList();
}
```

### Persistent Storage Pattern

The BanManager implements Java Serialization for ban registry persistence with automatic expiration handling:

```java
public class BanManager {
    private final Map<UUID, BanEntry> banRegistry = new ConcurrentHashMap<>();
    private final File persistenceFile;
    
    public void ban(Player player, String reason, long seconds) {
    }
    
    public List<BanEntry> getActiveBans() {
    }
}
```

## Permissions

| Node | Description | Default |
|------|-------------|---------|
| `tabframework.admin` | Full access to all commands and tab completion | op |
| `tabframework.use` | Basic command access (future use) | true |

## Testing

### Manual Testing

| Test Case | Command | Expected Result |
|-----------|---------|-----------------|
| Primary commands | `/tab [TAB]` | ban, unban, kick, reload |
| Player completion | `/tab ban [TAB]` | List of online players |
| Duration completion | `/tab ban Kaloudas [TAB]` | 10s, 30s, 1m, 5m, 10m, 30m, 1h, 2h, 6h, 12h, 1d, 2d, 7d, 30d |
| Unban completion | `/tab unban [TAB]` | List of banned players |
| Prefix filtering | `/tab b[TAB]` | ban |
| Permission restriction | Non-op user executing `/tab [TAB]` | No suggestions |

### Automated Testing (MockBukkit)

```java
@Test
public void testTabCompletion() {
    MockBukkit.mock();
    JavaPlugin plugin = MockBukkit.load(TabFrameworkPlugin.class);
    
    TabCompleter completer = new AdminCommandTabCompleter();
    List<String> result = completer.onTabComplete(
        mock(CommandSender.class),
        mock(Command.class),
        "tab",
        new String[] { "b" }
    );
    
    assertTrue(result.contains("ban"));
    MockBukkit.unmock();
}
```

## Implementation Patterns

### 1. Prefix Filtering

All suggestion lists undergo prefix filtering using the `startsWith` string comparison method against the current user input token.

### 2. Dynamic Player Enumeration

The `Bukkit.getOnlinePlayers()` method provides real-time player collection for target selection suggestions.

### 3. Ban Registry Query

The `getActiveBans()` method filters expired entries and returns the current ban list for unban suggestions.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| No suggestions appear | Verify TabCompleter is registered via `setTabCompleter()` in `onEnable()` |
| Wrong suggestions | Check `args.length` conditions and command aliases |
| Suggestions not filtering | Add `startsWith(partial)` filter to all streams |
| Permission errors | Verify permission node matches in `plugin.yml` and code |
| Player names not showing | Ensure `Bukkit.getOnlinePlayers()` returns non-empty collection |
| Unban shows no players | Verify ban registry has non-expired entries |
| Duration parser fails | Input must match regex `^\d+[smhdw]$` |

## Key Implementation Takeaways

1. Implement `TabCompleter` interface with depth-aware logic
2. Register completer via `setTabCompleter()` during plugin initialization
3. Use `args.length` to determine current parsing depth
4. Apply `startsWith(currentToken)` prefix filtering to all suggestion streams
5. Return `Collections.emptyList()` instead of `null` for empty results
6. Validate permissions before generating suggestions
7. Leverage `Bukkit.getOnlinePlayers().stream()` for dynamic player lists
8. Use Java 8+ streams with `filter()` and `collect()` for clean operations
9. Implement persistent storage with automatic expiration handling
10. Test all argument depths with partial and complete inputs

## Compatibility

| Server Software | Supported Versions |
|-----------------|-------------------|
| Paper | 1.16.5 – 1.21.4 |
| Spigot | 1.16.5 – 1.21.4 |
| Purpur | 1.16.5 – 1.21.4 |
| Arclight | 1.16.5 – 1.20.4 |

## Contributing

Contributions are welcome. Please ensure:  
1. Code maintains zero external dependencies beyond Spigot API  
2. All suggestion levels implement prefix filtering  
3. Permission checks precede all suggestion generation  
4. tream operations use appropriate null safety
5. Documentation updated for new features

## License

MIT © [KaloudasDev](https://github.com/KaloudasDev)

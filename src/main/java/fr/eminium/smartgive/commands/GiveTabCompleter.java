package fr.eminium.smartgive.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiveTabCompleter implements TabCompleter {
    
    private List<String> cachedItems = null;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 30000; // 30 secondes

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Auto-complétion pour les noms de joueurs
            if (sender instanceof Player) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        } else if (args.length == 2) {
            // Auto-complétion pour les items et loot tables
            String input = args[1].toLowerCase();
            
            // Ajouter les items vanilla
            for (Material material : Material.values()) {
                if (material.isItem() && material.name().toLowerCase().startsWith(input)) {
                    completions.add(material.name().toLowerCase());
                }
            }
            
            // Ajouter les loot tables
            List<String> lootTables = getLootTables();
            for (String lootTable : lootTables) {
                if (lootTable.toLowerCase().startsWith(input)) {
                    completions.add(lootTable);
                }
            }
            
            return completions;
        } else if (args.length == 3) {
            // Auto-complétion pour les quantités
            if (args[2].isEmpty()) {
                completions.add("1");
                completions.add("16");
                completions.add("32");
                completions.add("64");
            }
            return completions;
        }
        
        return completions;
    }
    
    private List<String> getLootTables() {
        long currentTime = System.currentTimeMillis();
        
        // Mettre en cache les loot tables pendant 30 secondes
        if (cachedItems == null || (currentTime - lastCacheTime) > CACHE_DURATION) {
            cachedItems = new ArrayList<>();
            
            // Liste des loot tables de base de Minecraft
            cachedItems.addAll(Arrays.asList(
                "minecraft:chests/simple_dungeon",
                "minecraft:chests/abandoned_mineshaft",
                "minecraft:chests/desert_pyramid",
                "minecraft:chests/jungle_temple",
                "minecraft:chests/stronghold_corridor",
                "minecraft:chests/village/village_armorer",
                "minecraft:chests/end_city_treasure",
                "minecraft:entities/zombie",
                "minecraft:entities/skeleton",
                "minecraft:entities/creeper",
                "minecraft:gameplay/fishing/treasure",
                "minecraft:gameplay/fishing/fish"
            ));
            
            lastCacheTime = currentTime;
        }
        
        return cachedItems;
    }
}

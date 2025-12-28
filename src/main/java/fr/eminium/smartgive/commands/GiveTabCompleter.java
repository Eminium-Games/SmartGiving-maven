package fr.eminium.smartgive.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                String input = args[0].toLowerCase();
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
            }
            return completions;
        } else if (args.length == 2) {
            // Auto-complétion pour les items et loot tables
            String input = args[1].toLowerCase();
            
            // Ajouter les items vanilla
            List<String> allItems = new ArrayList<>();
            
            // Items vanilla
            Arrays.stream(Material.values())
                .filter(Material::isItem)
                .map(m -> m.name().toLowerCase())
                .filter(name -> name.startsWith(input))
                .forEach(allItems::add);
            
            // Loot tables des datapacks
            getLootTables().stream()
                .filter(lt -> lt.startsWith(input))
                .forEach(allItems::add);
            
            return allItems;
        } else if (args.length == 3) {
            // Auto-complétion pour les quantités
            if (args[2].isEmpty()) {
                return Arrays.asList("1", "16", "32", "64");
            }
        }
        
        return completions;
    }
    
    private List<String> getLootTables() {
        long currentTime = System.currentTimeMillis();
        
        // Mettre en cache les loot tables pendant 30 secondes
        if (cachedItems == null || (currentTime - lastCacheTime) > CACHE_DURATION) {
            cachedItems = new ArrayList<>();
            
            // Parcourir tous les mondes pour trouver les datapacks
            for (World world : Bukkit.getWorlds()) {
                try {
                    File worldFolder = world.getWorldFolder();
                    File datapacksFolder = new File(worldFolder, "datapacks");
                    
                    if (datapacksFolder.exists() && datapacksFolder.isDirectory()) {
                        // Parcourir les dossiers de datapacks
                        for (File datapack : Objects.requireNonNull(datapacksFolder.listFiles(File::isDirectory))) {
                            File lootTablesFolder = new File(datapack, "data/minecraft/loot_tables");
                            scanLootTables(lootTablesFolder, "minecraft", cachedItems);
                            
                            // Parcourir tous les namespaces du datapack
                            File dataFolder = new File(datapack, "data");
                            if (dataFolder.exists() && dataFolder.isDirectory()) {
                                for (File namespace : Objects.requireNonNull(dataFolder.listFiles(File::isDirectory))) {
                                    lootTablesFolder = new File(namespace, "loot_tables");
                                    scanLootTables(lootTablesFolder, namespace.getName(), cachedItems);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs
                }
            }
            
            // Ajouter des loot tables communes si nécessaire
            if (cachedItems.isEmpty()) {
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
                    "minecraft:gameplay/fishing/treasure"
                ));
            }
            
            lastCacheTime = currentTime;
        }
        
        return cachedItems;
    }
    
    private void scanLootTables(File lootTablesFolder, String namespace, List<String> lootTables) {
        if (lootTablesFolder.exists() && lootTablesFolder.isDirectory()) {
            scanDirectory(lootTablesFolder, "", namespace, lootTables);
        }
    }
    
    private void scanDirectory(File dir, String path, String namespace, List<String> lootTables) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newPath = path.isEmpty() ? file.getName() : path + "/" + file.getName();
                    scanDirectory(file, newPath, namespace, lootTables);
                } else if (file.getName().endsWith(".json")) {
                    String tableName = file.getName().substring(0, file.getName().length() - 5);
                    String fullPath = path.isEmpty() ? tableName : path + "/" + tableName;
                    lootTables.add(namespace + ":" + fullPath);
                }
            }
        }
    }
}
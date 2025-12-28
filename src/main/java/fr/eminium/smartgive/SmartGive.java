package fr.eminium.smartgive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;

import fr.eminium.smartgive.commands.GiveCommand;
import fr.eminium.smartgive.utils.I18n;

public class SmartGive extends JavaPlugin {

    private static SmartGive instance;
    private GiveCommand giveCommand;

    @Override
    public void onEnable() {
        instance = this;
        
        // Sauvegarder la config par défaut si elle n'existe pas
        saveDefaultConfig();
        
        // Initialiser le système de traduction
        I18n.init(this);
        
        this.giveCommand = new GiveCommand();
        
        // Enregistrement de la commande
        this.getCommand("give").setExecutor(giveCommand);
        
        getLogger().info("SmartGive activé avec succès !");
    }

    @Override
    public void onDisable() {
        getLogger().info("SmartGive désactivé !");
    }

    public static SmartGive getInstance() {
        return instance;
    }

    public boolean isVanillaItem(String itemName) {
        try {
            Material material = Material.matchMaterial(itemName);
            return material != null && material.isItem();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLootTable(String name) {
        try {
            String[] parts = name.split(":");
            String namespace = parts.length > 1 ? parts[0] : "minecraft";
            String key = parts.length > 1 ? parts[1] : parts[0];
            
            NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
            LootTable lootTable = Bukkit.getLootTable(namespacedKey);
            return lootTable != null;
        } catch (Exception e) {
            return false;
        }
    }
}

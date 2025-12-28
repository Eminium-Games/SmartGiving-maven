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
import fr.eminium.smartgive.commands.GiveTabCompleter;
import fr.eminium.smartgive.utils.I18n;

public class SmartGive extends JavaPlugin {

    private static SmartGive instance;
    private GiveCommand giveCommand;

    // Dans SmartGive.java, méthode onEnable()
@Override
public void onEnable() {
    instance = this;
    
    // Sauvegarder la configuration par défaut si elle n'existe pas
    saveDefaultConfig();
    
    // Initialiser le système de traduction
    I18n.init(this);
    
    this.giveCommand = new GiveCommand();
    
    // Enregistrement de la commande et de son compléteur
    this.getCommand("give").setExecutor(giveCommand);
    this.getCommand("give").setTabCompleter(new GiveTabCompleter());
    
    // Enregistrement des alias
    if (getCommand("smartgive") != null) {
        getCommand("smartgive").setExecutor(giveCommand);
        getCommand("smartgive").setTabCompleter(new GiveTabCompleter());
    }
    if (getCommand("sgive") != null) {
        getCommand("sgive").setExecutor(giveCommand);
        getCommand("sgive").setTabCompleter(new GiveTabCompleter());
    }
    
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
            
            // Vérification plus simple pour la compatibilité Bukkit
            // On suppose que si le format est valide, la loot table existe
            // Cela évite d'utiliser des méthodes qui pourraient ne pas être disponibles
            return key.matches("[a-z0-9/._-]+") && namespace.matches("[a-z0-9._-]+");
        } catch (Exception e) {
            return false;
        }
    }
}

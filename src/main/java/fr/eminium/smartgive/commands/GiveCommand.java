package fr.eminium.smartgive.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.eminium.smartgive.SmartGive;
import fr.eminium.smartgive.utils.I18n;

public class GiveCommand implements CommandExecutor {

    private final SmartGive plugin;

    public GiveCommand() {
        this.plugin = SmartGive.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            I18n.sendMessage(sender, "only_players");
            return true;
        }
        
        if (args.length < 1) {
            I18n.sendMessage(sender, "invalid_usage");
            return true;
        }

        // Gestion des arguments
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            I18n.sendMessage(sender, "player_not_found", args[0]);
            return true;
        }

        if (args.length < 2) {
            I18n.sendMessage(sender, "specify_item_or_loot");
            return true;
        }

        String itemName = args[1].toLowerCase();
        int amount = 1;

        // Vérification de la quantité
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    I18n.sendMessage(sender, "invalid_amount");
                    return true;
                }
            } catch (NumberFormatException e) {
                I18n.sendMessage(sender, "invalid_amount");
                return true;
            }
        }

        // Vérification si c'est un item vanilla
        if (plugin.isVanillaItem(itemName)) {
            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                ItemStack item = new ItemStack(material, amount);
                target.getInventory().addItem(item);
                I18n.sendMessage(sender, "give_success", amount, material.name().toLowerCase(), target.getName());
                return true;
            }
        }
        // Vérification si c'est une loot table
        else if (plugin.isLootTable(itemName)) {
            String[] parts = itemName.split(":");
            String namespace = parts.length > 1 ? parts[0] : "minecraft";
            String key = parts.length > 1 ? parts[1] : parts[0];
            
            // Exécution de la commande loot
            String lootCommand = String.format("loot give %s loot %s:%s", 
                target.getName(), namespace, key);
            
            for (int i = 0; i < amount; i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), lootCommand);
            }
            
            I18n.sendMessage(sender, "give_loot_success", itemName, amount, target.getName());
            return true;
        }
        
        // Si on arrive ici, l'item n'est ni vanilla ni une loot table valide
        I18n.sendMessage(sender, "invalid_item", itemName);
        return true;
    }
}

package fr.eminium.smartgive.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        // Vérification des permissions
        if (!sender.hasPermission("minecraft.command.give")) {
            I18n.sendMessage(sender, "no_permission");
            return true;
        }

        // Vérification des arguments
        if (args.length < 2) {
            I18n.sendMessage(sender, "usage", "/" + label + " <player> <item> [amount]");
            return true;
        }

        // Récupération du joueur cible
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            I18n.sendMessage(sender, "player_not_found", args[0]);
            return true;
        }

        // Récupération de la quantité
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    I18n.sendMessage(sender, "invalid_amount");
                    return true;
                }
            } catch (NumberFormatException e) {
                I18n.sendMessage(sender, "invalid_number", args[2]);
                return true;
            }
        }

        String itemName = args[1].toLowerCase();
        ItemStack item = null;

        // Vérification si c'est un item vanilla
        if (plugin.isVanillaItem(itemName)) {
            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                item = new ItemStack(material, amount);
            }
        }
        // Vérification si c'est une loot table
        else if (plugin.isLootTable(itemName)) {
            String[] parts = itemName.split(":");
            String namespace = parts.length > 1 ? parts[0] : "minecraft";
            String path = parts.length > 1 ? parts[1] : parts[0];
            
            // Exécution de la commande loot
            String lootCommand = String.format("loot give %s loot %s", target.getName(), itemName);
            for (int i = 0; i < amount; i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), lootCommand);
            }
            
            I18n.sendMessage(sender, "give_loot_success", itemName, amount, target.getName());
            return true;
        }

        if (item == null) {
            I18n.sendMessage(sender, "item_not_found", itemName);
            return true;
        }

        // Don de l'item au joueur
        target.getInventory().addItem(item);
        I18n.sendMessage(sender, "give_success", amount, item.getType().name().toLowerCase(), target.getName());
        return true;
    }
}
package fr.eminium.smartgive.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.eminium.smartgive.SmartGive;

public class GiveCommand implements CommandExecutor {

    private final SmartGive plugin;

    public GiveCommand() {
        this.plugin = SmartGive.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérification des permissions
        if (!sender.hasPermission("minecraft.command.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Vérification des arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <item> [amount]");
            return true;
        }

        // Récupération du joueur cible
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        // Récupération de la quantité
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number.");
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
            String lootCommand = String.format("loot give %s loot %s", target.getName(), itemName);
            for (int i = 0; i < amount; i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), lootCommand);
            }
            sender.sendMessage(String.format("§aGave loot table %s %d times to %s", itemName, amount, target.getName()));
            return true;
        }

        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Item or loot table not found: " + itemName);
            return true;
        }

        // Don de l'item au joueur
        target.getInventory().addItem(item);
        sender.sendMessage(String.format("§aGave %d %s to %s", amount, item.getType().name().toLowerCase(), target.getName()));
        return true;
    }
}
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

public class GiveCommand implements CommandExecutor {

    private final SmartGive plugin;

    public GiveCommand() {
        this.plugin = SmartGive.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande ne peut être utilisée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage("§cUtilisation: /give <joueur> <item|loottable> [quantité]");
            return true;
        }

        // Gestion des arguments
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cVeuillez spécifier un item ou une loot table.");
            return true;
        }

        String itemName = args[1].toLowerCase();
        int amount = 1;

        // Vérification de la quantité
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64; // Limite de pile
            } catch (NumberFormatException e) {
                player.sendMessage("§cLa quantité doit être un nombre valide.");
                return true;
            }
        }

        // Vérification si c'est un item vanilla
        if (plugin.isVanillaItem(itemName)) {
            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                ItemStack item = new ItemStack(material, amount);
                target.getInventory().addItem(item);
                player.sendMessage("§aDonné " + amount + " " + itemName + " à " + target.getName());
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
            
            player.sendMessage("§aDonné " + amount + " fois la loot table " + itemName + " à " + target.getName());
            return true;
        }
        
        // Si on arrive ici, l'item n'est ni vanilla ni une loot table valide
        player.sendMessage("§cItem ou loot table introuvable: " + itemName);
        return true;
    }
}

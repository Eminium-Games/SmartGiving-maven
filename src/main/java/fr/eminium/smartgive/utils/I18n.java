package fr.eminium.smartgive.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n {
    private static final Map<Locale, Properties> translations = new HashMap<>();
    private static JavaPlugin plugin;
    private static Locale defaultLocale = Locale.ENGLISH;

    private I18n() {}

    public static void init(JavaPlugin plugin) {
        I18n.plugin = plugin;
        
        // Créer le dossier des langues s'il n'existe pas
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        // Charger les langues par défaut
        saveDefaultLocale("en");
        saveDefaultLocale("fr");
        
        // Charger les traductions
        loadTranslations();
    }
    
    private static void saveDefaultLocale(String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".properties");
        if (!langFile.exists()) {
            try (InputStream in = plugin.getResource("lang/" + lang + ".properties")) {
                if (in != null) {
                    Files.copy(in, langFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save default " + lang + " language file: " + e.getMessage());
            }
        }
    }
    
    private static void loadTranslations() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".properties"));
        
        if (files != null) {
            for (File file : files) {
                String lang = file.getName().replace(".properties", "");
                Locale locale = new Locale(lang);
                Properties props = new Properties();
                
                try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                    props.load(reader);
                    translations.put(locale, props);
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to load " + lang + " language file: " + e.getMessage());
                }
            }
        }
    }
    
    public static String translate(Locale locale, String key, Object... args) {
        Properties props = translations.getOrDefault(locale, translations.get(defaultLocale));
        if (props == null) {
            return key; // Fallback à la clé si la traduction n'existe pas
        }
        
        String message = props.getProperty(key, key);
        // Formatage du message avec les arguments
        try {
            return String.format(message, args);
        } catch (Exception e) {
            return message; // Retourne le message non formaté en cas d'erreur
        }
    }
    
    public static String translate(CommandSender sender, String key, Object... args) {
        // Pour le moment, on utilise toujours la locale par défaut
        // car la méthode getLocale() n'est pas disponible dans cette version de Bukkit
        return translate(defaultLocale, key, args);
    }
    
    public static void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(translate(sender, key, args));
    }
}

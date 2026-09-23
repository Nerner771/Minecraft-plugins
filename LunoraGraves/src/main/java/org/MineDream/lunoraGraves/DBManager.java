package org.MineDream.lunoraGraves;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class DBManager {


    public static void saveToYAML(Grave g) {


        Plugin pl = Bukkit.getPluginManager().getPlugin("LunoraGraves");
        if (pl ==null) {
            //System.out.println("Не удалость получить экземпляр плагина.");
            return;
        }

        File folderGrave = new File(pl.getDataFolder(), "graves");
        File folderPlayer = new File(folderGrave, g.getPlayer() + " " + g.getPlayerUUID().toString());
        if (folderPlayer.exists()) {
            folderPlayer.mkdirs();
        }


        String safeTime = g.getTimeDeath().replace(":", ".");
        File grave = new File (folderPlayer, safeTime + ".yml");

        YamlConfiguration config = new YamlConfiguration();

        config.set("playerUUID", g.getPlayerUUID().toString());
        config.set("playerName", g.getPlayer());
        config.set("deathTimestamp", g.getTimeDeath());

        config.set("inventory.hotbar", g.getPlayerHotbar());
        config.set("inventory.main", g.getPlayerInventory());
        config.set("inventory.armor", g.getPlayerEquipment());

        try {
            config.save(grave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Grave loadFromYaml(String playerName, UUID playerUUID, String timeDeath) {
        Plugin pl = Bukkit.getPluginManager().getPlugin("LunoraGraves");
        if (pl == null) {
            //System.out.println("[LunoraGraves] Не удалось получить экземпляр плагина при загрузке.");
            return null;
        }

        File folderGrave = new File(pl.getDataFolder(), "graves");
        File folderPlayer = new File(folderGrave, playerName + " " + playerUUID.toString());

        String safeTime = timeDeath.replace(":", ".");
        File graveFile = new File(folderPlayer, safeTime + ".yml");

        if (!graveFile.exists()) {
            //System.out.println("[LunoraGraves] Файл могилы не найден: " + graveFile.getAbsolutePath());
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(graveFile);

        try {
            String name = config.getString("playerName");
            UUID uuid = UUID.fromString(config.getString("playerUUID"));


            String deathTime = config.getString("deathTimestamp");
            LocalTime time = LocalTime.parse(deathTime);
            int hour = time.getHour();
            int min = time.getMinute();
            int sec = time.getSecond();
            LocalTime parsedTime = LocalTime.of(hour, min, sec);


            List<?> hotbarList = config.getList("inventory.hotbar");
            ItemStack[] hotbar = hotbarList != null ? hotbarList.toArray(new ItemStack[0]) : new ItemStack[9];

            List<?> mainList = config.getList("inventory.main");
            ItemStack[] mainInventory = mainList != null ? mainList.toArray(new ItemStack[0]) : new ItemStack[27];

            List<?> armorList = config.getList("inventory.armor");
            ItemStack[] armor = armorList != null ? armorList.toArray(new ItemStack[0]) : new ItemStack[4];

            return new Grave(name, uuid, mainInventory, hotbar, armor, parsedTime);

        } catch (Exception e) {
            //System.out.println("[LunoraGraves] Ошибка при парсинге YAML файла могилы: " + graveFile.getName());
            e.printStackTrace();
            return null;
        }
    }
}

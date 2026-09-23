package org.MineDream.lunoraGraves;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;

public class GraveTicker {

    public static void startGlobalTicker(Plugin plugin) {
        NamespacedKey keyDeleteAt = new NamespacedKey(plugin, "grave_delete_at");
        NamespacedKey keyOwnerName = new NamespacedKey(plugin, "grave_owner_name");
        NamespacedKey keyOwnerUuid = new NamespacedKey(plugin, "grave_owner_uuid");
        NamespacedKey keyDeathTime = new NamespacedKey(plugin, "grave_death_time");

        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                //Проходим по всем загруженным мирам сервера
                for (World world : Bukkit.getWorlds()) {

                    for (ArmorStand as : world.getEntitiesByClass(ArmorStand.class)) {
                        PersistentDataContainer pdc = as.getPersistentDataContainer();

                        //Если у стойки есть наш ключ удаления, значит это могила
                        if (!pdc.has(keyDeleteAt, PersistentDataType.LONG)) continue;

                        long deleteAt = pdc.get(keyDeleteAt, PersistentDataType.LONG);
                        long timeLeftMillis = deleteAt - now;

                        //Время вышло — удаляем могилу и файл
                        if (timeLeftMillis <= 0) {
                            String ownerName = pdc.get(keyOwnerName, PersistentDataType.STRING);
                            String ownerUuid = pdc.get(keyOwnerUuid, PersistentDataType.STRING);
                            String deathTime = pdc.get(keyDeathTime, PersistentDataType.STRING);

                            as.remove();

                            //Удаляем файл
                            File folderPlayer = new File(plugin.getDataFolder(), "graves/" + ownerName + " " + ownerUuid);
                            File graveFile = new File(folderPlayer, deathTime + ".yml");
                            String safeTime = deathTime.replace(":",".");
                            if (graveFile.exists()) {
                                graveFile.delete();
                                //System.out.println("Могила "+ safeTime+ " игрока " + ownerName + " опустошена и успешно удалена!");
                            }
                            else {
                                //System.out.println("Не удалось удалить файл могилы " + graveFile.getName());
                            }

                            continue;
                        }


                        long totalSeconds = timeLeftMillis / 1000;
                        long minutes = totalSeconds / 60;
                        long seconds = totalSeconds % 60;
                        String timeString = String.format("%02d:%02d", minutes, seconds);

                        String ownerName = pdc.get(keyOwnerName, PersistentDataType.STRING);

                        String format = plugin.getConfig().getString("grave-holo-text", "&7Могила &e%player% &7[&c%time%&7]");

                        String rawName = format
                                .replace("%player%", ownerName != null ? ownerName : "Неизвестно")
                                .replace("%time%", timeString);

                        var adventureComponent = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(rawName);
                        as.customName(adventureComponent);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Запуск каждую секунду
    }
}

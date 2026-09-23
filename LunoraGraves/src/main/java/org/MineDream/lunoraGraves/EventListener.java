package org.MineDream.lunoraGraves;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

import static org.MineDream.lunoraGraves.DBManager.loadFromYaml;
import static org.MineDream.lunoraGraves.DBManager.saveToYAML;

public class EventListener implements Listener {


    @EventHandler
    public void onDie(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        LocalTime td = LocalTime.now();
        UUID puu = p.getUniqueId();

        //Сохраняем содержимре инвентаря
        ItemStack[] pi = new ItemStack[27];
        for (int i = 9; i <= 35; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                pi[i-9] = item;
            }
        }

        //Сохраняем содержимое хотбара
        ItemStack[] ph = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) ph[i] = item;
        }


        //Сохраняем одетое снаряжение
        ItemStack[] armor = inv.getArmorContents();
        ItemStack[] peq = new ItemStack[armor.length];
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                peq[i] = armor[i];
            }
        }

        //Создаем могилу
        if (!inv.isEmpty()) {
            //System.out.println("[LunoraGraves] Создана могила игрока " + p.getName());
            Grave g = new Grave(p.getName(),puu,pi,ph,peq,td);

            spawnGrave(g, p);
            saveToYAML(g);
            e.getDrops().clear();
        }
        else {
            //System.out.println("[LunoraGraves] Инвентарь игрока " + p.getName() + " пустой, могила не создана!");
        }
    }

    public void spawnGrave(Grave g, Player p) {

        Location l = p.getLocation();
        l.setY(l.getBlockY() - 1.3);
        ArmorStand  ge = (ArmorStand) p.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);

        ge.setAI(false);
        ge.setSilent(true);
        ge.setInvulnerable(true);
        ge.setRemoveWhenFarAway(false);
        ge.setBasePlate(false);
        ge.setArms(false);
        ge.setInvisible(true);
        ge.setGravity(false);

        ge.setCustomName("§7Могила §e" + g.getPlayer() + " §7[§c15:00§7]");
        ge.setCustomNameVisible(true);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ge.addDisabledSlots(slot);
        }

        ItemStack graveItem = new ItemStack(Material.CHISELED_STONE_BRICKS);

        CustomModelData.Builder cmdBuilder = CustomModelData.customModelData();
        cmdBuilder.addString("grave_stone");
        graveItem.setData(DataComponentTypes.CUSTOM_MODEL_DATA, cmdBuilder.build());

        ge.getEquipment().setHelmet(graveItem);

        PersistentDataContainer pdc = ge.getPersistentDataContainer();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LunoraGraves");
        if (plugin == null) return;

        //Создаем ключи
        NamespacedKey keyOwnerName = new NamespacedKey(plugin, "grave_owner_name");
        NamespacedKey keyOwnerUuid = new NamespacedKey(plugin, "grave_owner_uuid");
        NamespacedKey keyDeathTime = new NamespacedKey(plugin, "grave_death_time");
        NamespacedKey keyDeleteAt = new NamespacedKey(plugin, "grave_delete_at");

        long deleteAtMillis = System.currentTimeMillis() + (15 * 60 * 1000);




        //Записываем данные
        pdc.set(keyDeleteAt, PersistentDataType.LONG, deleteAtMillis);
        pdc.set(keyOwnerName, PersistentDataType.STRING, g.getPlayer());
        pdc.set(keyOwnerUuid, PersistentDataType.STRING, g.getPlayerUUID().toString());

        String formattedTime = g.getTimeDeath();
        pdc.set(keyDeathTime, PersistentDataType.STRING, formattedTime);


    }

    @EventHandler
    public void onClickGrave(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Entity clicked = e.getRightClicked();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LunoraGraves");
        if (plugin == null) return;




        //Проверяем что сущность - могила
        PersistentDataContainer pdc = clicked.getPersistentDataContainer();
        NamespacedKey keyDeathTime = new NamespacedKey(plugin, "grave_death_time");



        //Вытаскиваем PDC
        if (pdc.has(keyDeathTime, PersistentDataType.STRING)) {
            e.setCancelled(true);

            NamespacedKey keyOwnerName = new NamespacedKey(plugin, "grave_owner_name");
            NamespacedKey keyOwnerUuid = new NamespacedKey(plugin, "grave_owner_uuid");

            String ownerName = pdc.get(keyOwnerName, PersistentDataType.STRING);
            UUID ownerUuid = UUID.fromString(pdc.get(keyOwnerUuid, PersistentDataType.STRING));
            String deathTime = pdc.get(keyDeathTime, PersistentDataType.STRING);

            Player p = e.getPlayer();
            //System.out.println("[LunoraGraves] Успешно открыта могила игрока " + ownerName);

            //Загружаем данные из YAML
            Grave g = loadFromYaml(ownerName, ownerUuid, deathTime);

            if (g != null) {
                if (p.isSneaking()) {
                    if (plugin.getConfig().getBoolean("quick-up-enabled", false)) {

                        PlayerInventory playerInv = p.getInventory();


                        ItemStack[] hotbar = g.getPlayerHotbar();
                        for (int i = 0; i < 9; i++) {
                            if (hotbar[i] == null || hotbar[i].getType() == Material.AIR) continue;

                            if (playerInv.getItem(i) == null || playerInv.getItem(i).getType() == Material.AIR) {
                                playerInv.setItem(i, hotbar[i]);
                            } else {
                                playerInv.addItem(hotbar[i]);
                            }
                        }

                        ItemStack[] armor = g.getPlayerEquipment();
                        ItemStack[] currentArmor = playerInv.getArmorContents();

                        for (int i = 0; i < 4; i++) {
                            if (armor[i] == null || armor[i].getType() == Material.AIR) continue;

                            if (currentArmor[i] == null || currentArmor[i].getType() == Material.AIR) {
                                currentArmor[i] = armor[i];
                            } else {
                                playerInv.addItem(armor[i]);
                            }
                        }
                        playerInv.setArmorContents(currentArmor);

                        ItemStack[] mainInv = g.getPlayerInventory();
                        for (int i = 0; i < mainInv.length; i++) {
                            if (mainInv[i] == null || mainInv[i].getType() == Material.AIR) continue;

                            int targetSlot = 9 + i;
                            if (playerInv.getItem(targetSlot) == null || playerInv.getItem(targetSlot).getType() == Material.AIR) {
                                playerInv.setItem(targetSlot, mainInv[i]);
                            } else {
                                playerInv.addItem(mainInv[i]);
                            }
                        }
                        clicked.remove();


                        if (plugin != null) {
                            File folderPlayer = new File(plugin.getDataFolder(), "graves/" + ownerName + " " + ownerUuid.toString());
                            String safeTime = deathTime.replace(":",".");
                            File graveFile = new File(folderPlayer, safeTime + ".yml");
                            if (graveFile.exists()) {
                                graveFile.delete();
                                //System.out.println("Могила "+ safeTime + "игрока " + ownerName + " опустошена и успешно удалена!");
                            }
                            else {
                                //System.out.println("Не удалось удалить файл могилы " + graveFile.getName());
                            }
                    }

                    }


                }
                else {
                    UUID graveEntityUUID = clicked.getUniqueId();
                    openGraveInventory(p, g, deathTime, graveEntityUUID);
                }
            } else {
                //System.out.println("[LunoraGraves] Файл могилы поврежден или отсутствует.");
            }
        }
    }

    public static void openGraveInventory(Player player, Grave grave, String deathTime, UUID graveEntityUUID) {
        GraveHolder holder = new GraveHolder(grave.getPlayer(), grave.getPlayerUUID(), deathTime, graveEntityUUID);
        Inventory gui = Bukkit.createInventory(holder, 54, "§8Могила: §0" + grave.getPlayer());

        //Заполняем инвентарь вещами
        ItemStack[] mainInv = grave.getPlayerInventory();
        ItemStack[] hotbar = grave.getPlayerHotbar();
        ItemStack[] armor = grave.getPlayerEquipment();

        for (int i = 0; i < mainInv.length; i++) if (mainInv[i] != null) gui.setItem(i, mainInv[i]);
        for (int i = 0; i < hotbar.length; i++) if (hotbar[i] != null) gui.setItem(27 + i, hotbar[i]);
        for (int i = 0; i < armor.length; i++) if (armor[i] != null) gui.setItem(36 + i, armor[i]);

        player.openInventory(gui);
    }

    @EventHandler
    public void onGraveClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        //Проверяем, является ли закрытый инвентарь могилой
        if (inventory.getHolder() instanceof GraveHolder) {
            GraveHolder holder = (GraveHolder) inventory.getHolder();

            boolean isEmpty = true;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    isEmpty = false;
                    break;
                }
            }

            Entity e = Bukkit.getEntity(holder.getArmorStandUUID());
            String timeDeath = holder.getDeathTime();
            String playerName = holder.getOwnerName();

            //Удаляем могилу, если она пуста
            if (isEmpty) {
                if (e != null) e.remove();


                org.bukkit.plugin.Plugin pl = Bukkit.getPluginManager().getPlugin("LunoraGraves");
                if (pl != null) {
                    File folderPlayer = new File(pl.getDataFolder(), "graves/" + holder.getOwnerName() + " " + holder.getOwnerUUID().toString());
                    String safeTime = timeDeath.replace(":",".");
                    File graveFile = new File(folderPlayer, timeDeath + ".yml");
                    if (graveFile.exists()) {
                        graveFile.delete();
                        //System.out.println("Могила "+ safeTime + "игрока " + playerName + " опустошена и успешно удалена!");
                    }
                    else {
                        //System.out.println("Не удалось удалить файл могилы " + graveFile.getName());
                    }
                }


            }
            else {
                //Вытаскиваем данные
                ItemStack[] currentContents = inventory.getContents();

                //Разделяю инвентари
                ItemStack[] mainInventory = Arrays.copyOfRange(currentContents, 0, 27);  // Слоты 0-26
                ItemStack[] hotbar = Arrays.copyOfRange(currentContents, 27, 36);         // Слоты 27-35
                ItemStack[] armor = Arrays.copyOfRange(currentContents, 36, 40);          // Слоты 36-39

                LocalTime parsedTime = LocalTime.parse(holder.getDeathTime());

                Grave updatedGrave = new Grave(
                        holder.getOwnerName(),
                        holder.getOwnerUUID(),
                        mainInventory,
                        hotbar,
                        armor,
                        parsedTime
                );

                //Сохраняем
                saveToYAML(updatedGrave);
                //System.out.println("Содержимое могилы "+ updatedGrave.getTimeDeath() + "игрока" + updatedGrave.getPlayer() +" успешно сохранено!");
            }
        }
    }
}





package org.MineDream.lunoraGraves;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.LocalTime;
import java.util.UUID;

public class Grave {

    private String player;



    private UUID playerUUID;
    private ItemStack[] playerInventory;
    private ItemStack[] playerHotbar;
    private ItemStack[] playerEquipment;
    private String timeDeath;

    public Grave(String player, UUID playerUUID, ItemStack[] playerInventory, ItemStack[] playerHotbar,  ItemStack[] playerEquipment ,LocalTime timeDeath) {
        this.player = player;
        this.playerUUID = playerUUID;
        this.playerInventory = playerInventory;
        this.playerHotbar = playerHotbar;
        this.timeDeath = timeDeath.toString();
        this.playerEquipment = playerEquipment;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getTimeDeath() {
        return timeDeath;
    }

    public void setTimeDeath(LocalTime timeDeath) {
        this.timeDeath = timeDeath.toString();
    }

    public ItemStack[] getPlayerHotbar() {
        return playerHotbar;
    }

    public void setPlayerHotbar(ItemStack[] playerHotbar) {
        this.playerHotbar = playerHotbar;
    }

    public ItemStack[] getPlayerEquipment() {
        return playerEquipment;
    }

    public void setPlayerEquipment(ItemStack[] playerEquipment) {
        this.playerEquipment = playerEquipment;
    }

    public ItemStack[] getPlayerInventory() {
        return playerInventory;
    }

    public void setPlayerInventory(ItemStack[] playerInventory) {
        this.playerInventory = playerInventory;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }
}

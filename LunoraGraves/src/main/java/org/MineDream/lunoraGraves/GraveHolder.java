package org.MineDream.lunoraGraves;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

public class GraveHolder implements InventoryHolder {
    private final String ownerName;
    private final UUID ownerUUID;
    private final String deathTime;
    private final UUID armorStandUUID;

    public GraveHolder(String ownerName, UUID ownerUUID, String deathTime, UUID armorStandUUID) {
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
        this.deathTime = deathTime;
        this.armorStandUUID = armorStandUUID;
    }

    public String getOwnerName() { return ownerName; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getDeathTime() { return deathTime; }
    public UUID getArmorStandUUID() {return armorStandUUID;}

    @Override
    public Inventory getInventory() {
        return null;
    }
}

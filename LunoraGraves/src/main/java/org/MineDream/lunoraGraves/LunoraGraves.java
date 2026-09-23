package org.MineDream.lunoraGraves;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.MineDream.lunoraGraves.GraveTicker.startGlobalTicker;

public final class LunoraGraves extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("Плагин LunovaGraves запущен");
        saveDefaultConfig();
        startGlobalTicker(this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}

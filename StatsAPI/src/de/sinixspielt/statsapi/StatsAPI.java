package de.sinixspielt.statsapi;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.sinixspielt.statsapi.database.SQLManager;
import de.sinixspielt.statsapi.file.FileManager;
import de.sinixspielt.statsapi.listener.PlayerListener;
import de.sinixspielt.statsapi.manager.StatsManager;

/*
Class created on 01.03.2019 by SinixSpielt
 * */

public class StatsAPI extends JavaPlugin{
	
	public static StatsAPI instance;
	public static FileManager fileManager;
	public static SQLManager sqlManager;
	public static StatsManager statsManager;
	
	@Override
	public void onEnable() {
		instance = this;
		fileManager = new FileManager();
		if (!loadSQL()) {
			return;
		}
		statsManager = new StatsManager();
		loadListeners();
	}
	
	@Override
	public void onDisable() {
		instance = null;
	}
	
	private void loadListeners() {
		PluginManager load = Bukkit.getPluginManager();
		load.registerEvents(new PlayerListener(), this);
	}
	
	private boolean loadSQL() {
		FileConfiguration cfg = fileManager.getDatabaseFile().getConfig();
		String host = cfg.getString("DATABASE.HOST");
		String port = cfg.getString("DATABASE.PORT");
		String user = cfg.getString("DATABASE.USER");
		String pass = cfg.getString("DATABASE.PASSWORD");
		String database = cfg.getString("DATABASE.DATABASE");
		sqlManager = new SQLManager(host, port, user, pass, database);
		return sqlManager.openConnection();
	}
	
	public static StatsAPI getInstance() {
		return instance;
	}
	
	public static FileManager getFileManager() {
		return fileManager;
	}
	
	public static SQLManager getSqlManager() {
		return sqlManager;
	}
	
	public static StatsManager getStatsManager() {
		return statsManager;
	}
}
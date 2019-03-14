package de.sinixspielt.statsapi.manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import javax.sql.rowset.FilteredRowSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.sinixspielt.statsapi.StatsAPI;
import de.sinixspielt.statsapi.database.FilteredRowSetImpl;
import de.sinixspielt.statsapi.database.StringFilter;

/*
Class created on 01.03.2019 by SinixSpielt
 * */

public class StatsManager {

	private HashMap<UUID, PlayerStats> playerStats;
	private RankingLoader rankingLoader;

	public StatsManager() {
		this.playerStats = new HashMap();
		StatsAPI.getSqlManager().executeUpdate("CREATE TABLE IF NOT EXISTS `StatsAPI` (UUID VARCHAR(100), Kills INT, Deaths INT, Games INT, Wins INT, Playtime BIGINT, Placedblocks INT, Brokenblocks INT, Openchests INT, UNIQUE KEY (UUID))");
		loadStatsForOnlines();
		this.rankingLoader = new RankingLoader();
		this.rankingLoader.setActive(true);
	}

	public RankingLoader getRankingLoader() {
		return this.rankingLoader;
	}

	public void loadStatsForOnlines() {
		for (Player all : Bukkit.getOnlinePlayers()) {
			addPlayerToCache(all.getUniqueId());
		}
	}

	public void addPlayerToCache(UUID uuid) {
		if (this.playerStats.containsKey(uuid)) {
			return;
		}
		this.playerStats.put(uuid, new PlayerStats(uuid));
	}

	public void removePlayerFromCache(UUID uuid) {
		if (!this.playerStats.containsKey(uuid)) {
			return;
		}
		PlayerStats stats = (PlayerStats) this.playerStats.get(uuid);
		stats.saveDataAsync();
		stats.removeFromUpdater();
		this.playerStats.remove(uuid);
	}

	public PlayerStats getPlayerStats(UUID uuid) {
		if (!this.playerStats.containsKey(uuid)) {
			return null;
		}
		return (PlayerStats) this.playerStats.get(uuid);
	}

	public HashMap<UUID, PlayerStats> getStatsCache() {
		return this.playerStats;
	}

	public class RankingLoader extends Thread {
		private boolean active;

		public RankingLoader() {
		}

		public void setActive(boolean active) {
			this.active = active;
			if (active) {
				start();
			}
		}

		public void run() {
			while (this.active) {
				try {
					ResultSet rs = StatsAPI.getSqlManager().executeQuery("SELECT * FROM `StatsAPI` ORDER BY `Kills` DESC");
					FilteredRowSet frs = new FilteredRowSetImpl();
					frs.populate(rs);
					frs.setFilter(new StringFilter("UUID", StatsManager.this.getOnlineUUIDList()));
					while (frs.next()) {
						UUID uuid = UUID.fromString(frs.getString("UUID"));
						PlayerStats stats = StatsAPI.getStatsManager().getPlayerStats(uuid);
						stats.setRank(frs.getRow());
					}
					frs.close();

					Thread.sleep(30000L);
				} catch (SQLException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private String[] getOnlineUUIDList() {
		String[] uuid = new String[Bukkit.getOnlinePlayers().size()];
		for (Player players : Bukkit.getOnlinePlayers()) {
			for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++) {
				uuid[i] = players.getUniqueId().toString();
			}
		}
		return uuid;
	}
	
	public boolean hasStats(UUID uuid) {
		return getPlayerStats(uuid) != null;
	}
}

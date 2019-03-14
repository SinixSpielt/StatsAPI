package de.sinixspielt.statsapi.manager;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import de.sinixspielt.statsapi.StatsAPI;
import de.sinixspielt.statsapi.database.DatabaseUpdate;

/*
Class created on 01.03.2019 by SinixSpielt
 * */

public class PlayerStats extends DatabaseUpdate {
	
	private UUID uuid;
	private int kills;
	private int deaths;
	private int rank;
	private int games;
	private int wins;
	private int openchests;
	private int placedblocks;
	private int brokenblocks;
	private long timeCreated;
	private long playtime;
	private boolean onlineMode;

	public PlayerStats(UUID uuid) {
		this(uuid, true, true);
	}

	public PlayerStats(UUID uuid, boolean addUpdater, boolean onlineMode) {
		this.uuid = uuid;
		this.kills = 0;
		this.wins = 0;
		this.games = 0;
		this.deaths = 0;
		this.rank = 0;
		this.playtime = 0L;
		this.openchests = 0;
		this.placedblocks = 0;
		this.brokenblocks = 0;
		this.onlineMode = onlineMode;
		this.timeCreated = System.currentTimeMillis();
		loadDataAsync();
		if (addUpdater) {
			setForceUpdate(true);
			addToUpdater();
		}
	}
	
	public int getBrokenblocks() {
		return this.brokenblocks;
	}
	
	public int getOpenchests() {
		return this.openchests;
	}
	
	public int getPlacedblocks() {
		return this.placedblocks;
	}
	
	public int getWins() {
		return this.wins;
	}
	
	public int getGames() {
		return this.games;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public int getKills() {
		return this.kills;
	}

	public int getDeaths() {
		return this.deaths;
	}

	public int getRank() {
		return this.rank;
	}
	
	public int getLoses() {
		return this.games-this.wins;
	}

	public long getPlaytime() {
		return this.playtime;
	}

	public long getTimeCreated() {
		return this.timeCreated;
	}

	public boolean isOnlineMode() {
		return this.onlineMode;
	}

	public double getKD() {
		if (getKills() <= 0) {
			return 0.0D;
		}
		if (getDeaths() <= 0) {
			return getKills();
		}
		BigDecimal dec = new BigDecimal(getKills() / getDeaths());
		dec = dec.setScale(2, 4);
		return dec.doubleValue();
	}
	
	public void addKills(int kills) {
		this.kills = this.kills+kills;
	}
	
	public void addDeaths(int deaths) {
		this.deaths = this.deaths+deaths;
	}
	
	public void addGames(int games) {
		this.games = this.games+games;
	}
	
	public void addWins(int win) {
		this.wins = this.wins+win;
	}
	
	public void addOpenChests(int chests) {
		this.openchests = this.wins+chests;
	}
	
	public void addBrokenblocks(int blocks) {
		this.brokenblocks = this.wins+blocks;
	}
	
	public void addPlacedblocks(int blocks) {
		this.placedblocks = this.placedblocks+blocks;
	}
	
	public void setOpenChests(int chests) {
		this.openchests = chests;
		setUpdate(true);
	}
	
	public void setBrokenblocks(int brokenblocks) {
		this.brokenblocks = brokenblocks;
		setUpdate(true);
	}
	
	public void setPlacedblocks(int placedblocks) {
		this.placedblocks = placedblocks;
		setUpdate(true);
	}
	
	public void setGames(int games) {
		this.games = games;
		setUpdate(true);
	}
	
	public void setWins(int wins) {
		this.wins = wins;
		setUpdate(true);
	}

	public void setKills(int kills) {
		this.kills = kills;
		setUpdate(true);
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
		setUpdate(true);
	}

	public void setRank(int rank) {
		this.rank = rank;
		setUpdate(true);
	}

	private void calculateNewPlaytime() {
		long currentTime = System.currentTimeMillis();
		long timeDiff = currentTime - this.timeCreated;
		this.playtime += timeDiff;
		this.timeCreated = currentTime;
	}
	
	public void saveData() {
		if (this.onlineMode) {
			calculateNewPlaytime();
		}
		try {
			PreparedStatement stCheck = StatsAPI.getSqlManager().getConnection().prepareStatement("SELECT * FROM `StatsAPI` WHERE `UUID` = ?");
			stCheck.setString(1, getUUID().toString());
			ResultSet rsCheck = StatsAPI.getSqlManager().executeQuery(stCheck);
			if (!rsCheck.next()) {
				PreparedStatement st = StatsAPI.getSqlManager().getConnection().prepareStatement("INSERT INTO `StatsAPI` (UUID, Kills, Deaths, Games, Wins, Playtime, Placedblocks, Brokenblocks, Openchests) VALUES (?, 0, 0, 0, 0 ,0 ,0 ,0 ,0)");
				st.setString(1, getUUID().toString());
				StatsAPI.getSqlManager().executeUpdate(st);
			} else {
				PreparedStatement st = StatsAPI.getSqlManager().getConnection().prepareStatement("UPDATE `StatsAPI` SET `Kills` = ?, `Deaths` = ?, `Games` = ?, `Wins` = ?, `Playtime` = ?, `Placedblocks` = ?, `Brokenblocks` = ?, `Openchests` = ? WHERE `UUID` = ?");
				st.setInt(1, getKills());
				st.setInt(2, getDeaths());
				st.setInt(3, getGames());
				st.setInt(4, getWins());
				st.setLong(5, getPlaytime());
				st.setInt(6, getPlacedblocks());
				st.setInt(7, getBrokenblocks());
				st.setInt(8, getOpenchests());
				st.setString(9, getUUID().toString());
				StatsAPI.getSqlManager().executeUpdate(st);
			}
			rsCheck.close();
			stCheck.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveDataAsync() {
		StatsAPI.getSqlManager().getAsyncHandler().getExecutor().execute(new Runnable() {
			public void run() {
				PlayerStats.this.saveData();
			}
		});
	}

	public void loadData() {
		try {
			PreparedStatement st = (PreparedStatement) StatsAPI.getSqlManager().getConnection().prepareStatement("SELECT * FROM `StatsAPI` WHERE `UUID` = ?");
			st.setString(1, getUUID().toString());
			ResultSet rs = StatsAPI.getSqlManager().executeQuery(st);
			if (!rs.next()) {
				saveData();
			} else {
				this.kills = rs.getInt("Kills");
				this.deaths = rs.getInt("Deaths");
				this.wins = rs.getInt("Wins");
				this.games = rs.getInt("Games");
				this.playtime = rs.getLong("Playtime");
			}
			
			ResultSet rs2 = StatsAPI.getSqlManager().executeQuery("SELECT * FROM `StatsAPI` ORDER BY `Kills` DESC");

			int count = 0;
			boolean done = false;
			
			while(rs2.next() && !(done)){
				count++;
				String nameUUID = rs2.getString("UUID");
				UUID uuid = UUID.fromString(nameUUID);
				if(uuid.equals(getUUID())){
					done = true;
					this.rank = count;
				}
			}
			rs2.close();
			rs.close();
			st.close();
			setReady(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadDataAsync() {
		StatsAPI.getSqlManager().getAsyncHandler().getExecutor().execute(new Runnable() {
			public void run() {
				PlayerStats.this.loadData();
			}
		});
	}
}

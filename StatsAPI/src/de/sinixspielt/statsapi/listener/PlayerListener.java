package de.sinixspielt.statsapi.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.sinixspielt.statsapi.StatsAPI;

/*
Class created on 01.03.2019 by SinixSpielt
 * */

public class PlayerListener implements Listener{
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		StatsAPI.getStatsManager().addPlayerToCache(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		StatsAPI.getStatsManager().removePlayerFromCache(e.getPlayer().getUniqueId());
	}
}
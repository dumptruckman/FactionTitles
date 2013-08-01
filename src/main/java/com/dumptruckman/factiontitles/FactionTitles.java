package com.dumptruckman.factiontitles;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.factions.event.FactionsEventMembershipChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class FactionTitles extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Factions") == null) {
            getLogger().severe("Factions not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent event) {
        refreshScoreboards();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        refreshScoreboards();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinFaction(FactionsEventMembershipChange event) {
        final Player player = Bukkit.getPlayerExact(event.getUPlayer().getName());
        if (player == null) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                refreshScoreboards();
            }
        }, 1L);
    }

    public void refreshScoreboards() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            p.setScoreboard(scoreboard);
            UPlayer uPlayer = UPlayer.get(p);
            Faction faction = uPlayer.getFaction();
            String factionName = faction.getName();

            Team team = scoreboard.getTeam(factionName);
            if (team == null) {
                team = scoreboard.registerNewTeam(factionName);
                team.setDisplayName(factionName.substring(0, factionName.length() <= 16 ? factionName.length() : 16));
            }
            team.setPrefix("");
            if (faction.equals(FactionColls.get().getForWorld(p.getWorld().getName()).getNone())) {
                team.setSuffix("");
            } else {
                team.setSuffix(" - " + ChatColor.GOLD + factionName.substring(0, factionName.length() <= 11 ? factionName.length() : 11));
            }
            team.addPlayer(p);
        }
    }
}

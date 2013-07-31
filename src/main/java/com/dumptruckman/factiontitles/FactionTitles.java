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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Set;

public class FactionTitles extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Factions") == null) {
            getLogger().severe("Factions not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                getServer().broadcastMessage("TEST PLUGIN IN USE!");
            }
        }, 600L, 600L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent event) {
        refreshScoreboards();
        System.out.println("Faction title set on player join.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        refreshScoreboards();
        System.out.println("Faction title set on player join.");
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
                System.out.println("Faction title set on faction change.");
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
            team.setSuffix(" " + factionName.substring(0, factionName.length() <= 15 ? factionName.length() : 15));
            team.addPlayer(p);

            Objective obj = scoreboard.getObjective(factionName);
            if (obj == null) {
                obj = scoreboard.registerNewObjective("online in faction", "dummy");
            }
            obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
            obj.setDisplayName("Online");

            Score score = obj.getScore(p);
            int size = faction.getUPlayersWhereOnline(true).size();
            score.setScore(size);
        }
    }
}

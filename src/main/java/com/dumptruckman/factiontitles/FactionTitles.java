package com.dumptruckman.factiontitles;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.factions.event.FactionsEventMembershipChange;
import com.massivecraft.factions.event.FactionsEventNameChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Set up new scoreboard for player that just joined to make sure nothing weird is going on with their scoreboards

        Scoreboard scoreboard = getServer().getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);

        // Set the faction names for all online players as their team suffix

        String factionName = UPlayer.get(player).getFaction().getName();
        for (Player p : Bukkit.getOnlinePlayers()) {
            getTeam(scoreboard, p).addPlayer(p);
            getTeam(p.getScoreboard(), player.getWorld(), factionName).addPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinFaction(FactionsEventMembershipChange event) {
        final Player player = Bukkit.getPlayerExact(event.getUPlayer().getName());
        if (player == null) {
            throw new IllegalStateException("Player should not be null!");
        }

        // After the player has joined the faction, refresh every ones team names for that player.

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                Scoreboard scoreboard = player.getScoreboard();
                String factionName = UPlayer.get(player).getFaction().getName();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    getTeam(scoreboard, p).addPlayer(p);
                    getTeam(p.getScoreboard(), player.getWorld(), factionName).addPlayer(player);
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void factionNameChange(FactionsEventNameChange event) {
        Faction faction = event.getFaction();
        List<Player> fPlayers = faction.getOnlinePlayers();
        String newName = event.getNewName();

        // Update all players on the server with the new team name

        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = p.getScoreboard();
            for (Player fPlayer : fPlayers) {
                getTeam(scoreboard, fPlayer.getWorld(), newName).addPlayer(fPlayer);
            }
        }
    }

    /**
     * Gets the team with appropriate suffix for the specified scoreboard and player's faction, registering it if necessary.
     */
    private Team getTeam(Scoreboard scoreboard, Player player) {
        return getTeam(scoreboard, player.getWorld(), UPlayer.get(player).getFaction().getName());
    }

    /**
     * Gets the team with appropriate suffix for the specified scoreboard and faction name, registering it if necessary.
     */
    private Team getTeam(Scoreboard scoreboard, World world, String factionName) {
        Team team = scoreboard.getTeam(factionName);
        if (team == null) {
            team = scoreboard.registerNewTeam(factionName);
            team.setDisplayName(factionName.substring(0, factionName.length() <= 16 ? factionName.length() : 16));
        }
        team.setPrefix("");
        if (factionName.equals(FactionColls.get().getForWorld(world.getName()).getNone().getName())) {
            team.setSuffix("");
        } else {
            team.setSuffix(" - " + ChatColor.GOLD + factionName.substring(0, factionName.length() <= 11 ? factionName.length() : 11));
        }
        return team;
    }
}

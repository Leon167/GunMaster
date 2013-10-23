package com.lekohd.gunmaster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class playerListener
  implements Listener
{
  private GunMaster gm;

  public playerListener(GunMaster _gm)
  {
    this.gm = _gm;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    Action action = event.getAction();
    if (event.getItem() == null)
      return;
    if (((action == Action.RIGHT_CLICK_BLOCK) || (action == Action.LEFT_CLICK_BLOCK)) && 
      (event.getItem().getTypeId() == this.gm.boundsSetTool))
    {
      if (action == Action.LEFT_CLICK_BLOCK)
      {
        this.gm.P1 = event.getClickedBlock().getLocation();
        event.getPlayer().sendMessage(ChatColor.BLUE + "[GMA] Point 1 is set to (X=" + 
          this.gm.P1.getBlockX() + " Y=" + this.gm.P1.getBlockY() + " Z=" + this.gm.P1.getBlockZ() + ")");
      }
      else {
        this.gm.P2 = event.getClickedBlock().getLocation();
        event.getPlayer().sendMessage(ChatColor.BLUE + "[GMA] Point 2 is set to (X=" + 
          this.gm.P2.getBlockX() + " Y=" + this.gm.P2.getBlockY() + " Z=" + this.gm.P2.getBlockZ() + ")");
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    Iterator localIterator2;
    for (Iterator localIterator1 = this.gm.arenas.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Arena a = (Arena)localIterator1.next();
      localIterator2 = a.teams.iterator(); Team t = (Team)localIterator2.next();
      for (PlayerInfo p : t.player)
        p.setNameColor(p.team.color);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    if ((e instanceof EntityDamageByEntityEvent)) {
      EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)e;
      if (((event.getDamager() instanceof Player)) && ((event.getEntity() instanceof Player))) {
        Player attacker = (Player)event.getDamager();
        Player player = (Player)event.getEntity();
        Arena arena = this.gm.findArenaByPlayer(player);
        if (arena == null)
          return;
        if (arena.allowTeamkill)
          return;
        PlayerInfo iPlayer = arena.findPlayer(player);
        PlayerInfo iAttacker = arena.findPlayer(attacker);
        if (!iAttacker.team.equals(iPlayer.team))
          return;
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    if (e.getEntityType() == EntityType.PLAYER) {
      Player player = e.getEntity();
      Player killer = player.getKiller();
      Arena arena = this.gm.findArenaByPlayer(player);
      if (arena == null) {
        return;
      }
      if ((arena.findPlayer(player) == null) || (arena.findPlayer(killer) == null)) {
        return;
      }
      e.getDrops().clear();
      PlayerInfo killerI = arena.findPlayer(killer);
      PlayerInfo playerI = arena.findPlayer(player);
      if (!killerI.team.equals(playerI.team))
        killerI.addPoint();
      else
        killerI.delPoint();
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event)
  {
    Arena arena = this.gm.findArenaByPlayer(event.getPlayer());
    if (arena == null)
      return;
    PlayerInfo p = arena.findPlayer(event.getPlayer());
    if (p == null)
      return;
    event.setRespawnLocation((Location)arena.mainArea.Spawn.get(p.team.TeamId));
    p.level.handleSpawnWithLevel(p.player);
    Iterator localIterator2;
    for (Iterator localIterator1 = this.gm.arenas.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Arena a = (Arena)localIterator1.next();
      localIterator2 = a.teams.iterator(); Team t = (Team)localIterator2.next();
      for (PlayerInfo pi : t.player)
        pi.setNameColor(pi.team.color);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    Arena arena = this.gm.findArenaByPlayer(event.getPlayer());
    if (arena == null)
      return;
    Player p = event.getPlayer();
    arena.leaveArena(p);
  }
}
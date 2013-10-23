package com.lekohd.gunmaster;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class Team
  implements Cloneable
{
  public ArrayList<PlayerInfo> player = new ArrayList();
  private GunMaster gm;
  private String teamName = "";
  Team a;
  public int TeamId = 0;
  public ChatColor color;
  public Arena arena;

  public Object clone()
  {
    try
    {
      this.a = ((Team)super.clone());
      this.a.gm = this.gm;
      this.a.TeamId = this.TeamId;
      this.a.color = this.color;
      this.a.arena = this.arena;
      this.a.player = ((ArrayList)this.player.clone());
    } catch (CloneNotSupportedException e) {
      return null;
    }

    return this.a.clone();
  }

  public Team(GunMaster _gm, String name, int id, ChatColor _color, Arena _arena)
  {
    this.gm = _gm;
    this.teamName = name;
    this.TeamId = id;
    this.color = _color;
    this.arena = _arena;
  }

  public String getTeamName(int i)
  {
    if (i == 0)
      return "Blue";
    if (i == 1)
      return "Red";
    return "";
  }

  public void teleportAll(Location loc)
  {
    for (PlayerInfo p : this.player)
      p.teleport(loc);
  }

  public boolean addPlayer(Player newPlayer)
  {
    boolean result = this.player.add(new PlayerInfo(this.gm, newPlayer, this, this.arena));
    if (this.gm.getConfig().getBoolean("general.useColoredNames"))
    {
      findPlayer(newPlayer).setNameColor(this.color);
    }
    return result;
  }

  public boolean deletePlayer(Player delPlayer)
  {
    if ((this.gm.getConfig().getBoolean("general.useColoredNames")) && 
      (findPlayer(delPlayer) != null))
      findPlayer(delPlayer).resetNameColor();
    return this.player.remove(findPlayer(delPlayer));
  }

  public PlayerInfo findPlayer(Player p)
  {
    for (PlayerInfo pi : this.player)
      if (pi.player.equals(p))
        return pi;
    return null;
  }

  public boolean containsPlayer(Player _player)
  {
    PlayerInfo pi = findPlayer(_player);
    return pi != null;
  }

  public void sendChatToAll(String str)
  {
    for (PlayerInfo p : this.player)
      p.player.sendMessage(str);
  }

  public String getTeamName()
  {
    return this.teamName;
  }

  public boolean checkAllReady()
  {
    for (PlayerInfo p : this.player)
      if (!p.isReady)
        return false;
    return true;
  }

  public void handleWin(PlayerInfo p)
  {
    this.arena.setAllNotReady();
    for (PlayerInfo pi : this.player)
    {
      pi.activArea = this.arena.winArea;
      pi.player.getInventory().clear();
      pi.player.teleport((Location)this.arena.winArea.Spawn.get(this.TeamId));
      pi.resetNameColor();
    }
    this.player.clear();
    this.arena.Phase = 0;
    sendChatToAll(ChatColor.BLUE + "[GMA] Player " + p.player.getDisplayName() + 
      ChatColor.BLUE + " finished the Game!");
    sendChatToAll(ChatColor.GOLD + "[GMA] Your Team wins!");
  }

  public void handleLoose(PlayerInfo p)
  {
    for (PlayerInfo pi : this.player)
    {
      pi.activArea = this.arena.looseArea;
      pi.player.getInventory().clear();
      pi.player.teleport((Location)this.arena.looseArea.Spawn.get(this.TeamId));

      pi.resetNameColor();
    }
    this.player.clear();
    this.arena.Phase = 0;
    sendChatToAll(ChatColor.BLUE + "[GMA] Player " + p.player.getDisplayName() + 
      ChatColor.BLUE + " finished the Game!");
    sendChatToAll(ChatColor.RED + "[GMA] Your Team loose!");
  }
}
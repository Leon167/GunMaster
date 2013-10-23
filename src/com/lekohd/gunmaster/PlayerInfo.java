package com.lekohd.gunmaster;


import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInfo
{
  public Player player;
  public boolean isReady = false;
  public Team team;
  public int points = 0;
  public Level level;
  public Area activArea;
  public PlayerInventory inventory = null;
  public Arena arena;

  public PlayerInfo(GunMaster _gm, Player p, Team t, Arena _arena)
  {
    this.player = p;
    this.team = t;
    this.arena = _arena;
  }

  public void saveInventory()
  {
    this.inventory = this.player.getInventory();
  }

  public void restoreInventory()
  {
    if (this.arena.gm.getConfig().getBoolean(this.arena.arenaName + ".saveInventory", true))
      this.player.getInventory().setContents(this.inventory.getContents());
  }

  public void teleport(Location loc)
  {
    this.player.teleport(loc);
    setNameColor(this.team.color);
  }

  public void levelUp()
  {
    this.player.sendMessage(ChatColor.GOLD + "You leveled up!");
    this.level = this.level.nextLevel;
    if (this.level != null) {
      this.level.handleSpawnWithLevel(this.player);
    }
    else {
      this.team.handleWin(this);
      for (Team t : this.arena.teams)
        if (!t.equals(this.team))
          t.handleLoose(this);
    }
  }

  public void addPoint()
  {
    this.points += 1;
    this.player.sendMessage(ChatColor.GOLD + "You now have " + this.points + " Points!");
    if (this.level.nextLevel == null)
    {
      this.team.handleWin(this);
      for (Team t : this.arena.teams)
        if (!t.equals(this.team))
          t.handleLoose(this);
    }
    else if (this.level.nextLevel.neededPoints == this.points)
    {
      levelUp();
    } else {
      this.player.sendMessage(ChatColor.GOLD + "" + (this.level.nextLevel.neededPoints - this.points) + " until the next Level");
    }
  }

  public void delPoint() {
    this.points -= 1;
    this.player.sendMessage(ChatColor.RED + "You loose 1 Point because of Teamkill!");
  }

  public void resetNameColor()
  {
    EntityPlayer changingName = ((CraftPlayer)this.player).getHandle();

    for (Player playerinworld : Bukkit.getOnlinePlayers())
      if (playerinworld != this.player)
        ((CraftPlayer)playerinworld).getHandle().playerConnection.sendPacket(
          new Packet20NamedEntitySpawn(changingName));
  }

  public void setNameColor(ChatColor color)
  {
    EntityPlayer changingName = ((CraftPlayer)this.player).getHandle();
    String playerName = this.player.getName();
    changingName.displayName = (color + this.player.getName());
    for (Player playerinworld : Bukkit.getOnlinePlayers()) {
      if (playerinworld != this.player) {
        ((CraftPlayer)playerinworld).getHandle().playerConnection.sendPacket(
          new Packet20NamedEntitySpawn(changingName));
      }
    }
    changingName.displayName = playerName;
  }
}
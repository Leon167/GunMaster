package com.lekohd.gunmaster;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class ThreadCountdown extends Thread
{
  int countdown = 10;
  public Arena arena = null;

  public void run()
  {
    this.countdown = 10;
    while (this.countdown != 0)
    {
      this.arena.sendChatToAll(ChatColor.BLUE + "[GMA] Match starts in " + ChatColor.RED + 
        this.countdown + ChatColor.BLUE + " Seconds");
      this.countdown -= 1;
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    this.arena.Phase = 2;
    for (int i = 0; i < this.arena.teams.size(); i++)
    {
      ((Team)this.arena.teams.get(i)).teleportAll((Location)this.arena.mainArea.Spawn.get(i));
      for (PlayerInfo pi : ((Team)this.arena.teams.get(i)).player)
      {
        pi.level = this.arena.level;
        pi.points = 0;
        pi.activArea = this.arena.mainArea;
        this.arena.level.handleSpawnWithLevel(pi.player);
        pi.isReady = false;
      }
    }
    this.arena.sendChatToAll(ChatColor.BLUE + "[GMA] " + ChatColor.RED + "Fight!");
  }
}
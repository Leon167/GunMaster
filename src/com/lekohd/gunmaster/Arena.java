package com.lekohd.gunmaster;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Arena
  implements Cloneable
{
  public GunMaster gm;
  public Area mainArea = new Area("Main", this);
  public Area warmUpArea = new Area("Warmup", this);
  public Area winArea = new Area("Win", this);
  public Area looseArea = new Area("Loose", this);

  public Level level = new Level();

  public int Phase = 0;

  ThreadCountdown thread = new ThreadCountdown();

  public ArrayList<Team> teams = new ArrayList();

  public String arenaName = "";

  public boolean isEnabled = true;

  public boolean allowTeamkill = true;
  Arena a;

  public Arena(GunMaster _gm, String name)
  {
    this.arenaName = name;
    this.gm = _gm;
    loadConfig();
  }

  public Object clone()
  {
    try {
      this.a = ((Arena)super.clone());
      this.a.arenaName = this.arenaName;
      this.a.Phase = this.Phase;
      this.a.isEnabled = this.isEnabled;
      this.a.allowTeamkill = this.allowTeamkill;
      this.a.teams = ((ArrayList)this.teams.clone());
    } catch (CloneNotSupportedException e) {
      return null;
    }

    return this.a.clone();
  }

  public void setActiveArea(Area area)
  {
    Iterator localIterator2;
    for (Iterator localIterator1 = this.teams.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Team t = (Team)localIterator1.next();
      localIterator2 = t.player.iterator(); PlayerInfo p = (PlayerInfo)localIterator2.next();
      p.activArea = area;
    }
  }

  public void setAllNotReady()
  {
    Iterator localIterator2;
    for (Iterator localIterator1 = this.teams.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Team t = (Team)localIterator1.next();
      localIterator2 = t.player.iterator(); PlayerInfo p = (PlayerInfo)localIterator2.next();
      p.isReady = false;
    }
  }

  public void saveConfig() {
    FileConfiguration c = this.gm.getConfig();
    String orderString = "";
    Level lvl = this.level;
    while (lvl != null)
    {
      orderString = orderString + lvl.Name;
      c.set(this.arenaName + ".level." + lvl.Name + ".neededPoints", Integer.valueOf(lvl.neededPoints));
      c.set(this.arenaName + ".level." + lvl.Name + ".weaponId", Integer.valueOf(lvl.WeaponId));
      lvl = lvl.nextLevel;
      if (lvl != null)
        orderString = orderString + ",";
    }
    c.set(this.arenaName + ".level.order", orderString);
    c.set(this.arenaName + ".allowTeamkill", Boolean.valueOf(this.allowTeamkill));
    if (!c.contains(this.arenaName + ".saveInventory"))
      c.set(this.arenaName + ".saveInventory", Boolean.valueOf(false));
    for (Team t : this.teams)
    {
      c.set(this.arenaName + ".teams.team" + (t.TeamId + 1) + ".name", t.getTeamName());
      c.set(this.arenaName + ".teams.team" + (t.TeamId + 1) + ".color", Character.valueOf(t.color.getChar()));
    }
    c.set(this.arenaName + ".world", this.mainArea.P1.getWorld().getName());
    this.mainArea.save();
    this.warmUpArea.save();
    this.winArea.save();
    this.looseArea.save();
    this.gm.saveConfig();
  }

  public void loadConfig()
  {
    String levelOrder = this.gm.getConfig().getString(this.arenaName + ".level.order");
    this.allowTeamkill = this.gm.getConfig().getBoolean(this.arenaName + ".allowTeamkill");
    if (levelOrder == null)
    {
      System.out.println("Initial Load config!");
      this.level = new Level();
      this.teams.add(new Team(this.gm, "Red", 0, ChatColor.RED, this));
      this.teams.add(new Team(this.gm, "Blue", 1, ChatColor.BLUE, this));
      return;
    }
    String[] s = levelOrder.split(",");
    this.level = new Level();
    Level lvl = this.level;
    for (String str : s)
    {
      lvl.Name = str;
      lvl.neededPoints = this.gm.getConfig().getInt(this.arenaName + ".level." + str + ".neededPoints");
      lvl.WeaponId = this.gm.getConfig().getInt(this.arenaName + ".level." + str + ".weaponId");
      lvl.nextLevel = new Level();
      lvl = lvl.nextLevel;
    }
    lvl = null;
    this.mainArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Main.Point1"), 1);
    this.mainArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Main.Point2"), 2);
    this.mainArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Main.Spawn1"), 3);
    this.mainArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Main.Spawn2"), 4);

    this.warmUpArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Warmup.Point1"), 1);
    this.warmUpArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Warmup.Point2"), 2);
    this.warmUpArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Warmup.Spawn1"), 3);
    this.warmUpArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Warmup.Spawn2"), 4);

    this.winArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Win.Point1"), 1);
    this.winArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Win.Point2"), 2);
    this.winArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Win.Spawn1"), 3);
    this.winArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Win.Spawn2"), 4);

    this.looseArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Loose.Point1"), 1);
    this.looseArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Loose.Point2"), 2);
    this.looseArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Loose.Spawn1"), 3);
    this.looseArea.setPointbySettingString(this.gm.getConfig().getString(this.arenaName + ".area.Loose.Spawn2"), 4);

    this.teams.add(new Team(this.gm, this.gm.getConfig().getString(this.arenaName + ".teams.team1.name"), 0, 
      ChatColor.getByChar(this.gm.getConfig().getString(this.arenaName + ".teams.team1.color")), this));
    this.teams.add(new Team(this.gm, this.gm.getConfig().getString(this.arenaName + ".teams.team2.name"), 1, 
      ChatColor.getByChar(this.gm.getConfig().getString(this.arenaName + ".teams.team2.color")), this));
  }

  public void startGame()
  {
    sendChatToAll(ChatColor.BLUE + "[GMA] All Players are ready!");
    if (this.Phase == 0)
    {
      this.Phase = 1;
      this.thread.arena = this;
      Bukkit.getScheduler().scheduleAsyncDelayedTask(this.gm, this.thread);
    }
  }

  public void kickAll()
  {
    for (Team team : this.teams)
      team.player.clear();
  }

  public String getPlayerReadyCount()
  {
    int r = 0; int t = 0;
    Iterator localIterator2;
    for (Iterator localIterator1 = this.teams.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Team team = (Team)localIterator1.next();

      localIterator2 = team.player.iterator(); PlayerInfo pi = (PlayerInfo)localIterator2.next();

      t++;
      if (pi.isReady) {
        r++;
      }
    }
    return r + "/" + t;
  }

  public boolean sendChatToAll(String str)
  {
    for (Team t : this.teams)
      t.sendChatToAll(str);
    return true;
  }

  public boolean checkAllReady()
  {
    for (Team t : this.teams)
      if (!t.checkAllReady())
        return false;
    return true;
  }

  public Area getAreaByName(String str)
  {
    if (str.equalsIgnoreCase("main"))
      return this.mainArea;
    if (str.equalsIgnoreCase("warmup"))
      return this.warmUpArea;
    if (str.equalsIgnoreCase("win"))
      return this.winArea;
    if (str.equalsIgnoreCase("loose"))
      return this.looseArea;
    return null;
  }

  public void stopGame()
  {
    this.Phase = 0;
    setActiveArea(this.winArea);
    ((Team)this.teams.get(0)).teleportAll((Location)this.winArea.Spawn.get(0));
    ((Team)this.teams.get(1)).teleportAll((Location)this.winArea.Spawn.get(1));
    sendChatToAll(ChatColor.GOLD + "The game was stopped because of no Players");
    ((Team)this.teams.get(0)).player.clear();
    ((Team)this.teams.get(1)).player.clear();
  }

  public void leaveArena(Player player)
  {
    for (int i = 0; i < this.teams.size(); i++)
      if (((Team)this.teams.get(i)).containsPlayer(player))
      {
        PlayerInfo p = ((Team)this.teams.get(i)).findPlayer(player);
        p.teleport((Location)this.winArea.Spawn.get(p.team.TeamId));
        ((Team)this.teams.get(i)).deletePlayer(player);
        player.sendMessage(ChatColor.BLUE + "[GMA] You have leave the Arena.");
      }
    int playerCount = 0;
    for (Team team : this.teams) {
      playerCount += team.player.size();
    }
    if (playerCount <= 1)
      stopGame();
  }

  public void joinArena(Player player, int team)
  {
    if (((Team)this.teams.get(team)).containsPlayer(player))
    {
      player.sendMessage(ChatColor.BLUE + "[GMA] You are already in Team " + 
        ((Team)this.teams.get(team)).getTeamName() + "!");
    }
    else
    {
      ((Team)this.teams.get(team)).addPlayer(player);
      PlayerInfo pi = findPlayer(player);
      pi.activArea = this.warmUpArea;
      player.sendMessage(ChatColor.BLUE + "[GMA] You are now in Team " + 
        ((Team)this.teams.get(team)).getTeamName() + "!");
    }
  }

  public PlayerInfo findPlayer(Player p)
  {
    for (Team team : this.teams)
    {
      PlayerInfo _p = team.findPlayer(p);
      if (_p != null)
        return _p;
    }
    return null;
  }

  private boolean checkAllSetArea(Area a, Player player)
  {
    boolean tmp2 = a.allSet();
    if (!tmp2)
      player.sendMessage(ChatColor.RED + "[GMA] You need to set boundings and spawns for the " + 
        a.Name + "-Area!");
    return tmp2;
  }

  public boolean checkAllSet(Player player)
  {
    return (checkAllSetArea(this.mainArea, player)) && 
      (checkAllSetArea(this.warmUpArea, player)) && 
      (checkAllSetArea(this.winArea, player)) && 
      (checkAllSetArea(this.looseArea, player));
  }
}
package com.lekohd.gunmaster;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_6_R3.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class GunMaster extends JavaPlugin
{
  Arena arn;
  Area ara;
  public int boundsSetTool = Item.STICK.id;
  public Location P1;
  public Location P2;
  public List<Arena> arenas;
  private ThreadColorNames colorNamesThread;
  Team team_;

  public void onEnable()
  {
    saveDefaultConfig();
    getServer().getPluginManager().registerEvents(new playerListener(this), this);

    String[] arl = getConfig().getString("general.enabledArenas").split(",");

    this.arenas = new ArrayList();

    for (String s : arl)
    {
      System.out.println("Load Arena " + s);
      this.arenas.add(new Arena(this, s));
    }

    System.out.println("GunMaster wurde geladen/aktiviert.");
    this.colorNamesThread = new ThreadColorNames();
    this.colorNamesThread.gm = this;
    Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, this.colorNamesThread, 0L, 40L);
  }

  public boolean globalChat(String str)
  {
    for (Player p : getServer().getOnlinePlayers())
      p.sendMessage(str);
    return true;
  }

  public void onDisable() {
    System.out.println("GunMaster wurde deaktiviert");
  }

  public void joinTeam(Player player, int team, Arena a)
  {
    a.joinArena(player, team);
    PlayerInfo p = a.findPlayer(player);
    String str = ChatColor.BLUE + "[GMA] Player " + ChatColor.RED + p.player.getDisplayName() + 
      ChatColor.BLUE + " has joined the " + ChatColor.RED + a.arenaName + ChatColor.BLUE + 
      "-Arena in Team " + ChatColor.GOLD + p.team.getTeamName();
    if (getConfig().getBoolean(a.arenaName + ".useGlobalAnnouncements"))
      globalChat(str);
    else
      a.sendChatToAll(str);
    p.teleport((Location)a.warmUpArea.Spawn.get(p.team.TeamId));
    p.saveInventory();
    player.getInventory().clear();
  }

  public boolean sendInfo(Player p, String msg)
  {
    p.sendMessage(ChatColor.GOLD + msg);
    return true;
  }

  public Arena getArenaByString(String str)
  {
    for (Arena a : this.arenas)
      if (a.arenaName.equalsIgnoreCase(str))
        return a;
    return null;
  }

  public Arena findArenaByPlayer(Player p)
  {
    Iterator localIterator1 = this.arenas.iterator();
    Arena a = (Arena)localIterator1.next();
    Iterator localIterator2 = a.teams.iterator();
    while (localIterator1.hasNext()) {
      localIterator2.hasNext();
      Team t = (Team)localIterator2.next();
      if (t.containsPlayer(p)) {
        return a;
      }
    }
    return null;
  }

  public boolean listCommands(Player player)
  {
    return listCommands(player, false);
  }

  public boolean listCommands(Player player, boolean doError)
  {
    if (doError)
      sendInfo(player, ChatColor.RED + "[GMA] Unknown command or syntax, available commands:");
    if (player.hasPermission("GunMaster.Admin.help"))
    {
      sendInfo(player, "--=== Player Commands [1/2] ===--");
    }
    else sendInfo(player, "--=== Player Commands ===--");

    sendInfo(player, ChatColor.BLUE + "/gg help " + ChatColor.DARK_GRAY + "- this Help");
    sendInfo(player, ChatColor.BLUE + "/gg leave " + ChatColor.DARK_GRAY + "- Leave the game");
    sendInfo(player, ChatColor.BLUE + "/gg ready " + ChatColor.DARK_GRAY + "- mark you as ready for the fight");
    sendInfo(player, ChatColor.BLUE + "/gg join <arena-name> <team-name> " + ChatColor.DARK_GRAY + "- Join the game in team <team-name>");
    sendInfo(player, ChatColor.BLUE + "/gg list " + ChatColor.DARK_GRAY + "- List all enabled Arenas");
    if (player.hasPermission("GunMaster.Admin.help"))
    {
      sendInfo(player, ChatColor.GREEN + "Type in '/gg help2' to see page 2");
    }

    return true;
  }

  public boolean listAdminCommands(Player player, boolean doOneError)
  {
    if (doOneError) {
      sendInfo(player, ChatColor.RED + "[GMA] Unknown command or syntax, available commands:");
    }
    sendInfo(player, "--=== Admin Commands [2/2] ===--");
    sendInfo(player, ChatColor.BLUE + "/gg save " + ChatColor.DARK_GRAY + "- save the Arena-borders and settings");
    sendInfo(player, ChatColor.BLUE + "/gg add <arena-name> " + ChatColor.DARK_GRAY + "- Add a new Arena");
    sendInfo(player, ChatColor.BLUE + "/gg quit <arena-name> " + ChatColor.DARK_GRAY + "- stop the game manually");
    sendInfo(player, ChatColor.BLUE + "/gg set <arena-name> <area-name> [spawn <teamname>] " + ChatColor.DARK_GRAY + "- Set borders and spawn");
    sendInfo(player, ChatColor.BLUE + "/gg kick <player-name> [reason] " + ChatColor.DARK_GRAY + "- Kick the given Player from the Arena");
    sendInfo(player, ChatColor.BLUE + "/gg copy level <src-arna-name> <dest-arena-name> " + ChatColor.DARK_GRAY + "- Copy the Level order from src-arena to dest-arena");

    sendInfo(player, ChatColor.BLUE + "/gg copy all <src-arena-name> <dest-arena-name> " + ChatColor.DARK_GRAY + "- Copy all Points and Level from src-arena to dest-arena");

    sendInfo(player, ChatColor.BLUE + "/gg enable <arena-name> " + ChatColor.DARK_GRAY + "- Enable Arena <arena-name>");
    sendInfo(player, ChatColor.BLUE + "/gg disable <arena-name> " + ChatColor.DARK_GRAY + "- Disable Arena <arena-name>");
    sendInfo(player, ChatColor.GREEN + "Type in '/gg help1' to see page 1");
    return true;
  }

  public boolean dontHavePermissions(Player player)
  {
    sendInfo(player, ChatColor.RED + "You dont have Permissions to do that!");
    return true;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    if (((sender instanceof Player)) && 
      (cmd.getName().equalsIgnoreCase("gg"))) {
      Player player = (Player)sender;
      if (args.length < 1)
      {
        return listCommands(player);
      }

      if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("help1")))
      {
        return listCommands(player);
      }

      if ((args[0].equalsIgnoreCase("help2")) || (args[0].equalsIgnoreCase("admin")))
      {
        if (!player.hasPermission("GunMaster.Admin.help"))
          return dontHavePermissions(player);
        return listAdminCommands(player, false);
      }
      if (args[0].equalsIgnoreCase("join"))
      {
        if (!player.hasPermission("GunMaster.Player.join"))
          return dontHavePermissions(player);
        if (args.length != 3) {
          return listCommands(player, true);
        }
        Arena arena = getArenaByString(args[1]);
        if (arena == null) {
          return sendInfo(player, ChatColor.RED + "Unknown Arena \"" + args[1] + "\"");
        }
        if ((arena.Phase != 0) && (arena.Phase != 3))
        {
          player.sendMessage(ChatColor.RED + "[GMA] You can not join during a battle!");
          return true;
        }
        int teamId;
        if (args[2].equalsIgnoreCase(((Team)arena.teams.get(0)).getTeamName().toLowerCase())) {
          teamId = 0;
        }
        else
        {
          //int teamId;
          if (args[2].equalsIgnoreCase(((Team)arena.teams.get(1)).getTeamName().toLowerCase())) {
            teamId = 1;
          }
          else {
            player.sendMessage(ChatColor.RED + "[GMA] Unknown Team-name!");
            return true;
          }
        }
        //int teamId;
        joinTeam(player, teamId, arena);
      }
      else if (args[0].equalsIgnoreCase("list"))
      {
        sendInfo(player, "All enabled Arenas:");
        for (Arena a : this.arenas)
          if (a.isEnabled)
            sendInfo(player, "- " + a.arenaName);
        sendInfo(player, "---------------");
      }
      else {
        if (args[0].equalsIgnoreCase("disable"))
        {
          if (!player.hasPermission("GunMaster.Admin.disable"))
            return dontHavePermissions(player);
          if (args.length != 2)
            return listCommands(player, true);
          Arena ar = getArenaByString(args[1]);
          if (ar == null)
            return sendInfo(player, ChatColor.RED + "Unknows Arena \"" + args[1] + "\"");
          ar.isEnabled = false;
          return sendInfo(player, "Arena " + ar.arenaName + " was disabed!");
        }

        if (args[0].equalsIgnoreCase("enable"))
        {
          if (!player.hasPermission("GunMaster.Admin.enable"))
            return dontHavePermissions(player);
          if (args.length != 2)
            return listCommands(player, true);
          Arena ar = null;
          for (Arena a : this.arenas)
            if (a.arenaName.equalsIgnoreCase(args[1]))
              ar = a;
          if (ar != null)
          {
            if (ar.isEnabled) {
              return sendInfo(player, "The Arena " + ar.arenaName + " is already enabled!");
            }
            ar.isEnabled = true;
          }
          else
          {
            if (!getConfig().contains(args[1]))
              return sendInfo(player, ChatColor.RED + "Cant find the Arena in the config." + 
                " Maybe you need to save the config first.");
            ar = new Arena(this, args[1]);
            ar.loadConfig();
            this.arenas.add(ar);
          }
          return sendInfo(player, "Arena " + ar.arenaName + " was enabled!");
        }
        String display;
        if (args[0].equalsIgnoreCase("kick"))
        {
          if (!player.hasPermission("GunMaster.Admin.kick"))
            return dontHavePermissions(player);
          if (args.length < 2)
            return sendInfo(player, ChatColor.RED + "You need to specify a Player!");
          PlayerInfo pi = null;
          String str = args[1];
          Arena ar = null;
          Iterator localIterator3;
          for (Iterator localIterator2 = this.arenas.iterator(); localIterator2.hasNext(); 
            localIterator3.hasNext())
          {
            Arena a = (Arena)localIterator2.next();
            localIterator3 = a.teams.iterator(); Team t = (Team)localIterator3.next();
            for (PlayerInfo p : t.player)
              if (p.player.getName().equalsIgnoreCase(str))
              {
                pi = p;
                ar = a;
              }
          }
          String reason = "";
          if (args.length > 2)
          {
            for (int i = 2; i < args.length; i++)
              reason = reason + args[i] + " ";
          }
          display = player.getDisplayName() + " kicked " + pi.player.getDisplayName();
          if (!reason.equalsIgnoreCase(""))
            display = display + "reason: " + reason;
          if (pi == null)
            return sendInfo(player, ChatColor.RED + "No such Player to kick!");
          pi.activArea = ar.looseArea;
          pi.player.getInventory().clear();
          pi.player.teleport((Location)ar.looseArea.Spawn.get(pi.team.TeamId));
          pi.team.deletePlayer(pi.player);
          pi.resetNameColor();
          if (getConfig().getBoolean(ar.arenaName + ".useGlobalAnnouncements", false)) {
            return globalChat(display);
          }
          return ar.sendChatToAll(display);
        }

        if (args[0].equalsIgnoreCase("copy"))
        {
          if (!player.hasPermission("GunMaster.Admin.copy"))
            return dontHavePermissions(player);
          if (args.length < 4)
            return listCommands(player, true);
          Arena src = getArenaByString(args[2]);
          Arena dest = getArenaByString(args[3]);
          if (src == null)
            return sendInfo(player, ChatColor.RED + "Can't find Arena with name \"" + args[2] + "\"");
          if (dest == null)
            return sendInfo(player, ChatColor.RED + "Can't find Arena with name \"" + args[3] + "\"");
          if (args[1].equalsIgnoreCase("level"))
          {
            dest.level = src.level;
            sendInfo(player, "Copied Level from " + src.arenaName + " to " + dest.arenaName);
          }
          else if (args[1].equalsIgnoreCase("all"))
          {
            Object destName = dest.arenaName;
            dest = (Arena)src.clone();
            dest.arenaName = ((String)destName);
            sendInfo(player, "Copied Arena " + src.arenaName + " to " + dest.arenaName);
          } else {
            return listCommands(player, true);
          }
          return true;
        }

        if (args[0].equalsIgnoreCase("leave"))
        {
          if (!player.hasPermission("GunMaster.Player.leave"))
            return dontHavePermissions(player);
          Arena arena = findArenaByPlayer(player);
          if (arena != null)
            arena.leaveArena(player);
          else {
            player.sendMessage(ChatColor.RED + "[GMA] You are not in an Arena!");
          }
        }
        else if (args[0].equalsIgnoreCase("quit"))
        {
          if (!player.hasPermission("GunMaster.Admin.quit"))
            return dontHavePermissions(player);
          if (args.length != 2)
            return true;
          Arena arena = getArenaByString(args[1]);
          if (arena == null)
            return true;
          arena.sendChatToAll(ChatColor.YELLOW + "[GMA] The game was manually stopped by " + 
            ChatColor.RED + player.getDisplayName());
          sendInfo(player, "You stopped the game in Arena " + arena.arenaName);
          arena.Phase = 0;
          arena.setActiveArea(arena.winArea);
          ((Team)arena.teams.get(0)).teleportAll((Location)arena.winArea.Spawn.get(0));
          ((Team)arena.teams.get(1)).teleportAll((Location)arena.winArea.Spawn.get(1));
          arena.kickAll();
        }
        else {
          if (args[0].equalsIgnoreCase("save"))
          {
            if (!player.hasPermission("GunMaster.Admin.save"))
              return dontHavePermissions(player);
            player.sendMessage(ChatColor.GOLD + "[GMA] Save all Settings...");
            String enabledArenas = "";
            for (Object destName = this.arenas.iterator(); ((Iterator)destName).hasNext(); ) { Arena ar = (Arena)((Iterator)destName).next();

              ar.saveConfig();
              if (ar.isEnabled)
                enabledArenas = enabledArenas + "," + ar.arenaName;
            }
            enabledArenas = enabledArenas.substring(1);
            getConfig().set("general.enabledArenas", enabledArenas);
            saveConfig();
            player.sendMessage(ChatColor.GOLD + "[GMA] All saved!");
            return true;
          }

          if (args[0].equalsIgnoreCase("add"))
          {
            if (!player.hasPermission("GunMaster.Admin.add"))
              return dontHavePermissions(player);
            if (args.length != 2)
              return sendInfo(player, ChatColor.RED + "[GMA] You need to specify a name!");
            String arenaName = args[1];
            boolean b = false;
            for (Arena ar : this.arenas)
              b = (b) || (ar.arenaName.equalsIgnoreCase(arenaName));
            if (b)
              return sendInfo(player, ChatColor.RED + 
                "[GMA] You can not have two Arenas with the same name!");
            this.arenas.add(new Arena(this, arenaName));
            sendInfo(player, ChatColor.GOLD + "[GMA] You added the Arena " + arenaName);
            return true;
          }

          if (args[0].equalsIgnoreCase("ready"))
          {
            if (!player.hasPermission("GunMaster.Player.ready"))
              return dontHavePermissions(player);
            Arena arena = findArenaByPlayer(player);
            if (arena == null)
            {
              player.sendMessage(ChatColor.RED + "[GMA] You need to join a Team first!");
              return true;
            }
            if (!arena.checkAllSet(player))
              return true;
            PlayerInfo i = arena.findPlayer(player);
            i.isReady = true;
            arena.sendChatToAll(ChatColor.BLUE + "[GMA] " + ChatColor.RED + player.getDisplayName() + 
              ChatColor.BLUE + " is ready for the " + ChatColor.RED + "Fight!");

            if (arena.checkAllReady()) {
              arena.startGame();
            }

          }
          else if (args[0].equalsIgnoreCase("set"))
          {
            if (!player.hasPermission("GunMaster.Admin.set"))
              return dontHavePermissions(player);
            if (args.length < 2)
              return listCommands(player, true);
            Arena arena = getArenaByString(args[1]);
            if (arena == null)
            {
              player.sendMessage(ChatColor.RED + "[GMA] Cant find the Arena!");
              return true;
            }
            if (args.length < 3)
              return true;
            Area ar = arena.getAreaByName(args[2]);
            if (ar != null)
            {
              if ((args.length >= 4) && (args[3].equalsIgnoreCase("spawn")))
              {
                if (args.length < 5)
                  return listCommands(player, true);
                if (!ar.isInBorders(player.getLocation()))
                {
                  player.sendMessage(ChatColor.RED + "[GMA] You can not set a Spawn outside of the Area!");
                  return true;
                }
                if (arena.teams.size() == 0)
                  return sendInfo(player, ChatColor.RED + "No teams! pleace save the Arena first!");
                if (args[4].equalsIgnoreCase(((Team)arena.teams.get(0)).getTeamName().toLowerCase()))
                {
                  ar.Spawn.set(0, player.getLocation());
                  getConfig().set(arena.arenaName + ".area." + ar.Name + ".Spawn1", ar.getPointSettingString(3));
                  player.sendMessage(ChatColor.BLUE + "[GMA] Set " + ((Team)arena.teams.get(0)).getTeamName() + 
                    " Spawn for the " + ar.Name + 
                    "-Area to (" + ar.getPointString(3) + ")");
                }
                else if (args[4].equalsIgnoreCase(((Team)arena.teams.get(1)).getTeamName().toLowerCase()))
                {
                  ar.Spawn.set(1, player.getLocation());
                  getConfig().set(arena.arenaName + ".area." + ar.Name + ".Spawn2", ar.getPointSettingString(4));
                  player.sendMessage(ChatColor.BLUE + "[GMA] Set " + ((Team)arena.teams.get(1)).getTeamName() + 
                    " Spawn for the " + ar.Name + 
                    "-Area to (" + ar.getPointString(4) + ")");
                } else {
                  return listCommands(player, true);
                }return true;
              }

              ar.P1 = this.P1;
              ar.P2 = this.P2;
              getConfig().set(arena.arenaName + ".area." + ar.Name + ".Point1", ar.getPointSettingString(1));
              getConfig().set(arena.arenaName + ".area." + ar.Name + ".Point2", ar.getPointSettingString(2));
              getConfig().set(arena.arenaName + ".world", this.P1.getWorld().getName());
              player.sendMessage(ChatColor.BLUE + "[GMA] Set " + ar.Name + 
                "-Area to P1(" + ar.getPointString(1) + ") P2(" + ar.getPointString(2) + ")");
              return true;
            }

            return listCommands(player, true);
          }
        }
      }
    }
    return true;
  }
}
package com.lekohd.gunmaster;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Area
  implements Cloneable
{
  GunMaster gm;
  Area a;
  public Location P1 = null;
  public Location P2 = null;

  public ArrayList<Location> Spawn = new ArrayList();
  private Arena arena;
  public String Name = "";

  public Area(String name, Location p1, Location p2, Location spawn1, Location spawn2, Arena a)
  {
    this.P1 = p1;
    this.P2 = p2;
    this.Spawn.add(spawn1);
    this.Spawn.add(spawn2);
    this.arena = a;
  }

  public Object clone()
  {
    try {
      this.a = ((Area)super.clone());
      this.a.P1 = this.P1.clone();
      this.a.P2 = this.P2.clone();
      this.a.Spawn = ((ArrayList)this.Spawn.clone());
      this.a.arena = this.arena;
      this.a.Name = this.Name;
    } catch (CloneNotSupportedException e) {
      return null;
    }

    return this.a.clone();
  }

  private Double getMax(Double a, Double b)
  {
    if (a.doubleValue() > b.doubleValue()) return a;
    return b;
  }

  private Double getMin(Double a, Double b)
  {
    if (a.doubleValue() < b.doubleValue()) return a;
    return b;
  }

  public boolean isInBorders(Location l)
  {
    if ((this.P1 == null) || (this.P2 == null))
      return false;
    Double minX = getMin(Double.valueOf(this.P1.getX() + 0.5D), Double.valueOf(this.P2.getX() + 0.5D));
    Double minY = getMin(Double.valueOf(this.P1.getY() - 0.5D), Double.valueOf(this.P2.getY() - 0.5D));
    Double minZ = getMin(Double.valueOf(this.P1.getZ() + 0.5D), Double.valueOf(this.P2.getZ() + 0.5D));
    Double maxX = getMax(Double.valueOf(this.P1.getX() + 0.5D), Double.valueOf(this.P2.getX() + 0.5D));
    Double maxY = getMax(Double.valueOf(this.P1.getY() - 0.5D), Double.valueOf(this.P2.getY() - 0.5D));
    Double maxZ = getMax(Double.valueOf(this.P1.getZ() + 0.5D), Double.valueOf(this.P2.getZ() + 0.5D));
    boolean b = false;
    b = (b) || (l.getX() < minX.doubleValue());
    b = (b) || (l.getY() < minY.doubleValue());
    b = (b) || (l.getZ() < minZ.doubleValue());
    b = (b) || (l.getX() > maxX.doubleValue());
    b = (b) || (l.getY() > maxY.doubleValue());
    b = (b) || (l.getZ() > maxZ.doubleValue());
    return !b;
  }

  public boolean holdPlayerInBorders(Player p, Location to)
  {
    boolean b = !isInBorders(to);
    if (b)
      p.teleport(p.getLocation());
    return b;
  }

  public boolean allSet()
  {
    boolean tmp = (this.P1 != null) && (this.P2 != null);
    for (Location loc : this.Spawn)
      tmp = (tmp) && (loc != null);
    return tmp;
  }

  public Area(String name, Arena a)
  {
    this.Name = name;
    this.arena = a;
    this.Spawn.add(new Location((World)Bukkit.getServer().getWorlds().get(0), 0.0D, 0.0D, 0.0D));
    this.Spawn.add(new Location((World)Bukkit.getServer().getWorlds().get(0), 0.0D, 0.0D, 0.0D));
  }

  public String getPointName(Location p)
  {
    if (p == this.P1)
      return "Point 1";
    if (p == this.P2)
      return "Point 2";
    if (p == this.Spawn.get(0))
      return "Spawn 1";
    if (p == this.Spawn.get(1))
      return "Spawn 2";
    return "???";
  }

  public String getSettingStringByPoint(Location loc)
  {
    if (loc == null)
      return "0,0,0,0,0";
    return loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + 
      loc.getPitch();
  }

  public void save()
  {
    FileConfiguration c = this.arena.gm.getConfig();
    c.set(this.arena.arenaName + ".area." + this.Name + ".Point1", getSettingStringByPoint(this.P1));
    c.set(this.arena.arenaName + ".area." + this.Name + ".Point2", getSettingStringByPoint(this.P2));
    c.set(this.arena.arenaName + ".area." + this.Name + ".Spawn1", getSettingStringByPoint((Location)this.Spawn.get(0)));
    c.set(this.arena.arenaName + ".area." + this.Name + ".Spawn2", getSettingStringByPoint((Location)this.Spawn.get(1)));
  }

  public void setPointbySettingString(String str, int i)
  {
    Location l = null;
    if (i == 1) l = this.P1;
    else if (i == 2) l = this.P2;
    else if (i == 3) l = (Location)this.Spawn.get(0);
    else if (i == 4) l = (Location)this.Spawn.get(1);
    String[] list = str.split(",");
    if (l == null)
      l = new Location(Bukkit.getWorld(this.arena.gm.getConfig().getString("arena.world")), 0.0D, 0.0D, 0.0D);
    l.setX(Double.parseDouble(list[0]));
    l.setY(Double.parseDouble(list[1]));
    l.setZ(Double.parseDouble(list[2]));
    l.setYaw(Float.parseFloat(list[3]));
    l.setPitch(Float.parseFloat(list[4]));
    if (i == 1) this.P1 = l;
    else if (i == 2) this.P2 = l;
    else if (i == 3) this.Spawn.set(0, l);
    else if (i == 4) this.Spawn.set(1, l);
  }

  public String getPointSettingString(int i)
  {
    Location l = null;
    if (i == 1) l = this.P1;
    else if (i == 2) l = this.P2;
    else if (i == 3) l = (Location)this.Spawn.get(0);
    else if (i == 4) l = (Location)this.Spawn.get(1);
    return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + l.getYaw() + "," + l.getPitch();
  }

  public String getPointString(int i)
  {
    if (i == 1)
      return "X=" + this.P1.getBlockX() + " Y=" + this.P1.getBlockY() + " Z=" + this.P1.getBlockZ();
    if (i == 2)
      return "X=" + this.P2.getBlockX() + " Y=" + this.P2.getBlockY() + " Z=" + this.P2.getBlockZ();
    if (i == 3) {
      return "X=" + ((Location)this.Spawn.get(0)).getBlockX() + " Y=" + ((Location)this.Spawn.get(0)).getBlockY() + " Z=" + ((Location)this.Spawn.get(0)).getBlockZ();
    }
    return "X=" + ((Location)this.Spawn.get(1)).getBlockX() + " Y=" + ((Location)this.Spawn.get(1)).getBlockY() + " Z=" + ((Location)this.Spawn.get(1)).getBlockZ();
  }
}
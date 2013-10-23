package com.lekohd.gunmaster;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Level
  implements Cloneable
{
  public String Name = "";

  public int neededPoints = -1;

  public int WeaponId = -1;

  public Level nextLevel = null;

  public Level prevLevel = null;
  Level a;

  public void handleSpawnWithLevel(Player p)
  {
    PlayerInventory pi = p.getInventory();
    pi.clear();
    pi.addItem(new ItemStack[] { new ItemStack(this.WeaponId) });
  }

  public Level(int needed, int weapon)
  {
    this.neededPoints = needed;
    this.WeaponId = weapon;
  }

  public Level()
  {
  }

  public Object clone()
  {
    try
    {
      this.a = ((Level)super.clone());
      this.a.Name = this.Name;
      this.a.neededPoints = this.neededPoints;
      this.a.WeaponId = this.WeaponId;
      this.a.nextLevel = ((Level)this.nextLevel.clone());
      this.a.prevLevel = ((Level)this.prevLevel.clone());
    } catch (CloneNotSupportedException e) {
      return null;
    }

    return this.a.clone();
  }
}
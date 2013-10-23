package com.lekohd.gunmaster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThreadColorNames extends Thread
{
  public GunMaster gm = null;

  public void run()
  {
    Iterator localIterator2;
    for (Iterator localIterator1 = this.gm.arenas.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Arena a = (Arena)localIterator1.next();
      localIterator2 = a.teams.iterator();
      Team t = (Team)localIterator2.next();
      for (PlayerInfo pi : t.player)
        pi.setNameColor(pi.team.color);
    }
  }
}
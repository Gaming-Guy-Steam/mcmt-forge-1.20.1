package mekanism.client.render.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class ScrollIncrementer {
   private final boolean discrete;
   private long lastScrollTime = -1L;
   private double scrollDelta;

   public ScrollIncrementer(boolean discrete) {
      this.discrete = discrete;
   }

   private long getTime() {
      ClientLevel level = Minecraft.m_91087_().f_91073_;
      return level == null ? -1L : level.m_46467_();
   }

   public int scroll(double delta) {
      long time = this.getTime();
      if (time - this.lastScrollTime > 20L) {
         this.scrollDelta = 0.0;
      }

      this.lastScrollTime = time;
      this.scrollDelta += delta;
      int shift = (int)this.scrollDelta;
      this.scrollDelta %= 1.0;
      if (this.discrete) {
         shift = Mth.m_14045_(shift, -1, 1);
      }

      return shift;
   }
}

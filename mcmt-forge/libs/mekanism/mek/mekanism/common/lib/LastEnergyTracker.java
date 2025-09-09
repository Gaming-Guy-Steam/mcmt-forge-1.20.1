package mekanism.common.lib;

import mekanism.api.math.FloatingLong;

public class LastEnergyTracker {
   private FloatingLong lastEnergyReceived = FloatingLong.ZERO;
   private FloatingLong currentEnergyReceived = FloatingLong.ZERO;
   private long currentGameTime;

   public void received(long gameTime, FloatingLong amount) {
      if (this.currentGameTime == gameTime) {
         this.currentEnergyReceived = this.currentEnergyReceived.plusEqual(amount);
      } else {
         this.lastEnergyReceived = this.currentEnergyReceived;
         this.currentGameTime = gameTime;
         this.currentEnergyReceived = amount.copy();
      }
   }

   public FloatingLong getLastEnergyReceived() {
      return this.lastEnergyReceived;
   }

   public void setLastEnergyReceived(FloatingLong lastEnergyReceived) {
      this.lastEnergyReceived = lastEnergyReceived;
   }
}

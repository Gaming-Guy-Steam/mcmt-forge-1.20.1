package mekanism.common.integration.lookingat;

import mekanism.api.math.FloatingLong;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

public class EnergyElement extends LookingAtElement {
   protected final FloatingLong energy;
   protected final FloatingLong maxEnergy;

   public EnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
      super(-16777216, 16777215);
      this.energy = energy;
      this.maxEnergy = maxEnergy;
   }

   @Override
   public int getScaledLevel(int level) {
      return this.energy.equals(FloatingLong.MAX_VALUE) ? level : (int)(level * this.energy.divideToLevel(this.maxEnergy));
   }

   public FloatingLong getEnergy() {
      return this.energy;
   }

   public FloatingLong getMaxEnergy() {
      return this.maxEnergy;
   }

   @Override
   public TextureAtlasSprite getIcon() {
      return MekanismRenderer.energyIcon;
   }

   @Override
   public Component getText() {
      return EnergyDisplay.of(this.energy, this.maxEnergy).getTextComponent();
   }
}

package mekanism.common.integration.lookingat;

import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidElement extends LookingAtElement {
   @NotNull
   protected final FluidStack stored;
   protected final int capacity;

   public FluidElement(@NotNull FluidStack stored, int capacity) {
      super(-16777216, 16777215);
      this.stored = stored;
      this.capacity = capacity;
   }

   @Override
   public int getScaledLevel(int level) {
      return this.capacity != 0 && this.stored.getAmount() != Integer.MAX_VALUE
         ? MathUtils.clampToInt((double)level * this.stored.getAmount() / this.capacity)
         : level;
   }

   @NotNull
   public FluidStack getStored() {
      return this.stored;
   }

   public int getCapacity() {
      return this.capacity;
   }

   @Override
   public TextureAtlasSprite getIcon() {
      return this.stored.isEmpty() ? null : MekanismRenderer.getFluidTexture(this.stored, MekanismRenderer.FluidTextureType.STILL);
   }

   @Override
   public Component getText() {
      int amount = this.stored.getAmount();
      return amount == Integer.MAX_VALUE
         ? MekanismLang.GENERIC_STORED.translate(new Object[]{this.stored, MekanismLang.INFINITE})
         : MekanismLang.GENERIC_STORED_MB.translate(new Object[]{this.stored, TextUtils.format((long)amount)});
   }

   @Override
   protected boolean applyRenderColor(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, this.stored);
      return true;
   }
}

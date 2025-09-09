package mekanism.common.integration.lookingat;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ChemicalElement extends LookingAtElement {
   @NotNull
   protected final ChemicalStack<?> stored;
   protected final long capacity;

   public ChemicalElement(@NotNull ChemicalStack<?> stored, long capacity) {
      super(-16777216, 16777215);
      this.stored = stored;
      this.capacity = capacity;
   }

   @Override
   public int getScaledLevel(int level) {
      return this.capacity != 0L && this.stored.getAmount() != Long.MAX_VALUE
         ? MathUtils.clampToInt((double)level * this.stored.getAmount() / this.capacity)
         : level;
   }

   public ChemicalType getChemicalType() {
      return ChemicalType.getTypeFor(this.stored);
   }

   @NotNull
   public ChemicalStack<?> getStored() {
      return this.stored;
   }

   public long getCapacity() {
      return this.capacity;
   }

   @Override
   public TextureAtlasSprite getIcon() {
      return this.stored.isEmpty() ? null : MekanismRenderer.getChemicalTexture(this.stored.getType());
   }

   @Override
   public Component getText() {
      long amount = this.stored.getAmount();
      return amount == Long.MAX_VALUE
         ? MekanismLang.GENERIC_STORED.translate(new Object[]{this.stored.getType(), MekanismLang.INFINITE})
         : MekanismLang.GENERIC_STORED_MB.translate(new Object[]{this.stored.getType(), TextUtils.format(amount)});
   }

   @Override
   protected boolean applyRenderColor(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, this.stored.getType());
      return true;
   }
}

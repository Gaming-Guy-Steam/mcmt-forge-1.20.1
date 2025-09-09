package mekanism.client.jei;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ChemicalStackRenderer<STACK extends ChemicalStack<?>> implements IIngredientRenderer<STACK> {
   private static final int TEXTURE_SIZE = 16;
   private static final int MIN_CHEMICAL_HEIGHT = 1;
   private final long capacityMb;
   private final ChemicalStackRenderer.TooltipMode tooltipMode;
   private final int width;
   private final int height;

   public ChemicalStackRenderer() {
      this(1000L, ChemicalStackRenderer.TooltipMode.ITEM_LIST, 16, 16);
   }

   public ChemicalStackRenderer(long capacityMb, int width, int height) {
      this(capacityMb, ChemicalStackRenderer.TooltipMode.SHOW_AMOUNT, width, height);
   }

   private ChemicalStackRenderer(long capacityMb, ChemicalStackRenderer.TooltipMode tooltipMode, int width, int height) {
      Preconditions.checkArgument(capacityMb > 0L, "capacity must be > 0");
      this.capacityMb = capacityMb;
      this.tooltipMode = tooltipMode;
      this.width = width;
      this.height = height;
   }

   public void render(@NotNull GuiGraphics guiGraphics, @NotNull STACK stack) {
      if (!stack.isEmpty()) {
         int desiredHeight = MathUtils.clampToInt((double)this.height * stack.getAmount() / this.capacityMb);
         if (desiredHeight < 1) {
            desiredHeight = 1;
         }

         if (desiredHeight > this.height) {
            desiredHeight = this.height;
         }

         Chemical<?> chemical = stack.getType();
         MekanismRenderer.color(guiGraphics, chemical);
         GuiUtils.drawTiledSprite(
            guiGraphics,
            0,
            0,
            this.height,
            this.width,
            desiredHeight,
            MekanismRenderer.getSprite(chemical.getIcon()),
            16,
            16,
            100,
            GuiUtils.TilingDirection.UP_RIGHT
         );
         MekanismRenderer.resetColor(guiGraphics);
      }
   }

   public List<Component> getTooltip(@NotNull STACK stack, TooltipFlag tooltipFlag) {
      Chemical<?> chemical = stack.getType();
      if (chemical.isEmptyType()) {
         return Collections.emptyList();
      } else {
         List<Component> tooltips = new ArrayList<>();
         tooltips.add(TextComponentUtil.build(chemical));
         if (this.tooltipMode == ChemicalStackRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            tooltips.add(
               MekanismLang.JEI_AMOUNT_WITH_CAPACITY
                  .translateColored(EnumColor.GRAY, new Object[]{TextUtils.format(stack.getAmount()), TextUtils.format(this.capacityMb)})
            );
         } else if (this.tooltipMode == ChemicalStackRenderer.TooltipMode.SHOW_AMOUNT) {
            tooltips.add(MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, new Object[]{TextUtils.format(stack.getAmount())}));
         }

         ChemicalUtil.addChemicalDataToTooltip(tooltips, stack.getType(), tooltipFlag.m_7050_());
         return tooltips;
      }
   }

   public Font getFontRenderer(Minecraft minecraft, @NotNull STACK stack) {
      return minecraft.f_91062_;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   static enum TooltipMode {
      SHOW_AMOUNT,
      SHOW_AMOUNT_AND_CAPACITY,
      ITEM_LIST;
   }
}

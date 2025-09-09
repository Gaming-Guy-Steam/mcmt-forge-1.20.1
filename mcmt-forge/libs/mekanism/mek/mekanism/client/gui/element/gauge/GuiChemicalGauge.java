package mekanism.client.gui.element.gauge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class GuiChemicalGauge<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
   extends GuiTankGauge<CHEMICAL, TANK> {
   protected Component label;

   public GuiChemicalGauge(
      GuiTankGauge.ITankInfoHandler<TANK> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, PacketDropperUse.TankType tankType
   ) {
      super(type, gui, x, y, sizeX, sizeY, handler, tankType);
   }

   public GuiChemicalGauge(
      Supplier<TANK> tankSupplier, Supplier<List<TANK>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, PacketDropperUse.TankType tankType
   ) {
      this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2, tankType);
   }

   public GuiChemicalGauge(
      Supplier<TANK> tankSupplier,
      Supplier<List<TANK>> tanksSupplier,
      GaugeType type,
      IGuiWrapper gui,
      int x,
      int y,
      int sizeX,
      int sizeY,
      PacketDropperUse.TankType tankType
   ) {
      this(new GuiTankGauge.ITankInfoHandler<TANK>() {
         @Nullable
         public TANK getTank() {
            return tankSupplier.get();
         }

         @Override
         public int getTankIndex() {
            TANK tank = (TANK)this.getTank();
            return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
         }
      }, type, gui, x, y, sizeX, sizeY, tankType);
   }

   public GuiChemicalGauge<CHEMICAL, STACK, TANK> setLabel(Component label) {
      this.label = label;
      return this;
   }

   @Override
   public int getScaledLevel() {
      if (this.dummy) {
         return this.f_93619_ - 2;
      } else {
         TANK tank = this.getTank();
         if (tank != null && !tank.isEmpty() && tank.getCapacity() != 0L) {
            double scale = (double)tank.getStored() / tank.getCapacity();
            return MathUtils.clampToInt(Math.round(scale * (this.f_93619_ - 2)));
         } else {
            return 0;
         }
      }
   }

   @Nullable
   @Override
   public TextureAtlasSprite getIcon() {
      if (this.dummy) {
         return MekanismRenderer.getChemicalTexture(this.dummyType);
      } else {
         TANK tank = this.getTank();
         return tank != null && !tank.isEmpty() ? MekanismRenderer.getChemicalTexture(tank.getType()) : null;
      }
   }

   @Override
   public Component getLabel() {
      return this.label;
   }

   @Override
   public List<Component> getTooltipText() {
      if (this.dummy) {
         return Collections.singletonList(TextComponentUtil.build(this.dummyType));
      } else {
         TANK tank = this.getTank();
         if (tank != null && !tank.isEmpty()) {
            List<Component> list = new ArrayList<>();
            long amount = tank.getStored();
            if (amount == Long.MAX_VALUE) {
               list.add(MekanismLang.GENERIC_STORED.translate(new Object[]{tank.getType(), MekanismLang.INFINITE}));
            } else {
               list.add(MekanismLang.GENERIC_STORED_MB.translate(new Object[]{tank.getType(), TextUtils.format(amount)}));
            }

            ChemicalUtil.addChemicalDataToTooltip(list, tank.getType(), Minecraft.m_91087_().f_91066_.f_92125_);
            return list;
         } else {
            return Collections.singletonList(MekanismLang.EMPTY.translate(new Object[0]));
         }
      }
   }

   @Override
   protected void applyRenderColor(GuiGraphics guiGraphics) {
      if (!this.dummy && this.getTank() != null) {
         MekanismRenderer.color(guiGraphics, this.getTank().getStack());
      } else {
         MekanismRenderer.color(guiGraphics, this.dummyType);
      }
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      return this.getTank().isEmpty() ? Optional.empty() : Optional.of(this.getTank().getStack());
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      return new Rect2i(this.m_252754_() + 1, this.m_252907_() + 1, this.f_93618_ - 2, this.f_93619_ - 2);
   }
}

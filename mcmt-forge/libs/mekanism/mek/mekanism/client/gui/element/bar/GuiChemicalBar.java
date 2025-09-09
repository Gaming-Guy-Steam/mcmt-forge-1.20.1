package mekanism.client.gui.element.bar;

import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalBar<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends GuiTankBar<STACK> {
   public GuiChemicalBar(IGuiWrapper gui, GuiTankBar.TankInfoProvider<STACK> infoProvider, int x, int y, int width, int height, boolean horizontal) {
      super(gui, infoProvider, x, y, width, height, horizontal);
   }

   protected boolean isEmpty(STACK stack) {
      return stack.isEmpty();
   }

   @Nullable
   protected PacketDropperUse.TankType getType(STACK stack) {
      CHEMICAL type = this.getHandler().getStack().getType();
      if (type instanceof Gas) {
         return PacketDropperUse.TankType.GAS_TANK;
      } else if (type instanceof InfuseType) {
         return PacketDropperUse.TankType.INFUSION_TANK;
      } else if (type instanceof Pigment) {
         return PacketDropperUse.TankType.PIGMENT_TANK;
      } else {
         return type instanceof Slurry ? PacketDropperUse.TankType.SLURRY_TANK : null;
      }
   }

   protected List<Component> getTooltip(STACK stack) {
      List<Component> tooltips = super.getTooltip(stack);
      ChemicalUtil.addChemicalDataToTooltip(tooltips, stack.getType(), Minecraft.m_91087_().f_91066_.f_92125_);
      return tooltips;
   }

   protected void applyRenderColor(GuiGraphics guiGraphics, STACK stack) {
      MekanismRenderer.color(guiGraphics, stack);
   }

   protected TextureAtlasSprite getIcon(STACK stack) {
      return MekanismRenderer.getChemicalTexture(stack.getType());
   }

   public static <STACK extends ChemicalStack<?>, TANK extends IChemicalTank<?, STACK>> GuiTankBar.TankInfoProvider<STACK> getProvider(
      TANK tank, List<TANK> tanks
   ) {
      return new GuiTankBar.TankInfoProvider<STACK>() {
         @NotNull
         public STACK getStack() {
            return tank.getStack();
         }

         @Override
         public int getTankIndex() {
            return tanks.indexOf(tank);
         }

         @Override
         public Component getTooltip() {
            if (tank.isEmpty()) {
               return MekanismLang.EMPTY.translate(new Object[0]);
            } else {
               return tank.getStored() == Long.MAX_VALUE
                  ? MekanismLang.GENERIC_STORED.translate(new Object[]{tank.getType(), MekanismLang.INFINITE})
                  : MekanismLang.GENERIC_STORED_MB.translate(new Object[]{tank.getType(), TextUtils.format(tank.getStored())});
            }
         }

         @Override
         public double getLevel() {
            return (double)tank.getStored() / tank.getCapacity();
         }
      };
   }
}

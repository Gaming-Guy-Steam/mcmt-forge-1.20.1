package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiMergedChemicalBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalTank extends GuiConfigurableTile<TileEntityChemicalTank, MekanismTileContainer<TileEntityChemicalTank>> {
   public GuiChemicalTank(MekanismTileContainer<TileEntityChemicalTank> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      this.addRenderableWidget(GuiSideHolder.armorHolder(this));
      super.addGuiElements();
      this.addRenderableWidget(new GuiMergedChemicalBar<>(this, this.tile, this.tile.getChemicalTank(), 42, 16, 116, 10, true));
      this.addRenderableWidget(
         new GuiInnerScreen(
            this,
            42,
            37,
            118,
            28,
            () -> {
               List<Component> ret = new ArrayList<>();
               switch (this.tile.getChemicalTank().getCurrent()) {
                  case EMPTY:
                     ret.add(MekanismLang.CHEMICAL.translate(new Object[]{MekanismLang.NONE}));
                     ret.add(
                        MekanismLang.GENERIC_FRACTION
                           .translate(
                              new Object[]{
                                 0,
                                 this.tile.getTier() == ChemicalTankTier.CREATIVE ? MekanismLang.INFINITE : TextUtils.format(this.tile.getTier().getStorage())
                              }
                           )
                     );
                     break;
                  case GAS:
                     this.addStored(ret, this.tile.getChemicalTank().getGasTank(), MekanismLang.GAS);
                     break;
                  case INFUSION:
                     this.addStored(ret, this.tile.getChemicalTank().getInfusionTank(), MekanismLang.INFUSE_TYPE);
                     break;
                  case PIGMENT:
                     this.addStored(ret, this.tile.getChemicalTank().getPigmentTank(), MekanismLang.PIGMENT);
                     break;
                  case SLURRY:
                     this.addStored(ret, this.tile.getChemicalTank().getSlurryTank(), MekanismLang.SLURRY);
                     break;
                  default:
                     throw new IllegalStateException("Unknown current type");
               }

               return ret;
            }
         )
      );
      this.addRenderableWidget(new GuiGasMode(this, 159, 72, true, () -> this.tile.dumping, this.tile.m_58899_(), 0));
   }

   private void addStored(List<Component> ret, IChemicalTank<?, ?> tank, ILangEntry langKey) {
      ret.add(langKey.translate(tank.getStack()));
      if (!tank.isEmpty() && this.tile.getTier() == ChemicalTankTier.CREATIVE) {
         ret.add(MekanismLang.INFINITE.translate(new Object[0]));
      } else {
         ret.add(
            MekanismLang.GENERIC_FRACTION
               .translate(
                  new Object[]{
                     TextUtils.format(tank.getStored()),
                     this.tile.getTier() == ChemicalTankTier.CREATIVE ? MekanismLang.INFINITE : TextUtils.format(tank.getCapacity())
                  }
               )
         );
      }
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}

package mekanism.client.gui.element.bar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.to_server.PacketDropperUse;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiTankBar<STACK> extends GuiBar<GuiTankBar.TankInfoProvider<STACK>> implements IJEIIngredientHelper {
   public GuiTankBar(IGuiWrapper gui, GuiTankBar.TankInfoProvider<STACK> infoProvider, int x, int y, int width, int height, boolean horizontal) {
      super(TextureAtlas.f_118259_, gui, infoProvider, x, y, width, height, horizontal);
   }

   protected abstract boolean isEmpty(STACK stack);

   @Nullable
   protected abstract PacketDropperUse.TankType getType(STACK stack);

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      STACK stored = this.getHandler().getStack();
      if (this.isEmpty(stored)) {
         super.renderToolTip(guiGraphics, mouseX, mouseY);
      } else {
         this.displayTooltips(guiGraphics, mouseX, mouseY, this.getTooltip(stored));
      }
   }

   protected List<Component> getTooltip(STACK stack) {
      List<Component> tooltips = new ArrayList<>();
      Component tooltip = this.getHandler().getTooltip();
      if (tooltip != null) {
         tooltips.add(tooltip);
      }

      return tooltips;
   }

   protected abstract void applyRenderColor(GuiGraphics guiGraphics, STACK stack);

   protected abstract TextureAtlasSprite getIcon(STACK stack);

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
      STACK stored = this.getHandler().getStack();
      if (!this.isEmpty(stored)) {
         int displayInt = (int)(handlerLevel * ((this.horizontal ? this.f_93618_ : this.f_93619_) - 2));
         if (displayInt > 0) {
            this.applyRenderColor(guiGraphics, stored);
            TextureAtlasSprite icon = this.getIcon(stored);
            if (this.horizontal) {
               this.drawTiledSprite(
                  guiGraphics,
                  this.relativeX + 1,
                  this.relativeY + 1,
                  this.f_93619_ - 2,
                  displayInt,
                  this.f_93619_ - 2,
                  icon,
                  GuiUtils.TilingDirection.DOWN_RIGHT
               );
            } else {
               this.drawTiledSprite(
                  guiGraphics,
                  this.relativeX + 1,
                  this.relativeY + 1,
                  this.f_93619_ - 2,
                  this.f_93618_ - 2,
                  displayInt,
                  icon,
                  GuiUtils.TilingDirection.DOWN_RIGHT
               );
            }

            MekanismRenderer.resetColor(guiGraphics);
         }
      }
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      ItemStack stack = this.gui().getCarriedItem();
      if (this.gui() instanceof GuiMekanismTile<?, ?> gui && !stack.m_41619_() && stack.m_41720_() instanceof ItemGaugeDropper) {
         PacketDropperUse.TankType tankType = this.getType(this.getHandler().getStack());
         if (tankType != null) {
            int index = this.getHandler().getTankIndex();
            if (index != -1) {
               PacketDropperUse.DropperAction action;
               if (button == 0) {
                  action = Screen.m_96638_() ? PacketDropperUse.DropperAction.DUMP_TANK : PacketDropperUse.DropperAction.FILL_DROPPER;
               } else {
                  action = PacketDropperUse.DropperAction.DRAIN_DROPPER;
               }

               Mekanism.packetHandler().sendToServer(new PacketDropperUse(gui.getTileEntity().m_58899_(), action, tankType, index));
            }
         }
      }
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      STACK stack = this.getHandler().getStack();
      return this.isEmpty(stack) ? Optional.empty() : Optional.of(stack);
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      return new Rect2i(this.m_252754_() + 1, this.m_252907_() + 1, this.f_93618_ - 2, this.f_93619_ - 2);
   }

   public interface TankInfoProvider<STACK> extends GuiBar.IBarInfoHandler {
      @NotNull
      STACK getStack();

      int getTankIndex();
   }
}

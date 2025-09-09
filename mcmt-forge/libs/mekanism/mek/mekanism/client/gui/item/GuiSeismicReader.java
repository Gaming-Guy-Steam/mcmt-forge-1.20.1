package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiArrowSelection;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;

public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {
   private final List<BlockState> blockList = new ArrayList<>();
   private final Reference2IntMap<Block> frequencies = new Reference2IntOpenHashMap();
   private final int minHeight;
   private MekanismButton upButton;
   private MekanismButton downButton;
   private GuiScrollBar scrollBar;

   public GuiSeismicReader(SeismicReaderContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97726_ = 147;
      this.f_97727_ = 182;
      this.minHeight = inv.f_35978_.m_9236_().m_141937_();
      BlockPos pos = inv.f_35978_.m_20183_();

      for (BlockPos p : BlockPos.m_121940_(new BlockPos(pos.m_123341_(), this.minHeight, pos.m_123343_()), pos)) {
         this.blockList.add(inv.f_35978_.m_9236_().m_8055_(p));
      }
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiInnerScreen(this, 7, 11, 63, 49));
      this.addRenderableWidget(new GuiInnerScreen(this, 74, 11, 51, 159));
      this.scrollBar = this.addRenderableWidget(new GuiScrollBar(this, 126, 25, 131, this.blockList::size, () -> 1));
      this.addRenderableWidget(new GuiArrowSelection(this, 76, 81, () -> {
         int currentLayer = this.scrollBar.getCurrentSelection();
         return currentLayer >= 0 ? this.blockList.get(this.blockList.size() - 1 - currentLayer).m_60734_().m_49954_() : null;
      }));
      this.upButton = this.addRenderableWidget(
         new MekanismImageButton(
            this, 126, 11, 14, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "up.png"), () -> this.scrollBar.adjustScroll(1.0)
         )
      );
      this.downButton = this.addRenderableWidget(
         new MekanismImageButton(
            this, 126, 156, 14, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "down.png"), () -> this.scrollBar.adjustScroll(-1.0)
         )
      );
      this.updateEnabledButtons();
   }

   @Override
   public void m_181908_() {
      super.m_181908_();
      this.updateEnabledButtons();
   }

   private void updateEnabledButtons() {
      int currentLayer = this.scrollBar.getCurrentSelection();
      this.upButton.f_93623_ = currentLayer > 0;
      this.downButton.f_93623_ = currentLayer + 1 < this.blockList.size();
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int currentLayer = this.blockList.size() - this.scrollBar.getCurrentSelection() - 1;
      this.drawTextScaledBound(guiGraphics, TextComponentUtil.build(this.minHeight + currentLayer), 111.0F, 87.0F, this.screenTextColor(), 13.0F);

      for (int i = 0; i < 9; i++) {
         int layer = currentLayer + (i - 4);
         if (0 <= layer && layer < this.blockList.size()) {
            BlockState state = this.blockList.get(layer);
            ItemStack stack = new ItemStack(state.m_60734_());
            GuiSeismicReader.RenderTarget renderTarget;
            if (stack.m_41619_()) {
               Fluid fluid = Fluids.f_76191_;
               if (state.m_60734_() instanceof LiquidBlock liquidBlock) {
                  fluid = liquidBlock.getFluid();
               } else if (state.m_60734_() instanceof IFluidBlock fluidBlock) {
                  fluid = fluidBlock.getFluid();
               } else if (state.m_60734_() instanceof BubbleColumnBlock bubbleColumn) {
                  fluid = bubbleColumn.m_5888_(state).m_76152_();
               }

               if (fluid == Fluids.f_76191_) {
                  continue;
               }

               IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
               renderTarget = (graphics, x, y) -> {
                  MekanismRenderer.color(guiGraphics, properties.getTintColor());
                  TextureAtlasSprite texture = MekanismRenderer.getSprite(properties.getStillTexture());
                  guiGraphics.m_280159_(x, y, 0, 16, 16, texture);
                  MekanismRenderer.resetColor(guiGraphics);
               };
            } else {
               renderTarget = (graphics, x, y) -> this.renderItem(graphics, stack, x, y);
            }

            int renderX = 92;
            int renderY = 146 - 16 * i;
            if (i == 4) {
               renderTarget.render(guiGraphics, renderX, renderY);
            } else {
               PoseStack pose = guiGraphics.m_280168_();
               pose.m_85836_();
               pose.m_252880_(renderX, renderY, 0.0F);
               if (i < 4) {
                  pose.m_252880_(1.7F, 2.5F, 0.0F);
               } else {
                  pose.m_252880_(1.5F, 0.0F, 0.0F);
               }

               pose.m_85841_(0.8F, 0.8F, 0.8F);
               renderTarget.render(guiGraphics, 0, 0);
               pose.m_85849_();
            }
         }
      }

      int frequency = 0;
      if (currentLayer >= 0) {
         Block block = this.blockList.get(currentLayer).m_60734_();
         Component displayName = block.m_49954_();
         this.drawTextScaledBound(guiGraphics, displayName, 10.0F, 16.0F, this.screenTextColor(), 57.0F);
         frequency = this.frequencies.computeIfAbsent(block, b -> (int)this.blockList.stream().filter(blockState -> b == blockState.m_60734_()).count());
      }

      this.drawTextScaledBound(guiGraphics, MekanismLang.ABUNDANCY.translate(new Object[]{frequency}), 10.0F, 26.0F, this.screenTextColor(), 57.0F);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return super.m_6050_(mouseX, mouseY, delta) || this.scrollBar.adjustScroll(delta);
   }

   @FunctionalInterface
   private interface RenderTarget {
      void render(GuiGraphics guiGraphics, int x, int y);
   }
}

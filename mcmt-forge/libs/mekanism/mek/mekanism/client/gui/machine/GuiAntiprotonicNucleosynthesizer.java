package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GuiAntiprotonicNucleosynthesizer
   extends GuiConfigurableTile<TileEntityAntiprotonicNucleosynthesizer, MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> {
   private static final Vec3 from = new Vec3(47.0, 50.0, 0.0);
   private static final Vec3 to = new Vec3(147.0, 50.0, 0.0);
   private static final BoltEffect.BoltRenderInfo boltRenderInfo = new BoltEffect.BoltRenderInfo().color(Color.rgbad(0.45F, 0.45F, 0.5, 1.0));
   private final BoltRenderer bolt = new BoltRenderer();
   private final Supplier<BoltEffect> boltSupplier = () -> new BoltEffect(boltRenderInfo, from, to, 15)
      .count((int)Math.min(Math.ceil(this.tile.getProcessRate() / 8.0), 20.0))
      .size(1.0F)
      .lifespan(1)
      .spawn(BoltEffect.SpawnFunction.CONSECUTIVE)
      .fade(BoltEffect.FadeFunction.NONE);

   public GuiAntiprotonicNucleosynthesizer(MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97727_ += 27;
      this.f_97726_ += 20;
      this.f_97731_ = this.f_97727_ - 93;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiInnerScreen(this, 45, 18, 104, 68).jeiCategory(this.tile));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.gasTank, () -> this.tile.getGasTanks(null), GaugeType.SMALL_MED, this, 5, 18))
         .warning(
            WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT)
         );
      this.addRenderableWidget(new GuiEnergyGauge(this.tile.getEnergyContainer(), GaugeType.SMALL_MED, this, 172, 18))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY));
      this.addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new GuiBar.IBarInfoHandler() {
            @Override
            public Component getTooltip() {
               return MekanismLang.PROGRESS.translate(new Object[]{TextUtils.getPercent(GuiAntiprotonicNucleosynthesizer.this.tile.getScaledProgress())});
            }

            @Override
            public double getLevel() {
               return Math.min(1.0, GuiAntiprotonicNucleosynthesizer.this.tile.getScaledProgress());
            }
         }, 5, 88, 183, Color.ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, (this.f_97726_ - this.getStringWidth(this.f_96539_)) / 2, this.f_97729_, this.titleTextColor());
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      this.drawTextScaledBound(
         guiGraphics,
         MekanismLang.PROCESS_RATE.translate(new Object[]{TextUtils.getPercent(this.tile.getProcessRate())}),
         48.0F,
         76.0F,
         this.screenTextColor(),
         100.0F
      );
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(0.0F, 0.0F, 100.0F);
      BufferSource renderer = guiGraphics.m_280091_();
      this.bolt.update(this, this.boltSupplier.get(), MekanismRenderer.getPartialTick());
      this.bolt.render(MekanismRenderer.getPartialTick(), pose, renderer);
      renderer.m_109912_(MekanismRenderType.MEK_LIGHTNING);
      pose.m_85849_();
   }
}

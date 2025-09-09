package mekanism.client.gui.machine;

import java.util.List;
import mekanism.client.gui.GuiFilterHolder;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.miner.GuiMinerFilerSelect;
import mekanism.client.gui.element.window.filter.miner.GuiMinerItemStackFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerModIDFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerTagFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalMinerConfig extends GuiFilterHolder<MinerFilter<?>, TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {
   private static final ResourceLocation INVERSE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "switch/inverse.png");
   private final int maxHeightLength;
   private GuiTextField radiusField;
   private GuiTextField minField;
   private GuiTextField maxField;

   public GuiDigitalMinerConfig(MekanismTileContainer<TileEntityDigitalMiner> container, Inventory inv, Component title) {
      super(container, inv, title);
      Level level = inv.f_35978_.m_9236_();
      this.maxHeightLength = Math.max(Integer.toString(level.m_141937_()).length(), Integer.toString(level.m_151558_() - 1).length());
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new TranslationButton(this, 56, 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER, () -> this.addWindow(new GuiMinerFilerSelect(this, this.tile)))
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            5,
            5,
            11,
            14,
            this.getButtonLocation("back"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton.BACK_BUTTON, this.tile)),
            this.getOnHover(MekanismLang.BACK)
         )
      );
      this.addRenderableWidget(
         new GuiDigitalSwitch(
            this,
            10,
            115,
            INVERSE,
            this.tile::getInverse,
            MekanismLang.MINER_INVERSE.translate(new Object[0]),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.INVERSE_BUTTON, this.tile)),
            GuiDigitalSwitch.SwitchType.LEFT_ICON
         )
      );
      this.addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 13, 135))
         .setRenderAboveSlots()
         .setRenderHover(true)
         .stored(() -> new ItemStack(this.tile.getInverseReplaceTarget()))
         .click((element, mouseX, mouseY) -> {
            if (Screen.m_96638_()) {
               this.updateInverseReplaceTarget(Items.f_41852_);
               return true;
            } else {
               ItemStack stack = this.getCarriedItem();
               if (!stack.m_41619_() && stack.m_41720_() instanceof BlockItem) {
                  this.updateInverseReplaceTarget(stack.m_41720_());
                  return true;
               } else {
                  return false;
               }
            }
         })
         .setGhostHandler((IJEIGhostTarget.IGhostBlockItemConsumer)ingredient -> {
            this.updateInverseReplaceTarget(((ItemStack)ingredient).m_41720_());
            this.f_96541_.m_91106_().m_120367_(SimpleSoundInstance.m_263171_(SoundEvents.f_12490_, 1.0F));
         });
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            35,
            137,
            14,
            16,
            this.getButtonLocation("exclamation"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.INVERSE_REQUIRES_REPLACEMENT_BUTTON, this.tile)),
            this.getOnHover(
               () -> MekanismLang.MINER_REQUIRE_REPLACE_INVERSE
                  .translate(new Object[]{BooleanStateDisplay.YesNo.of(this.tile.getInverseRequiresReplacement())})
            )
         )
      );
      this.radiusField = this.addRenderableWidget(new GuiTextField(this, 13, 49, 38, 11));
      this.radiusField.setMaxLength(Integer.toString(MekanismConfig.general.minerMaxRadius.get()).length());
      this.radiusField.setInputValidator(InputValidator.DIGIT);
      this.radiusField.configureDigitalBorderInput(() -> this.setText(this.radiusField, PacketGuiInteract.GuiInteraction.SET_RADIUS));
      this.minField = this.addRenderableWidget(new GuiTextField(this, 13, 74, 38, 11));
      this.minField.setMaxLength(this.maxHeightLength);
      this.minField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE);
      this.minField.configureDigitalBorderInput(() -> this.setText(this.minField, PacketGuiInteract.GuiInteraction.SET_MIN_Y));
      this.maxField = this.addRenderableWidget(new GuiTextField(this, 13, 99, 38, 11));
      this.maxField.setMaxLength(this.maxHeightLength);
      this.maxField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE);
      this.maxField.configureDigitalBorderInput(() -> this.setText(this.maxField, PacketGuiInteract.GuiInteraction.SET_MAX_Y));
      this.trackWarning(
         WarningTracker.WarningType.FILTER_HAS_BLACKLISTED_ELEMENT, () -> this.tile.getFilterManager().anyEnabledMatch(MinerFilter::hasBlacklistedElement)
      );
   }

   private void updateInverseReplaceTarget(Item target) {
      Mekanism.packetHandler()
         .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteractionItem.DIGITAL_MINER_INVERSE_REPLACE_ITEM, this.tile, new ItemStack(target)));
   }

   @Override
   protected void addGenericTabs() {
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
      this.renderTitleText(guiGraphics);
      this.drawScaledTextScaledBound(guiGraphics, MekanismLang.FILTERS.translate(new Object[0]), 14.0F, 22.0F, this.screenTextColor(), 36.0F, 0.8F);
      this.drawScaledTextScaledBound(
         guiGraphics, MekanismLang.FILTER_COUNT.translate(new Object[]{this.getFilterManager().count()}), 14.0F, 31.0F, this.screenTextColor(), 36.0F, 0.8F
      );
      this.drawScaledTextScaledBound(
         guiGraphics, MekanismLang.MINER_RADIUS.translate(new Object[]{this.tile.getRadius()}), 14.0F, 40.0F, this.screenTextColor(), 36.0F, 0.8F
      );
      this.drawScaledTextScaledBound(
         guiGraphics, MekanismLang.MIN.translate(new Object[]{this.tile.getMinY()}), 14.0F, 65.0F, this.screenTextColor(), 36.0F, 0.8F
      );
      this.drawScaledTextScaledBound(
         guiGraphics, MekanismLang.MAX.translate(new Object[]{this.tile.getMaxY()}), 14.0F, 90.0F, this.screenTextColor(), 36.0F, 0.8F
      );
   }

   @Override
   public void drawTitleText(GuiGraphics guiGraphics, Component text, float y) {
      int leftShift = 11;
      int xSize = this.getXSize() - leftShift;
      int maxLength = xSize - 12;
      float textWidth = this.getStringWidth(text);
      float scale = Math.min(1.0F, maxLength / textWidth);
      this.drawScaledCenteredText(guiGraphics, text, leftShift + xSize / 2.0F, y, this.titleTextColor(), scale);
   }

   @Override
   protected void onClick(IFilter<?> filter, int index) {
      if (filter instanceof IItemStackFilter) {
         this.addWindow(GuiMinerItemStackFilter.edit(this, this.tile, (MinerItemStackFilter)filter));
      } else if (filter instanceof ITagFilter) {
         this.addWindow(GuiMinerTagFilter.edit(this, this.tile, (MinerTagFilter)filter));
      } else if (filter instanceof IModIDFilter) {
         this.addWindow(GuiMinerModIDFilter.edit(this, this.tile, (MinerModIDFilter)filter));
      }
   }

   @Override
   protected FilterButton addFilterButton(FilterButton button) {
      return super.addFilterButton(button)
         .warning(
            WarningTracker.WarningType.FILTER_HAS_BLACKLISTED_ELEMENT,
            filter -> filter instanceof MinerFilter<?> minerFilter && filter.isEnabled() && minerFilter.hasBlacklistedElement()
         );
   }

   private void setText(GuiTextField field, PacketGuiInteract.GuiInteraction interaction) {
      if (!field.getText().isEmpty()) {
         try {
            Mekanism.packetHandler().sendToServer(new PacketGuiInteract(interaction, this.tile, Integer.parseInt(field.getText())));
         } catch (NumberFormatException var4) {
         }

         field.setText("");
      }
   }

   @Override
   protected List<ItemStack> getTagStacks(String tagName) {
      return TagCache.getBlockTagStacks(tagName).stacks();
   }

   @Override
   protected List<ItemStack> getModIDStacks(String tagName) {
      return TagCache.getBlockModIDStacks(tagName).stacks();
   }
}

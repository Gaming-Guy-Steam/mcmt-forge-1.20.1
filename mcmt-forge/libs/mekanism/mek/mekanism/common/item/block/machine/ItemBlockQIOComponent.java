package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockQIOComponent extends ItemBlockTooltip<BlockTile<?, ?>> implements IColoredItem {
   public ItemBlockQIOComponent(BlockTile<?, ?> block) {
      super(block);
   }

   @Override
   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.QIO, tooltip);
   }

   public void m_6883_(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
      super.m_6883_(stack, level, entity, slotId, isSelected);
      if (!level.f_46443_ && level.m_46467_() % 100L == 0L) {
         EnumColor frequencyColor = this.getFrequency(stack) instanceof QIOFrequency frequency ? frequency.getColor() : null;
         EnumColor color = this.getColor(stack);
         if (color != frequencyColor) {
            this.setColor(stack, frequencyColor);
         }
      }
   }

   private Frequency getFrequency(ItemStack stack) {
      if (ItemDataUtils.hasData(stack, "componentFrequency", 10)) {
         CompoundTag frequencyComponent = ItemDataUtils.getCompound(stack, "componentFrequency");
         if (frequencyComponent.m_128425_(FrequencyType.QIO.getName(), 10)) {
            CompoundTag frequencyCompound = frequencyComponent.m_128469_(FrequencyType.QIO.getName());
            Frequency.FrequencyIdentity identity = Frequency.FrequencyIdentity.load(FrequencyType.QIO, frequencyCompound);
            if (identity != null && frequencyCompound.m_128403_("owner")) {
               return FrequencyType.QIO.getManager(identity, frequencyCompound.m_128342_("owner")).getFrequency(identity.key());
            }
         }
      }

      return null;
   }

   public static class ItemBlockQIOInventoryComponent extends ItemBlockQIOComponent implements IItemSustainedInventory {
      public ItemBlockQIOInventoryComponent(BlockTile<?, ?> block) {
         super(block);
      }
   }
}

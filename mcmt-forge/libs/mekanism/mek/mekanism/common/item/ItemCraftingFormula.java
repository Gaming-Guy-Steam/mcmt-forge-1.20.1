package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCraftingFormula extends Item {
   public ItemCraftingFormula(Properties properties) {
      super(properties);
   }

   public void m_7373_(@NotNull ItemStack itemStack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      NonNullList<ItemStack> inv = this.getInventory(itemStack);
      if (inv != null) {
         List<ItemStack> stacks = new ArrayList<>();

         for (ItemStack stack : inv) {
            if (!stack.m_41619_()) {
               boolean found = false;

               for (ItemStack iterStack : stacks) {
                  if (InventoryUtils.areItemsStackable(stack, iterStack)) {
                     iterStack.m_41769_(stack.m_41613_());
                     found = true;
                  }
               }

               if (!found) {
                  stacks.add(stack);
               }
            }
         }

         tooltip.add(MekanismLang.INGREDIENTS.translateColored(EnumColor.GRAY, new Object[0]));

         for (ItemStack stackx : stacks) {
            tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, new Object[]{stackx, stackx.m_41613_()}));
         }
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (player.m_6144_()) {
         if (!world.f_46443_) {
            this.setInventory(stack, null);
            this.setInvalid(stack, false);
         }

         return InteractionResultHolder.m_19092_(stack, world.f_46443_);
      } else {
         return InteractionResultHolder.m_19098_(stack);
      }
   }

   public int getMaxStackSize(ItemStack stack) {
      return this.hasInventory(stack) ? 1 : 64;
   }

   @NotNull
   public Component m_7626_(@NotNull ItemStack stack) {
      if (this.hasInventory(stack)) {
         return this.isInvalid(stack)
            ? TextComponentUtil.build(super.m_7626_(stack), " ", EnumColor.DARK_RED, MekanismLang.INVALID)
            : TextComponentUtil.build(super.m_7626_(stack), " ", EnumColor.DARK_GREEN, MekanismLang.ENCODED);
      } else {
         return super.m_7626_(stack);
      }
   }

   public boolean isInvalid(ItemStack stack) {
      return ItemDataUtils.getBoolean(stack, "invalid");
   }

   public void setInvalid(ItemStack stack, boolean invalid) {
      ItemDataUtils.setBoolean(stack, "invalid", invalid);
   }

   public boolean hasInventory(ItemStack stack) {
      return ItemDataUtils.hasData(stack, "Items", 9);
   }

   @Nullable
   public NonNullList<ItemStack> getInventory(ItemStack stack) {
      if (!this.hasInventory(stack)) {
         return null;
      } else {
         ListTag tagList = ItemDataUtils.getList(stack, "Items");
         NonNullList<ItemStack> inventory = NonNullList.m_122780_(9, ItemStack.f_41583_);

         for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundTag tagCompound = tagList.m_128728_(tagCount);
            byte slotID = tagCompound.m_128445_("Slot");
            if (slotID >= 0 && slotID < 9) {
               inventory.set(slotID, ItemStack.m_41712_(tagCompound));
            }
         }

         return inventory;
      }
   }

   public void setInventory(ItemStack stack, @Nullable NonNullList<ItemStack> inv) {
      if (inv == null) {
         ItemDataUtils.removeData(stack, "Items");
      } else {
         ListTag tagList = new ListTag();

         for (int slotCount = 0; slotCount < 9; slotCount++) {
            ItemStack slotStack = (ItemStack)inv.get(slotCount);
            if (!slotStack.m_41619_()) {
               CompoundTag tagCompound = slotStack.serializeNBT();
               tagCompound.m_128344_("Slot", (byte)slotCount);
               tagList.add(tagCompound);
            }
         }

         ItemDataUtils.setListOrRemove(stack, "Items", tagList);
      }
   }
}

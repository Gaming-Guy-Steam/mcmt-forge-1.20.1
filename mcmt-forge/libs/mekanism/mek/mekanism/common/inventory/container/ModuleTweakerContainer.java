package mekanism.common.inventory.container;

import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.EnumUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleTweakerContainer extends MekanismContainer {
   public ModuleTweakerContainer(int id, Inventory inv) {
      super(MekanismContainerTypes.MODULE_TWEAKER, id, inv);
      this.addSlotsAndOpen();
   }

   public ModuleTweakerContainer(int id, Inventory inv, FriendlyByteBuf buf) {
      this(id, inv);
   }

   @Override
   protected void addInventorySlots(@NotNull Inventory inv) {
      int armorInventorySize = inv.f_35975_.size();

      for (int index = 0; index < armorInventorySize; index++) {
         EquipmentSlot slotType = EnumUtils.EQUIPMENT_SLOT_TYPES[2 + armorInventorySize - index - 1];
         this.m_38897_(new ArmorSlot(inv, 36 + slotType.ordinal() - 2, 8, 8 + index * 18, slotType) {
            @Override
            public boolean m_8010_(@NotNull Player player) {
               return false;
            }

            @Override
            public boolean m_5857_(@NotNull ItemStack stack) {
               return false;
            }
         });
      }

      for (int slotY = 0; slotY < Inventory.m_36059_(); slotY++) {
         this.m_38897_(new HotBarSlot(inv, slotY, 43 + slotY * 18, 161) {
            public boolean m_8010_(@NotNull Player player) {
               return false;
            }

            public boolean m_5857_(@NotNull ItemStack stack) {
               return false;
            }
         });
      }

      this.m_38897_(new OffhandSlot(inv, 40, 8, 88) {
         public boolean m_8010_(@NotNull Player player) {
            return false;
         }

         public boolean m_5857_(@NotNull ItemStack stack) {
            return false;
         }
      });
   }

   public static boolean isTweakableItem(ItemStack stack) {
      return !stack.m_41619_() && stack.m_41720_() instanceof IModuleContainerItem;
   }

   public static boolean hasTweakableItem(Player player) {
      for (int slot = 0; slot < Inventory.m_36059_(); slot++) {
         if (isTweakableItem((ItemStack)player.m_150109_().f_35974_.get(slot))) {
            return true;
         }
      }

      return player.m_150109_().f_35975_.stream().anyMatch(ModuleTweakerContainer::isTweakableItem)
         || player.m_150109_().f_35976_.stream().anyMatch(ModuleTweakerContainer::isTweakableItem);
   }

   @Nullable
   @Override
   public ICapabilityProvider getSecurityObject() {
      return null;
   }

   public boolean m_6875_(@NotNull Player player) {
      return true;
   }
}

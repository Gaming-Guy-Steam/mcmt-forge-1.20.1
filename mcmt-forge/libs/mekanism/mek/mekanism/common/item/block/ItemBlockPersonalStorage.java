package mekanism.common.item.block;

import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.BlockPersonalStorage;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.SecurityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

public class ItemBlockPersonalStorage<BLOCK extends BlockPersonalStorage<?, ?>> extends ItemBlockTooltip<BLOCK> implements IDroppableContents, IGuiItem {
   private final ResourceLocation openStat;

   public ItemBlockPersonalStorage(BLOCK block, ResourceLocation openStat) {
      super(block);
      this.openStat = openStat;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
      return SecurityUtils.get().claimOrOpenGui(world, player, hand, (p, h, s) -> {
         if (!world.f_46443_) {
            PersonalStorageManager.getInventoryFor(s);
         }

         this.getContainerType().tryOpenGui(p, h, s);
         p.m_36246_(Stats.f_12988_.m_12902_(this.openStat));
      });
   }

   @NotNull
   public InteractionResult m_6225_(@NotNull UseOnContext context) {
      InteractionResult result = this.m_40576_(new BlockPlaceContext(context));
      Player player = context.m_43723_();
      return !result.m_19077_() && player != null ? this.m_7203_(context.m_43725_(), player, context.m_43724_()).m_19089_() : result;
   }

   protected boolean m_40610_(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
      Player player = context.m_43723_();
      return (player == null || player instanceof FakePlayer || player.m_6144_()) && super.m_40610_(context, state);
   }

   @Override
   public ContainerTypeRegistryObject<PersonalStorageItemContainer> getContainerType() {
      return MekanismContainerTypes.PERSONAL_STORAGE_ITEM;
   }

   @Override
   public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
      super.onDestroyed(item, damageSource);
      if (!item.m_9236_().f_46443_) {
         ItemStack stack = item.m_32055_();
         PersonalStorageManager.getInventoryIfPresent(stack).ifPresent(inventory -> {
            if (inventory.isInventoryEmpty()) {
               PersonalStorageManager.deleteInventory(stack);
            }
         });
      }
   }

   @Override
   public List<IInventorySlot> getDroppedSlots(ItemStack stack) {
      return PersonalStorageManager.getInventoryIfPresent(stack).map(inventory -> inventory.getInventorySlots(null)).orElse(Collections.emptyList());
   }
}

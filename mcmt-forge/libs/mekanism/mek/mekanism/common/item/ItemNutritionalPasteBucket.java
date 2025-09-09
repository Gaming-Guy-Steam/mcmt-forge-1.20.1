package mekanism.common.item;

import java.util.function.Supplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemNutritionalPasteBucket extends BucketItem {
   public ItemNutritionalPasteBucket(Supplier<? extends Fluid> supplier, Properties builder) {
      super(supplier, builder);
   }

   public int m_8105_(@NotNull ItemStack stack) {
      return 32;
   }

   @NotNull
   public UseAnim m_6164_(@NotNull ItemStack stack) {
      return UseAnim.DRINK;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (MekanismUtils.isPlayingMode(player)) {
         int needed = Math.min(20 - player.m_36324_().m_38702_(), 1000 / MekanismConfig.general.nutritionalPasteMBPerFood.get());
         if (needed > 0) {
            return ItemUtils.m_150959_(level, player, hand);
         }
      }

      return super.m_7203_(level, player, hand);
   }

   @NotNull
   public ItemStack m_5922_(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
      if (entity instanceof Player player && MekanismUtils.isPlayingMode(player)) {
         int needed = Math.min(20 - player.m_36324_().m_38702_(), 1000 / MekanismConfig.general.nutritionalPasteMBPerFood.get());
         if (needed > 0) {
            if (entity instanceof ServerPlayer serverPlayer) {
               CriteriaTriggers.f_10592_.m_23682_(serverPlayer, stack);
               serverPlayer.m_36246_(Stats.f_12982_.m_12902_(this));
            }

            if (!level.f_46443_) {
               player.m_36324_().m_38707_(needed, MekanismConfig.general.nutritionalPasteSaturation.get());
            }

            stack.m_41774_(1);
            return stack.m_41619_() ? new ItemStack(Items.f_42446_) : stack;
         }
      }

      return super.m_5922_(stack, level, entity);
   }

   public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
      return new FluidBucketWrapper(stack);
   }
}

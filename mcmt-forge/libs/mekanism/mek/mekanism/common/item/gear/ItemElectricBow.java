package mekanism.common.item.gear;

import java.util.List;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

public class ItemElectricBow extends BowItem implements IModeItem, IItemHUDProvider, CreativeTabDeferredRegister.ICustomCreativeTabContents {
   public ItemElectricBow(Properties properties) {
      super(properties.m_41497_(Rarity.RARE).setNoRepair().m_41487_(1));
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredEnergy(stack, tooltip, true);
      tooltip.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, new Object[]{BooleanStateDisplay.OnOff.of(this.getFireState(stack))}));
   }

   public void m_5551_(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity entityLiving, int timeLeft) {
      if (entityLiving instanceof Player player) {
         IEnergyContainer energyContainer = null;
         FloatingLong energyNeeded = FloatingLong.ZERO;
         if (!player.m_7500_()) {
            energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            energyNeeded = this.getFireState(stack) ? MekanismConfig.gear.electricBowEnergyUsageFire.get() : MekanismConfig.gear.electricBowEnergyUsage.get();
            if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
               return;
            }
         }

         boolean infinity = player.m_7500_() || stack.getEnchantmentLevel(Enchantments.f_44952_) > 0;
         ItemStack ammo = player.m_6298_(stack);
         int charge = ForgeEventFactory.onArrowLoose(stack, world, player, this.m_8105_(stack) - timeLeft, !ammo.m_41619_() || infinity);
         if (charge < 0) {
            return;
         }

         if (!ammo.m_41619_() || infinity) {
            float velocity = m_40661_(charge);
            if (velocity < 0.1) {
               return;
            }

            if (ammo.m_41619_()) {
               ammo = new ItemStack(Items.f_42412_);
            }

            boolean noConsume = player.m_7500_() || ammo.m_41720_() instanceof ArrowItem arrow && arrow.isInfinite(ammo, stack, player);
            if (!world.f_46443_) {
               ArrowItem arrowitem = (ArrowItem)(ammo.m_41720_() instanceof ArrowItem ? ammo.m_41720_() : Items.f_42412_);
               AbstractArrow arrowEntity = arrowitem.m_6394_(world, ammo, player);
               arrowEntity = this.customArrow(arrowEntity);
               arrowEntity.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 3.0F * velocity, 1.0F);
               if (velocity == 1.0F) {
                  arrowEntity.m_36762_(true);
               }

               int power = stack.getEnchantmentLevel(Enchantments.f_44988_);
               if (power > 0) {
                  arrowEntity.m_36781_(arrowEntity.m_36789_() + 0.5 * power + 0.5);
               }

               int punch = stack.getEnchantmentLevel(Enchantments.f_44989_);
               if (punch > 0) {
                  arrowEntity.m_36735_(punch);
               }

               if (stack.getEnchantmentLevel(Enchantments.f_44990_) > 0) {
                  arrowEntity.m_20254_(100);
               }

               if (energyContainer != null) {
                  energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
               }

               if (noConsume || player.m_7500_() && (ammo.m_41720_() == Items.f_42737_ || ammo.m_41720_() == Items.f_42738_)) {
                  arrowEntity.f_36705_ = Pickup.CREATIVE_ONLY;
               }

               world.m_7967_(arrowEntity);
            }

            world.m_6263_(
               null,
               player.m_20185_(),
               player.m_20186_(),
               player.m_20189_(),
               SoundEvents.f_11687_,
               SoundSource.PLAYERS,
               1.0F,
               1.0F / (world.f_46441_.m_188501_() * 0.4F + 1.2F) + velocity * 0.5F
            );
            if (!noConsume && !player.m_7500_()) {
               ammo.m_41774_(1);
               if (ammo.m_41619_()) {
                  player.m_150109_().m_36057_(ammo);
               }
            }

            player.m_36246_(Stats.f_12982_.m_12902_(this));
         }
      }
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return enchantment != Enchantments.f_44990_ && super.canApplyAtEnchantingTable(stack, enchantment);
   }

   public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
      if (stack.m_41619_()) {
         return 0;
      } else {
         return enchantment == Enchantments.f_44990_ && this.getFireState(stack)
            ? Math.max(1, super.getEnchantmentLevel(stack, enchantment))
            : super.getEnchantmentLevel(stack, enchantment);
      }
   }

   public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
      Map<Enchantment, Integer> enchantments = super.getAllEnchantments(stack);
      if (this.getFireState(stack)) {
         enchantments.merge(Enchantments.f_44990_, 1, Math::max);
      }

      return enchantments;
   }

   private void setFireState(ItemStack stack, boolean state) {
      ItemDataUtils.setBoolean(stack, "mode", state);
   }

   private boolean getFireState(ItemStack stack) {
      return ItemDataUtils.getBoolean(stack, "mode");
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      list.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, new Object[]{BooleanStateDisplay.OnOff.of(this.getFireState(stack))}));
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getEnergyBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return MekanismConfig.client.energyColor.get();
   }

   @Override
   public void addItems(Output tabOutput) {
      tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekanismConfig.gear.electricBowMaxEnergy));
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return (ICapabilityProvider)(!MekanismConfig.gear.isLoaded()
         ? super.initCapabilities(stack, nbt)
         : new ItemCapabilityWrapper(
            stack,
            RateLimitEnergyHandler.create(
               MekanismConfig.gear.electricBowChargeRate,
               MekanismConfig.gear.electricBowMaxEnergy,
               BasicEnergyContainer.manualOnly,
               BasicEnergyContainer.alwaysTrue
            )
         ));
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      if (Math.abs(shift) % 2 == 1) {
         boolean newState = !this.getFireState(stack);
         this.setFireState(stack, newState);
         displayChange.sendMessage(player, () -> MekanismLang.FIRE_MODE.translate(new Object[]{BooleanStateDisplay.OnOff.of(newState, true)}));
      }
   }

   @NotNull
   @Override
   public Component getScrollTextComponent(@NotNull ItemStack stack) {
      return MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, new Object[]{BooleanStateDisplay.OnOff.of(this.getFireState(stack), true)});
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return slotChanged || oldStack.m_41720_() != newStack.m_41720_();
   }

   public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
      return oldStack.m_41720_() != newStack.m_41720_();
   }
}

package mekanism.common.tile;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChargepad extends TileEntityMekanism {
   private static final Predicate<LivingEntity> CHARGE_PREDICATE = entity -> !entity.m_5833_() && (entity instanceof Player || entity instanceof EntityRobit);
   private MachineEnergyContainer<TileEntityChargepad> energyContainer;

   public TileEntityChargepad(BlockPos pos, BlockState state) {
      super(MekanismBlocks.CHARGEPAD, pos, state);
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK, RelativeSide.BOTTOM);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      boolean active = false;

      for (LivingEntity entity : this.f_58857_
         .m_6443_(
            LivingEntity.class,
            new AABB(
               this.f_58858_.m_123341_(),
               this.f_58858_.m_123342_(),
               this.f_58858_.m_123343_(),
               this.f_58858_.m_123341_() + 1,
               this.f_58858_.m_123342_() + 0.4,
               this.f_58858_.m_123343_() + 1
            ),
            CHARGE_PREDICATE
         )) {
         active = !this.energyContainer.isEmpty();
         if (!active) {
            break;
         }

         if (entity instanceof EntityRobit robit) {
            this.provideEnergy(robit);
         } else if (entity instanceof Player) {
            Optional<IItemHandler> itemHandlerCap = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (!this.chargeHandler(itemHandlerCap) && Mekanism.hooks.CuriosLoaded) {
               this.chargeHandler(CuriosIntegration.getCuriosInventory(entity));
            }
         }
      }

      if (active != this.getActive()) {
         this.setActive(active);
      }
   }

   private boolean chargeHandler(Optional<? extends IItemHandler> itemHandlerCap) {
      if (itemHandlerCap.isPresent()) {
         IItemHandler itemHandler = itemHandlerCap.get();
         int slots = itemHandler.getSlots();

         for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.m_41619_() && this.provideEnergy(EnergyCompatUtils.getStrictEnergyHandler(stack))) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean provideEnergy(@Nullable IStrictEnergyHandler energyHandler) {
      if (energyHandler == null) {
         return false;
      } else {
         FloatingLong energyToGive = this.energyContainer.getEnergyPerTick();
         FloatingLong simulatedRemainder = energyHandler.insertEnergy(energyToGive, Action.SIMULATE);
         if (simulatedRemainder.smallerThan(energyToGive)) {
            FloatingLong extractedEnergy = this.energyContainer.extract(energyToGive.subtract(simulatedRemainder), Action.EXECUTE, AutomationType.INTERNAL);
            if (!extractedEnergy.isZero()) {
               MekanismUtils.logExpectedZero(energyHandler.insertEnergy(extractedEnergy, Action.EXECUTE));
               return true;
            }
         }

         return false;
      }
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      if (this.getActive()) {
         this.f_58857_
            .m_7106_(
               DustParticleOptions.f_123656_,
               this.m_58899_().m_123341_() + this.f_58857_.f_46441_.m_188500_(),
               this.m_58899_().m_123342_() + 0.15,
               this.m_58899_().m_123343_() + this.f_58857_.f_46441_.m_188500_(),
               0.0,
               0.0,
               0.0
            );
      }
   }

   @Override
   public void setActive(boolean active) {
      boolean wasActive = this.getActive();
      super.setActive(active);
      if (wasActive != active) {
         SoundEvent sound;
         float pitch;
         if (active) {
            sound = SoundEvents.f_12449_;
            pitch = 0.8F;
         } else {
            sound = SoundEvents.f_12448_;
            pitch = 0.7F;
         }

         this.f_58857_
            .m_6263_(
               null,
               this.m_58899_().m_123341_() + 0.5,
               this.m_58899_().m_123342_() + 0.1,
               this.m_58899_().m_123343_() + 0.5,
               sound,
               SoundSource.BLOCKS,
               0.3F,
               pitch
            );
      }
   }
}

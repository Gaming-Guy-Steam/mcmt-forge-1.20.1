package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicEnergyContainer implements IEnergyContainer {
   public static final Predicate<AutomationType> alwaysTrue = ConstantPredicates.alwaysTrue();
   public static final Predicate<AutomationType> alwaysFalse = ConstantPredicates.alwaysFalse();
   public static final Predicate<AutomationType> internalOnly = automationType -> automationType == AutomationType.INTERNAL;
   public static final Predicate<AutomationType> manualOnly = automationType -> automationType == AutomationType.MANUAL;
   public static final Predicate<AutomationType> notExternal = automationType -> automationType != AutomationType.EXTERNAL;
   private FloatingLong stored = FloatingLong.ZERO;
   protected final Predicate<AutomationType> canExtract;
   protected final Predicate<AutomationType> canInsert;
   private final FloatingLong maxEnergy;
   @Nullable
   private final IContentsListener listener;

   public static BasicEnergyContainer create(FloatingLong maxEnergy, @Nullable IContentsListener listener) {
      Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
      return new BasicEnergyContainer(maxEnergy, alwaysTrue, alwaysTrue, listener);
   }

   public static BasicEnergyContainer input(FloatingLong maxEnergy, @Nullable IContentsListener listener) {
      Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
      return new BasicEnergyContainer(maxEnergy, notExternal, alwaysTrue, listener);
   }

   public static BasicEnergyContainer output(FloatingLong maxEnergy, @Nullable IContentsListener listener) {
      Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
      return new BasicEnergyContainer(maxEnergy, alwaysTrue, internalOnly, listener);
   }

   public static BasicEnergyContainer create(
      FloatingLong maxEnergy, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, @Nullable IContentsListener listener
   ) {
      Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      return new BasicEnergyContainer(maxEnergy, canExtract, canInsert, listener);
   }

   protected BasicEnergyContainer(
      FloatingLong maxEnergy, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, @Nullable IContentsListener listener
   ) {
      this.maxEnergy = maxEnergy.copyAsConst();
      this.canExtract = canExtract;
      this.canInsert = canInsert;
      this.listener = listener;
   }

   @Override
   public void onContentsChanged() {
      if (this.listener != null) {
         this.listener.onContentsChanged();
      }
   }

   @Override
   public FloatingLong getEnergy() {
      return this.stored;
   }

   @Override
   public void setEnergy(FloatingLong energy) {
      if (!this.stored.equals(energy)) {
         this.stored = energy.copy();
         this.onContentsChanged();
      }
   }

   protected FloatingLong getRate(@Nullable AutomationType automationType) {
      return FloatingLong.MAX_VALUE;
   }

   @Override
   public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
      if (!amount.isZero() && this.canInsert.test(automationType)) {
         FloatingLong needed = this.getRate(automationType).min(this.getNeeded());
         if (needed.isZero()) {
            return amount;
         } else {
            FloatingLong toAdd = amount.min(needed);
            if (!toAdd.isZero() && action.execute()) {
               this.stored = this.stored.plusEqual(toAdd);
               this.onContentsChanged();
            }

            return amount.subtract(toAdd);
         }
      } else {
         return amount;
      }
   }

   @Override
   public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && !amount.isZero() && this.canExtract.test(automationType)) {
         FloatingLong ret = this.getRate(automationType).min(this.getEnergy()).min(amount).copy();
         if (!ret.isZero() && action.execute()) {
            this.stored = this.stored.minusEqual(ret);
            this.onContentsChanged();
         }

         return ret;
      } else {
         return FloatingLong.ZERO;
      }
   }

   @Override
   public boolean isEmpty() {
      return this.stored.isZero();
   }

   @Override
   public FloatingLong getMaxEnergy() {
      return this.maxEnergy;
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128359_("stored", this.stored.toString());
      }

      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      NBTUtils.setFloatingLongIfPresent(nbt, "stored", this::setEnergy);
   }
}

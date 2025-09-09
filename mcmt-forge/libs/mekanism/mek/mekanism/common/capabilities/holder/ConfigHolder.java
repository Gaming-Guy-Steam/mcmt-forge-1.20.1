package mekanism.common.capabilities.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigHolder<TYPE> implements IHolder {
   private static final ISlotInfo NO_CONFIG = new ISlotInfo() {
      @Override
      public boolean canInput() {
         return true;
      }

      @Override
      public boolean canOutput() {
         return true;
      }

      @Override
      public int hashCode() {
         return 0;
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this;
      }

      @Override
      public String toString() {
         return "No Config";
      }
   };
   private final Map<Direction, ISlotInfo> cachedSlotInfo = new EnumMap<>(Direction.class);
   private final Supplier<TileComponentConfig> configSupplier;
   private final Supplier<Direction> facingSupplier;
   protected final List<TYPE> slots = new ArrayList<>();
   @Nullable
   private Direction lastDirection;
   private boolean listenerAdded;

   protected ConfigHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
      this.facingSupplier = facingSupplier;
      this.configSupplier = configSupplier;
   }

   protected abstract TransmissionType getTransmissionType();

   @Override
   public boolean canInsert(@Nullable Direction side) {
      return this.canInteract(side, ISlotInfo::canInput);
   }

   @Override
   public boolean canExtract(@Nullable Direction side) {
      return this.canInteract(side, ISlotInfo::canOutput);
   }

   private boolean canInteract(@Nullable Direction side, @NotNull Predicate<ISlotInfo> interactPredicate) {
      if (side == null) {
         return false;
      } else {
         ISlotInfo slotInfo = this.getSlotInfo(side);
         return slotInfo == NO_CONFIG ? true : slotInfo != null && interactPredicate.test(slotInfo);
      }
   }

   @NotNull
   protected List<TYPE> getSlots(@Nullable Direction side, @NotNull Function<ISlotInfo, List<TYPE>> slotInfoParser) {
      if (side == null) {
         return this.slots;
      } else {
         ISlotInfo slotInfo = this.getSlotInfo(side);
         if (slotInfo == NO_CONFIG) {
            return this.slots;
         } else {
            return slotInfo == null ? Collections.emptyList() : slotInfoParser.apply(slotInfo);
         }
      }
   }

   @Nullable
   private ISlotInfo getSlotInfo(Direction side) {
      Direction direction = this.facingSupplier.get();
      if (direction != this.lastDirection) {
         this.cachedSlotInfo.clear();
         this.lastDirection = direction;
      } else if (this.cachedSlotInfo.containsKey(side)) {
         return this.cachedSlotInfo.get(side);
      }

      TileComponentConfig config = this.configSupplier.get();
      ISlotInfo slotInfo;
      if (config == null) {
         slotInfo = NO_CONFIG;
      } else {
         TransmissionType transmissionType = this.getTransmissionType();
         ConfigInfo configInfo = config.getConfig(transmissionType);
         if (configInfo == null) {
            slotInfo = NO_CONFIG;
         } else {
            if (!this.listenerAdded) {
               this.listenerAdded = true;
               config.addConfigChangeListener(transmissionType, this.cachedSlotInfo::remove);
            }

            slotInfo = configInfo.getSlotInfo(RelativeSide.fromDirections(direction, side));
            if (slotInfo != null && !slotInfo.isEnabled()) {
               slotInfo = null;
            }
         }
      }

      this.cachedSlotInfo.put(side, slotInfo);
      return slotInfo;
   }
}

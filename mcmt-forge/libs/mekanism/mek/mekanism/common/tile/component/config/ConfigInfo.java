package mekanism.common.tile.component.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigInfo {
   private final Supplier<Direction> facingSupplier;
   private boolean canEject;
   private boolean ejecting;
   private final Map<RelativeSide, DataType> sideConfig;
   private final Map<DataType, ISlotInfo> slotInfo;
   private final Map<Object, List<DataType>> containerTypeMapping;
   private Set<RelativeSide> disabledSides;

   public ConfigInfo(@NotNull Supplier<Direction> facingSupplier) {
      this.facingSupplier = facingSupplier;
      this.canEject = true;
      this.ejecting = false;
      this.sideConfig = new EnumMap<>(RelativeSide.class);

      for (RelativeSide side : EnumUtils.SIDES) {
         this.sideConfig.put(side, DataType.NONE);
      }

      this.slotInfo = new EnumMap<>(DataType.class);
      this.containerTypeMapping = new HashMap<>();
   }

   public boolean canEject() {
      return this.canEject;
   }

   public void setCanEject(boolean canEject) {
      this.canEject = canEject;
   }

   public boolean isEjecting() {
      return this.ejecting;
   }

   public void setEjecting(boolean ejecting) {
      this.ejecting = ejecting;
   }

   public void addDisabledSides(@NotNull RelativeSide... sides) {
      if (this.disabledSides == null) {
         this.disabledSides = EnumSet.noneOf(RelativeSide.class);
      }

      for (RelativeSide side : sides) {
         this.disabledSides.add(side);
         this.sideConfig.put(side, DataType.NONE);
      }
   }

   public boolean isSideEnabled(@NotNull RelativeSide side) {
      return this.disabledSides == null ? true : !this.disabledSides.contains(side);
   }

   @NotNull
   public DataType getDataType(@NotNull RelativeSide side) {
      return this.sideConfig.get(side);
   }

   public void setDataType(@NotNull DataType dataType, @NotNull RelativeSide... sides) {
      for (RelativeSide side : sides) {
         if (this.isSideEnabled(side)) {
            this.sideConfig.put(side, dataType);
         }
      }
   }

   @NotNull
   public Set<DataType> getSupportedDataTypes() {
      Set<DataType> dataTypes = EnumSet.of(DataType.NONE);
      dataTypes.addAll(this.slotInfo.keySet());
      return dataTypes;
   }

   public void fill(@NotNull DataType dataType) {
      for (RelativeSide side : EnumUtils.SIDES) {
         this.setDataType(dataType, side);
      }
   }

   @Nullable
   public ISlotInfo getSlotInfo(@NotNull RelativeSide side) {
      return this.getSlotInfo(this.getDataType(side));
   }

   @Nullable
   public ISlotInfo getSlotInfo(@NotNull DataType dataType) {
      return this.slotInfo.get(dataType);
   }

   public void addSlotInfo(@NotNull DataType dataType, @NotNull ISlotInfo info) {
      this.slotInfo.put(dataType, info);
      if (info instanceof ChemicalSlotInfo<?, ?, ?> slotInfo) {
         for (IChemicalTank<?, ?> tank : slotInfo.getTanks()) {
            this.containerTypeMapping.computeIfAbsent(tank, t -> new ArrayList<>()).add(dataType);
         }
      } else if (info instanceof FluidSlotInfo slotInfo) {
         for (IExtendedFluidTank tank : slotInfo.getTanks()) {
            this.containerTypeMapping.computeIfAbsent(tank, t -> new ArrayList<>()).add(dataType);
         }
      } else if (info instanceof InventorySlotInfo slotInfo) {
         for (IInventorySlot slot : slotInfo.getSlots()) {
            this.containerTypeMapping.computeIfAbsent(slot, t -> new ArrayList<>()).add(dataType);
         }
      }
   }

   public List<DataType> getDataTypeForContainer(Object container) {
      return this.containerTypeMapping.getOrDefault(container, new ArrayList<>());
   }

   public void setDefaults() {
      if (this.slotInfo.containsKey(DataType.INPUT)) {
         this.fill(DataType.INPUT);
      }

      if (this.slotInfo.containsKey(DataType.OUTPUT)) {
         this.setDataType(DataType.OUTPUT, RelativeSide.RIGHT);
      }

      if (this.slotInfo.containsKey(DataType.EXTRA)) {
         this.setDataType(DataType.EXTRA, RelativeSide.BOTTOM);
      }

      if (this.slotInfo.containsKey(DataType.ENERGY)) {
         this.setDataType(DataType.ENERGY, RelativeSide.BACK);
      }
   }

   public Set<Direction> getSidesForData(@NotNull DataType dataType) {
      return this.getSides(type -> type == dataType);
   }

   public Set<Direction> getSides(Predicate<DataType> predicate) {
      Direction facing = this.facingSupplier.get();
      Set<Direction> directions = null;

      for (Entry<RelativeSide, DataType> entry : this.sideConfig.entrySet()) {
         if (predicate.test(entry.getValue())) {
            if (directions == null) {
               directions = EnumSet.noneOf(Direction.class);
            }

            directions.add(entry.getKey().getDirection(facing));
         }
      }

      return directions == null ? Collections.emptySet() : directions;
   }

   public Set<Direction> getAllOutputtingSides() {
      return this.getSides(DataType::canOutput);
   }

   public Set<Direction> getSidesForOutput(DataType outputType) {
      return this.getSides(type -> type == outputType || type == DataType.INPUT_OUTPUT);
   }

   @NotNull
   public DataType incrementDataType(@NotNull RelativeSide relativeSide) {
      DataType current = this.getDataType(relativeSide);
      if (this.isSideEnabled(relativeSide)) {
         Set<DataType> supportedDataTypes = this.getSupportedDataTypes();
         DataType newType = current.getNext(supportedDataTypes::contains);
         this.sideConfig.put(relativeSide, newType);
         return newType;
      } else {
         return current;
      }
   }

   @NotNull
   public DataType decrementDataType(@NotNull RelativeSide relativeSide) {
      DataType current = this.getDataType(relativeSide);
      if (this.isSideEnabled(relativeSide)) {
         Set<DataType> supportedDataTypes = this.getSupportedDataTypes();
         DataType newType = current.getPrevious(supportedDataTypes::contains);
         this.sideConfig.put(relativeSide, newType);
         return newType;
      } else {
         return current;
      }
   }
}

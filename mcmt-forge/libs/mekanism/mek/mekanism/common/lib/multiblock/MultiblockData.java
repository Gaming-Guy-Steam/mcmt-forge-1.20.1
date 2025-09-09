package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.math.voxel.IShape;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiblockData
   implements IMekanismInventory,
   IMekanismFluidHandler,
   IMekanismStrictEnergyHandler,
   ITileHeatHandler,
   IGasTracker,
   IInfusionTracker,
   IPigmentTracker,
   ISlurryTracker {
   protected static final Map<Direction, Set<Direction>> SIDE_REFERENCES = new EnumMap<>(Direction.class);
   public Set<BlockPos> locations = new ObjectOpenHashSet();
   public Set<BlockPos> internalLocations = new ObjectOpenHashSet();
   public Set<IValveHandler.ValveData> valves = new ObjectOpenHashSet();
   @ContainerSync(
      getter = "getVolume",
      setter = "setVolume"
   )
   private int volume;
   public UUID inventoryID;
   public boolean hasMaster;
   @Nullable
   public BlockPos renderLocation;
   @ContainerSync
   private VoxelCuboid bounds = new VoxelCuboid(0, 0, 0);
   @ContainerSync
   private boolean formed;
   public boolean recheckStructure;
   private int currentRedstoneLevel;
   private final BooleanSupplier remoteSupplier;
   private final Supplier<Level> worldSupplier;
   protected final List<IInventorySlot> inventorySlots = new ArrayList<>();
   protected final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
   protected final List<IGasTank> gasTanks = new ArrayList<>();
   protected final List<IInfusionTank> infusionTanks = new ArrayList<>();
   protected final List<IPigmentTank> pigmentTanks = new ArrayList<>();
   protected final List<ISlurryTank> slurryTanks = new ArrayList<>();
   protected final List<IEnergyContainer> energyContainers = new ArrayList<>();
   protected final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();
   private final BiPredicate<Object, AutomationType> formedBiPred = (t, automationType) -> this.isFormed();
   private final BiPredicate<Object, AutomationType> notExternalFormedBiPred = (t, automationType) -> automationType != AutomationType.EXTERNAL
      && this.isFormed();
   private boolean dirty;

   public MultiblockData(BlockEntity tile) {
      this.remoteSupplier = () -> tile.m_58904_().m_5776_();
      this.worldSupplier = tile::m_58904_;
   }

   public <T> BiPredicate<T, AutomationType> formedBiPred() {
      return (BiPredicate<T, AutomationType>)this.formedBiPred;
   }

   public <T> BiPredicate<T, AutomationType> notExternalFormedBiPred() {
      return (BiPredicate<T, AutomationType>)this.notExternalFormedBiPred;
   }

   protected IContentsListener createSaveAndComparator() {
      return this.createSaveAndComparator(this);
   }

   protected IContentsListener createSaveAndComparator(IContentsListener contentsListener) {
      return () -> {
         contentsListener.onContentsChanged();
         if (!this.isRemote()) {
            this.markDirtyComparator(this.getWorld());
         }
      };
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void resetDirty() {
      this.dirty = false;
   }

   public void markDirty() {
      this.dirty = true;
   }

   public boolean tick(Level world) {
      boolean needsPacket = false;

      for (IValveHandler.ValveData data : this.valves) {
         data.activeTicks = Math.max(0, data.activeTicks - 1);
         if (data.activeTicks > 0 != data.prevActive) {
            needsPacket = true;
         }

         data.prevActive = data.activeTicks > 0;
      }

      return needsPacket;
   }

   protected double calculateAverageAmbientTemperature(Level world) {
      BlockPos min = this.getMinPos();
      BlockPos max = this.getMaxPos();
      return HeatAPI.getAmbientTemp(
         getBiomeTemp(
            world,
            min,
            new BlockPos(max.m_123341_(), min.m_123342_(), min.m_123343_()),
            new BlockPos(min.m_123341_(), min.m_123342_(), max.m_123343_()),
            new BlockPos(max.m_123341_(), min.m_123342_(), max.m_123343_()),
            new BlockPos(min.m_123341_(), max.m_123342_(), min.m_123343_()),
            new BlockPos(max.m_123341_(), max.m_123342_(), min.m_123343_()),
            new BlockPos(min.m_123341_(), max.m_123342_(), max.m_123343_()),
            max
         )
      );
   }

   private static double getBiomeTemp(Level world, BlockPos... positions) {
      if (positions.length == 0) {
         throw new IllegalArgumentException("No positions given.");
      } else {
         return Arrays.stream(positions).mapToDouble(pos -> ((Biome)world.m_204166_(pos).m_203334_()).m_47505_(pos)).sum() / positions.length;
      }
   }

   public boolean setShape(IShape shape) {
      if (shape instanceof VoxelCuboid cuboid) {
         this.bounds = cuboid;
         this.renderLocation = cuboid.getMinPos().m_121945_(Direction.UP);
         this.setVolume(this.bounds.length() * this.bounds.width() * this.bounds.height());
         return true;
      } else {
         return false;
      }
   }

   public void onCreated(Level world) {
      for (BlockPos pos : this.internalLocations) {
         if (WorldUtils.getTileEntity(world, pos) instanceof IInternalMultiblock internalMultiblock) {
            internalMultiblock.setMultiblock(this);
         }
      }

      if (this.shouldCap(MultiblockCache.CacheSubstance.FLUID)) {
         for (IExtendedFluidTank tank : this.getFluidTanks(null)) {
            tank.setStackSize(Math.min(tank.getFluidAmount(), tank.getCapacity()), Action.EXECUTE);
         }
      }

      if (this.shouldCap(MultiblockCache.CacheSubstance.GAS)) {
         for (IGasTank tank : this.getGasTanks(null)) {
            tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
         }
      }

      if (this.shouldCap(MultiblockCache.CacheSubstance.INFUSION)) {
         for (IInfusionTank tank : this.getInfusionTanks(null)) {
            tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
         }
      }

      if (this.shouldCap(MultiblockCache.CacheSubstance.PIGMENT)) {
         for (IPigmentTank tank : this.getPigmentTanks(null)) {
            tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
         }
      }

      if (this.shouldCap(MultiblockCache.CacheSubstance.SLURRY)) {
         for (ISlurryTank tank : this.getSlurryTanks(null)) {
            tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
         }
      }

      if (this.shouldCap(MultiblockCache.CacheSubstance.ENERGY)) {
         for (IEnergyContainer container : this.getEnergyContainers(null)) {
            container.setEnergy(container.getEnergy().min(container.getMaxEnergy()));
         }
      }

      this.updateEjectors(world);
      this.forceUpdateComparatorLevel();
   }

   protected void updateEjectors(Level world) {
      for (IValveHandler.ValveData valve : this.valves) {
         if (WorldUtils.getTileEntity(world, valve.location) instanceof IMultiblockEjector ejector) {
            Set<Direction> sides = SIDE_REFERENCES.computeIfAbsent(valve.side, Collections::singleton);
            ejector.setEjectSides(sides);
         }
      }
   }

   protected boolean isRemote() {
      return this.remoteSupplier.getAsBoolean();
   }

   protected Level getWorld() {
      return this.worldSupplier.get();
   }

   protected boolean shouldCap(MultiblockCache.CacheSubstance<?, ?> type) {
      return true;
   }

   public void remove(Level world) {
      for (BlockPos pos : this.internalLocations) {
         if (WorldUtils.getTileEntity(world, pos) instanceof IInternalMultiblock internalMultiblock) {
            internalMultiblock.setMultiblock(null);
         }
      }

      this.inventoryID = null;
      this.formed = false;
      this.recheckStructure = false;
   }

   public void meltdownHappened(Level world) {
   }

   public void readUpdateTag(CompoundTag tag) {
      NBTUtils.setIntIfPresent(tag, "volume", this::setVolume);
      NBTUtils.setBlockPosIfPresent(tag, "renderLocation", value -> this.renderLocation = value);
      this.bounds = new VoxelCuboid(NbtUtils.m_129239_(tag.m_128469_("min")), NbtUtils.m_129239_(tag.m_128469_("max")));
      NBTUtils.setUUIDIfPresentElse(tag, "inventoryID", value -> this.inventoryID = value, () -> this.inventoryID = null);
   }

   public void writeUpdateTag(CompoundTag tag) {
      tag.m_128405_("volume", this.getVolume());
      if (this.renderLocation != null) {
         tag.m_128365_("renderLocation", NbtUtils.m_129224_(this.renderLocation));
      }

      tag.m_128365_("min", NbtUtils.m_129224_(this.bounds.getMinPos()));
      tag.m_128365_("max", NbtUtils.m_129224_(this.bounds.getMaxPos()));
      if (this.inventoryID != null) {
         tag.m_128362_("inventoryID", this.inventoryID);
      }
   }

   @ComputerMethod(
      nameOverride = "getLength"
   )
   public int length() {
      return this.bounds.length();
   }

   @ComputerMethod(
      nameOverride = "getWidth"
   )
   public int width() {
      return this.bounds.width();
   }

   @ComputerMethod(
      nameOverride = "getHeight"
   )
   public int height() {
      return this.bounds.height();
   }

   @ComputerMethod
   public BlockPos getMinPos() {
      return this.bounds.getMinPos();
   }

   @ComputerMethod
   public BlockPos getMaxPos() {
      return this.bounds.getMaxPos();
   }

   public VoxelCuboid getBounds() {
      return this.bounds;
   }

   public <T extends MultiblockData> boolean isPositionInsideBounds(@NotNull Structure structure, @NotNull BlockPos pos) {
      if (this.isFormed()) {
         VoxelCuboid.CuboidRelative relativeLocation = this.getBounds().getRelativeLocation(pos);
         if (relativeLocation == VoxelCuboid.CuboidRelative.INSIDE) {
            return true;
         }

         if (relativeLocation.isWall()) {
            MultiblockManager<T> manager = (MultiblockManager<T>)structure.getManager();
            if (manager != null) {
               IStructureValidator<T> validator = manager.createValidator();
               if (validator instanceof CuboidStructureValidator<T> cuboidValidator) {
                  validator.init(this.getWorld(), manager, structure);
                  cuboidValidator.loadCuboid(this.getBounds());
                  return cuboidValidator.getStructureRequirement(pos) == FormationProtocol.StructureRequirement.INNER;
               }
            }
         }
      }

      return false;
   }

   public boolean isPositionOutsideBounds(@NotNull BlockPos pos) {
      return this.isFormed() && this.getBounds().getRelativeLocation(pos) == VoxelCuboid.CuboidRelative.OUTSIDE;
   }

   @Nullable
   public Direction getOutsideSide(@NotNull BlockPos pos) {
      if (this.isFormed()) {
         VoxelCuboid bounds = this.getBounds();

         for (Direction direction : EnumUtils.DIRECTIONS) {
            if (bounds.getRelativeLocation(pos.m_121945_(direction)) == VoxelCuboid.CuboidRelative.OUTSIDE) {
               return direction;
            }
         }
      }

      return null;
   }

   @NotNull
   @Override
   public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
      return this.isFormed() ? this.inventorySlots : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.isFormed() ? this.fluidTanks : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IGasTank> getGasTanks(@Nullable Direction side) {
      return this.isFormed() ? this.gasTanks : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
      return this.isFormed() ? this.infusionTanks : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
      return this.isFormed() ? this.pigmentTanks : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
      return this.isFormed() ? this.slurryTanks : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.isFormed() ? this.energyContainers : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
      return this.isFormed() ? this.heatCapacitors : Collections.emptyList();
   }

   public Set<Direction> getDirectionsToEmit(BlockPos pos) {
      Set<Direction> directionsToEmit = null;

      for (Direction direction : EnumUtils.DIRECTIONS) {
         BlockPos neighborPos = pos.m_121945_(direction);
         if (!this.isKnownLocation(neighborPos)) {
            if (directionsToEmit == null) {
               directionsToEmit = EnumSet.noneOf(Direction.class);
            }

            directionsToEmit.add(direction);
         }
      }

      return directionsToEmit == null ? Collections.emptySet() : directionsToEmit;
   }

   public boolean isKnownLocation(BlockPos pos) {
      return this.locations.contains(pos) || this.internalLocations.contains(pos);
   }

   public Collection<IValveHandler.ValveData> getValveData() {
      return this.valves;
   }

   @Override
   public void onContentsChanged() {
      this.markDirty();
   }

   @Override
   public int hashCode() {
      int code = 1;
      code = 31 * code + this.locations.hashCode();
      code = 31 * code + this.bounds.hashCode();
      return 31 * code + this.getVolume();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && obj.getClass() == this.getClass()) {
         MultiblockData data = (MultiblockData)obj;
         if (!data.locations.equals(this.locations)) {
            return false;
         } else {
            return !data.bounds.equals(this.bounds) ? false : data.getVolume() == this.getVolume();
         }
      } else {
         return false;
      }
   }

   public boolean isFormed() {
      return this.formed;
   }

   public void setFormedForce(boolean formed) {
      this.formed = formed;
   }

   public int getVolume() {
      return this.volume;
   }

   public void setVolume(int volume) {
      this.volume = volume;
   }

   public void markDirtyComparator(Level world) {
      if (this.isFormed()) {
         int newRedstoneLevel = this.getMultiblockRedstoneLevel();
         if (newRedstoneLevel != this.currentRedstoneLevel) {
            this.currentRedstoneLevel = newRedstoneLevel;
            this.notifyAllUpdateComparator(world);
         }
      }
   }

   public void notifyAllUpdateComparator(Level world) {
      for (IValveHandler.ValveData valve : this.valves) {
         TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, valve.location);
         if (tile != null) {
            tile.markDirtyComparator();
         }
      }
   }

   public void forceUpdateComparatorLevel() {
      this.currentRedstoneLevel = this.getMultiblockRedstoneLevel();
   }

   protected int getMultiblockRedstoneLevel() {
      return 0;
   }

   public int getCurrentRedstoneLevel() {
      return this.currentRedstoneLevel;
   }
}

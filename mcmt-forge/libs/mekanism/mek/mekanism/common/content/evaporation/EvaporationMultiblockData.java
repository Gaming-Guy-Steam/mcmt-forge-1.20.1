package mekanism.common.content.evaporation;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.IEvaporationSolar;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvaporationMultiblockData extends MultiblockData implements IValveHandler, ISingleRecipeLookupHandler.FluidRecipeLookupHandler<FluidToFluidRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final int MAX_HEIGHT = 18;
   public static final double MAX_MULTIPLIER_TEMP = 3000.0;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"},
      docPlaceholder = "input tank"
   )
   public BasicFluidTank inputTank;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output tank"
   )
   public BasicFluidTank outputTank;
   @ContainerSync
   public VariableHeatCapacitor heatCapacitor;
   private double biomeAmbientTemp;
   private double tempMultiplier;
   private int inputTankCapacity;
   public float prevScale;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getProductionAmount"
   )
   public double lastGain;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getEnvironmentalLoss"
   )
   public double lastEnvironmentLoss;
   private final RecipeCacheLookupMonitor<FluidToFluidRecipe> recipeCacheLookupMonitor;
   private final BooleanSupplier recheckAllRecipeErrors;
   @ContainerSync
   private final boolean[] trackedErrors = new boolean[TRACKED_ERROR_TYPES.size()];
   private final Int2ObjectMap<NonNullConsumer<LazyOptional<IEvaporationSolar>>> cachedSolarListeners = new Int2ObjectArrayMap(4);
   private final Int2ObjectMap<LazyOptional<IEvaporationSolar>> cachedSolar = new Int2ObjectArrayMap(4);
   private final IOutputHandler<FluidStack> outputHandler;
   private final IInputHandler<FluidStack> inputHandler;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItemInput"},
      docPlaceholder = "input side's input slot"
   )
   final FluidInventorySlot inputInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItemOutput"},
      docPlaceholder = "input side's output slot"
   )
   final OutputInventorySlot outputInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItemInput"},
      docPlaceholder = "output side's input slot"
   )
   final FluidInventorySlot inputOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItemOutput"},
      docPlaceholder = "output side's output slot"
   )
   final OutputInventorySlot outputOutputSlot;

   public EvaporationMultiblockData(TileEntityThermalEvaporationBlock tile) {
      super(tile);
      this.recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
      this.recheckAllRecipeErrors = TileEntityRecipeMachine.shouldRecheckAllErrors(tile);
      this.biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.m_58904_(), tile.getTilePos());
      this.fluidTanks
         .add(
            this.inputTank = VariableCapacityFluidTank.input(
               this, this::getMaxFluid, this::containsRecipe, this.createSaveAndComparator(this.recipeCacheLookupMonitor)
            )
         );
      this.fluidTanks
         .add(this.outputTank = VariableCapacityFluidTank.output(this, MekanismConfig.general.evaporationOutputTankCapacity, BasicFluidTank.alwaysTrue, this));
      this.inputHandler = InputHelper.getInputHandler(this.inputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
      this.inventorySlots.add(this.inputInputSlot = FluidInventorySlot.fill(this.inputTank, this, 28, 20));
      this.inventorySlots.add(this.outputInputSlot = OutputInventorySlot.at(this, 28, 51));
      this.inventorySlots.add(this.inputOutputSlot = FluidInventorySlot.drain(this.outputTank, this, 132, 20));
      this.inventorySlots.add(this.outputOutputSlot = OutputInventorySlot.at(this, 132, 51));
      this.inputInputSlot.setSlotType(ContainerSlotType.INPUT);
      this.inputOutputSlot.setSlotType(ContainerSlotType.INPUT);
      this.heatCapacitors
         .add(this.heatCapacitor = VariableHeatCapacitor.create(MekanismConfig.general.evaporationHeatCapacity.get() * 3.0, () -> this.biomeAmbientTemp, this));
   }

   @Override
   public void onCreated(Level world) {
      super.onCreated(world);
      this.biomeAmbientTemp = this.calculateAverageAmbientTemperature(world);
      this.heatCapacitor.setHeatCapacity(MekanismConfig.general.evaporationHeatCapacity.get() * this.height(), true);
      this.updateSolars(world);
   }

   @Override
   public boolean tick(Level world) {
      boolean needsPacket = super.tick(world);
      this.lastEnvironmentLoss = this.simulateEnvironment();
      this.updateHeatCapacitors(null);
      this.tempMultiplier = (Math.min(3000.0, this.getTemperature()) - 300.0) * MekanismConfig.general.evaporationTempMultiplier.get() * (this.height() / 18.0);
      this.inputOutputSlot.drainTank(this.outputOutputSlot);
      this.inputInputSlot.fillTank(this.outputInputSlot);
      this.recipeCacheLookupMonitor.updateAndProcess();
      float scale = MekanismUtils.getScale(this.prevScale, this.inputTank);
      if (scale != this.prevScale) {
         this.prevScale = scale;
         needsPacket = true;
      }

      return needsPacket;
   }

   @Override
   public void readUpdateTag(CompoundTag tag) {
      super.readUpdateTag(tag);
      NBTUtils.setFluidStackIfPresent(tag, "fluid", fluid -> this.inputTank.setStack(fluid));
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> this.prevScale = scale);
      this.readValves(tag);
   }

   @Override
   public void writeUpdateTag(CompoundTag tag) {
      super.writeUpdateTag(tag);
      tag.m_128365_("fluid", this.inputTank.getFluid().writeToNBT(new CompoundTag()));
      tag.m_128350_("scale", this.prevScale);
      this.writeValves(tag);
   }

   @Override
   public double simulateEnvironment() {
      double currentTemperature = this.getTemperature();
      double heatCapacity = this.heatCapacitor.getHeatCapacity();
      this.heatCapacitor.handleHeat(this.getActiveSolars() * MekanismConfig.general.evaporationSolarMultiplier.get() * heatCapacity);
      if (Math.abs(currentTemperature - this.biomeAmbientTemp) < 0.001) {
         this.heatCapacitor.handleHeat(this.biomeAmbientTemp * heatCapacity - this.heatCapacitor.getHeat());
      } else {
         double incr = MekanismConfig.general.evaporationHeatDissipation.get() * Math.sqrt(Math.abs(currentTemperature - this.biomeAmbientTemp));
         if (currentTemperature > this.biomeAmbientTemp) {
            incr = -incr;
         }

         this.heatCapacitor.handleHeat(heatCapacity * incr);
         if (incr < 0.0) {
            return -incr;
         }
      }

      return 0.0;
   }

   @ComputerMethod
   public double getTemperature() {
      return this.heatCapacitor.getTemperature();
   }

   @Override
   public void setVolume(int volume) {
      if (this.getVolume() != volume) {
         super.setVolume(volume);
         this.inputTankCapacity = volume / 4 * MekanismConfig.general.evaporationFluidPerTank.get();
      }
   }

   public int getMaxFluid() {
      return this.inputTankCapacity;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<FluidToFluidRecipe, InputRecipeCache.SingleFluid<FluidToFluidRecipe>> getRecipeType() {
      return MekanismRecipeType.EVAPORATING;
   }

   @Nullable
   public FluidToFluidRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   @Override
   public void clearRecipeErrors(int cacheIndex) {
      Arrays.fill(this.trackedErrors, false);
   }

   @NotNull
   public CachedRecipe<FluidToFluidRecipe> createNewCachedRecipe(@NotNull FluidToFluidRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.fluidToFluid(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(errors -> {
            for (int i = 0; i < this.trackedErrors.length; i++) {
               this.trackedErrors[i] = errors.contains(TRACKED_ERROR_TYPES.get(i));
            }
         })
         .setActive(active -> {
            if (active) {
               if (this.tempMultiplier > 0.0 && this.tempMultiplier < 1.0) {
                  this.lastGain = 1.0F / (int)Math.ceil(1.0 / this.tempMultiplier);
               } else {
                  this.lastGain = this.tempMultiplier;
               }
            } else {
               this.lastGain = 0.0;
            }
         })
         .setRequiredTicks(() -> this.tempMultiplier > 0.0 && this.tempMultiplier < 1.0 ? (int)Math.ceil(1.0 / this.tempMultiplier) : 1)
         .setBaselineMaxOperations(() -> this.tempMultiplier > 0.0 && this.tempMultiplier < 1.0 ? 1 : (int)this.tempMultiplier);
   }

   public boolean hasWarning(CachedRecipe.OperationTracker.RecipeError error) {
      int errorIndex = TRACKED_ERROR_TYPES.indexOf(error);
      return errorIndex == -1 ? false : this.trackedErrors[errorIndex];
   }

   @Override
   public Level getHandlerWorld() {
      return this.getWorld();
   }

   @ComputerMethod
   int getActiveSolars() {
      int ret = 0;
      ObjectIterator var2 = this.cachedSolar.values().iterator();

      while (var2.hasNext()) {
         LazyOptional<IEvaporationSolar> capability = (LazyOptional<IEvaporationSolar>)var2.next();
         if (capability.map(IEvaporationSolar::canSeeSun).orElse(false)) {
            ret++;
         }
      }

      return ret;
   }

   private void updateSolarSpot(Level world, BlockPos pos, int corner) {
      this.cachedSolar.remove(corner);
      BlockEntity tile = WorldUtils.getTileEntity(world, pos);
      if (tile != null && !tile.m_58901_()) {
         LazyOptional<IEvaporationSolar> capability = CapabilityUtils.getCapability(tile, Capabilities.EVAPORATION_SOLAR, Direction.DOWN);
         if (capability.isPresent()) {
            capability.addListener(
               (NonNullConsumer)this.cachedSolarListeners.computeIfAbsent(corner, c -> new EvaporationMultiblockData.RefreshListener(this, c))
            );
            this.cachedSolar.put(corner, capability);
         }
      }
   }

   public void updateSolarSpot(Level world, BlockPos pos) {
      BlockPos maxPos = this.getMaxPos();
      if (pos.m_123342_() == maxPos.m_123342_() && this.getBounds().isOnCorner(pos)) {
         int i = 0;
         if (pos.m_123341_() + 3 == maxPos.m_123341_()) {
            i++;
         }

         if (pos.m_123343_() + 3 == maxPos.m_123343_()) {
            i += 2;
         }

         this.updateSolarSpot(world, pos, i);
      }
   }

   private void updateSolars(Level world) {
      BlockPos maxPos = this.getMaxPos();
      this.updateSolarSpot(world, maxPos, 0);
      this.updateSolarSpot(world, maxPos.m_122025_(3), 1);
      this.updateSolarSpot(world, maxPos.m_122013_(3), 2);
      this.updateSolarSpot(world, maxPos.m_7918_(-3, 0, -3), 3);
   }

   @Override
   protected int getMultiblockRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.inputTank.getFluidAmount(), this.inputTank.getCapacity());
   }

   @Override
   public void remove(Level world) {
      this.cachedSolar.clear();
      super.remove(world);
   }

   private static class RefreshListener implements NonNullConsumer<LazyOptional<IEvaporationSolar>> {
      private final WeakReference<EvaporationMultiblockData> multiblock;
      private final int corner;

      private RefreshListener(EvaporationMultiblockData multiblock, int corner) {
         this.multiblock = new WeakReference<>(multiblock);
         this.corner = corner;
      }

      public void accept(@NotNull LazyOptional<IEvaporationSolar> ignored) {
         EvaporationMultiblockData multiblockData = this.multiblock.get();
         if (multiblockData != null && multiblockData.isFormed()) {
            BlockPos maxPos = multiblockData.getMaxPos();

            BlockPos pos = switch (this.corner) {
               case 1 -> maxPos.m_122025_(3);
               case 2 -> maxPos.m_122013_(3);
               case 3 -> maxPos.m_7918_(-3, 0, -3);
               default -> maxPos;
            };
            if (WorldUtils.isBlockLoaded(multiblockData.getWorld(), pos)) {
               multiblockData.updateSolarSpot(multiblockData.getWorld(), pos, this.corner);
            }
         }
      }
   }
}

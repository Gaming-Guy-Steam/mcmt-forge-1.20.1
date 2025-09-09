package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class TileEntitySeismicVibrator extends TileEntityMekanism implements IBoundingBlock {
   public int clientPiston;
   private MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntitySeismicVibrator(BlockPos pos, BlockState state) {
      super(MekanismBlocks.SEISMIC_VIBRATOR, pos, state);
      this.cacheCoord();
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 143, 35));
      return builder.build();
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      if (this.getActive()) {
         this.clientPiston++;
      }

      this.updateActiveVibrators();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      if (MekanismUtils.canFunction(this)) {
         FloatingLong energyPerTick = this.energyContainer.getEnergyPerTick();
         if (this.energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
            this.setActive(true);
            this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
            if (this.ticker % 40 == 0) {
               this.f_58857_.m_142346_(null, MekanismGameEvents.SEISMIC_VIBRATION.get(), this.f_58858_);
            }
         } else {
            this.setActive(false);
         }
      } else {
         this.setActive(false);
      }

      this.updateActiveVibrators();
   }

   private void updateActiveVibrators() {
      if (this.getActive()) {
         Mekanism.activeVibrators.add(this.getTileCoord());
      } else {
         Mekanism.activeVibrators.remove(this.getTileCoord());
      }
   }

   @Override
   public void m_7651_() {
      super.m_7651_();
      Mekanism.activeVibrators.remove(this.getTileCoord());
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      return new AABB(this.f_58858_, this.f_58858_.m_7918_(1, 2, 1));
   }

   public MachineEnergyContainer<TileEntitySeismicVibrator> getEnergyContainer() {
      return this.energyContainer;
   }

   @ComputerMethod
   boolean isVibrating() {
      return this.getActive();
   }

   private void validateVibrating() throws ComputerException {
      if (!this.isVibrating()) {
         throw new ComputerException("Seismic Vibrator is not currently vibrating any chunks");
      }
   }

   private BlockPos getVerticalPos(int chunkRelativeX, int y, int chunkRelativeZ) throws ComputerException {
      if (chunkRelativeX < 0 || chunkRelativeX > 15) {
         throw new ComputerException("Chunk Relative X '%d' is out of range must be between 0 and 15. (Inclusive)", chunkRelativeX);
      } else if (chunkRelativeZ >= 0 && chunkRelativeZ <= 15) {
         int x = SectionPos.m_175554_(SectionPos.m_123171_(this.f_58858_.m_123341_()), chunkRelativeX);
         int z = SectionPos.m_175554_(SectionPos.m_123171_(this.f_58858_.m_123343_()), chunkRelativeZ);
         return new BlockPos(x, y, z);
      } else {
         throw new ComputerException("Chunk Relative Z '%d' is out of range must be between 0 and 15. (Inclusive)", chunkRelativeZ);
      }
   }

   @ComputerMethod
   BlockState getBlockAt(int chunkRelativeX, int y, int chunkRelativeZ) throws ComputerException {
      this.validateVibrating();
      if (this.f_58857_.m_151562_(y)) {
         throw new ComputerException(
            "Y '%d' is out of range must be between %d and %d. (Inclusive)", y, this.f_58857_.m_141937_(), this.f_58857_.m_151558_() - 1
         );
      } else {
         BlockPos targetPos = this.getVerticalPos(chunkRelativeX, y, chunkRelativeZ);
         return this.f_58857_.m_8055_(targetPos);
      }
   }

   @ComputerMethod(
      methodDescription = "Get a column info, table key is the Y level"
   )
   Map<Integer, BlockState> getColumnAt(int chunkRelativeX, int chunkRelativeZ) throws ComputerException {
      this.validateVibrating();
      Int2ObjectMap<BlockState> blocks = new Int2ObjectOpenHashMap();
      BlockPos minPos = this.getVerticalPos(chunkRelativeX, this.f_58857_.m_141937_(), chunkRelativeZ);

      for (BlockPos pos : BlockPos.m_121940_(minPos, new BlockPos(minPos.m_123341_(), this.f_58857_.m_151558_(), minPos.m_123343_()))) {
         blocks.put(pos.m_123342_(), this.f_58857_.m_8055_(pos));
      }

      return blocks;
   }
}

package mekanism.common.tile;

import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityBoundingBlock extends TileEntityUpdateable implements IUpgradeTile, Nameable {
   private BlockPos mainPos = BlockPos.f_121853_;
   private boolean receivedCoords;
   private int currentRedstoneLevel;

   public TileEntityBoundingBlock(BlockPos pos, BlockState state) {
      super(MekanismTileEntityTypes.BOUNDING_BLOCK, pos, state);
   }

   public void setMainLocation(BlockPos pos) {
      this.receivedCoords = pos != null;
      if (!this.isRemote()) {
         this.mainPos = pos;
         this.sendUpdatePacket();
      }
   }

   public boolean hasReceivedCoords() {
      return this.receivedCoords;
   }

   public BlockPos getMainPos() {
      if (this.mainPos == null) {
         this.mainPos = BlockPos.f_121853_;
      }

      return this.mainPos;
   }

   @Nullable
   public BlockEntity getMainTile() {
      return this.receivedCoords ? WorldUtils.getTileEntity(this.f_58857_, this.getMainPos()) : null;
   }

   @Nullable
   private IBoundingBlock getMain() {
      BlockEntity tile = this.getMainTile();
      if (tile != null && !(tile instanceof IBoundingBlock)) {
         Mekanism.logger.error("Found tile {} instead of an IBoundingBlock, at {}. Multiblock cannot function", tile, this.getMainPos());
         return null;
      } else {
         return (IBoundingBlock)tile;
      }
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
      IBoundingBlock main = this.getMain();
      return main == null ? super.getCapability(capability, side) : main.getOffsetCapability(capability, side, this.f_58858_.m_121996_(this.getMainPos()));
   }

   public boolean m_7531_(int id, int param) {
      boolean handled = super.m_7531_(id, param);
      IBoundingBlock main = this.getMain();
      return main != null && main.triggerBoundingEvent(this.f_58858_.m_121996_(this.getMainPos()), id, param) || handled;
   }

   public void onNeighborChange(Block block, BlockPos neighborPos) {
      if (!this.isRemote()) {
         int power = this.f_58857_.m_277086_(this.m_58899_());
         if (this.currentRedstoneLevel != power) {
            IBoundingBlock main = this.getMain();
            if (main != null) {
               main.onBoundingBlockPowerChange(this.f_58858_, this.currentRedstoneLevel, power);
            }

            this.currentRedstoneLevel = power;
         }
      }
   }

   public int getComparatorSignal() {
      IBoundingBlock main = this.getMain();
      return main != null && main.supportsComparator() ? main.getBoundingComparatorSignal(this.f_58858_.m_121996_(this.getMainPos())) : 0;
   }

   @Override
   public boolean supportsUpgrades() {
      IBoundingBlock main = this.getMain();
      return main != null && main.supportsUpgrades();
   }

   @Override
   public TileComponentUpgrade getComponent() {
      IBoundingBlock main = this.getMain();
      return main != null && main.supportsUpgrades() ? main.getComponent() : null;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgradeType) {
      IBoundingBlock main = this.getMain();
      if (main != null && main.supportsUpgrades()) {
         main.recalculateUpgrades(upgradeType);
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      NBTUtils.setBlockPosIfPresent(nbt, "main", pos -> this.mainPos = pos);
      this.currentRedstoneLevel = nbt.m_128451_("redstone");
      this.receivedCoords = nbt.m_128471_("receivedCoords");
   }

   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      if (this.receivedCoords) {
         nbtTags.m_128365_("main", NbtUtils.m_129224_(this.getMainPos()));
      }

      nbtTags.m_128405_("redstone", this.currentRedstoneLevel);
      nbtTags.m_128379_("receivedCoords", this.receivedCoords);
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      if (this.receivedCoords) {
         updateTag.m_128365_("main", NbtUtils.m_129224_(this.getMainPos()));
      }

      updateTag.m_128405_("redstone", this.currentRedstoneLevel);
      updateTag.m_128379_("receivedCoords", this.receivedCoords);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setBlockPosIfPresent(tag, "main", pos -> this.mainPos = pos);
      this.currentRedstoneLevel = tag.m_128451_("redstone");
      this.receivedCoords = tag.m_128471_("receivedCoords");
   }

   public boolean m_8077_() {
      return this.getMainTile() instanceof Nameable mainTile && mainTile.m_8077_();
   }

   @NotNull
   public Component m_7755_() {
      return this.m_8077_() ? this.m_7770_() : MekanismBlocks.BOUNDING_BLOCK.getTextComponent();
   }

   @NotNull
   public Component m_5446_() {
      return this.getMainTile() instanceof Nameable mainTile ? mainTile.m_5446_() : MekanismBlocks.BOUNDING_BLOCK.getTextComponent();
   }

   @Nullable
   public Component m_7770_() {
      return this.getMainTile() instanceof Nameable mainTile ? mainTile.m_7770_() : null;
   }
}

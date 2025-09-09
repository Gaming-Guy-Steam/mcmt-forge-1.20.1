package mekanism.common.tile;

import mekanism.common.block.BlockCardboardBox;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityCardboardBox extends TileEntityUpdateable {
   public BlockCardboardBox.BlockData storedData;

   public TileEntityCardboardBox(BlockPos pos, BlockState state) {
      super(MekanismTileEntityTypes.CARDBOARD_BOX, pos, state);
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      NBTUtils.setCompoundIfPresent(nbt, "data", tag -> this.storedData = BlockCardboardBox.BlockData.read(this.f_58857_, tag));
   }

   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      if (this.storedData != null) {
         nbtTags.m_128365_("data", this.storedData.write(new CompoundTag()));
      }
   }
}

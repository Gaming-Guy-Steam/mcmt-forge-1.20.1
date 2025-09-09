package mekanism.common.tile;

import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityPersonalStorage extends TileEntityMekanism {
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void m_142292_(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
         TileEntityPersonalStorage.this.onOpen(level, pos, state);
      }

      protected void m_142289_(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
         TileEntityPersonalStorage.this.onClose(level, pos, state);
      }

      protected void m_142148_(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int oldCount, int openCount) {
         level.m_7696_(pos, state.m_60734_(), 1, openCount);
      }

      protected boolean m_142718_(@NotNull Player player) {
         return player.f_36096_ instanceof MekanismTileContainer<?> container && container.getTileEntity() == TileEntityPersonalStorage.this;
      }
   };

   protected TileEntityPersonalStorage(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      BiPredicate<ItemStack, AutomationType> canInteract = (stack, automationType) -> automationType == AutomationType.MANUAL
         || ISecurityUtils.INSTANCE.getEffectiveSecurityMode(this, this.isRemote()) == SecurityMode.PUBLIC;
      PersonalStorageManager.createSlots(builder::addSlot, canInteract, listener);
      return builder.build();
   }

   @Override
   public void open(Player player) {
      super.open(player);
      if (!this.m_58901_() && !player.m_5833_() && this.f_58857_ != null) {
         this.openersCounter.m_155452_(player, this.f_58857_, this.m_58899_(), this.m_58900_());
      }
   }

   @Override
   public void close(Player player) {
      super.close(player);
      if (!this.m_58901_() && !player.m_5833_() && this.f_58857_ != null) {
         this.openersCounter.m_155468_(player, this.f_58857_, this.m_58899_(), this.m_58900_());
      }
   }

   public void recheckOpen() {
      if (!this.m_58901_() && this.f_58857_ != null) {
         this.openersCounter.m_155476_(this.f_58857_, this.m_58899_(), this.m_58900_());
      }
   }

   protected abstract void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state);

   protected abstract void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state);

   protected abstract ResourceLocation getStat();

   @Override
   public InteractionResult openGui(Player player) {
      InteractionResult result = super.openGui(player);
      if (result.m_19077_() && !this.isRemote()) {
         player.m_36246_(Stats.f_12988_.m_12902_(this.getStat()));
         PiglinAi.m_34873_(player, true);
      }

      return result;
   }
}

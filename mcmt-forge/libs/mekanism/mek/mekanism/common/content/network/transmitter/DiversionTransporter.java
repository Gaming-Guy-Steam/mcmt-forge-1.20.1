package mekanism.common.content.network.transmitter;

import java.util.Arrays;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiversionTransporter extends LogisticalTransporterBase {
   public final DiversionTransporter.DiversionControl[] modes = new DiversionTransporter.DiversionControl[EnumUtils.DIRECTIONS.length];
   @Nullable
   private Boolean wasGettingPower;

   public DiversionTransporter(TileEntityTransmitter tile) {
      super(tile, TransporterTier.BASIC);
      Arrays.fill(this.modes, DiversionTransporter.DiversionControl.DISABLED);
   }

   @Override
   public void onNeighborBlockChange(Direction side) {
      boolean receivingPower = this.isGettingPowered();
      if (this.wasGettingPower == null || this.wasGettingPower != receivingPower) {
         this.wasGettingPower = receivingPower;
         byte current = this.getAllCurrentConnections();
         this.refreshConnections();
         if (current != this.getAllCurrentConnections()) {
            this.markDirtyTransmitters();
         }

         TileEntityTransmitter transmitterTile = this.getTransmitterTile();

         for (Direction direction : EnumUtils.DIRECTIONS) {
            if (super.exposesInsertCap(direction)) {
               if (!this.modeReqsMet(direction)) {
                  transmitterTile.invalidateCapability(ForgeCapabilities.ITEM_HANDLER, direction);
               }

               WorldUtils.notifyNeighborOfChange(transmitterTile.m_58904_(), direction, transmitterTile.getTilePos());
            }
         }
      }
   }

   private void readModes(@NotNull CompoundTag tag) {
      for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
         int index = i;
         NBTUtils.setEnumIfPresent(tag, "mode" + index, DiversionTransporter.DiversionControl::byIndexStatic, mode -> this.modes[index] = mode);
      }
   }

   @NotNull
   private CompoundTag writeModes(@NotNull CompoundTag nbtTags) {
      for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
         NBTUtils.writeEnum(nbtTags, "mode" + i, this.modes[i]);
      }

      return nbtTags;
   }

   @Override
   public void read(@NotNull CompoundTag nbtTags) {
      super.read(nbtTags);
      this.readModes(nbtTags);
   }

   @NotNull
   @Override
   public CompoundTag write(@NotNull CompoundTag nbtTags) {
      return this.writeModes(super.write(nbtTags));
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
      return this.writeModes(super.getReducedUpdateTag(updateTag));
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      this.readModes(tag);
   }

   public void updateMode(Direction side, DiversionTransporter.DiversionControl mode) {
      int ordinal = side.ordinal();
      DiversionTransporter.DiversionControl oldMode = this.modes[ordinal];
      if (oldMode != mode) {
         this.modes[ordinal] = mode;
         TileEntityTransmitter transmitterTile = this.getTransmitterTile();
         if (super.exposesInsertCap(side)) {
            boolean nowExposes = this.modeReqsMet(mode);
            if (nowExposes != this.modeReqsMet(oldMode)) {
               if (!nowExposes) {
                  transmitterTile.invalidateCapability(ForgeCapabilities.ITEM_HANDLER, side);
               }

               WorldUtils.notifyNeighborOfChange(transmitterTile.m_58904_(), side, transmitterTile.getTilePos());
            }
         }

         this.refreshConnections();
         this.notifyTileChange();
         transmitterTile.sendUpdatePacket();
      }
   }

   @Override
   public InteractionResult onRightClick(Player player, Direction side) {
      side = this.getTransmitterTile().getSideLookingAt(player, side);
      DiversionTransporter.DiversionControl newMode = this.modes[side.ordinal()].getNext();
      this.updateMode(side, newMode);
      player.m_5661_(MekanismLang.TOGGLE_DIVERTER.translateColored(EnumColor.GRAY, new Object[]{EnumColor.RED, newMode}), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public boolean exposesInsertCap(@NotNull Direction side) {
      return super.exposesInsertCap(side) && this.modeReqsMet(side);
   }

   @Override
   public boolean canConnect(Direction side) {
      return super.canConnect(side) && this.modeReqsMet(side);
   }

   private boolean modeReqsMet(Direction side) {
      return this.modeReqsMet(this.modes[side.ordinal()]);
   }

   private boolean modeReqsMet(DiversionTransporter.DiversionControl mode) {
      return switch (mode) {
         case HIGH -> this.isGettingPowered();
         case LOW -> !this.isGettingPowered();
         default -> true;
      };
   }

   private boolean isGettingPowered() {
      return WorldUtils.isGettingPowered(this.getTileWorld(), this.getTilePos());
   }

   @NothingNullByDefault
   public static enum DiversionControl implements IIncrementalEnum<DiversionTransporter.DiversionControl>, IHasTextComponent {
      DISABLED(MekanismLang.DIVERSION_CONTROL_DISABLED),
      HIGH(MekanismLang.DIVERSION_CONTROL_HIGH),
      LOW(MekanismLang.DIVERSION_CONTROL_LOW);

      private static final DiversionTransporter.DiversionControl[] MODES = values();
      private final ILangEntry langEntry;

      private DiversionControl(ILangEntry langEntry) {
         this.langEntry = langEntry;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translate();
      }

      public DiversionTransporter.DiversionControl byIndex(int index) {
         return byIndexStatic(index);
      }

      public static DiversionTransporter.DiversionControl byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}

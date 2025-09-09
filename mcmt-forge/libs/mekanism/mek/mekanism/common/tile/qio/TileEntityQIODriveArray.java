package mekanism.common.tile.qio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

public class TileEntityQIODriveArray extends TileEntityQIOComponent implements IQIODriveHolder {
   public static final ModelProperty<byte[]> DRIVE_STATUS_PROPERTY = new ModelProperty();
   public static final int DRIVE_SLOTS = 12;
   private List<IInventorySlot> driveSlots;
   private byte[] driveStatus = new byte[12];
   private int prevDriveHash = -1;

   public TileEntityQIODriveArray(BlockPos pos, BlockState state) {
      super(MekanismBlocks.QIO_DRIVE_ARRAY, pos, state);
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      int xSize = 176;
      this.driveSlots = new ArrayList<>();

      for (int y = 0; y < 2; y++) {
         for (int x = 0; x < 6; x++) {
            QIODriveSlot slot = new QIODriveSlot(this, y * 6 + x, listener, 34 + x * 18, 70 + y * 18);
            this.driveSlots.add(slot);
            builder.addSlot(slot);
         }
      }

      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.f_58857_.m_46467_() % 10L == 0L) {
         QIOFrequency frequency = this.getQIOFrequency();

         for (int i = 0; i < 12; i++) {
            QIODriveSlot slot = (QIODriveSlot)this.driveSlots.get(i);
            QIODriveData data = frequency == null ? null : frequency.getDriveData(slot.getKey());
            if (frequency != null && data != null) {
               if (data.getTotalCount() == data.getCountCapacity()) {
                  this.setDriveStatus(i, TileEntityQIODriveArray.DriveStatus.FULL);
               } else if (data.getTotalTypes() != data.getTypeCapacity() && !(data.getTotalCount() >= data.getCountCapacity() * 0.75)) {
                  this.setDriveStatus(i, TileEntityQIODriveArray.DriveStatus.READY);
               } else {
                  this.setDriveStatus(i, TileEntityQIODriveArray.DriveStatus.NEAR_FULL);
               }
            } else {
               this.setDriveStatus(i, slot.isEmpty() ? TileEntityQIODriveArray.DriveStatus.NONE : TileEntityQIODriveArray.DriveStatus.OFFLINE);
            }
         }

         int newHash = Arrays.hashCode(this.driveStatus);
         if (newHash != this.prevDriveHash) {
            this.sendUpdatePacket();
            this.prevDriveHash = newHash;
         }
      }
   }

   private void setDriveStatus(int slot, TileEntityQIODriveArray.DriveStatus status) {
      this.driveStatus[slot] = status.status();
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      QIOFrequency freq = this.getQIOFrequency();
      if (freq != null) {
         freq.saveAll();
      }

      super.m_183515_(nbtTags);
   }

   @NotNull
   public ModelData getModelData() {
      return ModelData.builder().with(DRIVE_STATUS_PROPERTY, this.driveStatus).build();
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128382_("drives", this.driveStatus);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      byte[] status = tag.m_128463_("drives");
      if (!Arrays.equals(status, this.driveStatus)) {
         this.driveStatus = status;
         this.updateModelData();
      }
   }

   @Override
   public void onDataUpdate() {
      this.markForSave();
   }

   @Override
   public List<IInventorySlot> getDriveSlots() {
      return this.driveSlots;
   }

   @ComputerMethod
   int getSlotCount() {
      return 12;
   }

   private void validateSlot(int slot) throws ComputerException {
      int slots = this.getSlotCount();
      if (slot < 0 || slot >= slots) {
         throw new ComputerException("Slot: '%d' is out of bounds, as this QIO drive array only has '%d' drive slots (zero indexed).", slot, slots);
      }
   }

   @ComputerMethod
   ItemStack getDrive(int slot) throws ComputerException {
      this.validateSlot(slot);
      return this.driveSlots.get(slot).getStack();
   }

   @ComputerMethod
   TileEntityQIODriveArray.DriveStatus getDriveStatus(int slot) throws ComputerException {
      this.validateSlot(slot);
      return TileEntityQIODriveArray.DriveStatus.byIndexStatic(this.driveStatus[slot]);
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   long getFrequencyItemCount() throws ComputerException {
      return this.computerGetFrequency().getTotalItemCount();
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   long getFrequencyItemCapacity() throws ComputerException {
      return this.computerGetFrequency().getTotalItemCountCapacity();
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   double getFrequencyItemPercentage() throws ComputerException {
      QIOFrequency frequency = this.computerGetFrequency();
      return (double)frequency.getTotalItemCount() / frequency.getTotalItemCountCapacity();
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   long getFrequencyItemTypeCount() throws ComputerException {
      return this.computerGetFrequency().getTotalItemTypes(false);
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   long getFrequencyItemTypeCapacity() throws ComputerException {
      return this.computerGetFrequency().getTotalItemTypeCapacity();
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   double getFrequencyItemTypePercentage() throws ComputerException {
      QIOFrequency frequency = this.computerGetFrequency();
      return (double)frequency.getTotalItemTypes(false) / frequency.getTotalItemTypeCapacity();
   }

   public static enum DriveStatus {
      NONE(null),
      OFFLINE(Mekanism.rl("block/qio_drive/qio_drive_offline")),
      READY(Mekanism.rl("block/qio_drive/qio_drive_empty")),
      NEAR_FULL(Mekanism.rl("block/qio_drive/qio_drive_partial")),
      FULL(Mekanism.rl("block/qio_drive/qio_drive_full"));

      private final ResourceLocation model;
      public static final TileEntityQIODriveArray.DriveStatus[] STATUSES = values();

      private DriveStatus(ResourceLocation model) {
         this.model = model;
      }

      public int ledIndex() {
         return this.ordinal() - READY.ordinal();
      }

      public ResourceLocation getModel() {
         return this.model;
      }

      public byte status() {
         return (byte)this.ordinal();
      }

      public static TileEntityQIODriveArray.DriveStatus byIndexStatic(int index) {
         return MathUtils.getByIndexMod(STATUSES, index);
      }
   }
}

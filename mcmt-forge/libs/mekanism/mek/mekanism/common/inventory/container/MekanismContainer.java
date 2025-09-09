package mekanism.common.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortUnaryOperator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.IHasExtraData;
import mekanism.common.inventory.container.slot.IInsertableSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBlockPos;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableRegistryEntry;
import mekanism.common.inventory.container.sync.SyncableShort;
import mekanism.common.inventory.container.sync.chemical.SyncableChemicalStack;
import mekanism.common.inventory.container.sync.chemical.SyncableGasStack;
import mekanism.common.inventory.container.sync.chemical.SyncableInfusionStack;
import mekanism.common.inventory.container.sync.chemical.SyncablePigmentStack;
import mekanism.common.inventory.container.sync.chemical.SyncableSlurryStack;
import mekanism.common.inventory.container.sync.list.SyncableList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.to_client.container.PacketUpdateContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_server.PacketWindowSelect;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismContainer extends AbstractContainerMenu implements ISecurityContainer {
   public static final int BASE_Y_OFFSET = 84;
   public static final int TRANSPORTER_CONFIG_WINDOW = 0;
   public static final int SIDE_CONFIG_WINDOW = 1;
   public static final int UPGRADE_WINDOW = 2;
   public static final int SKIN_SELECT_WINDOW = 3;
   protected final Inventory inv;
   protected final List<InventoryContainerSlot> inventoryContainerSlots = new ArrayList<>();
   protected final List<ArmorSlot> armorSlots = new ArrayList<>();
   protected final List<MainInventorySlot> mainInventorySlots = new ArrayList<>();
   protected final List<HotBarSlot> hotBarSlots = new ArrayList<>();
   protected final List<OffhandSlot> offhandSlots = new ArrayList<>();
   private final List<ISyncableData> trackedData = new ArrayList<>();
   private final Map<Object, List<ISyncableData>> specificTrackedData = new Object2ObjectOpenHashMap();
   @Nullable
   protected SelectedWindowData selectedWindow;
   private Map<UUID, SelectedWindowData> selectedWindows;

   protected MekanismContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv) {
      super(type.get(), id);
      this.inv = inv;
      if (!this.isRemote()) {
         this.selectedWindows = new HashMap<>(1);
      }
   }

   public boolean isRemote() {
      return this.inv.f_35978_.m_9236_().f_46443_;
   }

   public UUID getPlayerUUID() {
      return this.inv.f_35978_.m_20148_();
   }

   @NotNull
   protected Slot m_38897_(@NotNull Slot slot) {
      super.m_38897_(slot);
      if (slot instanceof IHasExtraData hasExtraData) {
         hasExtraData.addTrackers(this.inv.f_35978_, this::track);
      }

      if (slot instanceof InventoryContainerSlot inventorySlot) {
         this.inventoryContainerSlots.add(inventorySlot);
      } else if (slot instanceof ArmorSlot armorSlot) {
         this.armorSlots.add(armorSlot);
      } else if (slot instanceof MainInventorySlot inventorySlot) {
         this.mainInventorySlots.add(inventorySlot);
      } else if (slot instanceof HotBarSlot hotBarSlot) {
         this.hotBarSlots.add(hotBarSlot);
      } else if (slot instanceof OffhandSlot offhandSlot) {
         this.offhandSlots.add(offhandSlot);
      }

      return slot;
   }

   protected void addSlotsAndOpen() {
      this.addSlots();
      this.addInventorySlots(this.inv);
      this.openInventory(this.inv);
   }

   public void startTrackingServer(Object key, MekanismContainer.ISpecificContainerTracker tracker) {
      int currentSize = this.trackedData.size();
      List<ISyncableData> list = this.startTracking(key, tracker);
      this.sendInitialDataToRemote(list, index -> (short)(index + currentSize));
   }

   public List<ISyncableData> startTracking(Object key, MekanismContainer.ISpecificContainerTracker tracker) {
      List<ISyncableData> list = tracker.getSpecificSyncableData();

      for (ISyncableData data : list) {
         this.track(data);
      }

      this.specificTrackedData.put(key, list);
      return list;
   }

   public void stopTracking(Object key) {
      List<ISyncableData> list = this.specificTrackedData.remove(key);
      if (list != null) {
         this.trackedData.removeAll(list);
      }
   }

   public boolean m_5882_(@NotNull ItemStack stack, @NotNull Slot slot) {
      if (!(slot instanceof IInsertableSlot insertableSlot)) {
         return super.m_5882_(stack, slot);
      } else if (!insertableSlot.canMergeWith(stack)) {
         return false;
      } else {
         SelectedWindowData selectedWindow = this.isRemote() ? this.getSelectedWindow() : this.getSelectedWindow(this.getPlayerUUID());
         return insertableSlot.exists(selectedWindow) && super.m_5882_(stack, slot);
      }
   }

   public void m_6877_(@NotNull Player player) {
      super.m_6877_(player);
      this.closeInventory(player);
   }

   protected void closeInventory(@NotNull Player player) {
      if (!player.m_9236_().m_5776_()) {
         this.clearSelectedWindow(player.m_20148_());
      }
   }

   protected void openInventory(@NotNull Inventory inv) {
   }

   protected int getInventoryYOffset() {
      return 84;
   }

   protected int getInventoryXOffset() {
      return 8;
   }

   protected void addInventorySlots(@NotNull Inventory inv) {
      if (!(this instanceof IEmptyContainer)) {
         int yOffset = this.getInventoryYOffset();
         int xOffset = this.getInventoryXOffset();

         for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
               this.m_38897_(new MainInventorySlot(inv, Inventory.m_36059_() + slotX + slotY * 9, xOffset + slotX * 18, yOffset + slotY * 18));
            }
         }

         yOffset += 58;

         for (int slotX = 0; slotX < Inventory.m_36059_(); slotX++) {
            this.m_38897_(this.createHotBarSlot(inv, slotX, xOffset + slotX * 18, yOffset));
         }
      }
   }

   protected void addArmorSlots(@NotNull Inventory inv, int x, int y, int offhandOffset) {
      for (int index = 0; index < inv.f_35975_.size(); index++) {
         EquipmentSlot slotType = EnumUtils.EQUIPMENT_SLOT_TYPES[2 + inv.f_35975_.size() - index - 1];
         this.m_38897_(new ArmorSlot(inv, 36 + inv.f_35975_.size() - index - 1, x, y, slotType));
         y += 18;
      }

      if (offhandOffset != -1) {
         this.m_38897_(new OffhandSlot(inv, 40, x, y + offhandOffset));
      }
   }

   protected HotBarSlot createHotBarSlot(@NotNull Inventory inv, int index, int x, int y) {
      return new HotBarSlot(inv, index, x, y);
   }

   protected void addSlots() {
   }

   public List<InventoryContainerSlot> getInventoryContainerSlots() {
      return Collections.unmodifiableList(this.inventoryContainerSlots);
   }

   public List<MainInventorySlot> getMainInventorySlots() {
      return Collections.unmodifiableList(this.mainInventorySlots);
   }

   public List<HotBarSlot> getHotBarSlots() {
      return Collections.unmodifiableList(this.hotBarSlots);
   }

   @NotNull
   public ItemStack m_7648_(@NotNull Player player, int slotID) {
      Slot currentSlot = (Slot)this.f_38839_.get(slotID);
      if (currentSlot != null && currentSlot.m_6657_()) {
         SelectedWindowData selectedWindow = player.m_9236_().f_46443_ ? this.getSelectedWindow() : this.getSelectedWindow(player.m_20148_());
         if (currentSlot instanceof IInsertableSlot insertableSlot && !insertableSlot.exists(selectedWindow)) {
            return ItemStack.f_41583_;
         } else {
            ItemStack slotStack = currentSlot.m_7993_();
            ItemStack stackToInsert = slotStack;
            if (currentSlot instanceof InventoryContainerSlot) {
               if (slotStack.m_41613_() > slotStack.m_41741_()) {
                  stackToInsert = slotStack = slotStack.m_255036_(slotStack.m_41741_());
               }

               stackToInsert = insertItem(this.armorSlots, stackToInsert, true, selectedWindow);
               stackToInsert = insertItem(this.hotBarSlots, stackToInsert, true, selectedWindow);
               stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, true, selectedWindow);
               stackToInsert = insertItem(this.armorSlots, stackToInsert, false, selectedWindow);
               stackToInsert = insertItem(this.hotBarSlots, stackToInsert, false, selectedWindow);
               stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, false, selectedWindow);
            } else {
               stackToInsert = insertItem(this.inventoryContainerSlots, slotStack, true, selectedWindow);
               if (slotStack.m_41613_() == stackToInsert.m_41613_()) {
                  stackToInsert = insertItem(this.inventoryContainerSlots, stackToInsert, false, selectedWindow);
                  if (slotStack.m_41613_() == stackToInsert.m_41613_()) {
                     if (currentSlot instanceof ArmorSlot || currentSlot instanceof OffhandSlot) {
                        stackToInsert = insertItem(this.hotBarSlots, stackToInsert, true, selectedWindow);
                        stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, true, selectedWindow);
                        stackToInsert = insertItem(this.hotBarSlots, stackToInsert, false, selectedWindow);
                        stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, false, selectedWindow);
                     } else if (currentSlot instanceof MainInventorySlot) {
                        stackToInsert = insertItem(this.armorSlots, stackToInsert, false, selectedWindow);
                        stackToInsert = insertItem(this.hotBarSlots, stackToInsert, selectedWindow);
                     } else if (currentSlot instanceof HotBarSlot) {
                        stackToInsert = insertItem(this.armorSlots, stackToInsert, false, selectedWindow);
                        stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, selectedWindow);
                     }
                  }
               }
            }

            return stackToInsert.m_41613_() == slotStack.m_41613_() ? ItemStack.f_41583_ : this.transferSuccess(currentSlot, player, slotStack, stackToInsert);
         }
      } else {
         return ItemStack.f_41583_;
      }
   }

   public static <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(
      List<SLOT> slots, @NotNull ItemStack stack, @Nullable SelectedWindowData selectedWindow
   ) {
      stack = insertItem(slots, stack, true, selectedWindow);
      return insertItem(slots, stack, false, selectedWindow);
   }

   public static <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(
      List<SLOT> slots, @NotNull ItemStack stack, boolean ignoreEmpty, @Nullable SelectedWindowData selectedWindow
   ) {
      return insertItem(slots, stack, ignoreEmpty, selectedWindow, Action.EXECUTE);
   }

   @NotNull
   public static <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(
      List<SLOT> slots, @NotNull ItemStack stack, boolean ignoreEmpty, @Nullable SelectedWindowData selectedWindow, Action action
   ) {
      return insertItem(slots, stack, ignoreEmpty, false, selectedWindow, action);
   }

   @NotNull
   public static <SLOT extends Slot & IInsertableSlot> ItemStack insertItemCheckAll(
      List<SLOT> slots, @NotNull ItemStack stack, @Nullable SelectedWindowData selectedWindow, Action action
   ) {
      return insertItem(slots, stack, false, true, selectedWindow, action);
   }

   @NotNull
   public static <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(
      List<SLOT> slots, @NotNull ItemStack stack, boolean ignoreEmpty, boolean checkAll, @Nullable SelectedWindowData selectedWindow, Action action
   ) {
      if (stack.m_41619_()) {
         return stack;
      } else {
         for (SLOT slot : slots) {
            if ((checkAll || ignoreEmpty == slot.m_6657_()) && slot.exists(selectedWindow)) {
               stack = slot.insertItem(stack, action);
               if (stack.m_41619_()) {
                  break;
               }
            }
         }

         return stack;
      }
   }

   @NotNull
   protected ItemStack transferSuccess(@NotNull Slot currentSlot, @NotNull Player player, @NotNull ItemStack slotStack, @NotNull ItemStack stackToInsert) {
      int difference = slotStack.m_41613_() - stackToInsert.m_41613_();
      ItemStack newStack = currentSlot.m_6201_(difference);
      currentSlot.m_142406_(player, newStack);
      return newStack;
   }

   @Nullable
   public SelectedWindowData getSelectedWindow() {
      return this.selectedWindow;
   }

   @Nullable
   public SelectedWindowData getSelectedWindow(UUID player) {
      return this.selectedWindows.get(player);
   }

   public void setSelectedWindow(@Nullable SelectedWindowData selectedWindow) {
      if (!Objects.equals(this.selectedWindow, selectedWindow)) {
         this.selectedWindow = selectedWindow;
         Mekanism.packetHandler().sendToServer(new PacketWindowSelect(this.selectedWindow));
      }
   }

   public void setSelectedWindow(UUID player, @Nullable SelectedWindowData selectedWindow) {
      if (selectedWindow == null) {
         this.clearSelectedWindow(player);
      } else {
         this.selectedWindows.put(player, selectedWindow);
      }
   }

   private void clearSelectedWindow(UUID player) {
      this.selectedWindows.remove(player);
   }

   public void track(ISyncableData data) {
      this.trackedData.add(data);
   }

   @NotNull
   protected DataSlot m_38895_(@NotNull DataSlot referenceHolder) {
      this.track(SyncableInt.create(referenceHolder::m_6501_, referenceHolder::m_6422_));
      return referenceHolder;
   }

   public void trackArray(boolean[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableBoolean.create(arrayIn, i));
      }
   }

   public void trackArray(byte[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableByte.create(arrayIn, i));
      }
   }

   public void trackArray(double[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableDouble.create(arrayIn, i));
      }
   }

   public void trackArray(float[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableFloat.create(arrayIn, i));
      }
   }

   public void trackArray(int[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableInt.create(arrayIn, i));
      }
   }

   public void trackArray(long[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableLong.create(arrayIn, i));
      }
   }

   public void trackArray(short[] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         this.track(SyncableShort.create(arrayIn, i));
      }
   }

   public void trackArray(boolean[][] arrayIn) {
      for (int i = 0; i < arrayIn.length; i++) {
         for (int j = 0; j < arrayIn[i].length; j++) {
            this.track(SyncableBoolean.create(arrayIn, i, j));
         }
      }
   }

   @Nullable
   private ISyncableData getTrackedData(short property) {
      if (property >= 0 && property < this.trackedData.size()) {
         return this.trackedData.get(property);
      } else {
         Mekanism.logger
            .warn(
               "Received out of bounds window property {} for container {}. There are currently {} tracked properties.",
               new Object[]{property, RegistryUtils.getName(this.m_6772_()), this.trackedData.size()}
            );
         return null;
      }
   }

   public void handleWindowProperty(short property, boolean value) {
      if (this.getTrackedData(property) instanceof SyncableBoolean syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, byte value) {
      if (this.getTrackedData(property) instanceof SyncableByte syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, short value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableShort syncable) {
         syncable.set(value);
      } else if (data instanceof SyncableFloatingLong syncable) {
         syncable.setDecimal(value);
      }
   }

   public void handleWindowProperty(short property, int value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableInt syncable) {
         syncable.set(value);
      } else if (data instanceof SyncableEnum<?> syncable) {
         syncable.set(value);
      } else if (data instanceof SyncableFluidStack syncable) {
         syncable.set(value);
      } else if (data instanceof SyncableItemStack syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, long value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableLong syncable) {
         syncable.set(value);
      } else if (data instanceof SyncableChemicalStack<?, ?> syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, float value) {
      if (this.getTrackedData(property) instanceof SyncableFloat syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, double value) {
      if (this.getTrackedData(property) instanceof SyncableDouble syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, @NotNull ItemStack value) {
      if (this.getTrackedData(property) instanceof SyncableItemStack syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, @NotNull FluidStack value) {
      if (this.getTrackedData(property) instanceof SyncableFluidStack syncable) {
         syncable.set(value);
      }
   }

   public void handleWindowProperty(short property, @Nullable BlockPos value) {
      if (this.getTrackedData(property) instanceof SyncableBlockPos syncable) {
         syncable.set(value);
      }
   }

   public <V> void handleWindowProperty(short property, @NotNull V value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableRegistryEntry) {
         ((SyncableRegistryEntry)data).set(value);
      }
   }

   public <STACK extends ChemicalStack<?>> void handleWindowProperty(short property, @NotNull STACK value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableGasStack syncable && value instanceof GasStack stack) {
         syncable.set(stack);
      } else if (data instanceof SyncableInfusionStack syncable && value instanceof InfusionStack stack) {
         syncable.set(stack);
      } else if (data instanceof SyncablePigmentStack syncable && value instanceof PigmentStack stack) {
         syncable.set(stack);
      } else if (data instanceof SyncableSlurryStack syncable && value instanceof SlurryStack stack) {
         syncable.set(stack);
      }
   }

   public <FREQUENCY extends Frequency> void handleWindowProperty(short property, @Nullable FREQUENCY value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableFrequency) {
         ((SyncableFrequency)data).set(value);
      }
   }

   public void handleWindowProperty(short property, @NotNull FloatingLong value) {
      if (this.getTrackedData(property) instanceof SyncableFloatingLong syncable) {
         syncable.set(value);
      }
   }

   public <TYPE> void handleWindowProperty(short property, @NotNull List<TYPE> value) {
      ISyncableData data = this.getTrackedData(property);
      if (data instanceof SyncableList) {
         ((SyncableList)data).set(value);
      }
   }

   public void m_38946_() {
      super.m_38946_();
      if (this.inv.f_35978_ instanceof ServerPlayer player) {
         List<PropertyData> dirtyData = new ArrayList<>();

         for (short i = 0; i < this.trackedData.size(); i++) {
            ISyncableData data = this.trackedData.get(i);
            ISyncableData.DirtyType dirtyType = data.isDirty();
            if (dirtyType != ISyncableData.DirtyType.CLEAN) {
               dirtyData.add(data.getPropertyData(i, dirtyType));
            }
         }

         if (!dirtyData.isEmpty()) {
            Mekanism.packetHandler().sendTo(new PacketUpdateContainer((short)this.f_38840_, dirtyData), player);
         }
      }
   }

   public void m_150429_() {
      super.m_150429_();
      this.sendInitialDataToRemote(this.trackedData, ShortUnaryOperator.identity());
   }

   private void sendInitialDataToRemote(List<ISyncableData> syncableData, ShortUnaryOperator propertyIndex) {
      if (this.inv.f_35978_ instanceof ServerPlayer player) {
         List<PropertyData> dirtyData = new ArrayList<>();

         for (short i = 0; i < syncableData.size(); i++) {
            ISyncableData data = syncableData.get(i);
            data.isDirty();
            dirtyData.add(data.getPropertyData(propertyIndex.apply(i), ISyncableData.DirtyType.DIRTY));
         }

         if (!dirtyData.isEmpty()) {
            Mekanism.packetHandler().sendTo(new PacketUpdateContainer((short)this.f_38840_, dirtyData), player);
         }
      }
   }

   public interface ISpecificContainerTracker {
      List<ISyncableData> getSpecificSyncableData();
   }
}

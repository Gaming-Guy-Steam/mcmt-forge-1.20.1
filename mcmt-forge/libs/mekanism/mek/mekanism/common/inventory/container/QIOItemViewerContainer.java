package mekanism.common.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.math.MathUtils;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingTransferHelper;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.SearchQueryParser;
import mekanism.common.inventory.GuiComponents;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.VirtualCraftingOutputSlot;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.to_server.PacketGuiItemDataRequest;
import mekanism.common.network.to_server.PacketQIOItemViewerSlotInteract;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class QIOItemViewerContainer extends MekanismContainer implements ISlotClickHandler {
   public static final int SLOTS_X_MIN = 8;
   public static final int SLOTS_X_MAX = 16;
   public static final int SLOTS_Y_MIN = 2;
   public static final int SLOTS_Y_MAX = 48;
   public static final int SLOTS_START_Y = 43;
   private static final int DOUBLE_CLICK_TRANSFER_DURATION = 20;
   private QIOItemViewerContainer.ListSortType sortType;
   private QIOItemViewerContainer.SortDirection sortDirection;
   private Object2LongMap<HashedItem.UUIDAwareHashedItem> cachedInventory = new Object2LongOpenHashMap();
   private long cachedCountCapacity;
   private int cachedTypeCapacity;
   private long totalItems;
   @Nullable
   private List<ISlotClickHandler.IScrollableSlot> itemList;
   @Nullable
   private List<ISlotClickHandler.IScrollableSlot> searchList;
   private Map<String, List<ISlotClickHandler.IScrollableSlot>> searchCache = new Object2ObjectOpenHashMap();
   private String searchQuery = "";
   private int doubleClickTransferTicks = 0;
   private int lastSlot = -1;
   private ItemStack lastStack = ItemStack.f_41583_;
   private List<InventoryContainerSlot>[] craftingGridInputSlots;
   protected final IQIOCraftingWindowHolder craftingWindowHolder;
   private final VirtualInventoryContainerSlot[][] craftingSlots = new VirtualInventoryContainerSlot[3][10];

   public static int getSlotsYMax() {
      int maxY = (int)Math.ceil(Minecraft.m_91087_().m_91268_().m_85446_() * 0.05 - 8.0) + 1;
      return Mth.m_14045_(maxY, 2, 48);
   }

   protected QIOItemViewerContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder) {
      super(type, id, inv);
      this.craftingWindowHolder = craftingWindowHolder;
      if (craftingWindowHolder == null) {
         Mekanism.logger.error("Error getting crafting window holder, closing.");
         this.closeInventory(inv.f_35978_);
      } else {
         if (remote) {
            this.sortType = MekanismConfig.client.qioItemViewerSortType.get();
            this.sortDirection = MekanismConfig.client.qioItemViewerSortDirection.get();
            int maxY = getSlotsYMax();
            if (MekanismConfig.client.qioItemViewerSlotsY.get() > maxY) {
               MekanismConfig.client.qioItemViewerSlotsY.set(maxY);
               MekanismConfig.client.save();
            }
         } else {
            this.sortType = QIOItemViewerContainer.ListSortType.NAME;
            this.sortDirection = QIOItemViewerContainer.SortDirection.ASCENDING;
            this.craftingGridInputSlots = new List[3];
         }
      }
   }

   @Nullable
   public QIOFrequency getFrequency() {
      return this.craftingWindowHolder.getFrequency();
   }

   public abstract QIOItemViewerContainer recreate();

   protected void sync(QIOItemViewerContainer container) {
      container.sortType = this.sortType;
      container.cachedInventory = this.cachedInventory;
      container.cachedCountCapacity = this.cachedCountCapacity;
      container.cachedTypeCapacity = this.cachedTypeCapacity;
      container.totalItems = this.totalItems;
      container.itemList = this.itemList;
      container.searchList = this.searchList;
      container.searchCache = this.searchCache;
      container.searchQuery = this.searchQuery;
      container.selectedWindow = this.getSelectedWindow();
   }

   @Override
   protected int getInventoryYOffset() {
      return 43 + MekanismConfig.client.qioItemViewerSlotsY.getOrDefault() * 18 + 15;
   }

   @Override
   protected int getInventoryXOffset() {
      return super.getInventoryXOffset() + (MekanismConfig.client.qioItemViewerSlotsX.getOrDefault() - 8) * 18 / 2;
   }

   @Override
   protected void addSlots() {
      super.addSlots();

      for (QIOCraftingWindow craftingWindow : this.craftingWindowHolder.getCraftingWindows()) {
         byte tableIndex = craftingWindow.getWindowIndex();

         for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            this.addCraftingSlot(craftingWindow.getInputSlot(slotIndex), tableIndex, slotIndex);
         }

         this.addCraftingSlot(craftingWindow.getOutputSlot(), tableIndex, 9);
      }
   }

   private void addCraftingSlot(CraftingWindowInventorySlot slot, byte tableIndex, int slotIndex) {
      VirtualInventoryContainerSlot containerSlot = slot.createContainerSlot();
      this.craftingSlots[tableIndex][slotIndex] = containerSlot;
      this.m_38897_(containerSlot);
   }

   public VirtualInventoryContainerSlot getCraftingWindowSlot(byte tableIndex, int slotIndex) {
      return this.craftingSlots[tableIndex][slotIndex];
   }

   @Override
   protected void openInventory(@NotNull Inventory inv) {
      super.openInventory(inv);
      if (this.isRemote()) {
         Mekanism.packetHandler().sendToServer(PacketGuiItemDataRequest.qioItemViewer());
      }
   }

   @Override
   protected void closeInventory(@NotNull Player player) {
      super.closeInventory(player);
      if (!player.m_9236_().m_5776_()) {
         QIOFrequency freq = this.getFrequency();
         if (freq != null) {
            freq.closeItemViewer((ServerPlayer)player);
         }
      }
   }

   @Override
   public void m_38946_() {
      super.m_38946_();
      if (this.doubleClickTransferTicks > 0) {
         this.doubleClickTransferTicks--;
      } else {
         this.resetTransferTracker();
      }
   }

   private void resetTransferTracker() {
      this.doubleClickTransferTicks = 0;
      this.lastSlot = -1;
      this.lastStack = ItemStack.f_41583_;
   }

   private void setTransferTracker(ItemStack stack, int slot) {
      this.doubleClickTransferTicks = 20;
      this.lastSlot = slot;
      this.lastStack = stack;
   }

   private void doDoubleClickTransfer(Player player) {
      QIOFrequency freq = this.getFrequency();
      if (freq != null) {
         Consumer<InsertableSlot> slotConsumer = slot -> {
            if (slot.m_6657_() && slot.m_8010_(player)) {
               ItemStack slotItem = slot.m_7993_();
               if (InventoryUtils.areItemsStackable(this.lastStack, slotItem)) {
                  this.transferSuccess(slot, player, slotItem, freq.addItem(slotItem));
               }
            }
         };
         this.mainInventorySlots.forEach(slotConsumer);
         this.hotBarSlots.forEach(slotConsumer);
      }
   }

   private List<InventoryContainerSlot> getCraftingGridSlots(byte selectedCraftingGrid) {
      List<InventoryContainerSlot> craftingGridSlots = this.craftingGridInputSlots[selectedCraftingGrid];
      if (craftingGridSlots == null) {
         craftingGridSlots = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            craftingGridSlots.add(this.getCraftingWindowSlot(selectedCraftingGrid, i));
         }

         this.craftingGridInputSlots[selectedCraftingGrid] = craftingGridSlots;
      }

      return craftingGridSlots;
   }

   @NotNull
   @Override
   public ItemStack m_7648_(@NotNull Player player, int slotID) {
      Slot currentSlot = (Slot)this.f_38839_.get(slotID);
      if (currentSlot == null) {
         return ItemStack.f_41583_;
      } else if (currentSlot instanceof VirtualCraftingOutputSlot virtualSlot) {
         return virtualSlot.shiftClickSlot(player, this.hotBarSlots, this.mainInventorySlots);
      } else if (currentSlot instanceof InventoryContainerSlot) {
         return super.m_7648_(player, slotID);
      } else {
         if (!player.m_9236_().m_5776_()) {
            ItemStack slotStack = currentSlot.m_7993_();
            byte selectedCraftingGrid = this.getSelectedCraftingGrid(player.m_20148_());
            if (selectedCraftingGrid != -1) {
               QIOCraftingWindow craftingWindow = this.getCraftingWindow(selectedCraftingGrid);
               if (!craftingWindow.isOutput(slotStack)) {
                  List<InventoryContainerSlot> craftingGridSlots = this.getCraftingGridSlots(selectedCraftingGrid);
                  SelectedWindowData windowData = craftingWindow.getWindowData();
                  ItemStack ret = insertItem(craftingGridSlots, slotStack, windowData);
                  if (ret.m_41613_() != slotStack.m_41613_()) {
                     return this.transferSuccess(currentSlot, player, slotStack, ret);
                  }
               }
            }

            QIOFrequency frequency = this.getFrequency();
            if (frequency != null) {
               if (!slotStack.m_41619_()) {
                  ItemStack ret = frequency.addItem(slotStack);
                  if (slotStack.m_41613_() == ret.m_41613_()) {
                     return ItemStack.f_41583_;
                  }

                  this.setTransferTracker(slotStack.m_41777_(), slotID);
                  return this.transferSuccess(currentSlot, player, slotStack, ret);
               }

               if (slotID == this.lastSlot && !this.lastStack.m_41619_()) {
                  this.doDoubleClickTransfer(player);
               }

               this.resetTransferTracker();
               return ItemStack.f_41583_;
            }
         }

         return ItemStack.f_41583_;
      }
   }

   public void handleBatchUpdate(Object2LongMap<HashedItem.UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
      this.cachedInventory = itemMap;
      this.cachedCountCapacity = countCapacity;
      this.cachedTypeCapacity = typeCapacity;
      this.syncItemList();
   }

   public void handleUpdate(Object2LongMap<HashedItem.UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
      this.cachedCountCapacity = countCapacity;
      this.cachedTypeCapacity = typeCapacity;
      if (!itemMap.isEmpty()) {
         itemMap.object2LongEntrySet().forEach(entry -> {
            long value = entry.getLongValue();
            if (value == 0L) {
               this.cachedInventory.removeLong(entry.getKey());
            } else {
               this.cachedInventory.put((HashedItem.UUIDAwareHashedItem)entry.getKey(), value);
            }
         });
         this.syncItemList();
      }
   }

   public void handleKill() {
      this.itemList = null;
      this.searchList = null;
      this.cachedInventory.clear();
   }

   public QIOCraftingTransferHelper getTransferHelper(Player player, QIOCraftingWindow craftingWindow) {
      return new QIOCraftingTransferHelper(this.cachedInventory, this.hotBarSlots, this.mainInventorySlots, craftingWindow, player);
   }

   private void syncItemList() {
      if (this.itemList == null) {
         this.itemList = new ArrayList<>();
      }

      this.itemList.clear();
      this.searchCache.clear();
      this.totalItems = 0L;
      this.cachedInventory.forEach((key, value) -> {
         this.itemList.add(new QIOItemViewerContainer.ItemSlotData(key, key.getUUID(), value));
         this.totalItems = this.totalItems + value;
      });
      this.sortItemList();
      if (!this.searchQuery.isEmpty()) {
         this.updateSearch(this.searchQuery);
      }
   }

   private void sortItemList() {
      if (this.itemList != null) {
         this.sortType.sort(this.itemList, this.sortDirection);
      }
   }

   public void setSortDirection(QIOItemViewerContainer.SortDirection sortDirection) {
      this.sortDirection = sortDirection;
      MekanismConfig.client.qioItemViewerSortDirection.set(sortDirection);
      MekanismConfig.client.save();
      this.sortItemList();
   }

   public QIOItemViewerContainer.SortDirection getSortDirection() {
      return this.sortDirection;
   }

   public void setSortType(QIOItemViewerContainer.ListSortType sortType) {
      this.sortType = sortType;
      MekanismConfig.client.qioItemViewerSortType.set(sortType);
      MekanismConfig.client.save();
      this.sortItemList();
   }

   public QIOItemViewerContainer.ListSortType getSortType() {
      return this.sortType;
   }

   @Nullable
   public List<ISlotClickHandler.IScrollableSlot> getQIOItemList() {
      return this.searchQuery.isEmpty() ? this.itemList : this.searchList;
   }

   public long getCountCapacity() {
      return this.cachedCountCapacity;
   }

   public int getTypeCapacity() {
      return this.cachedTypeCapacity;
   }

   public long getTotalItems() {
      return this.totalItems;
   }

   public int getTotalTypes() {
      return this.itemList == null ? 0 : this.itemList.size();
   }

   public byte getSelectedCraftingGrid() {
      return this.getSelectedCraftingGrid(this.getSelectedWindow());
   }

   public byte getSelectedCraftingGrid(UUID player) {
      return this.getSelectedCraftingGrid(this.getSelectedWindow(player));
   }

   private byte getSelectedCraftingGrid(@Nullable SelectedWindowData selectedWindow) {
      return selectedWindow != null && selectedWindow.type == SelectedWindowData.WindowType.CRAFTING ? selectedWindow.extraData : -1;
   }

   public QIOCraftingWindow getCraftingWindow(int selectedCraftingGrid) {
      if (selectedCraftingGrid >= 0 && selectedCraftingGrid < 3) {
         return this.craftingWindowHolder.getCraftingWindows()[selectedCraftingGrid];
      } else {
         throw new IllegalArgumentException("Selected crafting grid not in range.");
      }
   }

   public ItemStack insertIntoPlayerInventory(UUID player, ItemStack stack) {
      SelectedWindowData selectedWindow = this.getSelectedWindow(player);
      stack = insertItem(this.hotBarSlots, stack, true, selectedWindow);
      stack = insertItem(this.mainInventorySlots, stack, true, selectedWindow);
      stack = insertItem(this.hotBarSlots, stack, false, selectedWindow);
      return insertItem(this.mainInventorySlots, stack, false, selectedWindow);
   }

   public ItemStack simulateInsertIntoPlayerInventory(UUID player, ItemStack stack) {
      SelectedWindowData selectedWindow = this.getSelectedWindow(player);
      stack = insertItemCheckAll(this.hotBarSlots, stack, selectedWindow, Action.SIMULATE);
      return insertItemCheckAll(this.mainInventorySlots, stack, selectedWindow, Action.SIMULATE);
   }

   public void updateSearch(String queryText) {
      if (this.isRemote() && this.itemList != null) {
         this.searchQuery = queryText;
         this.searchList = this.searchCache.computeIfAbsent(queryText, text -> {
            List<ISlotClickHandler.IScrollableSlot> list = new ArrayList<>();
            SearchQueryParser.ISearchQuery query = SearchQueryParser.parse(text);

            for (ISlotClickHandler.IScrollableSlot slot : this.itemList) {
               if (query.matches(slot.item().getInternalStack())) {
                  list.add(slot);
               }
            }

            return list;
         });
      }
   }

   @Override
   public void onClick(Supplier<ISlotClickHandler.IScrollableSlot> slotProvider, int button, boolean hasShiftDown, ItemStack heldItem) {
      if (hasShiftDown) {
         ISlotClickHandler.IScrollableSlot slot = slotProvider.get();
         if (slot != null) {
            Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.shiftTake(slot.itemUUID()));
         }
      } else if (button == 0 || button == 1) {
         if (heldItem.m_41619_()) {
            ISlotClickHandler.IScrollableSlot slot = slotProvider.get();
            if (slot != null) {
               long baseExtract = button == 0 ? slot.count() : slot.count() / 2L;
               int toTake = Mth.m_14045_(MathUtils.clampToInt(baseExtract), 1, slot.item().getMaxStackSize());
               Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.take(slot.itemUUID(), toTake));
            }
         } else {
            int toAdd = button == 0 ? heldItem.m_41613_() : 1;
            Mekanism.packetHandler().sendToServer(PacketQIOItemViewerSlotInteract.put(toAdd));
         }
      }
   }

   private record ItemSlotData(HashedItem item, UUID itemUUID, long count) implements ISlotClickHandler.IScrollableSlot {
   }

   public static enum ListSortType implements GuiComponents.IDropdownEnum<QIOItemViewerContainer.ListSortType> {
      NAME(MekanismLang.LIST_SORT_NAME, MekanismLang.LIST_SORT_NAME_DESC, Comparator.comparing(ISlotClickHandler.IScrollableSlot::getDisplayName)),
      SIZE(
         MekanismLang.LIST_SORT_COUNT,
         MekanismLang.LIST_SORT_COUNT_DESC,
         Comparator.comparingLong(ISlotClickHandler.IScrollableSlot::count).thenComparing(ISlotClickHandler.IScrollableSlot::getDisplayName),
         Comparator.comparingLong(ISlotClickHandler.IScrollableSlot::count).reversed().thenComparing(ISlotClickHandler.IScrollableSlot::getDisplayName)
      ),
      MOD(
         MekanismLang.LIST_SORT_MOD,
         MekanismLang.LIST_SORT_MOD_DESC,
         Comparator.comparing(ISlotClickHandler.IScrollableSlot::getModID).thenComparing(ISlotClickHandler.IScrollableSlot::getDisplayName),
         Comparator.comparing(ISlotClickHandler.IScrollableSlot::getModID).reversed().thenComparing(ISlotClickHandler.IScrollableSlot::getDisplayName)
      );

      private final ILangEntry name;
      private final ILangEntry tooltip;
      private final Comparator<ISlotClickHandler.IScrollableSlot> ascendingComparator;
      private final Comparator<ISlotClickHandler.IScrollableSlot> descendingComparator;

      private ListSortType(ILangEntry name, ILangEntry tooltip, Comparator<ISlotClickHandler.IScrollableSlot> ascendingComparator) {
         this(name, tooltip, ascendingComparator, ascendingComparator.reversed());
      }

      private ListSortType(
         ILangEntry name,
         ILangEntry tooltip,
         Comparator<ISlotClickHandler.IScrollableSlot> ascendingComparator,
         Comparator<ISlotClickHandler.IScrollableSlot> descendingComparator
      ) {
         this.name = name;
         this.tooltip = tooltip;
         this.ascendingComparator = ascendingComparator;
         this.descendingComparator = descendingComparator;
      }

      public void sort(List<ISlotClickHandler.IScrollableSlot> list, QIOItemViewerContainer.SortDirection direction) {
         list.sort(direction.isAscending() ? this.ascendingComparator : this.descendingComparator);
      }

      @Override
      public Component getTooltip() {
         return this.tooltip.translate();
      }

      @Override
      public Component getShortName() {
         return this.name.translate();
      }
   }

   public static enum SortDirection implements GuiComponents.IToggleEnum<QIOItemViewerContainer.SortDirection> {
      ASCENDING(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "arrow_up.png"), MekanismLang.LIST_SORT_ASCENDING_DESC),
      DESCENDING(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "arrow_down.png"), MekanismLang.LIST_SORT_DESCENDING_DESC);

      private final ResourceLocation icon;
      private final ILangEntry tooltip;

      private SortDirection(ResourceLocation icon, ILangEntry tooltip) {
         this.icon = icon;
         this.tooltip = tooltip;
      }

      @Override
      public ResourceLocation getIcon() {
         return this.icon;
      }

      @Override
      public Component getTooltip() {
         return this.tooltip.translate();
      }

      public boolean isAscending() {
         return this == ASCENDING;
      }
   }
}

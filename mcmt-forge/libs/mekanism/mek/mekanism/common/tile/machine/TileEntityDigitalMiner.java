package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableRegistryEntry;
import mekanism.common.inventory.container.tile.DigitalMinerConfigContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityDigitalMiner
   extends TileEntityMekanism
   implements ISustainedData,
   IChunkLoader,
   IBoundingBlock,
   ITileFilterHolder<MinerFilter<?>>,
   IHasVisualization {
   public static final int DEFAULT_HEIGHT_RANGE = 60;
   public static final int DEFAULT_RADIUS = 10;
   private final SortableFilterManager<MinerFilter<?>> filterManager = new SortableFilterManager<>(MinerFilter.class, this::markForSave);
   private Long2ObjectMap<BitSet> oresToMine = Long2ObjectMaps.emptyMap();
   public ThreadMinerSearch searcher = new ThreadMinerSearch(this);
   private int radius;
   private boolean inverse;
   private boolean inverseRequiresReplacement;
   private Item inverseReplaceTarget = Items.f_41852_;
   private int minY;
   private int maxY;
   private boolean doEject;
   private boolean doPull;
   public ItemStack missingStack;
   private final Predicate<ItemStack> overflowCollector;
   private final Object2IntMap<HashedItem> overflow;
   private boolean hasOverflow;
   private boolean recheckOverflow;
   private int delay;
   private int delayLength;
   private int cachedToMine;
   private boolean silkTouch;
   private boolean running;
   private int delayTicks;
   private boolean initCalc;
   private int numPowering;
   private boolean clientRendering;
   private final TileComponentChunkLoader<TileEntityDigitalMiner> chunkLoaderComponent;
   @Nullable
   private ChunkPos targetChunk;
   private MinerEnergyContainer energyContainer;
   private List<IInventorySlot> mainSlots;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityDigitalMiner(BlockPos pos, BlockState state) {
      super(MekanismBlocks.DIGITAL_MINER, pos, state);
      this.maxY = this.minY + 60;
      this.doEject = false;
      this.doPull = false;
      this.missingStack = ItemStack.f_41583_;
      this.overflowCollector = this::trackOverflow;
      this.overflow = new Object2IntLinkedOpenHashMap();
      this.delayLength = MekanismConfig.general.minerTicksPerMine.get();
      this.initCalc = false;
      this.chunkLoaderComponent = new TileComponentChunkLoader<>(this);
      this.radius = 10;
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
      this.addDisabledCapabilities(new Capability[]{ForgeCapabilities.ITEM_HANDLER});
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MinerEnergyContainer.input(this, listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BOTTOM);
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      this.mainSlots = new ArrayList<>();
      IContentsListener mainSlotListener = () -> {
         listener.onContentsChanged();
         this.recheckOverflow = true;
      };
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection, side -> side == RelativeSide.TOP, side -> side == RelativeSide.BACK);
      BiPredicate<ItemStack, AutomationType> canInsert = (stack, automationType) -> automationType != AutomationType.EXTERNAL
         || this.isReplaceTarget(stack.m_41720_());
      BiPredicate<ItemStack, AutomationType> canExtract = (stack, automationType) -> automationType != AutomationType.EXTERNAL
         || !this.isReplaceTarget(stack.m_41720_());

      for (int slotY = 0; slotY < 3; slotY++) {
         for (int slotX = 0; slotX < 9; slotX++) {
            BasicInventorySlot slot = BasicInventorySlot.at(canExtract, canInsert, mainSlotListener, 8 + slotX * 18, 92 + slotY * 18);
            builder.addSlot(slot, RelativeSide.BACK, RelativeSide.TOP);
            this.mainSlots.add(slot);
         }
      }

      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 152, 20));
      return builder.build();
   }

   private void closeInvalidScreens() {
      if (this.getActive() && !this.playersUsing.isEmpty()) {
         ObjectIterator var1 = new ObjectOpenHashSet(this.playersUsing).iterator();

         while (var1.hasNext()) {
            Player player = (Player)var1.next();
            if (player.f_36096_ instanceof DigitalMinerConfigContainer) {
               player.m_6915_();
            }
         }
      }
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      this.closeInvalidScreens();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.closeInvalidScreens();
      if (!this.initCalc) {
         if (this.searcher.state == ThreadMinerSearch.State.FINISHED) {
            boolean prevRunning = this.running;
            this.reset();
            this.start();
            this.running = prevRunning;
         }

         this.initCalc = true;
      }

      this.energySlot.fillContainerOrConvert();
      if (this.recheckOverflow) {
         this.tryAddOverflow();
      }

      if (!this.hasOverflow
         && MekanismUtils.canFunction(this)
         && this.running
         && this.searcher.state == ThreadMinerSearch.State.FINISHED
         && !this.oresToMine.isEmpty()) {
         FloatingLong energyPerTick = this.energyContainer.getEnergyPerTick();
         if (this.energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
            this.setActive(true);
            if (this.delay > 0) {
               this.delay--;
            }

            this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
            if (this.delay == 0) {
               this.tryMineBlock();
               this.delay = this.getDelay();
            }
         } else {
            this.setActive(false);
         }
      } else {
         this.setActive(false);
      }

      if (this.doEject && this.delayTicks == 0) {
         Direction oppositeDirection = this.getOppositeDirection();
         BlockEntity ejectInv = WorldUtils.getTileEntity(this.f_58857_, this.m_58899_().m_7494_().m_5484_(oppositeDirection, 2));
         BlockEntity ejectTile = WorldUtils.getTileEntity(this.m_58904_(), this.m_58899_().m_7494_().m_121945_(oppositeDirection));
         if (ejectInv != null && ejectTile != null) {
            TransitRequest ejectMap = InventoryUtils.getEjectItemMap(ejectTile, oppositeDirection, this.mainSlots);
            if (!ejectMap.isEmpty()) {
               TransitRequest.TransitResponse response;
               if (ejectInv instanceof TileEntityLogisticalTransporterBase transporter) {
                  response = transporter.getTransmitter().insert(ejectTile, ejectMap, transporter.getTransmitter().getColor(), true, 0);
               } else {
                  response = ejectMap.addToInventory(ejectInv, oppositeDirection, 0, false);
               }

               if (!response.isEmpty()) {
                  response.useAll();
               }
            }

            this.delayTicks = 10;
         }
      } else if (this.delayTicks > 0) {
         this.delayTicks--;
      }
   }

   public void updateFromSearch(Long2ObjectMap<BitSet> oresToMine, int found) {
      this.oresToMine = oresToMine;
      this.cachedToMine = found;
      this.updateTargetChunk(null);
      this.markForSave();
   }

   public int getDelay() {
      return this.delayLength;
   }

   @ComputerMethod(
      methodDescription = "Whether Silk Touch mode is enabled or not"
   )
   public boolean getSilkTouch() {
      return this.silkTouch;
   }

   @ComputerMethod(
      methodDescription = "Get the current radius configured (blocks)"
   )
   public int getRadius() {
      return this.radius;
   }

   @ComputerMethod(
      methodDescription = "Gets the configured minimum Y level for mining"
   )
   public int getMinY() {
      return this.minY;
   }

   @ComputerMethod(
      methodDescription = "Gets the configured maximum Y level for mining"
   )
   public int getMaxY() {
      return this.maxY;
   }

   @ComputerMethod(
      nameOverride = "getInverseMode",
      methodDescription = "Whether Inverse Mode is enabled or not"
   )
   public boolean getInverse() {
      return this.inverse;
   }

   @ComputerMethod(
      nameOverride = "getInverseModeRequiresReplacement",
      methodDescription = "Whether Inverse Mode Require Replacement is turned on"
   )
   public boolean getInverseRequiresReplacement() {
      return this.inverseRequiresReplacement;
   }

   @ComputerMethod(
      nameOverride = "getInverseModeReplaceTarget",
      methodDescription = "Get the configured Replacement target item"
   )
   public Item getInverseReplaceTarget() {
      return this.inverseReplaceTarget;
   }

   private void setSilkTouch(boolean newSilkTouch) {
      if (this.silkTouch != newSilkTouch) {
         this.silkTouch = newSilkTouch;
         if (this.m_58898_() && !this.isRemote()) {
            this.energyContainer.updateMinerEnergyPerTick();
         }
      }
   }

   public void toggleSilkTouch() {
      this.setSilkTouch(!this.getSilkTouch());
      this.markForSave();
   }

   public void toggleInverse() {
      this.inverse = !this.inverse;
      this.markForSave();
   }

   public void toggleInverseRequiresReplacement() {
      this.inverseRequiresReplacement = !this.inverseRequiresReplacement;
      this.markForSave();
   }

   public void setInverseReplaceTarget(Item target) {
      if (target != this.inverseReplaceTarget) {
         this.inverseReplaceTarget = target;
         this.markForSave();
      }
   }

   public void toggleAutoEject() {
      this.doEject = !this.doEject;
      this.markForSave();
   }

   public void toggleAutoPull() {
      this.doPull = !this.doPull;
      this.markForSave();
   }

   public void setRadiusFromPacket(int newRadius) {
      this.setRadius(Mth.m_14045_(newRadius, 0, MekanismConfig.general.minerMaxRadius.get()));
      this.sendUpdatePacket();
      this.markForSave();
   }

   private void setRadius(int newRadius) {
      if (this.radius != newRadius) {
         this.radius = newRadius;
         if (this.m_58898_() && !this.isRemote()) {
            this.energyContainer.updateMinerEnergyPerTick();
            this.getChunkLoader().refreshChunkTickets();
         }
      }
   }

   public void setMinYFromPacket(int newMinY) {
      if (this.f_58857_ != null) {
         this.setMinY(Mth.m_14045_(newMinY, this.f_58857_.m_141937_(), this.getMaxY()));
         this.sendUpdatePacket();
         this.markForSave();
      }
   }

   private void setMinY(int newMinY) {
      if (this.minY != newMinY) {
         this.minY = newMinY;
         if (this.m_58898_() && !this.isRemote()) {
            this.energyContainer.updateMinerEnergyPerTick();
         }
      }
   }

   public void setMaxYFromPacket(int newMaxY) {
      if (this.f_58857_ != null) {
         this.setMaxY(Mth.m_14045_(newMaxY, this.getMinY(), this.f_58857_.m_151558_() - 1));
         this.sendUpdatePacket();
         this.markForSave();
      }
   }

   private void setMaxY(int newMaxY) {
      if (this.maxY != newMaxY) {
         this.maxY = newMaxY;
         if (this.m_58898_() && !this.isRemote()) {
            this.energyContainer.updateMinerEnergyPerTick();
         }
      }
   }

   private void tryMineBlock() {
      BlockPos startingPos = this.getStartingPos();
      int diameter = this.getDiameter();
      long target = this.targetChunk == null ? ChunkPos.f_45577_ : this.targetChunk.m_45588_();
      ObjectIterator<Entry<BitSet>> it = this.oresToMine.long2ObjectEntrySet().iterator();

      while (it.hasNext()) {
         Entry<BitSet> entry = (Entry<BitSet>)it.next();
         long chunk = entry.getLongKey();
         BitSet chunkToMine = (BitSet)entry.getValue();
         ChunkPos currentChunk = null;
         if (target == chunk) {
            currentChunk = this.targetChunk;
         }

         int previous = chunkToMine.length() - 1;

         while (true) {
            int index = chunkToMine.previousSetBit(previous);
            if (index == -1) {
               it.remove();
               break;
            }

            if (currentChunk == null) {
               this.updateTargetChunk(currentChunk = new ChunkPos(chunk));
               target = chunk;
            }

            BlockPos pos = getOffsetForIndex(startingPos, diameter, index);
            Optional<BlockState> blockState = WorldUtils.getBlockState(this.f_58857_, pos);
            if (blockState.isPresent()) {
               BlockState state = blockState.get();
               if (!state.m_60795_() && !state.m_204336_(MekanismTags.Blocks.MINER_BLACKLIST)) {
                  MinerFilter<?> matchingFilter = null;

                  for (MinerFilter<?> filter : this.filterManager.getEnabledFilters()) {
                     if (filter.canFilter(state)) {
                        matchingFilter = filter;
                        break;
                     }
                  }

                  if (this.inverse == (matchingFilter == null) && this.canMine(state, pos)) {
                     List<ItemStack> drops = this.getDrops(state, pos);
                     if (this.canInsert(drops)) {
                        CommonWorldTickHandler.fallbackItemCollector = this.overflowCollector;
                        if (this.setReplace(state, pos, matchingFilter)) {
                           this.add(drops);
                           this.tryAddOverflow();
                           this.missingStack = ItemStack.f_41583_;
                           this.f_58857_.m_46796_(2001, pos, Block.m_49956_(state));
                           this.cachedToMine--;
                           chunkToMine.clear(index);
                           if (chunkToMine.isEmpty()) {
                              it.remove();
                              this.updateTargetChunk(null);
                           }
                        }

                        CommonWorldTickHandler.fallbackItemCollector = null;
                     }

                     return;
                  }
               }
            }

            this.cachedToMine--;
            chunkToMine.clear(index);
            if (chunkToMine.isEmpty()) {
               it.remove();
               break;
            }

            previous = index - 1;
         }
      }

      this.updateTargetChunk(null);
   }

   private boolean setReplace(BlockState state, BlockPos pos, @Nullable MinerFilter<?> filter) {
      if (this.f_58857_ == null) {
         return false;
      } else {
         Item replaceTarget;
         ItemStack stack;
         if (filter == null) {
            stack = this.getReplace(replaceTarget = this.inverseReplaceTarget, this::inverseReplaceTargetMatches);
         } else {
            stack = this.getReplace(replaceTarget = filter.replaceTarget, filter::replaceTargetMatches);
         }

         if (!stack.m_41619_()) {
            BlockState newState = this.withFakePlayer(fakePlayer -> StackUtils.getStateForPlacement(stack, pos, fakePlayer));
            if (newState != null && newState.m_60710_(this.f_58857_, pos)) {
               this.f_58857_.m_220407_(GameEvent.f_157794_, pos, Context.m_223719_(null, state));
               this.f_58857_.m_46597_(pos, newState);
               this.f_58857_.m_220407_(GameEvent.f_157797_, pos, Context.m_223719_(null, newState));
               return true;
            } else {
               return false;
            }
         } else if (replaceTarget != Items.f_41852_ && (filter != null || this.inverseRequiresReplacement) && (filter == null || filter.requiresReplacement)) {
            this.missingStack = new ItemStack(replaceTarget);
            return false;
         } else {
            this.f_58857_.m_7471_(pos, false);
            this.f_58857_.m_220407_(GameEvent.f_157794_, pos, Context.m_223719_(null, state));
            return true;
         }
      }
   }

   private boolean canMine(BlockState state, BlockPos pos) {
      return this.withFakePlayer(dummy -> !MinecraftForge.EVENT_BUS.post(new BreakEvent(this.f_58857_, pos, state, dummy)));
   }

   private <R> R withFakePlayer(Function<MekFakePlayer, R> fakePlayerConsumer) {
      return MekFakePlayer.withFakePlayer(
         (ServerLevel)this.f_58857_, this.f_58858_.m_123341_(), this.f_58858_.m_123342_(), this.f_58858_.m_123343_(), dummy -> {
            dummy.setEmulatingUUID(this.getOwnerUUID());
            return fakePlayerConsumer.apply(dummy);
         }
      );
   }

   private ItemStack getReplace(Item replaceTarget, Predicate<Item> replaceStackMatches) {
      if (replaceTarget == Items.f_41852_) {
         return ItemStack.f_41583_;
      } else {
         for (IInventorySlot slot : this.mainSlots) {
            ItemStack slotStack = slot.getStack();
            if (replaceStackMatches.test(slotStack.m_41720_())) {
               MekanismUtils.logMismatchedStackSize(slot.shrinkStack(1, Action.EXECUTE), 1L);
               return slotStack.m_255036_(1);
            }
         }

         if ((replaceTarget == Items.f_42594_ || replaceTarget == Items.f_41905_) && this.upgradeComponent.isUpgradeInstalled(Upgrade.STONE_GENERATOR)) {
            return new ItemStack(replaceTarget);
         } else {
            if (this.doPull) {
               BlockEntity pullInv = this.getPullInv();
               if (pullInv != null && InventoryUtils.isItemHandler(pullInv, Direction.DOWN)) {
                  TransitRequest request = TransitRequest.definedItem(pullInv, Direction.DOWN, 1, Finder.item(replaceTarget));
                  if (!request.isEmpty()) {
                     TransitRequest.TransitResponse response = request.createSimpleResponse();
                     if (response.useAll().m_41619_()) {
                        return response.getStack().m_255036_(1);
                     }
                  }
               }
            }

            return ItemStack.f_41583_;
         }
      }
   }

   public boolean canInsert(List<ItemStack> toInsert) {
      if (toInsert.isEmpty()) {
         return true;
      } else {
         int slots = this.mainSlots.size();
         Int2ObjectMap<TileEntityDigitalMiner.ItemCount> cachedStacks = new Int2ObjectOpenHashMap(slots);

         for (int i = 0; i < slots; i++) {
            IInventorySlot slot = this.mainSlots.get(i);
            if (!slot.isEmpty()) {
               cachedStacks.put(i, new TileEntityDigitalMiner.ItemCount(slot.getStack(), slot.getCount()));
            }
         }

         for (ItemStack stackToInsert : toInsert) {
            ItemStack stack = this.simulateInsert(cachedStacks, slots, stackToInsert);
            if (!stack.m_41619_()) {
               return false;
            }
         }

         return true;
      }
   }

   private ItemStack simulateInsert(Int2ObjectMap<TileEntityDigitalMiner.ItemCount> cachedStacks, int slots, ItemStack stackToInsert) {
      if (stackToInsert.m_41619_()) {
         return stackToInsert;
      } else {
         ItemStack stack = stackToInsert.m_41777_();

         for (int i = 0; i < slots; i++) {
            TileEntityDigitalMiner.ItemCount cachedItem = (TileEntityDigitalMiner.ItemCount)cachedStacks.get(i);
            if (cachedItem != null && ItemHandlerHelper.canItemStacksStack(stack, cachedItem.stack)) {
               IInventorySlot slot = this.mainSlots.get(i);
               int limit = slot.getLimit(stack);
               if (cachedItem.count < limit) {
                  cachedItem.count = cachedItem.count + stack.m_41613_();
                  if (cachedItem.count <= limit) {
                     return ItemStack.f_41583_;
                  }

                  stack = stack.m_255036_(cachedItem.count - limit);
                  cachedItem.count = limit;
               }
            }
         }

         for (int ix = 0; ix < slots; ix++) {
            if (!cachedStacks.containsKey(ix)) {
               IInventorySlot slot = this.mainSlots.get(ix);
               int stackSize = stack.m_41613_();
               stack = slot.insertItem(stack, Action.SIMULATE, AutomationType.INTERNAL);
               int remainderSize = stack.m_41613_();
               if (remainderSize < stackSize) {
                  cachedStacks.put(ix, new TileEntityDigitalMiner.ItemCount(stackToInsert, stackSize - remainderSize));
                  if (stack.m_41619_()) {
                     return ItemStack.f_41583_;
                  }
               }
            }
         }

         return stack;
      }
   }

   private BlockEntity getPullInv() {
      return WorldUtils.getTileEntity(this.m_58904_(), this.m_58899_().m_6630_(2));
   }

   private void add(List<ItemStack> stacks) {
      for (ItemStack stack : stacks) {
         stack = InventoryUtils.insertItem(this.mainSlots, stack, Action.EXECUTE, AutomationType.INTERNAL);
         if (!stack.m_41619_()) {
            this.trackOverflow(stack);
         }
      }
   }

   private boolean trackOverflow(ItemStack stack) {
      if (!stack.m_41619_()) {
         this.overflow.mergeInt(HashedItem.create(stack), stack.m_41613_(), Integer::sum);
         this.hasOverflow = true;
         this.recheckOverflow = true;
         this.markForSave();
         return true;
      } else {
         return false;
      }
   }

   private void tryAddOverflow() {
      if (this.hasOverflow) {
         boolean recheck = false;
         ObjectIterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>> iter = this.overflow.object2IntEntrySet().iterator();

         while (iter.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>)iter.next();
            int amount = entry.getIntValue();
            ItemStack stack = ((HashedItem)entry.getKey()).createStack(amount);
            stack = InventoryUtils.insertItem(this.mainSlots, stack, Action.EXECUTE, AutomationType.INTERNAL);
            if (stack.m_41619_()) {
               iter.remove();
               recheck = true;
            } else if (stack.m_41613_() != amount) {
               entry.setValue(stack.m_41613_());
            }
         }

         if (recheck) {
            this.hasOverflow = !this.overflow.isEmpty();
         }
      }

      this.recheckOverflow = false;
   }

   public void start() {
      if (this.m_58904_() != null) {
         if (this.searcher.state == ThreadMinerSearch.State.IDLE) {
            BlockPos startingPos = this.getStartingPos();
            int diameter = this.getDiameter();
            this.searcher
               .setChunkCache(
                  new PathNavigationRegion(this.m_58904_(), startingPos, startingPos.m_7918_(diameter, this.getMaxY() - this.getMinY() + 1, diameter))
               );
            this.searcher.start();
         }

         this.running = true;
         this.markForSave();
      }
   }

   public void stop() {
      if (this.searcher.state == ThreadMinerSearch.State.SEARCHING) {
         this.searcher.interrupt();
         this.reset();
      } else if (this.searcher.state == ThreadMinerSearch.State.FINISHED) {
         this.running = false;
         this.markForSave();
         this.updateTargetChunk(null);
      }
   }

   public void reset() {
      this.searcher = new ThreadMinerSearch(this);
      this.running = false;
      this.cachedToMine = 0;
      this.oresToMine = Long2ObjectMaps.emptyMap();
      this.missingStack = ItemStack.f_41583_;
      this.setActive(false);
      this.updateTargetChunk(null);
      this.markForSave();
   }

   public boolean isReplaceTarget(Item target) {
      return this.inverse ? this.inverseReplaceTargetMatches(target) : this.filterManager.anyEnabledMatch(filter -> filter.replaceTargetMatches(target));
   }

   private boolean inverseReplaceTargetMatches(Item target) {
      return this.inverseReplaceTarget != Items.f_41852_ && this.inverseReplaceTarget == target;
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.running = nbt.m_128471_("running");
      this.delay = nbt.m_128451_("delay");
      this.numPowering = nbt.m_128451_("numPowering");
      NBTUtils.setEnumIfPresent(nbt, "state", ThreadMinerSearch.State::byIndexStatic, s -> {
         if (!this.initCalc && s == ThreadMinerSearch.State.SEARCHING) {
            s = ThreadMinerSearch.State.FINISHED;
         }

         this.searcher.state = s;
      });
      this.energyContainer.updateMinerEnergyPerTick();
   }

   @Override
   public void m_142339_(@NotNull Level world) {
      super.m_142339_(world);
      this.energyContainer.updateMinerEnergyPerTick();
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128379_("running", this.running);
      nbtTags.m_128405_("delay", this.delay);
      nbtTags.m_128405_("numPowering", this.numPowering);
      NBTUtils.writeEnum(nbtTags, "state", this.searcher.state);
      if (!this.overflow.isEmpty()) {
         ListTag overflowTag = new ListTag();
         ObjectIterator var3 = this.overflow.object2IntEntrySet().iterator();

         while (var3.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>)var3.next();
            CompoundTag overflowComponent = new CompoundTag();
            overflowComponent.m_128365_("type", ((HashedItem)entry.getKey()).internalToNBT());
            overflowComponent.m_128405_("Count", entry.getIntValue());
            overflowTag.add(overflowComponent);
         }

         nbtTags.m_128365_("overflow", overflowTag);
      }
   }

   public int getTotalSize() {
      int diameter = this.getDiameter();
      return diameter * diameter * (this.getMaxY() - this.getMinY() + 1);
   }

   public int getDiameter() {
      return this.radius * 2 + 1;
   }

   public BlockPos getStartingPos() {
      return new BlockPos(this.m_58899_().m_123341_() - this.radius, this.getMinY(), this.m_58899_().m_123343_() - this.radius);
   }

   public static BlockPos getOffsetForIndex(BlockPos start, int diameter, int index) {
      return start.m_7918_(index % diameter, index / diameter / diameter, index / diameter % diameter);
   }

   @Override
   public boolean isPowered() {
      return this.redstone || this.numPowering > 0;
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      return this.isClientRendering() && this.canDisplayVisuals()
         ? new AABB(
            this.f_58858_.m_123341_() - this.radius,
            this.minY,
            this.f_58858_.m_123343_() - this.radius,
            this.f_58858_.m_123341_() + this.radius + 1,
            this.maxY + 1,
            this.f_58858_.m_123343_() + this.radius + 1
         )
         : super.getRenderBoundingBox();
   }

   @Override
   public boolean isClientRendering() {
      return this.clientRendering;
   }

   @Override
   public void toggleClientRendering() {
      this.clientRendering = !this.clientRendering;
   }

   @Override
   public boolean canDisplayVisuals() {
      return this.getRadius() <= 64;
   }

   @Override
   public void onBoundingBlockPowerChange(BlockPos boundingPos, int oldLevel, int newLevel) {
      if (oldLevel > 0) {
         if (newLevel == 0) {
            this.numPowering--;
         }
      } else if (newLevel > 0) {
         this.numPowering++;
      }
   }

   @Override
   public int getBoundingComparatorSignal(Vec3i offset) {
      Direction facing = this.getDirection();
      Direction back = facing.m_122424_();
      if (offset.equals(new Vec3i(back.m_122429_(), 1, back.m_122431_()))) {
         return this.getCurrentRedstoneLevel();
      } else {
         Direction left = MekanismUtils.getLeft(facing);
         if (offset.equals(new Vec3i(left.m_122429_(), 0, left.m_122431_()))) {
            return this.getCurrentRedstoneLevel();
         } else {
            Direction right = left.m_122424_();
            return offset.equals(new Vec3i(right.m_122429_(), 0, right.m_122431_())) ? this.getCurrentRedstoneLevel() : 0;
         }
      }
   }

   @Override
   protected void notifyComparatorChange() {
      super.notifyComparatorChange();
      Direction facing = this.getDirection();
      Direction left = MekanismUtils.getLeft(facing);
      this.f_58857_.m_46717_(this.f_58858_.m_121945_(left), MekanismBlocks.BOUNDING_BLOCK.getBlock());
      this.f_58857_.m_46717_(this.f_58858_.m_121945_(left.m_122424_()), MekanismBlocks.BOUNDING_BLOCK.getBlock());
      this.f_58857_.m_46717_(this.f_58858_.m_121945_(facing.m_122424_()).m_7494_(), MekanismBlocks.BOUNDING_BLOCK.getBlock());
   }

   @Override
   public void configurationDataSet() {
      super.configurationDataSet();
      if (this.isRunning()) {
         this.stop();
         this.reset();
         this.start();
      }
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      dataMap.m_128405_("radius", this.getRadius());
      dataMap.m_128405_("min", this.getMinY());
      dataMap.m_128405_("max", this.getMaxY());
      dataMap.m_128379_("eject", this.doEject);
      dataMap.m_128379_("pull", this.doPull);
      dataMap.m_128379_("silkTouch", this.getSilkTouch());
      dataMap.m_128379_("inverse", this.inverse);
      if (this.inverseReplaceTarget != Items.f_41852_) {
         NBTUtils.writeRegistryEntry(dataMap, "replaceStack", ForgeRegistries.ITEMS, this.inverseReplaceTarget);
      }

      dataMap.m_128379_("inverseReplace", this.inverseRequiresReplacement);
      this.filterManager.writeToNBT(dataMap);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      this.setRadius(Math.min(dataMap.m_128451_("radius"), MekanismConfig.general.minerMaxRadius.get()));
      NBTUtils.setIntIfPresent(dataMap, "min", newMinY -> {
         if (this.m_58898_() && !this.isRemote()) {
            this.setMinY(Math.max(newMinY, this.f_58857_.m_141937_()));
         } else {
            this.setMinY(newMinY);
         }
      });
      NBTUtils.setIntIfPresent(dataMap, "max", newMaxY -> {
         if (this.m_58898_() && !this.isRemote()) {
            this.setMaxY(Math.min(newMaxY, this.f_58857_.m_151558_() - 1));
         } else {
            this.setMaxY(newMaxY);
         }
      });
      NBTUtils.setBooleanIfPresent(dataMap, "eject", eject -> this.doEject = eject);
      NBTUtils.setBooleanIfPresent(dataMap, "pull", pull -> this.doPull = pull);
      NBTUtils.setBooleanIfPresent(dataMap, "silkTouch", this::setSilkTouch);
      NBTUtils.setBooleanIfPresent(dataMap, "inverse", inverse -> this.inverse = inverse);
      this.inverseReplaceTarget = NBTUtils.readRegistryEntry(dataMap, "replaceStack", ForgeRegistries.ITEMS, Items.f_41852_);
      NBTUtils.setBooleanIfPresent(dataMap, "inverseReplace", requiresReplace -> this.inverseRequiresReplacement = requiresReplace);
      this.filterManager.readFromNBT(dataMap);
      NBTUtils.setListIfPresent(dataMap, "overflow", 10, overflowTag -> {
         this.overflow.clear();
         int i = 0;

         for (int size = overflowTag.size(); i < size; i++) {
            CompoundTag overflowComponent = overflowTag.m_128728_(i);
            int count = overflowComponent.m_128451_("Count");
            if (count > 0) {
               CompoundTag type = overflowComponent.m_128469_("type");
               ItemStack stack = ItemStack.m_41712_(type);
               if (!stack.m_41619_()) {
                  this.overflow.put(HashedItem.raw(stack), count);
               }
            }
         }

         this.hasOverflow = !this.overflow.isEmpty();
         this.recheckOverflow = this.hasOverflow;
      });
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("radius", "radius");
      remap.put("min", "min");
      remap.put("max", "max");
      remap.put("eject", "eject");
      remap.put("pull", "pull");
      remap.put("silkTouch", "silkTouch");
      remap.put("inverse", "inverse");
      remap.put("replaceStack", "replaceStack");
      remap.put("inverseReplace", "inverseReplace");
      remap.put("filters", "filters");
      remap.put("overflow", "overflow");
      return remap;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.delayLength = MekanismUtils.getTicks(this, MekanismConfig.general.minerTicksPerMine.get());
      }
   }

   @NotNull
   @Override
   public List<Component> getInfo(@NotNull Upgrade upgrade) {
      return UpgradeUtils.getMultScaledInfo(this, upgrade);
   }

   @NotNull
   @Override
   public <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@NotNull Capability<T> capability, Direction side, @NotNull Vec3i offset) {
      return capability == ForgeCapabilities.ITEM_HANDLER ? this.itemHandlerManager.resolve(capability, side) : this.getCapability(capability, side);
   }

   @Override
   public boolean isOffsetCapabilityDisabled(@NotNull Capability<?> capability, Direction side, @NotNull Vec3i offset) {
      if (!capability.isRegistered()) {
         return true;
      } else if (capability == ForgeCapabilities.ITEM_HANDLER) {
         return this.notItemPort(side, offset);
      } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
         return this.notEnergyPort(side, offset);
      } else {
         return this.canEverResolve(capability) && IBoundingBlock.super.isOffsetCapabilityDisabled(capability, side, offset)
            ? this.notItemPort(side, offset) && this.notEnergyPort(side, offset)
            : false;
      }
   }

   private boolean notItemPort(Direction side, Vec3i offset) {
      if (offset.equals(new Vec3i(0, 1, 0))) {
         return side != Direction.UP;
      } else {
         Direction back = this.getOppositeDirection();
         return offset.equals(new Vec3i(back.m_122429_(), 1, back.m_122431_())) ? side != back : true;
      }
   }

   private boolean notEnergyPort(Direction side, Vec3i offset) {
      if (offset.equals(Vec3i.f_123288_)) {
         return side != Direction.DOWN;
      } else {
         Direction left = this.getLeftSide();
         if (offset.equals(new Vec3i(left.m_122429_(), 0, left.m_122431_()))) {
            return side != left;
         } else {
            Direction right = left.m_122424_();
            return offset.equals(new Vec3i(right.m_122429_(), 0, right.m_122431_())) ? side != right : true;
         }
      }
   }

   @Override
   public TileComponentChunkLoader<TileEntityDigitalMiner> getChunkLoader() {
      return this.chunkLoaderComponent;
   }

   private void updateTargetChunk(@Nullable ChunkPos target) {
      if (!Objects.equals(this.targetChunk, target)) {
         this.targetChunk = target;
         this.getChunkLoader().refreshChunkTickets();
      }
   }

   @Override
   public Set<ChunkPos> getChunkSet() {
      ChunkPos minerChunk = new ChunkPos(this.f_58858_);
      if (this.targetChunk == null
         || SectionPos.m_123171_(this.f_58858_.m_123341_() - this.radius) > this.targetChunk.f_45578_
         || this.targetChunk.f_45578_ > SectionPos.m_123171_(this.f_58858_.m_123341_() + this.radius)
         || SectionPos.m_123171_(this.f_58858_.m_123343_() - this.radius) > this.targetChunk.f_45579_
         || this.targetChunk.f_45579_ > SectionPos.m_123171_(this.f_58858_.m_123343_() + this.radius)) {
         return Collections.singleton(minerChunk);
      } else {
         return minerChunk.equals(this.targetChunk) ? Set.of(minerChunk) : Set.of(minerChunk, this.targetChunk);
      }
   }

   public SortableFilterManager<MinerFilter<?>> getFilterManager() {
      return this.filterManager;
   }

   public MinerEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }

   @ComputerMethod(
      methodDescription = "Get the count of block found but not yet mined"
   )
   public int getToMine() {
      return !this.isRemote() && this.searcher.state == ThreadMinerSearch.State.SEARCHING ? this.searcher.found : this.cachedToMine;
   }

   @ComputerMethod(
      methodDescription = "Whether the miner is currently running"
   )
   public boolean isRunning() {
      return this.running;
   }

   @ComputerMethod(
      nameOverride = "getAutoEject",
      methodDescription = "Whether Auto Eject is turned on"
   )
   public boolean getDoEject() {
      return this.doEject;
   }

   @ComputerMethod(
      nameOverride = "getAutoPull",
      methodDescription = "Whether Auto Pull is turned on"
   )
   public boolean getDoPull() {
      return this.doPull;
   }

   public boolean hasOverflow() {
      return this.hasOverflow;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      this.addConfigContainerTrackers(container);
      container.track(SyncableBoolean.create(this::getDoEject, value -> this.doEject = value));
      container.track(SyncableBoolean.create(this::getDoPull, value -> this.doPull = value));
      container.track(SyncableBoolean.create(this::isRunning, value -> this.running = value));
      container.track(SyncableBoolean.create(this::getSilkTouch, this::setSilkTouch));
      container.track(
         SyncableEnum.create(
            ThreadMinerSearch.State::byIndexStatic, ThreadMinerSearch.State.IDLE, () -> this.searcher.state, value -> this.searcher.state = value
         )
      );
      container.track(SyncableInt.create(this::getToMine, value -> this.cachedToMine = value));
      container.track(SyncableItemStack.create(() -> this.missingStack, value -> this.missingStack = value));
      container.track(SyncableBoolean.create(this::hasOverflow, value -> this.hasOverflow = value));
   }

   public void addConfigContainerTrackers(MekanismContainer container) {
      container.track(SyncableInt.create(this::getRadius, this::setRadius));
      container.track(SyncableInt.create(this::getMinY, this::setMinY));
      container.track(SyncableInt.create(this::getMaxY, this::setMaxY));
      container.track(SyncableBoolean.create(this::getInverse, value -> this.inverse = value));
      container.track(SyncableBoolean.create(this::getInverseRequiresReplacement, value -> this.inverseRequiresReplacement = value));
      container.track(SyncableRegistryEntry.create(ForgeRegistries.ITEMS, this::getInverseReplaceTarget, value -> this.inverseReplaceTarget = value));
      this.filterManager.addContainerTrackers(container);
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128405_("radius", this.getRadius());
      updateTag.m_128405_("min", this.getMinY());
      updateTag.m_128405_("max", this.getMaxY());
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setIntIfPresent(tag, "radius", this::setRadius);
      NBTUtils.setIntIfPresent(tag, "min", this::setMinY);
      NBTUtils.setIntIfPresent(tag, "max", this::setMaxY);
   }

   private List<ItemStack> getDrops(BlockState state, BlockPos pos) {
      if (state.m_60795_()) {
         return Collections.emptyList();
      } else {
         ItemStack stack = ItemAtomicDisassembler.fullyChargedStack();
         if (this.getSilkTouch()) {
            stack.m_41663_(Enchantments.f_44985_, 1);
         }

         ServerLevel level = (ServerLevel)this.getWorldNN();
         return this.withFakePlayer(fakePlayer -> Block.m_49874_(state, level, pos, WorldUtils.getTileEntity(level, pos), fakePlayer, stack));
      }
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }

   @ComputerMethod(
      methodDescription = "Get the size of the Miner's internal inventory"
   )
   int getSlotCount() {
      return this.mainSlots.size();
   }

   @ComputerMethod(
      methodDescription = "Get the contents of the internal inventory slot. 0 based."
   )
   ItemStack getItemInSlot(int slot) throws ComputerException {
      int slots = this.getSlotCount();
      if (slot >= 0 && slot < slots) {
         return this.mainSlots.get(slot).getStack();
      } else {
         throw new ComputerException("Slot: '%d' is out of bounds, as this digital miner only has '%d' slots (zero indexed).", slot, slots);
      }
   }

   @ComputerMethod(
      methodDescription = "Get the state of the Miner's search"
   )
   ThreadMinerSearch.State getState() {
      return this.searcher.state;
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Update the Auto Eject setting"
   )
   void setAutoEject(boolean eject) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.doEject != eject) {
         this.toggleAutoEject();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Update the Auto Pull setting"
   )
   void setAutoPull(boolean pull) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.doPull != pull) {
         this.toggleAutoPull();
      }
   }

   @ComputerMethod(
      nameOverride = "setSilkTouch",
      requiresPublicSecurity = true,
      methodDescription = "Update the Silk Touch setting"
   )
   void computerSetSilkTouch(boolean silk) throws ComputerException {
      this.validateSecurityIsPublic();
      this.setSilkTouch(silk);
   }

   @ComputerMethod(
      nameOverride = "start",
      requiresPublicSecurity = true,
      methodDescription = "Attempt to start the mining process"
   )
   void computerStart() throws ComputerException {
      this.validateSecurityIsPublic();
      this.start();
   }

   @ComputerMethod(
      nameOverride = "stop",
      requiresPublicSecurity = true,
      methodDescription = "Attempt to stop the mining process"
   )
   void computerStop() throws ComputerException {
      this.validateSecurityIsPublic();
      this.stop();
   }

   @ComputerMethod(
      nameOverride = "reset",
      requiresPublicSecurity = true,
      methodDescription = "Stop the mining process and reset the Miner to be able to change settings"
   )
   void computerReset() throws ComputerException {
      this.validateSecurityIsPublic();
      this.reset();
   }

   @ComputerMethod(
      methodDescription = "Get the maximum allowable Radius value, determined from the mod's config"
   )
   int getMaxRadius() {
      return MekanismConfig.general.minerMaxRadius.get();
   }

   private void validateCanChangeConfiguration() throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.searcher.state != ThreadMinerSearch.State.IDLE) {
         throw new ComputerException("Miner must be stopped and reset before its targeting configuration is changed.");
      }
   }

   @ComputerMethod(
      nameOverride = "setRadius",
      requiresPublicSecurity = true,
      methodDescription = "Update the mining radius (blocks). Requires miner to be stopped/reset first"
   )
   void computerSetRadius(int radius) throws ComputerException {
      this.validateCanChangeConfiguration();
      if (radius >= 0 && radius <= MekanismConfig.general.minerMaxRadius.get()) {
         this.setRadiusFromPacket(radius);
      } else {
         throw new ComputerException("Radius '%d' is out of range must be between 0 and %d. (Inclusive)", radius, MekanismConfig.general.minerMaxRadius.get());
      }
   }

   @ComputerMethod(
      nameOverride = "setMinY",
      requiresPublicSecurity = true,
      methodDescription = "Update the minimum Y level for mining. Requires miner to be stopped/reset first"
   )
   void computerSetMinY(int minY) throws ComputerException {
      this.validateCanChangeConfiguration();
      if (this.f_58857_ != null) {
         int min = this.f_58857_.m_141937_();
         if (minY < min || minY > this.getMaxY()) {
            throw new ComputerException("Min Y '%d' is out of range must be between %d and %d. (Inclusive)", minY, min, this.getMaxY());
         }

         this.setMinYFromPacket(minY);
      }
   }

   @ComputerMethod(
      nameOverride = "setMaxY",
      requiresPublicSecurity = true,
      methodDescription = "Update the maximum Y level for mining. Requires miner to be stopped/reset first"
   )
   void computerSetMaxY(int maxY) throws ComputerException {
      this.validateCanChangeConfiguration();
      if (this.f_58857_ != null) {
         int max = this.f_58857_.m_151558_() - 1;
         if (maxY < this.getMinY() || maxY > max) {
            throw new ComputerException("Max Y '%d' is out of range must be between %d and %d. (Inclusive)", maxY, this.getMinY(), max);
         }

         this.setMaxYFromPacket(maxY);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Update the Inverse Mode setting. Requires miner to be stopped/reset first"
   )
   void setInverseMode(boolean enabled) throws ComputerException {
      this.validateCanChangeConfiguration();
      if (this.inverse != enabled) {
         this.toggleInverse();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Update the Inverse Mode Requires Replacement setting. Requires miner to be stopped/reset first"
   )
   void setInverseModeRequiresReplacement(boolean requiresReplacement) throws ComputerException {
      this.validateCanChangeConfiguration();
      if (this.inverseRequiresReplacement != requiresReplacement) {
         this.toggleInverseRequiresReplacement();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Update the target for Replacement in Inverse Mode. Requires miner to be stopped/reset first"
   )
   void setInverseModeReplaceTarget(Item target) throws ComputerException {
      this.validateCanChangeConfiguration();
      this.setInverseReplaceTarget(target);
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Remove the target for Replacement in Inverse Mode. Requires miner to be stopped/reset first"
   )
   void clearInverseModeReplaceTarget() throws ComputerException {
      this.setInverseModeReplaceTarget(Items.f_41852_);
   }

   @ComputerMethod(
      methodDescription = "Get the current list of Miner Filters"
   )
   List<MinerFilter<?>> getFilters() {
      return this.filterManager.getFilters();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Add a new filter to the miner. Requires miner to be stopped/reset first"
   )
   boolean addFilter(MinerFilter<?> filter) throws ComputerException {
      this.validateCanChangeConfiguration();
      return this.filterManager.addFilter(filter);
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Removes the exactly matching filter from the miner. Requires miner to be stopped/reset first"
   )
   boolean removeFilter(MinerFilter<?> filter) throws ComputerException {
      this.validateCanChangeConfiguration();
      return this.filterManager.removeFilter(filter);
   }

   private static class ItemCount {
      private final ItemStack stack;
      private int count;

      public ItemCount(ItemStack stack, int count) {
         this.stack = stack;
         this.count = count;
      }
   }
}

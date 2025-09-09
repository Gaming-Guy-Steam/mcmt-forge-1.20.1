package mekanism.common.tile.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager;
import mekanism.common.capabilities.resolver.manager.EnergyHandlerManager;
import mekanism.common.capabilities.resolver.manager.FluidHandlerManager;
import mekanism.common.capabilities.resolver.manager.HeatHandlerManager;
import mekanism.common.capabilities.resolver.manager.ICapabilityHandlerManager;
import mekanism.common.capabilities.resolver.manager.ItemHandlerManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.chemical.SyncableGasStack;
import mekanism.common.inventory.container.sync.chemical.SyncableInfusionStack;
import mekanism.common.inventory.container.sync.chemical.SyncablePigmentStack;
import mekanism.common.inventory.container.sync.chemical.SyncableSlurryStack;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.LastEnergyTracker;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRadioactive;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.ITileUpgradable;
import mekanism.common.tile.interfaces.chemical.IGasTile;
import mekanism.common.tile.interfaces.chemical.IInfusionTile;
import mekanism.common.tile.interfaces.chemical.IPigmentTile;
import mekanism.common.tile.interfaces.chemical.ISlurryTile;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityMekanism
   extends CapabilityTileEntity
   implements IFrequencyHandler,
   ITileDirectional,
   IConfigCardAccess,
   ITileActive,
   ITileSound,
   ITileRedstone,
   ISecurityTile,
   IMekanismInventory,
   ISustainedInventory,
   ITileUpgradable,
   ITierUpgradable,
   IComparatorSupport,
   ITrackableContainer,
   IMekanismFluidHandler,
   IMekanismStrictEnergyHandler,
   ITileHeatHandler,
   IGasTile,
   IInfusionTile,
   IPigmentTile,
   ISlurryTile,
   IComputerTile,
   ITileRadioactive,
   Nameable {
   public final Set<Player> playersUsing = new ObjectOpenHashSet();
   public int ticker;
   private final List<ICapabilityHandlerManager<?>> capabilityHandlerManagers = new ArrayList<>();
   private final List<ITileComponent> components = new ArrayList<>();
   protected final IBlockProvider blockProvider;
   private boolean supportsComparator;
   private boolean supportsComputers;
   private boolean supportsUpgrades;
   private boolean supportsRedstone;
   private boolean canBeUpgraded;
   private boolean isDirectional;
   private boolean isActivatable;
   private boolean hasSecurity;
   private boolean hasSound;
   private boolean hasGui;
   private boolean hasChunkloader;
   private boolean nameable;
   @Nullable
   private Component customName;
   @Nullable
   private Direction cachedDirection;
   protected boolean redstone = false;
   private boolean redstoneLastTick = false;
   private IRedstoneControl.RedstoneControl controlType = IRedstoneControl.RedstoneControl.DISABLED;
   private int currentRedstoneLevel;
   private boolean updateComparators;
   protected TileComponentUpgrade upgradeComponent;
   protected final TileComponentFrequency frequencyComponent;
   protected final ItemHandlerManager itemHandlerManager;
   private final ChemicalHandlerManager.GasHandlerManager gasHandlerManager;
   private float radiationScale;
   private final ChemicalHandlerManager.InfusionHandlerManager infusionHandlerManager;
   private final ChemicalHandlerManager.PigmentHandlerManager pigmentHandlerManager;
   private final ChemicalHandlerManager.SlurryHandlerManager slurryHandlerManager;
   private final FluidHandlerManager fluidHandlerManager;
   private final EnergyHandlerManager energyHandlerManager;
   private final LastEnergyTracker lastEnergyTracker = new LastEnergyTracker();
   protected final CachedAmbientTemperature ambientTemperature;
   protected final HeatHandlerManager heatHandlerManager;
   private TileComponentSecurity securityComponent;
   private boolean currentActive;
   private int updateDelay;
   protected IntSupplier delaySupplier = MekanismConfig.general.blockDeactivationDelay;
   @Nullable
   protected final SoundEvent soundEvent;
   private SoundInstance activeSound;
   private int playSoundCooldown = 0;

   public TileEntityMekanism(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(((IHasTileEntity)blockProvider.getBlock()).getTileType(), pos, state);
      this.blockProvider = blockProvider;
      Block block = this.blockProvider.getBlock();
      this.setSupportedTypes(block);
      this.presetVariables();
      IContentsListener saveOnlyListener = this::markForSave;
      this.capabilityHandlerManagers.add(this.gasHandlerManager = this.getInitialGasManager(this.getListener(SubstanceType.GAS, saveOnlyListener)));
      this.capabilityHandlerManagers
         .add(this.infusionHandlerManager = this.getInitialInfusionManager(this.getListener(SubstanceType.INFUSION, saveOnlyListener)));
      this.capabilityHandlerManagers.add(this.pigmentHandlerManager = this.getInitialPigmentManager(this.getListener(SubstanceType.PIGMENT, saveOnlyListener)));
      this.capabilityHandlerManagers.add(this.slurryHandlerManager = this.getInitialSlurryManager(this.getListener(SubstanceType.SLURRY, saveOnlyListener)));
      this.capabilityHandlerManagers
         .add(this.fluidHandlerManager = new FluidHandlerManager(this.getInitialFluidTanks(this.getListener(SubstanceType.FLUID, saveOnlyListener)), this));
      this.capabilityHandlerManagers
         .add(
            this.energyHandlerManager = new EnergyHandlerManager(
               this.getInitialEnergyContainers(this.getListener(SubstanceType.ENERGY, saveOnlyListener)), this
            )
         );
      this.capabilityHandlerManagers
         .add(this.itemHandlerManager = new ItemHandlerManager(this.getInitialInventory(this.getListener(null, saveOnlyListener)), this));
      CachedAmbientTemperature ambientTemperature = new CachedAmbientTemperature(this::m_58904_, this::m_58899_);
      this.capabilityHandlerManagers
         .add(
            this.heatHandlerManager = new HeatHandlerManager(
               this.getInitialHeatCapacitors(this.getListener(SubstanceType.HEAT, saveOnlyListener), ambientTemperature), this
            )
         );
      this.ambientTemperature = this.canHandleHeat() ? ambientTemperature : null;
      this.addCapabilityResolvers(this.capabilityHandlerManagers);
      this.frequencyComponent = new TileComponentFrequency(this);
      if (this.supportsUpgrades()) {
         this.upgradeComponent = new TileComponentUpgrade(this);
      }

      if (this.hasSecurity()) {
         this.securityComponent = new TileComponentSecurity(this);
         this.addCapabilityResolver(BasicCapabilityResolver.security(this));
      }

      this.soundEvent = this.hasSound() ? Attribute.get(block, AttributeSound.class).getSoundEvent() : null;
      ComputerCapabilityHelper.addComputerCapabilities(this, this::addCapabilityResolver);
   }

   private void setSupportedTypes(Block block) {
      this.supportsUpgrades = Attribute.has(block, AttributeUpgradeSupport.class);
      this.canBeUpgraded = Attribute.has(block, AttributeUpgradeable.class);
      this.isDirectional = Attribute.has(block, AttributeStateFacing.class);
      this.supportsRedstone = Attribute.has(block, Attributes.AttributeRedstone.class);
      this.hasSound = Attribute.has(block, AttributeSound.class);
      this.hasGui = Attribute.has(block, AttributeGui.class);
      this.hasSecurity = Attribute.has(block, Attributes.AttributeSecurity.class);
      this.isActivatable = this.hasSound || Attribute.has(block, AttributeStateActive.class);
      this.supportsComparator = Attribute.has(block, Attributes.AttributeComparator.class);
      this.supportsComputers = Mekanism.hooks.computerCompatEnabled() && Attribute.has(block, Attributes.AttributeComputerIntegration.class);
      this.hasChunkloader = this instanceof IChunkLoader;
      this.nameable = this.hasGui() && !Attribute.get(this.getBlockType(), AttributeGui.class).hasCustomName();
   }

   protected void presetVariables() {
   }

   public Block getBlockType() {
      return this.blockProvider.getBlock();
   }

   public ResourceLocation getBlockTypeRegistryName() {
      return this.blockProvider.getRegistryName();
   }

   public boolean persists(SubstanceType type) {
      return type.canHandle(this);
   }

   public boolean handles(SubstanceType type) {
      return this.persists(type);
   }

   @Override
   public final boolean supportsUpgrades() {
      return this.supportsUpgrades;
   }

   @Override
   public final boolean supportsComparator() {
      return this.supportsComparator;
   }

   @Override
   public final boolean canBeUpgraded() {
      return this.canBeUpgraded;
   }

   @Override
   public final boolean isDirectional() {
      return this.isDirectional;
   }

   @Override
   public final boolean supportsRedstone() {
      return this.supportsRedstone;
   }

   @Override
   public final boolean hasSound() {
      return this.hasSound;
   }

   public final boolean hasGui() {
      return this.hasGui;
   }

   @Override
   public final boolean hasSecurity() {
      return this.hasSecurity;
   }

   @Override
   public final boolean isActivatable() {
      return this.isActivatable;
   }

   @Override
   public final boolean hasComputerSupport() {
      return this.supportsComputers;
   }

   @Override
   public final boolean hasInventory() {
      return this.itemHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandleGas() {
      return this.gasHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandleInfusion() {
      return this.infusionHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandlePigment() {
      return this.pigmentHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandleSlurry() {
      return this.slurryHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandleFluid() {
      return this.fluidHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandleEnergy() {
      return this.energyHandlerManager.canHandle();
   }

   @Override
   public final boolean canHandleHeat() {
      return this.heatHandlerManager.canHandle();
   }

   public void addComponent(ITileComponent component) {
      this.components.add(component);
      if (component instanceof TileComponentConfig config) {
         this.addConfigComponent(config);
      }
   }

   public List<ITileComponent> getComponents() {
      return this.components;
   }

   @NotNull
   public Component m_7755_() {
      return (Component)(this.m_8077_() ? this.m_7770_() : TextComponentUtil.build(this.getBlockType()));
   }

   @NotNull
   public Component m_5446_() {
      if (this.isNameable()) {
         return (Component)(this.m_8077_() ? this.m_7770_() : TextComponentUtil.translate(Util.m_137492_("container", this.getBlockTypeRegistryName())));
      } else {
         return TextComponentUtil.build(this.getBlockType());
      }
   }

   @Nullable
   public Component m_7770_() {
      return this.isNameable() ? this.customName : null;
   }

   public void setCustomName(@Nullable Component name) {
      if (this.isNameable()) {
         this.customName = name;
      }
   }

   public boolean isNameable() {
      return this.nameable;
   }

   @Override
   public void markDirtyComparator() {
      if (this.supportsComparator()) {
         this.updateComparators = true;
      }
   }

   protected void notifyComparatorChange() {
      this.f_58857_.m_46717_(this.f_58858_, this.getBlockType());
   }

   public WrenchResult tryWrench(BlockState state, Player player, InteractionHand hand, BlockHitResult rayTrace) {
      ItemStack stack = player.m_21120_(hand);
      if (MekanismUtils.canUseAsWrench(stack)) {
         if (this.hasSecurity() && !ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, this)) {
            return WrenchResult.NO_SECURITY;
         } else if (player.m_6144_()) {
            WorldUtils.dismantleBlock(state, this.m_58904_(), this.f_58858_, this);
            return WrenchResult.DISMANTLED;
         } else {
            if (this.isDirectional() && Attribute.get(this.getBlockType(), AttributeStateFacing.class).canRotate()) {
               this.setFacing(this.getDirection().m_122427_());
            }

            return WrenchResult.SUCCESS;
         }
      } else {
         return WrenchResult.PASS;
      }
   }

   public InteractionResult openGui(Player player) {
      if (!this.hasGui() || this.isRemote() || player.m_6144_()) {
         return InteractionResult.PASS;
      } else if (this.hasSecurity() && !ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, this)) {
         return InteractionResult.FAIL;
      } else {
         ItemStack stack = player.m_21205_();
         if (this.isDirectional()
            && !stack.m_41619_()
            && stack.m_41720_() instanceof ItemConfigurator configurator
            && configurator.getMode(stack) == ItemConfigurator.ConfiguratorMode.ROTATE) {
            return InteractionResult.PASS;
         } else if (this.getCapability(Capabilities.CONFIG_CARD, null).isPresent() && !stack.m_41619_() && stack.m_41720_() instanceof ItemConfigurationCard) {
            return InteractionResult.PASS;
         } else {
            NetworkHooks.openScreen((ServerPlayer)player, Attribute.get(this.getBlockType(), AttributeGui.class).getProvider(this), this.f_58858_);
            return InteractionResult.CONSUME;
         }
      }
   }

   public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile) {
      if (tile.hasSound()) {
         tile.updateSound();
      }

      tile.onUpdateClient();
      tile.ticker++;
      if (tile.supportsRedstone()) {
         tile.redstoneLastTick = tile.redstone;
      }
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile) {
      tile.frequencyComponent.tickServer();
      if (tile.supportsUpgrades()) {
         tile.upgradeComponent.tickServer();
      }

      if (tile.hasChunkloader) {
         ((IChunkLoader)tile).getChunkLoader().tickServer();
      }

      if (tile.isActivatable() && tile.updateDelay > 0) {
         tile.updateDelay--;
         if (tile.updateDelay == 0 && tile.getClientActive() != tile.currentActive) {
            level.m_46597_(pos, Attribute.setActive(state, tile.currentActive));
         }
      }

      tile.onUpdateServer();
      tile.updateRadiationScale();
      if (tile.persists(SubstanceType.HEAT)) {
         tile.updateHeatCapacitors(null);
      }

      tile.lastEnergyTracker.received(level.m_46467_(), FloatingLong.ZERO);
      if (tile.supportsComparator() && tile.updateComparators && !state.m_60795_()) {
         int newRedstoneLevel = tile.getRedstoneLevel();
         if (newRedstoneLevel != tile.currentRedstoneLevel) {
            tile.currentRedstoneLevel = newRedstoneLevel;
            tile.notifyComparatorChange();
         }

         tile.updateComparators = false;
      }

      tile.ticker++;
      if (tile.supportsRedstone()) {
         tile.redstoneLastTick = tile.redstone;
      }
   }

   public void open(Player player) {
      this.playersUsing.add(player);
   }

   public void close(Player player) {
      this.playersUsing.remove(player);
   }

   public void m_7651_() {
      super.m_7651_();

      for (ITileComponent component : this.components) {
         component.invalidate();
      }

      if (this.isRemote() && this.hasSound()) {
         this.updateSound();
      }
   }

   @Override
   public void blockRemoved() {
      super.blockRemoved();

      for (ITileComponent component : this.components) {
         component.removed();
      }

      if (!this.isRemote() && IRadiationManager.INSTANCE.isRadiationEnabled() && this.shouldDumpRadiation()) {
         IRadiationManager.INSTANCE.dumpRadiation(this.getTileCoord(), this.getGasTanks(null), false);
      }
   }

   protected void onUpdateClient() {
   }

   protected void onUpdateServer() {
   }

   @Deprecated
   public void m_155250_(@NotNull BlockState newState) {
      super.m_155250_(newState);
      if (this.isDirectional()) {
         this.cachedDirection = null;
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      NBTUtils.setBooleanIfPresent(nbt, "redstone", value -> this.redstone = value);

      for (ITileComponent component : this.components) {
         component.read(nbt);
      }

      this.loadGeneralPersistentData(nbt);
      if (this.hasInventory() && this.persistInventory()) {
         DataHandlerUtils.readContainers(this.getInventorySlots(null), nbt.m_128437_("Items", 10));
      }

      for (SubstanceType type : EnumUtils.SUBSTANCES) {
         if (type.canHandle(this) && this.persists(type)) {
            type.read(this, nbt);
         }
      }

      if (this.isActivatable()) {
         NBTUtils.setBooleanIfPresent(nbt, "activeState", value -> this.currentActive = value);
         NBTUtils.setIntIfPresent(nbt, "updateDelay", value -> this.updateDelay = value);
      }

      if (this.supportsComparator()) {
         NBTUtils.setIntIfPresent(nbt, "currentRedstone", value -> this.currentRedstoneLevel = value);
      }

      if (this.isNameable()) {
         NBTUtils.setStringIfPresent(nbt, "CustomName", value -> this.customName = Serializer.m_130701_(value));
      }
   }

   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128379_("redstone", this.redstone);

      for (ITileComponent component : this.components) {
         component.write(nbtTags);
      }

      this.addGeneralPersistentData(nbtTags);
      if (this.hasInventory() && this.persistInventory()) {
         nbtTags.m_128365_("Items", DataHandlerUtils.writeContainers(this.getInventorySlots(null)));
      }

      for (SubstanceType type : EnumUtils.SUBSTANCES) {
         if (type.canHandle(this) && this.persists(type)) {
            type.write(this, nbtTags);
         }
      }

      if (this.isActivatable()) {
         nbtTags.m_128379_("activeState", this.currentActive);
         nbtTags.m_128405_("updateDelay", this.updateDelay);
      }

      if (this.supportsComparator()) {
         nbtTags.m_128405_("currentRedstone", this.currentRedstoneLevel);
      }

      if (this.customName != null && this.isNameable()) {
         nbtTags.m_128359_("CustomName", Serializer.m_130703_(this.customName));
      }
   }

   protected void addGeneralPersistentData(CompoundTag data) {
      if (this.supportsRedstone()) {
         NBTUtils.writeEnum(data, "controlType", this.controlType);
      }

      if (this instanceof ISustainedData sustainedData) {
         sustainedData.writeSustainedData(data);
      }
   }

   protected void loadGeneralPersistentData(CompoundTag data) {
      if (this.supportsRedstone()) {
         NBTUtils.setEnumIfPresent(data, "controlType", IRedstoneControl.RedstoneControl::byIndexStatic, type -> this.controlType = type);
      }

      if (this instanceof ISustainedData sustainedData) {
         sustainedData.readSustainedData(data);
      }
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      SyncMapper.INSTANCE.setup(container, this.getClass(), () -> this);

      for (ITileComponent component : this.components) {
         component.trackForMainContainer(container);
      }

      if (this.supportsRedstone()) {
         container.track(
            SyncableEnum.create(
               IRedstoneControl.RedstoneControl::byIndexStatic,
               IRedstoneControl.RedstoneControl.DISABLED,
               () -> this.controlType,
               value -> this.controlType = value
            )
         );
      }

      boolean isClient = this.isRemote();
      if (this.canHandleGas() && this.handles(SubstanceType.GAS)) {
         for (IGasTank gasTank : this.getGasTanks(null)) {
            container.track(SyncableGasStack.create(gasTank, isClient));
         }
      }

      if (this.canHandleInfusion() && this.handles(SubstanceType.INFUSION)) {
         for (IInfusionTank infusionTank : this.getInfusionTanks(null)) {
            container.track(SyncableInfusionStack.create(infusionTank, isClient));
         }
      }

      if (this.canHandlePigment() && this.handles(SubstanceType.PIGMENT)) {
         for (IPigmentTank pigmentTank : this.getPigmentTanks(null)) {
            container.track(SyncablePigmentStack.create(pigmentTank, isClient));
         }
      }

      if (this.canHandleSlurry() && this.handles(SubstanceType.SLURRY)) {
         for (ISlurryTank slurryTank : this.getSlurryTanks(null)) {
            container.track(SyncableSlurryStack.create(slurryTank, isClient));
         }
      }

      if (this.canHandleFluid() && this.handles(SubstanceType.FLUID)) {
         for (IExtendedFluidTank fluidTank : this.getFluidTanks(null)) {
            container.track(SyncableFluidStack.create(fluidTank, isClient));
         }
      }

      if (this.canHandleHeat() && this.handles(SubstanceType.HEAT)) {
         for (IHeatCapacitor capacitor : this.getHeatCapacitors(null)) {
            container.track(SyncableDouble.create(capacitor::getHeat, capacitor::setHeat));
            if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
               container.track(SyncableDouble.create(capacitor::getHeatCapacity, capacity -> heatCapacitor.setHeatCapacity(capacity, false)));
            }
         }
      }

      if (this.canHandleEnergy() && this.handles(SubstanceType.ENERGY)) {
         container.track(SyncableFloatingLong.create(this.lastEnergyTracker::getLastEnergyReceived, this.lastEnergyTracker::setLastEnergyReceived));

         for (IEnergyContainer energyContainer : this.getEnergyContainers(null)) {
            container.track(SyncableFloatingLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
            if (energyContainer instanceof MachineEnergyContainer<?> machineEnergy && (this.supportsUpgrades() || machineEnergy.adjustableRates())) {
               container.track(SyncableFloatingLong.create(machineEnergy::getMaxEnergy, machineEnergy::setMaxEnergy));
               container.track(SyncableFloatingLong.create(machineEnergy::getEnergyPerTick, machineEnergy::setEnergyPerTick));
            }
         }
      }
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();

      for (ITileComponent component : this.components) {
         component.addToUpdateTag(updateTag);
      }

      updateTag.m_128350_("radiation", this.radiationScale);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);

      for (ITileComponent component : this.components) {
         component.readFromUpdateTag(tag);
      }

      this.radiationScale = tag.m_128457_("radiation");
   }

   public void onNeighborChange(Block block, BlockPos neighborPos) {
      if (!this.isRemote()) {
         this.updatePower();
      }
   }

   public void onAdded() {
      this.updatePower();
   }

   @Override
   public TileComponentFrequency getFrequencyComponent() {
      return this.frequencyComponent;
   }

   public void parseUpgradeData(@NotNull IUpgradeData data) {
      Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
   }

   @NotNull
   @ComputerMethod(
      restriction = MethodRestriction.DIRECTIONAL
   )
   @Override
   public final Direction getDirection() {
      if (this.isDirectional()) {
         if (this.cachedDirection != null) {
            return this.cachedDirection;
         }

         BlockState state = this.m_58900_();
         this.cachedDirection = Attribute.getFacing(state);
         if (this.cachedDirection != null) {
            return this.cachedDirection;
         }

         if (!this.m_58903_().m_155262_(state)) {
            Mekanism.logger
               .warn(
                  "Error invalid block for tile {} at {} in {}. Unable to get direction, falling back to north, things will probably not work correctly. This is almost certainly due to another mod incorrectly trying to move this tile and not properly updating the position.",
                  new Object[]{RegistryUtils.getName(this.m_58903_()), this.f_58858_, this.f_58857_}
               );
         }
      }

      return Direction.NORTH;
   }

   @Override
   public void setFacing(@NotNull Direction direction) {
      if (this.isDirectional() && direction != this.cachedDirection && this.f_58857_ != null) {
         this.cachedDirection = direction;
         BlockState state = Attribute.setFacing(this.m_58900_(), direction);
         if (state != null) {
            this.f_58857_.m_46597_(this.f_58858_, state);
         }
      }
   }

   @ComputerMethod(
      nameOverride = "getRedstoneMode",
      restriction = MethodRestriction.REDSTONE_CONTROL
   )
   @Override
   public IRedstoneControl.RedstoneControl getControlType() {
      return this.controlType;
   }

   @Override
   public void setControlType(@NotNull IRedstoneControl.RedstoneControl type) {
      if (this.supportsRedstone()) {
         this.controlType = Objects.requireNonNull(type);
         this.markForSave();
      }
   }

   @Override
   public boolean isPowered() {
      return this.supportsRedstone() && this.redstone;
   }

   @Override
   public boolean wasPowered() {
      return this.supportsRedstone() && this.redstoneLastTick;
   }

   public final void updatePower() {
      if (this.supportsRedstone()) {
         boolean power = this.f_58857_.m_276867_(this.m_58899_());
         if (this.redstone != power) {
            this.redstone = power;
            this.onPowerChange();
         }
      }
   }

   @Override
   public int getRedstoneLevel() {
      return this.supportsComparator() && this.hasInventory() ? MekanismUtils.redstoneLevelFromContents(this.getInventorySlots(null)) : 0;
   }

   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == null;
   }

   protected final IContentsListener getListener(@Nullable SubstanceType type, IContentsListener saveOnlyListener) {
      return (IContentsListener)(this.supportsComparator() && !this.makesComparatorDirty(type) ? saveOnlyListener : this);
   }

   @ComputerMethod(
      nameOverride = "getComparatorLevel",
      restriction = MethodRestriction.COMPARATOR
   )
   @Override
   public int getCurrentRedstoneLevel() {
      return this.currentRedstoneLevel;
   }

   @NotNull
   @Override
   public Set<Upgrade> getSupportedUpgrade() {
      return this.supportsUpgrades() ? Attribute.get(this.getBlockType(), AttributeUpgradeSupport.class).supportedUpgrades() : Collections.emptySet();
   }

   @Override
   public TileComponentUpgrade getComponent() {
      return this.upgradeComponent;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      if (upgrade == Upgrade.SPEED) {
         for (IEnergyContainer energyContainer : this.getEnergyContainers(null)) {
            if (energyContainer instanceof MachineEnergyContainer<?> machineEnergy) {
               machineEnergy.updateEnergyPerTick();
            }
         }
      } else if (upgrade == Upgrade.ENERGY) {
         for (IEnergyContainer energyContainerx : this.getEnergyContainers(null)) {
            if (energyContainerx instanceof MachineEnergyContainer<?> machineEnergy) {
               machineEnergy.updateMaxEnergy();
               machineEnergy.updateEnergyPerTick();
            }
         }
      }
   }

   @Nullable
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      return null;
   }

   @NotNull
   @Override
   public final List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
      return this.itemHandlerManager.getContainers(side);
   }

   @Override
   public void onContentsChanged() {
      this.m_6596_();
   }

   @Override
   public void setSustainedInventory(ListTag nbtTags) {
      if (nbtTags != null && !nbtTags.isEmpty() && this.persistInventory()) {
         DataHandlerUtils.readContainers(this.getInventorySlots(null), nbtTags);
      }
   }

   @Override
   public ListTag getSustainedInventory() {
      return this.persistInventory() ? DataHandlerUtils.writeContainers(this.getInventorySlots(null)) : new ListTag();
   }

   public boolean persistInventory() {
      return this.hasInventory();
   }

   @NotNull
   @Override
   public ChemicalHandlerManager.GasHandlerManager getGasManager() {
      return this.gasHandlerManager;
   }

   public boolean shouldDumpRadiation() {
      return true;
   }

   private void updateRadiationScale() {
      if (this.shouldDumpRadiation()) {
         float scale = ITileRadioactive.calculateRadiationScale(this.getGasTanks(null));
         if (Math.abs(scale - this.radiationScale) > 0.05F) {
            this.radiationScale = scale;
            this.sendUpdatePacket();
         }
      }
   }

   @Override
   public float getRadiationScale() {
      return IRadiationManager.INSTANCE.isRadiationEnabled() ? this.radiationScale : 0.0F;
   }

   @NotNull
   @Override
   public ChemicalHandlerManager.InfusionHandlerManager getInfusionManager() {
      return this.infusionHandlerManager;
   }

   @NotNull
   @Override
   public ChemicalHandlerManager.PigmentHandlerManager getPigmentManager() {
      return this.pigmentHandlerManager;
   }

   @NotNull
   @Override
   public ChemicalHandlerManager.SlurryHandlerManager getSlurryManager() {
      return this.slurryHandlerManager;
   }

   @Nullable
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      return null;
   }

   @NotNull
   @Override
   public final List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.fluidHandlerManager.getContainers(side);
   }

   @Nullable
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      return null;
   }

   @NotNull
   @Override
   public final List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.energyHandlerManager.getContainers(side);
   }

   @NotNull
   @Override
   public FloatingLong insertEnergy(int container, @NotNull FloatingLong amount, @Nullable Direction side, @NotNull Action action) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      if (energyContainer == null) {
         return amount;
      } else {
         FloatingLong remainder = energyContainer.insert(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
         if (action.execute()) {
            this.lastEnergyTracker.received(this.f_58857_ == null ? 0L : this.f_58857_.m_46467_(), amount.subtract(remainder));
         }

         return remainder;
      }
   }

   public final FloatingLong getInputRate() {
      return this.lastEnergyTracker.getLastEnergyReceived();
   }

   @Nullable
   protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
      return null;
   }

   @Override
   public double getAmbientTemperature(@NotNull Direction side) {
      return this.canHandleHeat() && this.ambientTemperature != null
         ? this.ambientTemperature.getTemperature(side)
         : ITileHeatHandler.super.getAmbientTemperature(side);
   }

   @Nullable
   @Override
   public IHeatHandler getAdjacent(@NotNull Direction side) {
      if (this.canHandleHeat() && this.getHeatCapacitorCount(side) > 0) {
         BlockEntity adj = WorldUtils.getTileEntity(this.m_58904_(), this.m_58899_().m_121945_(side));
         return (IHeatHandler)CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER, side.m_122424_()).resolve().orElse(null);
      } else {
         return null;
      }
   }

   @NotNull
   @Override
   public final List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
      return this.heatHandlerManager.getContainers(side);
   }

   @Override
   public String getConfigCardName() {
      return this.getBlockType().m_7705_();
   }

   @Override
   public CompoundTag getConfigurationData(Player player) {
      CompoundTag data = new CompoundTag();
      this.addGeneralPersistentData(data);
      this.getFrequencyComponent().writeConfiguredFrequencies(data);
      return data;
   }

   @Override
   public void setConfigurationData(Player player, CompoundTag data) {
      this.loadGeneralPersistentData(data);
      this.getFrequencyComponent().readConfiguredFrequencies(player, data);
   }

   @Override
   public BlockEntityType<?> getConfigurationDataType() {
      return this.m_58903_();
   }

   @Override
   public void configurationDataSet() {
      this.m_6596_();
      this.invalidateCachedCapabilities();
      this.sendUpdatePacket();
      WorldUtils.notifyLoadedNeighborsOfTileChange(this.m_58904_(), this.getTilePos());
   }

   @Override
   public TileComponentSecurity getSecurity() {
      return this.securityComponent;
   }

   @Override
   public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
      if (!this.isRemote() && this.hasGui()) {
         SecurityUtils.get().securityChanged(this.playersUsing, this, old, mode);
      }
   }

   @Override
   public boolean getActive() {
      return this.isRemote() ? this.getClientActive() : this.currentActive;
   }

   private boolean getClientActive() {
      return this.isActivatable() && Attribute.isActive(this.m_58900_());
   }

   @Override
   public void setActive(boolean active) {
      if (this.isActivatable() && active != this.currentActive) {
         BlockState state = this.m_58900_();
         AttributeStateActive activeAttribute = Attribute.get(state, AttributeStateActive.class);
         if (activeAttribute != null) {
            this.currentActive = active;
            if (this.getClientActive() != active) {
               if (active) {
                  this.f_58857_.m_46597_(this.f_58858_, activeAttribute.setActive(state, true));
               } else {
                  if (this.updateDelay == 0) {
                     this.f_58857_.m_46597_(this.f_58858_, activeAttribute.setActive(state, this.currentActive));
                  }

                  this.updateDelay = this.delaySupplier.getAsInt();
               }
            }
         }
      }
   }

   protected boolean canPlaySound() {
      return this.getActive();
   }

   private void updateSound() {
      if (this.hasSound() && MekanismConfig.client.enableMachineSounds.get() && this.soundEvent != null) {
         if (this.canPlaySound() && !this.m_58901_()) {
            if (--this.playSoundCooldown > 0) {
               return;
            }

            if (!this.isFullyMuffled() && (this.activeSound == null || !Minecraft.m_91087_().m_91106_().m_120403_(this.activeSound))) {
               this.activeSound = SoundHandler.startTileSound(
                  this.soundEvent, this.getSoundCategory(), this.getInitialVolume(), this.f_58857_.m_213780_(), this.getSoundPos()
               );
            }

            this.playSoundCooldown = 20;
         } else if (this.activeSound != null) {
            SoundHandler.stopTileSound(this.getSoundPos());
            this.activeSound = null;
            this.playSoundCooldown = 0;
         }
      }
   }

   protected boolean isFullyMuffled() {
      return this.hasSound() && this.supportsUpgrade(Upgrade.MUFFLING) ? this.getComponent().getUpgrades(Upgrade.MUFFLING) == Upgrade.MUFFLING.getMax() : false;
   }

   @Override
   public String getComputerName() {
      return this.hasComputerSupport() ? Attribute.get(this.getBlockType(), Attributes.AttributeComputerIntegration.class).name() : "";
   }

   public void validateSecurityIsPublic() throws ComputerException {
      if (this.hasSecurity() && ISecurityUtils.INSTANCE.getSecurityMode(this, this.isRemote()) != SecurityMode.PUBLIC) {
         throw new ComputerException("Setter not available due to machine security not being public.");
      }
   }

   @Override
   public void getComputerMethods(BoundMethodHolder holder) {
      IComputerTile.super.getComputerMethods(holder);

      for (ITileComponent component : this.components) {
         FactoryRegistry.bindTo(holder, component);
      }
   }

   @ComputerMethod(
      nameOverride = "getEnergy",
      restriction = MethodRestriction.ENERGY
   )
   FloatingLong getTotalEnergy() {
      return this.getTotalEnergy(IEnergyContainer::getEnergy);
   }

   @ComputerMethod(
      nameOverride = "getMaxEnergy",
      restriction = MethodRestriction.ENERGY
   )
   FloatingLong getTotalMaxEnergy() {
      return this.getTotalEnergy(IEnergyContainer::getMaxEnergy);
   }

   @ComputerMethod(
      nameOverride = "getEnergyNeeded",
      restriction = MethodRestriction.ENERGY
   )
   FloatingLong getTotalEnergyNeeded() {
      return this.getTotalEnergy(IEnergyContainer::getNeeded);
   }

   private FloatingLong getTotalEnergy(Function<IEnergyContainer, FloatingLong> getter) {
      FloatingLong total = FloatingLong.ZERO;

      for (IEnergyContainer energyContainer : this.getEnergyContainers(null)) {
         total = total.plusEqual(getter.apply(energyContainer));
      }

      return total;
   }

   @ComputerMethod(
      nameOverride = "getEnergyFilledPercentage",
      restriction = MethodRestriction.ENERGY
   )
   double getTotalEnergyFilledPercentage() {
      FloatingLong stored = FloatingLong.ZERO;
      FloatingLong max = FloatingLong.ZERO;

      for (IEnergyContainer energyContainer : this.getEnergyContainers(null)) {
         stored = stored.plusEqual(energyContainer.getEnergy());
         max = max.plusEqual(energyContainer.getMaxEnergy());
      }

      return stored.divideToLevel(max);
   }

   @ComputerMethod(
      restriction = MethodRestriction.REDSTONE_CONTROL,
      requiresPublicSecurity = true
   )
   void setRedstoneMode(IRedstoneControl.RedstoneControl type) throws ComputerException {
      this.validateSecurityIsPublic();
      if (type == IRedstoneControl.RedstoneControl.PULSE && !this.canPulse()) {
         throw new ComputerException("Unsupported redstone control mode: %s", IRedstoneControl.RedstoneControl.PULSE);
      } else {
         this.setControlType(type);
      }
   }
}

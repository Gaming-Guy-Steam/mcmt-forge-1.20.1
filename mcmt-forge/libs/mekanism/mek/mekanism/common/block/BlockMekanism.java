package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.DataHandlerUtils;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.security.ISecurityUtils;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.radiation.Meltdown;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileRadioactive;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockMekanism extends Block {
   protected BlockMekanism(Properties properties) {
      super(BlockStateHelper.applyLightLevelAdjustments(properties));
      this.m_49959_(BlockStateHelper.getDefaultState((BlockState)this.f_49792_.m_61090_()));
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.particles());
   }

   @Nullable
   public PushReaction getPistonPushReaction(@NotNull BlockState state) {
      return state.m_155947_() ? PushReaction.BLOCK : super.getPistonPushReaction(state);
   }

   @NotNull
   public ItemStack getCloneItemStack(@NotNull BlockState state, HitResult target, @NotNull BlockGetter world, @NotNull BlockPos pos, Player player) {
      ItemStack itemStack = new ItemStack(this);
      TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
      if (tile == null) {
         return itemStack;
      } else {
         Item item = itemStack.m_41720_();
         Lazy<CompoundTag> lazyDataMap = Lazy.of(() -> ItemDataUtils.getDataMap(itemStack));
         if (tile.getFrequencyComponent().hasCustomFrequencies()) {
            tile.getFrequencyComponent().write((CompoundTag)lazyDataMap.get());
         }

         if (tile.hasSecurity()) {
            itemStack.getCapability(Capabilities.OWNER_OBJECT).ifPresent(ownerObject -> {
               ownerObject.setOwnerUUID(tile.getOwnerUUID());
               itemStack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(securityObject -> securityObject.setSecurityMode(tile.getSecurityMode()));
            });
         }

         if (tile.supportsUpgrades()) {
            tile.getComponent().write((CompoundTag)lazyDataMap.get());
         }

         if (tile instanceof ISideConfiguration config) {
            CompoundTag dataMap = (CompoundTag)lazyDataMap.get();
            config.getConfig().write(dataMap);
            config.getEjector().write(dataMap);
         }

         if (tile instanceof ISustainedData sustainedData) {
            sustainedData.writeSustainedData((CompoundTag)lazyDataMap.get());
         }

         if (tile.supportsRedstone()) {
            NBTUtils.writeEnum((CompoundTag)lazyDataMap.get(), "controlType", tile.getControlType());
         }

         for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (tile.handles(type)) {
               ((CompoundTag)lazyDataMap.get()).m_128365_(type.getContainerTag(), DataHandlerUtils.writeContainers(type.getContainers(tile)));
            }
         }

         if (item instanceof IItemSustainedInventory sustainedInventory && tile.persistInventory() && tile.getSlots() > 0) {
            sustainedInventory.setSustainedInventory(tile.getSustainedInventory(), itemStack);
         }

         return itemStack;
      }
   }

   @Deprecated
   @NotNull
   public List<ItemStack> m_49635_(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> drops = super.m_49635_(state, builder);
      if (IRadiationManager.INSTANCE.isRadiationEnabled()
         && state.m_60734_() instanceof IHasTileEntity<?> hasTileEntity
         && hasTileEntity.createDummyBlockEntity(state) instanceof TileEntityMekanism mekTile
         && !mekTile.getGasTanks(null).isEmpty()
         && !(mekTile instanceof TileEntityChemicalTank chemicalTank && chemicalTank.getTier() == ChemicalTankTier.CREATIVE)) {
         for (ItemStack drop : drops) {
            ListTag gasTankList = ItemDataUtils.getList(drop, "GasTanks");
            if (!gasTankList.isEmpty()) {
               int count = DataHandlerUtils.getMaxId(gasTankList, "Tank");
               List<IGasTank> tanks = new ArrayList<>(count);

               for (int i = 0; i < count; i++) {
                  tanks.add((IGasTank)ChemicalTankBuilder.GAS.createDummy(Long.MAX_VALUE));
               }

               DataHandlerUtils.readContainers(tanks, gasTankList);
               boolean hasRadioactive = false;

               for (IGasTank tank : tanks) {
                  if (!tank.isEmpty() && tank.getStack().has(GasAttributes.Radiation.class)) {
                     hasRadioactive = true;
                     tank.setEmpty();
                  }
               }

               if (hasRadioactive) {
                  ListTag newGasTankList = DataHandlerUtils.writeContainers(tanks);
                  ItemDataUtils.setListOrRemove(drop, "GasTanks", newGasTankList);
               }
            }
         }
      }

      return drops;
   }

   @Deprecated
   public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      boolean triggered = super.m_8133_(state, level, pos, id, param);
      return this instanceof IHasTileEntity<?> hasTileEntity ? hasTileEntity.triggerBlockEntityEvent(state, level, pos, id, param) : triggered;
   }

   protected void m_7926_(@NotNull Builder<Block, BlockState> builder) {
      super.m_7926_(builder);
      BlockStateHelper.fillBlockStateContainer(this, builder);
   }

   @Nullable
   public BlockState m_5573_(@NotNull BlockPlaceContext context) {
      return BlockStateHelper.getStateForPlacement(this, super.m_5573_(context), context);
   }

   @Deprecated
   @NotNull
   public FluidState m_5888_(BlockState state) {
      return state.m_60734_() instanceof IStateFluidLoggable fluidLoggable ? fluidLoggable.getFluid(state) : super.m_5888_(state);
   }

   @Deprecated
   @NotNull
   public BlockState m_7417_(
      BlockState state,
      @NotNull Direction facing,
      @NotNull BlockState facingState,
      @NotNull LevelAccessor world,
      @NotNull BlockPos currentPos,
      @NotNull BlockPos facingPos
   ) {
      if (state.m_60734_() instanceof IStateFluidLoggable fluidLoggable) {
         fluidLoggable.updateFluids(state, world, currentPos);
      }

      return super.m_7417_(state, facing, facingState, world, currentPos, facingPos);
   }

   @Deprecated
   public void m_6810_(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (!state.m_60713_(newState.m_60734_())) {
         AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
         if (hasBounding != null) {
            hasBounding.removeBoundingBlocks(world, pos, state);
         }
      }

      if (state.m_155947_() && (!state.m_60713_(newState.m_60734_()) || !newState.m_155947_())) {
         TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos);
         if (tile != null) {
            tile.blockRemoved();
         }
      }

      super.m_6810_(state, world, pos, newState, isMoving);
   }

   public void m_6402_(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
      super.m_6402_(world, pos, state, placer, stack);
      AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
      if (hasBounding != null) {
         hasBounding.placeBoundingBlocks(world, pos, state);
      }

      TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
      if (tile != null) {
         if (tile.supportsRedstone()) {
            tile.updatePower();
         }

         if (tile.isNameable() && stack.m_41788_()) {
            tile.setCustomName(stack.m_41786_());
         }

         Item item = stack.m_41720_();
         CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
         if (dataMap == null) {
            dataMap = new CompoundTag();
         }

         if (!world.f_46443_ && tile.getFrequencyComponent().hasCustomFrequencies()) {
            tile.getFrequencyComponent().read(dataMap);
         }

         if (tile.hasSecurity()) {
            stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> tile.setSecurityMode(security.getSecurityMode()));
            UUID ownerUUID = ISecurityUtils.INSTANCE.getOwnerUUID(stack);
            if (ownerUUID != null) {
               tile.setOwnerUUID(ownerUUID);
            } else if (placer != null) {
               tile.setOwnerUUID(placer.m_20148_());
               if (!world.f_46443_) {
                  Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(placer.m_20148_()));
               }
            }
         }

         if (tile.supportsUpgrades()) {
            tile.getComponent().read(dataMap);
         }

         if (tile instanceof ISideConfiguration config) {
            config.getConfig().read(dataMap);
            config.getEjector().read(dataMap);
         }

         for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(tile)) {
               DataHandlerUtils.readContainers(type.getContainers(tile), dataMap.m_128437_(type.getContainerTag(), 10));
            }
         }

         if (tile instanceof ISustainedData sustainedData && stack.m_41782_()) {
            sustainedData.readSustainedData(dataMap);
         }

         if (tile.supportsRedstone()) {
            NBTUtils.setEnumIfPresent(dataMap, "controlType", IRedstoneControl.RedstoneControl::byIndexStatic, tile::setControlType);
         }

         if (item instanceof IItemSustainedInventory sustainedInventory && tile.persistInventory()) {
            tile.setSustainedInventory(sustainedInventory.getSustainedInventory(stack));
         }
      }
   }

   public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
      if (!world.f_46443_) {
         AttributeMultiblock multiblockAttribute = Attribute.get(state, AttributeMultiblock.class);
         if (multiblockAttribute != null && explosion instanceof Meltdown.MeltdownExplosion meltdown) {
            MultiblockData multiblock = multiblockAttribute.getMultiblock(world, pos, meltdown.getMultiblockID());
            if (multiblock != null) {
               multiblock.meltdownHappened(world);
            }
         }
      }

      super.onBlockExploded(state, world, pos, explosion);
   }

   public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
      return AttributeStateFacing.rotate(state, world, pos, rotation);
   }

   @Deprecated
   @NotNull
   public BlockState m_6843_(@NotNull BlockState state, @NotNull Rotation rotation) {
      return AttributeStateFacing.rotate(state, rotation);
   }

   @Deprecated
   @NotNull
   public BlockState m_6943_(@NotNull BlockState state, @NotNull Mirror mirror) {
      return AttributeStateFacing.mirror(state, mirror);
   }

   @Deprecated
   public void m_6807_(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
      if (state.m_155947_() && oldState.m_60734_() != state.m_60734_()) {
         TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
         if (tile != null) {
            tile.onAdded();
         }
      }

      super.m_6807_(state, world, pos, oldState, isMoving);
   }

   @Deprecated
   public boolean m_7278_(@NotNull BlockState blockState) {
      return Attribute.has(this, Attributes.AttributeComparator.class);
   }

   @Deprecated
   public int m_6782_(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos) {
      return this.m_7278_(blockState)
            && WorldUtils.getTileEntity(world, pos) instanceof IComparatorSupport comparatorTile
            && comparatorTile.supportsComparator()
         ? comparatorTile.getCurrentRedstoneLevel()
         : 0;
   }

   @Deprecated
   public float m_5880_(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos) {
      return this.getDestroyProgress(state, player, world, pos, state.m_155947_() ? WorldUtils.getTileEntity(world, pos) : null);
   }

   protected float getDestroyProgress(
      @NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos, @Nullable BlockEntity tile
   ) {
      float speed = super.m_5880_(state, player, world, pos);
      return IRadiationManager.INSTANCE.isRadiationEnabled() && tile instanceof ITileRadioactive radioactiveTile && radioactiveTile.getRadiationScale() > 0.0F
         ? speed / 5.0F
         : speed;
   }

   public void m_214162_(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
      super.m_214162_(state, world, pos, random);
      if (IRadiationManager.INSTANCE.isRadiationEnabled() && WorldUtils.getTileEntity(world, pos) instanceof ITileRadioactive radioactiveTile) {
         int count = radioactiveTile.getRadiationParticleCount();
         if (count > 0) {
            count = random.m_188503_(count);

            for (int i = 0; i < count; i++) {
               double randX = pos.m_123341_() - 0.1 + random.m_188500_() * 1.2;
               double randY = pos.m_123342_() - 0.1 + random.m_188500_() * 1.2;
               double randZ = pos.m_123343_() - 0.1 + random.m_188500_() * 1.2;
               world.m_7106_((ParticleOptions)MekanismParticleTypes.RADIATION.get(), randX, randY, randZ, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   protected InteractionResult genericClientActivated(@NotNull Player player, @NotNull InteractionHand hand) {
      return !Attribute.has(this, AttributeGui.class) && !MekanismUtils.canUseAsWrench(player.m_21120_(hand))
         ? InteractionResult.PASS
         : InteractionResult.SUCCESS;
   }
}

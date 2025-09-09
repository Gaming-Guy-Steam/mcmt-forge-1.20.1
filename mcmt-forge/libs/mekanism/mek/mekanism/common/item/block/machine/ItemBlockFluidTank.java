package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockFluidTank extends ItemBlockMachine implements IModeItem {
   public ItemBlockFluidTank(BlockFluidTank block) {
      super(block);
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.fluidTank());
   }

   @NotNull
   public FluidTankTier getTier() {
      return Attribute.getTier(this.m_40614_(), FluidTankTier.class);
   }

   @Override
   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      FluidTankTier tier = this.getTier();
      FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
      if (fluidStack.isEmpty()) {
         tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED, new Object[0]));
      } else if (tier == FluidTankTier.CREATIVE) {
         tooltip.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.PINK, new Object[]{fluidStack, EnumColor.GRAY, MekanismLang.INFINITE}));
      } else {
         tooltip.add(
            MekanismLang.GENERIC_STORED_MB
               .translateColored(EnumColor.PINK, new Object[]{fluidStack, EnumColor.GRAY, TextUtils.format((long)fluidStack.getAmount())})
         );
      }

      if (tier == FluidTankTier.CREATIVE) {
         tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, MekanismLang.INFINITE}));
      } else {
         tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format((long)tier.getStorage())}));
      }
   }

   @Override
   protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      tooltip.add(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, new Object[]{BooleanStateDisplay.YesNo.of(this.getBucketMode(stack))}));
      super.addTypeDetails(stack, world, tooltip, flag);
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      return context.m_43723_() != null && !this.getBucketMode(context.m_43722_()) ? super.m_6225_(context) : InteractionResult.PASS;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (this.getBucketMode(stack)) {
         if (SecurityUtils.get().tryClaimItem(world, player, stack)) {
            return InteractionResultHolder.m_19092_(stack, world.f_46443_);
         }

         if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, stack)) {
            return InteractionResultHolder.m_19100_(stack);
         }

         BlockHitResult result = m_41435_(world, player, player.m_6144_() ? Fluid.NONE : Fluid.SOURCE_ONLY);
         if (result.m_6662_() == Type.BLOCK) {
            BlockPos pos = result.m_82425_();
            if (!world.m_7966_(player, pos)) {
               return InteractionResultHolder.m_19100_(stack);
            }

            IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
            if (fluidTank == null) {
               return InteractionResultHolder.m_19100_(stack);
            }

            if (!player.m_6144_()) {
               if (!player.m_36204_(pos, result.m_82434_(), stack)) {
                  return InteractionResultHolder.m_19100_(stack);
               }

               BlockState blockState = world.m_8055_(pos);
               FluidState fluidState = blockState.m_60819_();
               Optional<SoundEvent> sound = Optional.empty();
               if (!fluidState.m_76178_() && fluidState.m_76170_()) {
                  net.minecraft.world.level.material.Fluid fluid = fluidState.m_76152_();
                  FluidStack fluidStack = new FluidStack(fluid, 1000);
                  Block block = blockState.m_60734_();
                  if (block instanceof IFluidBlock fluidBlock) {
                     fluidStack = fluidBlock.drain(world, pos, FluidAction.SIMULATE);
                     if (!validFluid(fluidTank, fluidStack)) {
                        return InteractionResultHolder.m_19098_(stack);
                     }

                     fluidStack = fluidBlock.drain(world, pos, FluidAction.EXECUTE);
                  } else if (block instanceof BucketPickup bucketPickup && validFluid(fluidTank, fluidStack)) {
                     ItemStack pickedUpStack = bucketPickup.m_142598_(world, pos, blockState);
                     if (pickedUpStack.m_41619_()) {
                        return InteractionResultHolder.m_19098_(stack);
                     }

                     if (pickedUpStack.m_41720_() instanceof BucketItem bucket) {
                        fluid = bucket.getFluid();
                        fluidStack = new FluidStack(fluid, 1000);
                        if (!validFluid(fluidTank, fluidStack)) {
                           Mekanism.logger
                              .warn(
                                 "Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                 new Object[]{RegistryUtils.getName(fluidState.m_76152_()), pos, world.m_46472_().m_135782_(), RegistryUtils.getName(fluid)}
                              );
                           return InteractionResultHolder.m_19100_(stack);
                        }
                     }

                     sound = bucketPickup.getPickupSound(blockState);
                  }

                  if (validFluid(fluidTank, fluidStack)) {
                     this.uncheckedGrow(fluidTank, fluidStack);
                     WorldUtils.playFillSound(player, world, pos, fluidStack, sound.orElse(null));
                     world.m_142346_(player, GameEvent.f_157816_, pos);
                     return InteractionResultHolder.m_19090_(stack);
                  }

                  return InteractionResultHolder.m_19100_(stack);
               }
            } else {
               if (fluidTank.extract(1000, Action.SIMULATE, AutomationType.MANUAL).getAmount() < 1000
                  || !player.m_36204_(pos.m_121945_(result.m_82434_()), result.m_82434_(), stack)) {
                  return InteractionResultHolder.m_19100_(stack);
               }

               if (WorldUtils.tryPlaceContainedLiquid(player, world, pos, fluidTank.getFluid(), result.m_82434_())) {
                  if (!player.m_7500_()) {
                     MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(1000, Action.EXECUTE), 1000L);
                  }

                  world.m_142346_(player, GameEvent.f_157769_, pos);
                  return InteractionResultHolder.m_19090_(stack);
               }
            }
         }
      }

      return InteractionResultHolder.m_19098_(stack);
   }

   private void uncheckedGrow(IExtendedFluidTank fluidTank, FluidStack fluidStack) {
      if (this.getTier() != FluidTankTier.CREATIVE) {
         if (fluidTank.isEmpty()) {
            fluidTank.setStack(fluidStack);
         } else {
            MekanismUtils.logMismatchedStackSize(fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE), fluidStack.getAmount());
         }
      }
   }

   private static boolean validFluid(@NotNull IExtendedFluidTank fluidTank, @NotNull FluidStack fluidStack) {
      return !fluidStack.isEmpty() && fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
   }

   private static IExtendedFluidTank getExtendedFluidTank(@NotNull ItemStack stack) {
      Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
      if (capability.isPresent()) {
         IFluidHandlerItem fluidHandlerItem = capability.get();
         if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
            return fluidHandler.getFluidTank(0, null);
         }
      }

      return null;
   }

   public void setBucketMode(ItemStack itemStack, boolean bucketMode) {
      ItemDataUtils.setBoolean(itemStack, "bucketMode", bucketMode);
   }

   public boolean getBucketMode(ItemStack itemStack) {
      return ItemDataUtils.getBoolean(itemStack, "bucketMode");
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(RateLimitFluidHandler.create(this.getTier()));
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      if (Math.abs(shift) % 2 == 1) {
         boolean newState = !this.getBucketMode(stack);
         this.setBucketMode(stack, newState);
         displayChange.sendMessage(player, () -> MekanismLang.BUCKET_MODE.translate(new Object[]{BooleanStateDisplay.OnOff.of(newState, true)}));
      }
   }

   @NotNull
   @Override
   public Component getScrollTextComponent(@NotNull ItemStack stack) {
      return MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, new Object[]{BooleanStateDisplay.OnOff.of(this.getBucketMode(stack), true)});
   }

   public abstract static class BasicCauldronInteraction implements CauldronInteraction {
      public static final ItemBlockFluidTank.BasicCauldronInteraction EMPTY = new ItemBlockFluidTank.BasicCauldronInteraction() {
         @Nullable
         private BlockState getState(FluidStack current) {
            net.minecraft.world.level.material.Fluid type = current.getFluid();
            if (type == Fluids.f_76193_) {
               return (BlockState)Blocks.f_152476_.m_49966_().m_61124_(LayeredCauldronBlock.f_153514_, 3);
            } else {
               return type == Fluids.f_76195_ ? Blocks.f_152477_.m_49966_() : null;
            }
         }

         @NotNull
         @Override
         protected InteractionResult interact(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull ItemStack stack,
            @NotNull IExtendedFluidTank fluidTank
         ) {
            FluidStack fluidStack = fluidTank.getFluid();
            BlockState endState = this.getState(fluidStack);
            if (endState != null && fluidTank.extract(1000, Action.SIMULATE, AutomationType.MANUAL).getAmount() >= 1000) {
               if (!level.f_46443_) {
                  if (!player.m_7500_()) {
                     MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(1000, Action.EXECUTE), 1000L);
                  }

                  player.m_36220_(Stats.f_12943_);
                  player.m_36246_(Stats.f_12982_.m_12902_(stack.m_41720_()));
                  level.m_46597_(pos, endState);
                  SoundEvent emptySound = fluidStack.getFluid().getFluidType().getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
                  if (emptySound != null) {
                     level.m_5594_(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                  }

                  level.m_142346_(null, GameEvent.f_157769_, pos);
               }

               return InteractionResult.m_19078_(level.f_46443_);
            } else {
               return InteractionResult.PASS;
            }
         }
      };

      @NotNull
      public final InteractionResult m_175710_(
         @NotNull BlockState state,
         @NotNull Level level,
         @NotNull BlockPos pos,
         @NotNull Player player,
         @NotNull InteractionHand hand,
         @NotNull ItemStack stack
      ) {
         if (stack.m_41720_() instanceof ItemBlockFluidTank tank && tank.getBucketMode(stack)) {
            IExtendedFluidTank fluidTank = ItemBlockFluidTank.getExtendedFluidTank(stack);
            return fluidTank == null ? InteractionResult.PASS : this.interact(state, level, pos, player, hand, stack, fluidTank);
         } else {
            return InteractionResult.PASS;
         }
      }

      @NotNull
      protected abstract InteractionResult interact(
         @NotNull BlockState state,
         @NotNull Level level,
         @NotNull BlockPos pos,
         @NotNull Player player,
         @NotNull InteractionHand hand,
         @NotNull ItemStack stack,
         @NotNull IExtendedFluidTank fluidTank
      );
   }

   public static class BasicDrainCauldronInteraction extends ItemBlockFluidTank.BasicCauldronInteraction {
      public static final ItemBlockFluidTank.BasicDrainCauldronInteraction WATER = new ItemBlockFluidTank.BasicDrainCauldronInteraction(Fluids.f_76193_) {
         @NotNull
         @Override
         protected InteractionResult interact(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull ItemStack stack,
            @NotNull IExtendedFluidTank fluidTank
         ) {
            return state.m_61143_(LayeredCauldronBlock.f_153514_) == 3
               ? super.interact(state, level, pos, player, hand, stack, fluidTank)
               : InteractionResult.PASS;
         }
      };
      public static final ItemBlockFluidTank.BasicDrainCauldronInteraction LAVA = new ItemBlockFluidTank.BasicDrainCauldronInteraction(Fluids.f_76195_);
      private final net.minecraft.world.level.material.Fluid type;

      private BasicDrainCauldronInteraction(net.minecraft.world.level.material.Fluid type) {
         this.type = type;
      }

      @NotNull
      @Override
      protected InteractionResult interact(
         @NotNull BlockState state,
         @NotNull Level level,
         @NotNull BlockPos pos,
         @NotNull Player player,
         @NotNull InteractionHand hand,
         @NotNull ItemStack stack,
         @NotNull IExtendedFluidTank fluidTank
      ) {
         FluidStack fluidStack = new FluidStack(this.type, 1000);
         FluidStack remainder = fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL);
         if (remainder.isEmpty()) {
            if (!level.f_46443_) {
               if (!player.m_7500_()) {
                  ((ItemBlockFluidTank)stack.m_41720_()).uncheckedGrow(fluidTank, fluidStack);
               }

               player.m_36220_(Stats.f_12944_);
               player.m_36246_(Stats.f_12982_.m_12902_(stack.m_41720_()));
               level.m_46597_(pos, Blocks.f_50256_.m_49966_());
               SoundEvent fillSound = fluidStack.getFluid().getFluidType().getSound(null, level, pos, SoundActions.BUCKET_FILL);
               if (fillSound != null) {
                  level.m_5594_(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
               }

               level.m_142346_(null, GameEvent.f_157816_, pos);
            }

            return InteractionResult.m_19078_(level.f_46443_);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public static class FluidTankItemDispenseBehavior extends DefaultDispenseItemBehavior {
      public static final ItemBlockFluidTank.FluidTankItemDispenseBehavior INSTANCE = new ItemBlockFluidTank.FluidTankItemDispenseBehavior();

      private FluidTankItemDispenseBehavior() {
      }

      @NotNull
      public ItemStack m_7498_(@NotNull BlockSource source, @NotNull ItemStack stack) {
         if (stack.m_41720_() instanceof ItemBlockFluidTank tank && tank.getBucketMode(stack)) {
            IExtendedFluidTank fluidTank = ItemBlockFluidTank.getExtendedFluidTank(stack);
            if (fluidTank == null) {
               return super.m_7498_(source, stack);
            }

            Level world = source.m_7727_();
            BlockPos pos = source.m_7961_().m_121945_((Direction)source.m_6414_().m_61143_(DispenserBlock.f_52659_));
            BlockState blockState = world.m_8055_(pos);
            FluidState fluidState = blockState.m_60819_();
            Optional<SoundEvent> sound = Optional.empty();
            if (!fluidState.m_76178_() && fluidState.m_76170_()) {
               net.minecraft.world.level.material.Fluid fluid = fluidState.m_76152_();
               FluidStack fluidStack = new FluidStack(fluid, 1000);
               Block block = blockState.m_60734_();
               if (block instanceof IFluidBlock fluidBlock) {
                  fluidStack = fluidBlock.drain(world, pos, FluidAction.SIMULATE);
                  if (!ItemBlockFluidTank.validFluid(fluidTank, fluidStack)) {
                     return super.m_7498_(source, stack);
                  }

                  fluidStack = fluidBlock.drain(world, pos, FluidAction.EXECUTE);
               } else if (block instanceof BucketPickup bucketPickup && ItemBlockFluidTank.validFluid(fluidTank, fluidStack)) {
                  ItemStack pickedUpStack = bucketPickup.m_142598_(world, pos, blockState);
                  if (pickedUpStack.m_41619_()) {
                     return super.m_7498_(source, stack);
                  }

                  if (pickedUpStack.m_41720_() instanceof BucketItem bucket) {
                     fluid = bucket.getFluid();
                     fluidStack = new FluidStack(fluid, 1000);
                     if (!ItemBlockFluidTank.validFluid(fluidTank, fluidStack)) {
                        Mekanism.logger
                           .warn(
                              "Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                              new Object[]{RegistryUtils.getName(fluidState.m_76152_()), pos, world.m_46472_().m_135782_(), RegistryUtils.getName(fluid)}
                           );
                        return super.m_7498_(source, stack);
                     }
                  }

                  sound = bucketPickup.getPickupSound(blockState);
               }

               if (ItemBlockFluidTank.validFluid(fluidTank, fluidStack)) {
                  tank.uncheckedGrow(fluidTank, fluidStack);
                  WorldUtils.playFillSound(null, world, pos, fluidStack, sound.orElse(null));
                  world.m_142346_(null, GameEvent.f_157816_, pos);
                  return stack;
               }
            } else if (fluidTank.extract(1000, Action.SIMULATE, AutomationType.MANUAL).getAmount() >= 1000
               && WorldUtils.tryPlaceContainedLiquid(null, world, pos, fluidTank.getFluid(), null)) {
               MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(1000, Action.EXECUTE), 1000L);
               world.m_142346_(null, GameEvent.f_157769_, pos);
               return stack;
            }
         }

         return super.m_7498_(source, stack);
      }
   }
}

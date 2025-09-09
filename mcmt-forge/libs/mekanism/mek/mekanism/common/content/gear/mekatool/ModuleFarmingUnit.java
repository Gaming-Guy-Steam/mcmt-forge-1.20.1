package mekanism.common.content.gear.mekatool;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleFarmingUnit implements ICustomModule<ModuleFarmingUnit> {
   private IModuleConfigItem<ModuleFarmingUnit.FarmingRadius> farmingRadius;

   @Override
   public void init(IModule<ModuleFarmingUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.farmingRadius = configItemCreator.createConfigItem(
         "farming_radius", MekanismLang.MODULE_FARMING_RADIUS, new ModuleEnumData<>(ModuleFarmingUnit.FarmingRadius.LOW, module.getInstalledCount() + 1)
      );
   }

   @NotNull
   @Override
   public InteractionResult onItemUse(IModule<ModuleFarmingUnit> module, UseOnContext context) {
      Player player = context.m_43723_();
      if (player != null && !player.m_6144_()) {
         int diameter = this.farmingRadius.get().getRadius();
         if (diameter == 0) {
            return InteractionResult.PASS;
         } else {
            ItemStack stack = context.m_43722_();
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer == null) {
               return InteractionResult.FAIL;
            } else {
               Lazy<BlockState> lazyClickedState = Lazy.of(() -> context.m_43725_().m_8055_(context.m_8083_()));
               return MekanismUtils.performActions(
                  this.useAxeAOE(context, lazyClickedState, energyContainer, diameter, ToolActions.AXE_STRIP, SoundEvents.f_11688_, -1),
                  () -> this.useAxeAOE(context, lazyClickedState, energyContainer, diameter, ToolActions.AXE_SCRAPE, SoundEvents.f_144059_, 3005),
                  () -> this.useAxeAOE(context, lazyClickedState, energyContainer, diameter, ToolActions.AXE_WAX_OFF, SoundEvents.f_144060_, 3004),
                  () -> this.flattenAOE(context, lazyClickedState, energyContainer, diameter),
                  () -> this.dowseCampfire(context, lazyClickedState, energyContainer),
                  () -> this.tillAOE(context, lazyClickedState, energyContainer, diameter)
               );
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public boolean canPerformAction(IModule<ModuleFarmingUnit> module, ToolAction action) {
      if (action == ToolActions.AXE_STRIP || action == ToolActions.AXE_SCRAPE || action == ToolActions.AXE_WAX_OFF) {
         return module.hasEnoughEnergy(MekanismConfig.gear.mekaToolEnergyUsageAxe);
      } else if (action == ToolActions.SHOVEL_FLATTEN) {
         return module.hasEnoughEnergy(MekanismConfig.gear.mekaToolEnergyUsageShovel);
      } else {
         return action == ToolActions.HOE_TILL
            ? module.hasEnoughEnergy(MekanismConfig.gear.mekaToolEnergyUsageHoe)
            : ToolActions.DEFAULT_AXE_ACTIONS.contains(action)
               || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action)
               || ToolActions.DEFAULT_HOE_ACTIONS.contains(action);
      }
   }

   private InteractionResult dowseCampfire(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer) {
      FloatingLong energy = energyContainer.getEnergy();
      FloatingLong energyUsage = MekanismConfig.gear.mekaToolEnergyUsageShovel.get();
      if (energy.smallerThan(energyUsage)) {
         return InteractionResult.FAIL;
      } else {
         BlockState clickedState = (BlockState)lazyClickedState.get();
         if (clickedState.m_60734_() instanceof CampfireBlock && (Boolean)clickedState.m_61143_(CampfireBlock.f_51227_)) {
            Level world = context.m_43725_();
            BlockPos pos = context.m_8083_();
            if (!world.m_5776_()) {
               world.m_5898_(null, 1009, pos, 0);
            }

            CampfireBlock.m_152749_(context.m_43723_(), world, pos, clickedState);
            if (!world.m_5776_()) {
               world.m_7731_(pos, (BlockState)clickedState.m_61124_(CampfireBlock.f_51227_, Boolean.FALSE), 11);
               energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.MANUAL);
            }

            return InteractionResult.m_19078_(world.f_46443_);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   private InteractionResult tillAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter) {
      return this.useAOE(
         context,
         lazyClickedState,
         energyContainer,
         diameter,
         ToolActions.HOE_TILL,
         SoundEvents.f_11955_,
         -1,
         MekanismConfig.gear.mekaToolEnergyUsageHoe.get(),
         new ModuleFarmingUnit.HoeToolAOEData()
      );
   }

   private InteractionResult flattenAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter) {
      Direction sideHit = context.m_43719_();
      return sideHit == Direction.DOWN
         ? InteractionResult.PASS
         : this.useAOE(
            context,
            lazyClickedState,
            energyContainer,
            diameter,
            ToolActions.SHOVEL_FLATTEN,
            SoundEvents.f_12406_,
            -1,
            MekanismConfig.gear.mekaToolEnergyUsageShovel.get(),
            new ModuleFarmingUnit.ShovelToolAOEData()
         );
   }

   private InteractionResult useAxeAOE(
      UseOnContext context,
      Lazy<BlockState> lazyClickedState,
      IEnergyContainer energyContainer,
      int diameter,
      ToolAction action,
      SoundEvent sound,
      int particle
   ) {
      return this.useAOE(
         context,
         lazyClickedState,
         energyContainer,
         diameter,
         action,
         sound,
         particle,
         MekanismConfig.gear.mekaToolEnergyUsageAxe.get(),
         new ModuleFarmingUnit.AxeToolAOEData()
      );
   }

   private InteractionResult useAOE(
      UseOnContext context,
      Lazy<BlockState> lazyClickedState,
      IEnergyContainer energyContainer,
      int diameter,
      ToolAction action,
      SoundEvent sound,
      int particle,
      FloatingLong energyUsage,
      ModuleFarmingUnit.IToolAOEData toolAOEData
   ) {
      FloatingLong energy = energyContainer.getEnergy();
      if (energy.smallerThan(energyUsage)) {
         return InteractionResult.FAIL;
      } else {
         Level world = context.m_43725_();
         BlockPos pos = context.m_8083_();
         BlockState clickedState = (BlockState)lazyClickedState.get();
         if (!toolAOEData.isValid(world, pos, clickedState)) {
            return InteractionResult.PASS;
         } else {
            BlockState modifiedState = clickedState.getToolModifiedState(context, action, false);
            if (modifiedState == null) {
               return InteractionResult.PASS;
            } else if (world.f_46443_) {
               return InteractionResult.SUCCESS;
            } else {
               world.m_7731_(pos, modifiedState, 11);
               world.m_5594_(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
               if (particle != -1) {
                  world.m_5898_(null, particle, pos, 0);
               }

               Direction side = context.m_43719_();
               toolAOEData.persistData(world, pos, clickedState, side);
               FloatingLong energyUsed = energyUsage;

               for (BlockPos newPos : toolAOEData.getTargetPositions(pos, side, (diameter - 1) / 2)) {
                  if (!pos.equals(newPos)) {
                     FloatingLong nextEnergyUsed = energyUsed.add(energyUsage);
                     if (nextEnergyUsed.greaterThan(energy)) {
                        break;
                     }

                     BlockState state = world.m_8055_(newPos);
                     UseOnContext adjustedContext = new UseOnContext(
                        world,
                        context.m_43723_(),
                        context.m_43724_(),
                        context.m_43722_(),
                        new BlockHitResult(
                           context.m_43720_()
                              .m_82520_(newPos.m_123341_() - pos.m_123341_(), newPos.m_123342_() - pos.m_123342_(), newPos.m_123343_() - pos.m_123343_()),
                           context.m_43719_(),
                           newPos,
                           context.m_43721_()
                        )
                     );
                     if (toolAOEData.isValid(world, newPos, state) && modifiedState == state.getToolModifiedState(adjustedContext, action, true)) {
                        newPos = newPos.m_7949_();
                        energyUsed = nextEnergyUsed;
                        state.getToolModifiedState(adjustedContext, action, false);
                        world.m_7731_(newPos, modifiedState, 11);
                        world.m_5594_(null, newPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (particle != -1) {
                           world.m_5898_(null, particle, newPos, 0);
                        }

                        Mekanism.packetHandler()
                           .sendToAllTracking(
                              new PacketLightningRender(
                                 PacketLightningRender.LightningPreset.TOOL_AOE,
                                 Objects.hash(pos, newPos),
                                 toolAOEData.getLightningPos(pos),
                                 toolAOEData.getLightningPos(newPos),
                                 10
                              ),
                              world,
                              pos
                           );
                     }
                  }
               }

               energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
               return InteractionResult.CONSUME;
            }
         }
      }
   }

   private static class AxeToolAOEData implements ModuleFarmingUnit.IToolAOEData {
      @Nullable
      private Axis axis;
      private boolean isSet;
      private Vec3 offset = Vec3.f_82478_;

      @Override
      public boolean isValid(Level level, BlockPos blockPos, BlockState state) {
         return !this.isSet || this.axis == this.getAxis(state);
      }

      @Override
      public void persistData(Level level, BlockPos pos, BlockState state, Direction side) {
         this.axis = this.getAxis(state);
         this.isSet = true;
         this.offset = Vec3.m_82528_(side.m_122436_()).m_82490_(0.5);
      }

      @Override
      public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
         Vec3i adjustment = switch (side) {
            case EAST, WEST -> new Vec3i(0, radius, radius);
            case UP, DOWN -> new Vec3i(radius, 0, radius);
            case SOUTH, NORTH -> new Vec3i(radius, radius, 0);
            default -> throw new IncompatibleClassChangeError();
         };
         AABB box = new AABB(pos.m_121996_(adjustment), pos.m_121955_(adjustment));
         return BlockPos.m_121940_(BlockPos.m_274561_(box.f_82288_, box.f_82289_, box.f_82290_), BlockPos.m_274561_(box.f_82291_, box.f_82292_, box.f_82293_));
      }

      @Nullable
      private Axis getAxis(BlockState state) {
         return state.m_61138_(RotatedPillarBlock.f_55923_) ? (Axis)state.m_61143_(RotatedPillarBlock.f_55923_) : null;
      }

      @Override
      public Vec3 getLightningPos(BlockPos pos) {
         return Vec3.m_82512_(pos).m_82549_(this.offset);
      }
   }

   @NothingNullByDefault
   public static enum FarmingRadius implements IHasTextComponent {
      OFF(0),
      LOW(1),
      MED(3),
      HIGH(5),
      ULTRA(7);

      private final int radius;
      private final Component label;

      private FarmingRadius(int radius) {
         this.radius = radius;
         this.label = TextComponentUtil.getString(Integer.toString(radius));
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public int getRadius() {
         return this.radius;
      }
   }

   private abstract static class FlatToolAOEData implements ModuleFarmingUnit.IToolAOEData {
      @Override
      public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
         return BlockPos.m_121940_(pos.m_7918_(-radius, 0, -radius), pos.m_7918_(radius, 0, radius));
      }

      @Override
      public Vec3 getLightningPos(BlockPos pos) {
         return Vec3.m_82514_(pos, 0.94);
      }
   }

   private static class HoeToolAOEData extends ModuleFarmingUnit.FlatToolAOEData {
      @Override
      public boolean isValid(Level level, BlockPos pos, BlockState state) {
         return true;
      }
   }

   private interface IToolAOEData {
      boolean isValid(Level level, BlockPos pos, BlockState state);

      default void persistData(Level level, BlockPos pos, BlockState state, Direction side) {
      }

      Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius);

      Vec3 getLightningPos(BlockPos pos);
   }

   private static class ShovelToolAOEData extends ModuleFarmingUnit.FlatToolAOEData {
      @Override
      public boolean isValid(Level level, BlockPos pos, BlockState state) {
         BlockPos abovePos = pos.m_7494_();
         BlockState aboveState = level.m_8055_(abovePos);
         if (aboveState.m_60795_()) {
            return true;
         } else {
            return aboveState.m_204336_(MekanismTags.Blocks.FARMING_OVERRIDE) || aboveState.m_247087_() && aboveState.m_60734_() instanceof IPlantable
               ? aboveState.m_60819_().m_76178_() && !aboveState.m_60804_(level, abovePos)
               : false;
         }
      }
   }
}

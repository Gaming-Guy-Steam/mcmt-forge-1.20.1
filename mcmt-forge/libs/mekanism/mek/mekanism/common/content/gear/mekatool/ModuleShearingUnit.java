package mekanism.common.content.gear.mekatool;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity.BeeReleaseStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {
   private static final Predicate<Entity> SHEARABLE = entity -> !entity.m_5833_() && entity instanceof IForgeShearable;

   @Override
   public boolean canPerformAction(IModule<ModuleShearingUnit> module, ToolAction action) {
      if (action == ToolActions.SHEARS_DISARM) {
         ItemStack container = module.getContainer();
         if (container.m_41720_() instanceof ItemMekaTool mekaTool) {
            FloatingLong cost = mekaTool.getDestroyEnergy(container, 0.0F, mekaTool.isModuleEnabled(container, MekanismModules.SILK_TOUCH_UNIT));
            return module.hasEnoughEnergy(cost);
         } else {
            return true;
         }
      } else if (action != ToolActions.SHEARS_DIG) {
         return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(action);
      } else {
         ItemStack container = module.getContainer();
         return !(container.m_41720_() instanceof ItemMekaTool mekaTool && !mekaTool.hasEnergyForDigAction(container));
      }
   }

   @NotNull
   @Override
   public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand) {
      if (entity instanceof IForgeShearable) {
         FloatingLong cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
         IEnergyContainer energyContainer = module.getEnergyContainer();
         if (cost.isZero()
            || energyContainer != null
               && energyContainer.getEnergy().greaterOrEqual(cost)
               && this.shearEntity(energyContainer, entity, player, module.getContainer(), entity.m_9236_(), entity.m_20183_())) {
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   @NotNull
   @Override
   public ICustomModule.ModuleDispenseResult onDispense(IModule<ModuleShearingUnit> module, BlockSource source) {
      ServerLevel world = source.m_7727_();
      Direction facing = (Direction)source.m_6414_().m_61143_(DispenserBlock.f_52659_);
      BlockPos pos = source.m_7961_().m_121945_(facing);
      return !this.tryShearBlock(world, pos, facing.m_122424_()) && !this.tryShearLivingEntity(module.getEnergyContainer(), world, pos, module.getContainer())
         ? ICustomModule.ModuleDispenseResult.FAIL_PREVENT_DROP
         : ICustomModule.ModuleDispenseResult.HANDLED;
   }

   private boolean tryShearBlock(ServerLevel world, BlockPos pos, Direction sideClicked) {
      BlockState state = world.m_8055_(pos);
      if (state.m_204336_(BlockTags.f_13072_) && state.m_60734_() instanceof BeehiveBlock beehive && (Integer)state.m_61143_(BeehiveBlock.f_49564_) >= 5) {
         world.m_5594_(null, pos, SoundEvents.f_11697_, SoundSource.BLOCKS, 1.0F, 1.0F);
         BeehiveBlock.m_49600_(world, pos);
         beehive.m_49594_(world, state, pos, null, BeeReleaseStatus.BEE_RELEASED);
         return true;
      } else if (state.m_60713_(Blocks.f_50133_)) {
         Direction side = sideClicked.m_122434_() == Axis.Y ? Direction.NORTH : sideClicked;
         world.m_5594_(null, pos, SoundEvents.f_12296_, SoundSource.BLOCKS, 1.0F, 1.0F);
         world.m_7731_(pos, (BlockState)Blocks.f_50143_.m_49966_().m_61124_(CarvedPumpkinBlock.f_51367_, side), 11);
         Block.m_49840_(world, pos, new ItemStack(Items.f_42577_, 4));
         return true;
      } else {
         return false;
      }
   }

   private boolean tryShearLivingEntity(@Nullable IEnergyContainer energyContainer, ServerLevel world, BlockPos pos, ItemStack stack) {
      FloatingLong cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
      if (cost.isZero() || energyContainer != null && energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get())) {
         for (LivingEntity entity : world.m_6443_(LivingEntity.class, new AABB(pos), SHEARABLE)) {
            if (this.shearEntity(energyContainer, entity, null, stack, world, pos)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean shearEntity(
      @Nullable IEnergyContainer energyContainer, LivingEntity entity, @Nullable Player player, ItemStack stack, Level world, BlockPos pos
   ) {
      IForgeShearable target = (IForgeShearable)entity;
      if (!target.isShearable(stack, world, pos)) {
         return false;
      } else {
         if (!world.f_46443_) {
            for (ItemStack drop : target.onSheared(player, stack, world, pos, stack.getEnchantmentLevel(Enchantments.f_44987_))) {
               ItemEntity ent = entity.m_5552_(drop, 1.0F);
               if (ent != null) {
                  ent.m_20256_(
                     ent.m_20184_()
                        .m_82520_(
                           (world.f_46441_.m_188501_() - world.f_46441_.m_188501_()) * 0.1F,
                           world.f_46441_.m_188501_() * 0.05F,
                           (world.f_46441_.m_188501_() - world.f_46441_.m_188501_()) * 0.1F
                        )
                  );
               }
            }

            if (energyContainer != null) {
               energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get(), Action.EXECUTE, AutomationType.MANUAL);
            }
         }

         return true;
      }
   }
}

package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.mekatool.ModuleBlastingUnit;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaTool extends ItemEnergized implements IModuleContainerItem, IBlastingItem, IGenericRadialModeItem {
   private static final ResourceLocation RADIAL_ID = Mekanism.rl("meka_tool");
   private final Int2ObjectMap<AttributeCache> attributeCaches = new Int2ObjectArrayMap(ModuleAttackAmplificationUnit.AttackDamage.values().length);

   public ItemMekaTool(Properties properties) {
      super(MekanismConfig.gear.mekaToolBaseChargeRate, MekanismConfig.gear.mekaToolBaseEnergyCapacity, properties.m_41497_(Rarity.EPIC).setNoRepair());
   }

   public boolean m_8096_(@NotNull BlockState state) {
      return true;
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         this.addModuleDetails(stack, tooltip);
      } else {
         StorageUtils.addStoredEnergy(stack, tooltip, true);
         tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }

   public boolean canPerformAction(ItemStack stack, ToolAction action) {
      return ItemAtomicDisassembler.ALWAYS_SUPPORTED_ACTIONS.contains(action)
         ? this.hasEnergyForDigAction(stack)
         : this.getModules(stack).stream().anyMatch(module -> module.isEnabled() && this.canPerformAction(module, action));
   }

   private <MODULE extends ICustomModule<MODULE>> boolean canPerformAction(IModule<MODULE> module, ToolAction action) {
      return module.getCustomInstance().canPerformAction(module, action);
   }

   public boolean hasEnergyForDigAction(ItemStack stack) {
      IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
      if (energyContainer == null) {
         return false;
      } else {
         FloatingLong energyRequired = this.getDestroyEnergy(stack, 0.0F, this.isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
         FloatingLong energyAvailable = energyContainer.getEnergy();
         return energyRequired.smallerOrEqual(energyAvailable) || !energyAvailable.divide(energyRequired).isZero();
      }
   }

   public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
      return super.isNotReplaceableByPickAction(stack, player, inventorySlot) || ItemDataUtils.hasData(stack, "modules", 10);
   }

   public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
      if (stack.m_41619_()) {
         return 0;
      } else {
         ListTag enchantments = ItemDataUtils.getList(stack, "Enchantments");
         return Math.max(MekanismUtils.getEnchantmentLevel(enchantments, enchantment), super.getEnchantmentLevel(stack, enchantment));
      }
   }

   public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.m_44882_(ItemDataUtils.getList(stack, "Enchantments"));
      super.getAllEnchantments(stack).forEach((enchantment, level) -> enchantments.merge(enchantment, level, Math::max));
      return enchantments;
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      for (Module<?> module : this.getModules(context.m_43722_())) {
         if (module.isEnabled()) {
            InteractionResult result = this.onModuleUse(module, context);
            if (result != InteractionResult.PASS) {
               return result;
            }
         }
      }

      return super.m_6225_(context);
   }

   private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleUse(IModule<MODULE> module, UseOnContext context) {
      return module.getCustomInstance().onItemUse(module, context);
   }

   @NotNull
   public InteractionResult m_6880_(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
      for (Module<?> module : this.getModules(stack)) {
         if (module.isEnabled()) {
            InteractionResult result = this.onModuleInteract(module, player, entity, hand);
            if (result != InteractionResult.PASS) {
               return result;
            }
         }
      }

      return super.m_6880_(stack, player, entity, hand);
   }

   private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleInteract(
      IModule<MODULE> module, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand
   ) {
      return module.getCustomInstance().onInteract(module, player, entity, hand);
   }

   public float m_8102_(@NotNull ItemStack stack, @NotNull BlockState state) {
      IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
      if (energyContainer == null) {
         return 0.0F;
      } else {
         FloatingLong energyRequired = this.getDestroyEnergy(stack, state.f_60599_, this.isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
         FloatingLong energyAvailable = energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL);
         if (energyAvailable.smallerThan(energyRequired)) {
            return MekanismConfig.gear.mekaToolBaseEfficiency.get() * energyAvailable.divide(energyRequired).floatValue();
         } else {
            IModule<ModuleExcavationEscalationUnit> module = this.getModule(stack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
            return module != null && module.isEnabled() ? module.getCustomInstance().getEfficiency() : MekanismConfig.gear.mekaToolBaseEfficiency.get();
         }
      }
   }

   public boolean m_6813_(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityliving) {
      IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
      if (energyContainer != null) {
         FloatingLong energyRequired = this.getDestroyEnergy(stack, state.m_60800_(world, pos), this.isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
         energyContainer.extract(energyRequired, Action.EXECUTE, AutomationType.MANUAL);
      }

      return true;
   }

   public boolean m_7579_(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
      IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = this.getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
      if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
         int unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
         if (unitDamage > 0) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer != null && !energyContainer.isEmpty()) {
               energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply(unitDamage / 4.0), Action.EXECUTE, AutomationType.MANUAL);
            }
         }
      }

      return true;
   }

   @Override
   public Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state) {
      if (!player.m_6144_()) {
         IModule<ModuleBlastingUnit> blastingUnit = this.getModule(stack, MekanismModules.BLASTING_UNIT);
         if (blastingUnit != null && blastingUnit.isEnabled()) {
            int radius = blastingUnit.getCustomInstance().getBlastRadius();
            if (radius > 0 && IBlastingItem.canBlastBlock(world, pos, state)) {
               return IBlastingItem.findPositions(world, pos, player, radius);
            }
         }
      }

      return Collections.emptyMap();
   }

   private Object2IntMap<BlockPos> getVeinedBlocks(Level world, ItemStack stack, Map<BlockPos, BlockState> blocks, Reference2BooleanMap<Block> oreTracker) {
      IModule<ModuleVeinMiningUnit> veinMiningUnit = this.getModule(stack, MekanismModules.VEIN_MINING_UNIT);
      if (veinMiningUnit != null && veinMiningUnit.isEnabled()) {
         ModuleVeinMiningUnit customInstance = veinMiningUnit.getCustomInstance();
         return ModuleVeinMiningUnit.findPositions(world, blocks, customInstance.isExtended() ? customInstance.getExcavationRange() : 0, oreTracker);
      } else {
         return blocks.entrySet().stream().collect(Collectors.toMap(Entry::getKey, be -> 0, (l, r) -> l, Object2IntArrayMap::new));
      }
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      if (!player.m_9236_().f_46443_ && !player.m_7500_()) {
         IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
         if (energyContainer != null) {
            Level world = player.m_9236_();
            BlockState state = world.m_8055_(pos);
            boolean silk = this.isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT);
            FloatingLong modDestroyEnergy = this.getDestroyEnergy(stack, silk);
            FloatingLong energyRequired = this.getDestroyEnergy(modDestroyEnergy, state.m_60800_(world, pos));
            if (energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).greaterOrEqual(energyRequired)) {
               Map<BlockPos, BlockState> blocks = this.getBlastedBlocks(world, player, stack, pos, state);
               blocks = blocks.isEmpty() && ModuleVeinMiningUnit.canVeinBlock(state) ? Map.of(pos, state) : blocks;
               Reference2BooleanMap<Block> oreTracker = blocks.values()
                  .stream()
                  .collect(
                     Collectors.toMap(
                        BlockStateBase::m_60734_, bs -> bs.m_204336_(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE), (l, r) -> l, Reference2BooleanArrayMap::new
                     )
                  );
               Object2IntMap<BlockPos> veinedBlocks = this.getVeinedBlocks(world, stack, blocks, oreTracker);
               if (!veinedBlocks.isEmpty()) {
                  FloatingLong baseDestroyEnergy = this.getDestroyEnergy(silk);
                  MekanismUtils.veinMineArea(
                     energyContainer,
                     energyRequired,
                     world,
                     pos,
                     (ServerPlayer)player,
                     stack,
                     this,
                     veinedBlocks,
                     hardness -> this.getDestroyEnergy(modDestroyEnergy, hardness),
                     (hardness, distance, bs) -> this.getDestroyEnergy(baseDestroyEnergy, hardness)
                        .multiply(0.5 * Math.pow(distance, oreTracker.getBoolean(bs.m_60734_()) ? 1.5 : 2.0))
                  );
               }
            }
         }

         return super.onBlockStartBreak(stack, pos, player);
      } else {
         return super.onBlockStartBreak(stack, pos, player);
      }
   }

   private FloatingLong getDestroyEnergy(boolean silk) {
      return silk ? MekanismConfig.gear.mekaToolEnergyUsageSilk.get() : MekanismConfig.gear.mekaToolEnergyUsage.get();
   }

   public FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness, boolean silk) {
      return this.getDestroyEnergy(this.getDestroyEnergy(itemStack, silk), hardness);
   }

   private FloatingLong getDestroyEnergy(FloatingLong baseDestroyEnergy, float hardness) {
      return hardness == 0.0F ? baseDestroyEnergy.divide(2L) : baseDestroyEnergy;
   }

   private FloatingLong getDestroyEnergy(ItemStack itemStack, boolean silk) {
      FloatingLong destroyEnergy = this.getDestroyEnergy(silk);
      IModule<ModuleExcavationEscalationUnit> module = this.getModule(itemStack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
      float efficiency = module != null && module.isEnabled() ? module.getCustomInstance().getEfficiency() : MekanismConfig.gear.mekaToolBaseEfficiency.get();
      return destroyEnergy.multiply((double)efficiency);
   }

   @NotNull
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
      if (slot == EquipmentSlot.MAINHAND) {
         int unitDamage = 0;
         IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = this.getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
         if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
            unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
            if (unitDamage > 0) {
               FloatingLong energyCost = MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply(unitDamage / 4.0);
               IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
               FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
               if (energy.smallerThan(energyCost)) {
                  double bonusDamage = unitDamage * energy.divideToLevel(energyCost);
                  if (bonusDamage > 0.0) {
                     Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                     builder.put(
                        Attributes.f_22281_,
                        new AttributeModifier(f_41374_, "Weapon modifier", MekanismConfig.gear.mekaToolBaseDamage.get() + bonusDamage, Operation.ADDITION)
                     );
                     builder.put(
                        Attributes.f_22283_,
                        new AttributeModifier(f_41375_, "Weapon modifier", MekanismConfig.gear.mekaToolAttackSpeed.get(), Operation.ADDITION)
                     );
                     return builder.build();
                  }

                  unitDamage = 0;
               }
            }
         }

         return ((AttributeCache)this.attributeCaches
               .computeIfAbsent(
                  unitDamage,
                  damage -> new AttributeCache(
                     builderx -> {
                        builderx.put(
                           Attributes.f_22281_,
                           new AttributeModifier(f_41374_, "Weapon modifier", MekanismConfig.gear.mekaToolBaseDamage.get() + damage, Operation.ADDITION)
                        );
                        builderx.put(
                           Attributes.f_22283_,
                           new AttributeModifier(f_41375_, "Weapon modifier", MekanismConfig.gear.mekaToolAttackSpeed.get(), Operation.ADDITION)
                        );
                     },
                     MekanismConfig.gear.mekaToolBaseDamage,
                     MekanismConfig.gear.mekaToolAttackSpeed
                  )
               ))
            .get();
      } else {
         return super.getAttributeModifiers(slot, stack);
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!world.m_5776_()) {
         IModule<ModuleTeleportationUnit> module = this.getModule(stack, MekanismModules.TELEPORTATION_UNIT);
         if (module != null && module.isEnabled()) {
            BlockHitResult result = MekanismUtils.rayTrace(player, MekanismConfig.gear.mekaToolMaxTeleportReach.get());
            if (!module.getCustomInstance().requiresBlockTarget() || result.m_6662_() != Type.MISS) {
               BlockPos pos = result.m_82425_();
               if (this.isValidDestinationBlock(world, pos.m_7494_()) && this.isValidDestinationBlock(world, pos.m_6630_(2))) {
                  double distance = player.m_20275_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
                  if (distance < 5.0) {
                     return InteractionResultHolder.m_19098_(stack);
                  }

                  IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                  FloatingLong energyNeeded = MekanismConfig.gear.mekaToolEnergyUsageTeleport.get().multiply(distance / 10.0);
                  if (energyContainer != null && !energyContainer.getEnergy().smallerThan(energyNeeded)) {
                     double targetX = pos.m_123341_() + 0.5;
                     double targetY = pos.m_123342_() + 1.5;
                     double targetZ = pos.m_123343_() + 0.5;
                     MekanismTeleportEvent.MekaTool event = new MekanismTeleportEvent.MekaTool(player, targetX, targetY, targetZ, stack, result);
                     if (MinecraftForge.EVENT_BUS.post(event)) {
                        return InteractionResultHolder.m_19100_(stack);
                     }

                     energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                     if (player.m_20159_()) {
                        player.m_142098_(targetX, targetY, targetZ);
                     } else {
                        player.m_6021_(targetX, targetY, targetZ);
                     }

                     player.m_183634_();
                     Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(pos.m_7494_()), world, pos);
                     world.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11852_, SoundSource.PLAYERS, 1.0F, 1.0F);
                     return InteractionResultHolder.m_19090_(stack);
                  }

                  return InteractionResultHolder.m_19100_(stack);
               }
            }
         }
      }

      return InteractionResultHolder.m_19098_(stack);
   }

   private boolean isValidDestinationBlock(Level world, BlockPos pos) {
      BlockState blockState = world.m_8055_(pos);
      return blockState.m_60795_() || MekanismUtils.isLiquidBlock(blockState.m_60734_());
   }

   public boolean m_8120_(@NotNull ItemStack stack) {
      return false;
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return false;
   }

   @Override
   public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
      return IGenericRadialModeItem.super.supportsSlotType(stack, slotType) && this.getModules(stack).stream().anyMatch(Module::handlesAnyModeChange);
   }

   @Nullable
   @Override
   public Component getScrollTextComponent(@NotNull ItemStack stack) {
      return this.getModules(stack).stream().filter(Module::handlesModeChange).findFirst().map(module -> module.getModeScrollComponent(stack)).orElse(null);
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      for (Module<?> module : this.getModules(stack)) {
         if (module.handlesModeChange()) {
            module.changeMode(player, stack, shift, displayChange);
            return;
         }
      }
   }

   @Override
   protected FloatingLong getMaxEnergy(ItemStack stack) {
      IModule<ModuleEnergyUnit> module = this.getModule(stack, MekanismModules.ENERGY_UNIT);
      return module == null ? MekanismConfig.gear.mekaToolBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module);
   }

   @Override
   protected FloatingLong getChargeRate(ItemStack stack) {
      IModule<ModuleEnergyUnit> module = this.getModule(stack, MekanismModules.ENERGY_UNIT);
      return module == null ? MekanismConfig.gear.mekaToolBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module);
   }

   @Nullable
   @Override
   public RadialData<?> getRadialData(ItemStack stack) {
      List<NestedRadialMode> nestedModes = new ArrayList<>();
      Consumer<NestedRadialMode> adder = nestedModes::add;

      for (Module<?> module : this.getModules(stack)) {
         if (module.handlesRadialModeChange()) {
            module.addRadialModes(stack, adder);
         }
      }

      if (nestedModes.isEmpty()) {
         return null;
      } else {
         return (RadialData<?>)(nestedModes.size() == 1 ? nestedModes.get(0).nestedData() : new NestingRadialData(RADIAL_ID, nestedModes));
      }
   }

   @Nullable
   @Override
   public <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
      for (Module<?> module : this.getModules(stack)) {
         if (module.handlesRadialModeChange()) {
            M mode = module.getMode(stack, radialData);
            if (mode != null) {
               return mode;
            }
         }
      }

      return null;
   }

   @Override
   public <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode) {
      for (Module<?> module : this.getModules(stack)) {
         if (module.handlesRadialModeChange() && module.setMode(player, stack, radialData, mode)) {
            return;
         }
      }
   }
}

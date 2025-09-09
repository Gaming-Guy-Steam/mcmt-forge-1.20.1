package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.chemical.item.RateLimitMultiTankGasHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler;
import mekanism.common.capabilities.laser.item.LaserDissipationHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekasuit.ModuleElytraUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaSuitArmor
   extends ItemSpecialArmor
   implements IModuleContainerItem,
   IModeItem,
   IJetpackItem,
   IAttributeRefresher,
   CreativeTabDeferredRegister.ICustomCreativeTabContents {
   private static final ItemMekaSuitArmor.MekaSuitMaterial MEKASUIT_MATERIAL = new ItemMekaSuitArmor.MekaSuitMaterial();
   public static final List<ResourceKey<DamageType>> BASE_ALWAYS_SUPPORTED = List.of(
      DamageTypes.f_268526_,
      DamageTypes.f_268585_,
      DamageTypes.f_268613_,
      DamageTypes.f_268482_,
      DamageTypes.f_268752_,
      DamageTypes.f_268671_,
      DamageTypes.f_268659_,
      DamageTypes.f_268576_,
      DamageTypes.f_268433_,
      DamageTypes.f_268434_,
      DamageTypes.f_268631_,
      DamageTypes.f_268612_,
      DamageTypes.f_268546_,
      DamageTypes.f_268450_,
      DamageTypes.f_268468_,
      DamageTypes.f_268469_,
      DamageTypes.f_268493_,
      DamageTypes.f_268444_,
      DamageTypes.f_268513_,
      DamageTypes.f_268669_,
      DamageTypes.f_268679_
   );
   private final AttributeCache attributeCache;
   private final List<ChemicalTankSpec<Gas>> gasTankSpecs = new ArrayList<>();
   private final List<ChemicalTankSpec<Gas>> gasTankSpecsView = Collections.unmodifiableList(this.gasTankSpecs);
   private final List<RateLimitMultiTankFluidHandler.FluidTankSpec> fluidTankSpecs = new ArrayList<>();
   private final List<RateLimitMultiTankFluidHandler.FluidTankSpec> fluidTankSpecsView = Collections.unmodifiableList(this.fluidTankSpecs);
   private final float absorption;
   private final double laserDissipation;
   private final double laserRefraction;

   public static float getBaseDamageRatio(ResourceKey<DamageType> damageType) {
      return damageType == DamageTypes.f_268679_ ? 0.75F : 1.0F;
   }

   public ItemMekaSuitArmor(Type armorType, Properties properties) {
      super(MEKASUIT_MATERIAL, armorType, properties.m_41497_(Rarity.EPIC).setNoRepair().m_41487_(1));

      this.attributeCache = new AttributeCache(
         this,
         switch (armorType) {
            case HELMET -> {
               this.fluidTankSpecs
                  .add(
                     RateLimitMultiTankFluidHandler.FluidTankSpec.createFillOnly(
                        MekanismConfig.gear.mekaSuitNutritionalTransferRate,
                        MekanismConfig.gear.mekaSuitNutritionalMaxStorage,
                        fluid -> fluid.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid(),
                        stack -> this.hasModule(stack, MekanismModules.NUTRITIONAL_INJECTION_UNIT)
                     )
                  );
               this.absorption = 0.15F;
               this.laserDissipation = 0.15;
               this.laserRefraction = 0.2;
               yield MekanismConfig.gear.mekaSuitHelmetArmor;
            }
            case CHESTPLATE -> {
               this.gasTankSpecs
                  .add(
                     ChemicalTankSpec.createFillOnly(
                        MekanismConfig.gear.mekaSuitJetpackTransferRate,
                        MekanismConfig.gear.mekaSuitJetpackMaxStorage,
                        gas -> gas == MekanismGases.HYDROGEN.get(),
                        stack -> this.hasModule(stack, MekanismModules.JETPACK_UNIT)
                     )
                  );
               this.absorption = 0.4F;
               this.laserDissipation = 0.3;
               this.laserRefraction = 0.4;
               yield MekanismConfig.gear.mekaSuitBodyArmorArmor;
            }
            case LEGGINGS -> {
               this.absorption = 0.3F;
               this.laserDissipation = 0.1875;
               this.laserRefraction = 0.25;
               yield MekanismConfig.gear.mekaSuitPantsArmor;
            }
            case BOOTS -> {
               this.absorption = 0.15F;
               this.laserDissipation = 0.1125;
               this.laserRefraction = 0.15;
               yield MekanismConfig.gear.mekaSuitBootsArmor;
            }
            default -> throw new IllegalArgumentException("Unknown Equipment Slot Type");
         },
         MekanismConfig.gear.mekaSuitToughness,
         MekanismConfig.gear.mekaSuitKnockbackResistance
      );
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.mekaSuit());
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      return 0;
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         this.addModuleDetails(stack, tooltip);
      } else {
         StorageUtils.addStoredEnergy(stack, tooltip, true);
         if (!this.gasTankSpecs.isEmpty()) {
            StorageUtils.addStoredGas(stack, tooltip, true, false);
         }

         if (!this.fluidTankSpecs.isEmpty()) {
            StorageUtils.addStoredFluid(stack, tooltip, true);
         }

         tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }

   public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
      return true;
   }

   public boolean isEnderMask(@NotNull ItemStack stack, @NotNull Player player, @NotNull EnderMan enderman) {
      return this.f_265916_ == Type.HELMET;
   }

   public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
      return this.f_265916_ == Type.BOOTS;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getEnergyBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return MekanismConfig.client.energyColor.get();
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

   @Override
   public void addItems(Output tabOutput) {
      tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekanismConfig.gear.mekaSuitBaseEnergyCapacity));
   }

   public void onArmorTick(ItemStack stack, Level world, Player player) {
      super.onArmorTick(stack, world, player);

      for (Module<?> module : this.getModules(stack)) {
         module.tick(player);
      }
   }

   @Override
   protected boolean areCapabilityConfigsLoaded() {
      return super.areCapabilityConfigsLoaded() && MekanismConfig.gear.isLoaded();
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(
         RateLimitEnergyHandler.create(
            () -> this.getChargeRate(stack), () -> this.getMaxEnergy(stack), BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue
         )
      );
      capabilities.add(
         RadiationShieldingHandler.create(
            item -> this.isModuleEnabled(item, MekanismModules.RADIATION_SHIELDING_UNIT) ? ItemHazmatSuitArmor.getShieldingByArmor(this.m_266204_()) : 0.0
         )
      );
      capabilities.add(
         LaserDissipationHandler.create(
            item -> this.isModuleEnabled(item, MekanismModules.LASER_DISSIPATION_UNIT) ? this.laserDissipation : 0.0,
            item -> this.isModuleEnabled(item, MekanismModules.LASER_DISSIPATION_UNIT) ? this.laserRefraction : 0.0
         )
      );
      if (!this.gasTankSpecs.isEmpty()) {
         capabilities.add(RateLimitMultiTankGasHandler.create(this.gasTankSpecs));
      }

      if (!this.fluidTankSpecs.isEmpty()) {
         capabilities.add(RateLimitMultiTankFluidHandler.create(this.fluidTankSpecs));
      }
   }

   public List<ChemicalTankSpec<Gas>> getGasTankSpecs() {
      return this.gasTankSpecsView;
   }

   public List<RateLimitMultiTankFluidHandler.FluidTankSpec> getFluidTankSpecs() {
      return this.fluidTankSpecsView;
   }

   @NotNull
   public GasStack useGas(ItemStack stack, Gas type, long amount) {
      Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
      if (capability.isPresent()) {
         IGasHandler gasHandlerItem = capability.get();
         return gasHandlerItem.extractChemical(new GasStack(type, amount), Action.EXECUTE);
      } else {
         return GasStack.EMPTY;
      }
   }

   public GasStack getContainedGas(ItemStack stack, Gas type) {
      Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
      if (capability.isPresent()) {
         IGasHandler gasHandlerItem = capability.get();

         for (int i = 0; i < gasHandlerItem.getTanks(); i++) {
            GasStack gasInTank = gasHandlerItem.getChemicalInTank(i);
            if (gasInTank.getType() == type) {
               return gasInTank;
            }
         }
      }

      return GasStack.EMPTY;
   }

   public FluidStack getContainedFluid(ItemStack stack, FluidStack type) {
      Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
      if (capability.isPresent()) {
         IFluidHandlerItem fluidHandlerItem = capability.get();

         for (int i = 0; i < fluidHandlerItem.getTanks(); i++) {
            FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(i);
            if (fluidInTank.isFluidEqual(type)) {
               return fluidInTank;
            }
         }
      }

      return FluidStack.EMPTY;
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
   public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
      return slotType == this.m_40402_() && this.getModules(stack).stream().anyMatch(Module::handlesModeChange);
   }

   public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
      if (this.m_266204_() == Type.CHESTPLATE && !entity.m_6144_()) {
         IModule<ModuleElytraUnit> module = this.getModule(stack, MekanismModules.ELYTRA_UNIT);
         if (module != null && module.isEnabled() && module.canUseEnergy(entity, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get())) {
            IModule<ModuleJetpackUnit> jetpack = this.getModule(stack, MekanismModules.JETPACK_UNIT);
            return jetpack == null
               || !jetpack.isEnabled()
               || jetpack.getCustomInstance().getMode() != IJetpackItem.JetpackMode.HOVER
               || this.getContainedGas(stack, MekanismGases.HYDROGEN.get()).isEmpty();
         }
      }

      return false;
   }

   public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
      if (!entity.m_9236_().f_46443_) {
         int nextFlightTicks = flightTicks + 1;
         if (nextFlightTicks % 10 == 0) {
            if (nextFlightTicks % 20 == 0) {
               IModule<ModuleElytraUnit> module = this.getModule(stack, MekanismModules.ELYTRA_UNIT);
               if (module != null && module.isEnabled()) {
                  module.useEnergy(entity, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get());
               }
            }

            entity.m_146850_(GameEvent.f_223705_);
         }
      }

      return true;
   }

   @Override
   public boolean canUseJetpack(ItemStack stack) {
      return this.f_265916_ == Type.CHESTPLATE
         && (
            this.isModuleEnabled(stack, MekanismModules.JETPACK_UNIT)
               ? ChemicalUtil.hasChemical(stack, MekanismGases.HYDROGEN.get())
               : this.getModules(stack)
                  .stream()
                  .anyMatch(module -> module.isEnabled() && module.getData().isExclusive(ModuleData.ExclusiveFlag.OVERRIDE_JUMP.getMask()))
         );
   }

   @Override
   public IJetpackItem.JetpackMode getJetpackMode(ItemStack stack) {
      IModule<ModuleJetpackUnit> module = this.getModule(stack, MekanismModules.JETPACK_UNIT);
      return module != null && module.isEnabled() ? module.getCustomInstance().getMode() : IJetpackItem.JetpackMode.DISABLED;
   }

   @Override
   public void useJetpackFuel(ItemStack stack) {
      this.useGas(stack, MekanismGases.HYDROGEN.get(), 1L);
   }

   private FloatingLong getMaxEnergy(ItemStack stack) {
      IModule<ModuleEnergyUnit> module = this.getModule(stack, MekanismModules.ENERGY_UNIT);
      return module == null ? MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module);
   }

   private FloatingLong getChargeRate(ItemStack stack) {
      IModule<ModuleEnergyUnit> module = this.getModule(stack, MekanismModules.ENERGY_UNIT);
      return module == null ? MekanismConfig.gear.mekaSuitBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module);
   }

   @NotNull
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
      return (Multimap<Attribute, AttributeModifier>)(slot == this.m_40402_() ? this.attributeCache.get() : ImmutableMultimap.of());
   }

   @Override
   public void addToBuilder(Builder<Attribute, AttributeModifier> builder) {
      UUID modifier = (UUID)f_265987_.get(this.m_266204_());
      builder.put(Attributes.f_22284_, new AttributeModifier(modifier, "Armor modifier", this.m_40404_(), Operation.ADDITION));
      builder.put(Attributes.f_22285_, new AttributeModifier(modifier, "Armor toughness", this.m_40405_(), Operation.ADDITION));
      builder.put(Attributes.f_22278_, new AttributeModifier(modifier, "Armor knockback resistance", this.m_40401_().m_6649_(), Operation.ADDITION));
   }

   public int m_40404_() {
      return this.m_40401_().m_7366_(this.m_266204_());
   }

   public float m_40405_() {
      return this.m_40401_().m_6651_();
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return slotChanged || oldStack.m_41720_() != newStack.m_41720_();
   }

   public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
      return oldStack.m_41720_() != newStack.m_41720_();
   }

   public static float getDamageAbsorbed(Player player, DamageSource source, float amount) {
      return getDamageAbsorbed(player, source, amount, null);
   }

   public static boolean tryAbsorbAll(Player player, DamageSource source, float amount) {
      List<Runnable> energyUsageCallbacks = new ArrayList<>(4);
      if (!(getDamageAbsorbed(player, source, amount, energyUsageCallbacks) >= 1.0F)) {
         return false;
      } else {
         for (Runnable energyUsageCallback : energyUsageCallbacks) {
            energyUsageCallback.run();
         }

         return true;
      }
   }

   private static float getDamageAbsorbed(Player player, DamageSource source, float amount, @Nullable List<Runnable> energyUseCallbacks) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else {
         float ratioAbsorbed = 0.0F;
         List<ItemMekaSuitArmor.FoundArmorDetails> armorDetails = new ArrayList<>();

         label91:
         for (ItemStack stack : player.m_6168_()) {
            if (!stack.m_41619_() && stack.m_41720_() instanceof ItemMekaSuitArmor armor) {
               IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
               if (energyContainer != null) {
                  ItemMekaSuitArmor.FoundArmorDetails details = new ItemMekaSuitArmor.FoundArmorDetails(energyContainer, armor);
                  armorDetails.add(details);
                  Iterator var11 = details.armor.getModules(stack).iterator();

                  while (true) {
                     if (var11.hasNext()) {
                        Module<?> module = (Module<?>)var11.next();
                        if (!module.isEnabled()) {
                           continue;
                        }

                        ICustomModule.ModuleDamageAbsorbInfo damageAbsorbInfo = getModuleDamageAbsorbInfo(module, source);
                        if (damageAbsorbInfo == null) {
                           continue;
                        }

                        float absorption = damageAbsorbInfo.absorptionRatio().getAsFloat();
                        ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, damageAbsorbInfo.energyCost());
                        if (!(ratioAbsorbed >= 1.0F)) {
                           continue;
                        }
                     }

                     if (ratioAbsorbed >= 1.0F) {
                        break label91;
                     }
                     break;
                  }
               }
            }
         }

         if (ratioAbsorbed < 1.0F) {
            Float absorbRatio = null;

            for (ItemMekaSuitArmor.FoundArmorDetails details : armorDetails) {
               if (absorbRatio == null) {
                  if (!source.m_269533_(MekanismTags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED) && source.m_269533_(DamageTypeTags.f_268490_)) {
                     break;
                  }

                  ResourceLocation damageTypeName = source.m_269150_()
                     .m_203543_()
                     .<ResourceLocation>map(ResourceKey::m_135782_)
                     .orElseGet(
                        () -> player.m_9236_().m_9598_().m_6632_(Registries.f_268580_).map(registry -> registry.m_7981_(source.m_269415_())).orElse(null)
                     );
                  if (damageTypeName != null) {
                     absorbRatio = (Float)((Map)MekanismConfig.gear.mekaSuitDamageRatios.get()).get(damageTypeName);
                  }

                  if (absorbRatio == null) {
                     absorbRatio = MekanismConfig.gear.mekaSuitUnspecifiedDamageRatio.getAsFloat();
                  }

                  if (absorbRatio == 0.0F) {
                     break;
                  }
               }

               float absorption = details.armor.absorption * absorbRatio;
               ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage);
               if (ratioAbsorbed >= 1.0F) {
                  break;
               }
            }
         }

         for (ItemMekaSuitArmor.FoundArmorDetails details : armorDetails) {
            if (!details.usageInfo.energyUsed.isZero()) {
               if (energyUseCallbacks == null) {
                  details.energyContainer.extract(details.usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL);
               } else {
                  energyUseCallbacks.add(() -> details.energyContainer.extract(details.usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL));
               }
            }
         }

         return Math.min(ratioAbsorbed, 1.0F);
      }
   }

   @Nullable
   private static <MODULE extends ICustomModule<MODULE>> ICustomModule.ModuleDamageAbsorbInfo getModuleDamageAbsorbInfo(
      IModule<MODULE> module, DamageSource damageSource
   ) {
      return module.getCustomInstance().getDamageAbsorbInfo(module, damageSource);
   }

   private static float absorbDamage(
      ItemMekaSuitArmor.EnergyUsageInfo usageInfo, float amount, float absorption, float currentAbsorbed, FloatingLongSupplier energyCost
   ) {
      absorption = Math.min(1.0F - currentAbsorbed, absorption);
      float toAbsorb = amount * absorption;
      if (toAbsorb > 0.0F) {
         FloatingLong usage = energyCost.get().multiply((double)toAbsorb);
         if (usage.isZero()) {
            return absorption;
         }

         if (usageInfo.energyAvailable.greaterOrEqual(usage)) {
            usageInfo.energyUsed = usageInfo.energyUsed.plusEqual(usage);
            usageInfo.energyAvailable = usageInfo.energyAvailable.minusEqual(usage);
            return absorption;
         }

         if (!usageInfo.energyAvailable.isZero()) {
            float absorbedPercent = usageInfo.energyAvailable.divide(usage).floatValue();
            usageInfo.energyUsed = usageInfo.energyUsed.plusEqual(usageInfo.energyAvailable);
            usageInfo.energyAvailable = FloatingLong.ZERO;
            return absorption * absorbedPercent;
         }
      }

      return 0.0F;
   }

   private static class EnergyUsageInfo {
      private FloatingLong energyAvailable;
      private FloatingLong energyUsed = FloatingLong.ZERO;

      public EnergyUsageInfo(FloatingLong energyAvailable) {
         this.energyAvailable = energyAvailable.copy();
      }
   }

   private static class FoundArmorDetails {
      private final IEnergyContainer energyContainer;
      private final ItemMekaSuitArmor.EnergyUsageInfo usageInfo;
      private final ItemMekaSuitArmor armor;

      public FoundArmorDetails(IEnergyContainer energyContainer, ItemMekaSuitArmor armor) {
         this.energyContainer = energyContainer;
         this.usageInfo = new ItemMekaSuitArmor.EnergyUsageInfo(energyContainer.getEnergy());
         this.armor = armor;
      }
   }

   protected static class MekaSuitMaterial extends BaseSpecialArmorMaterial {
      @Override
      public int m_7366_(@NotNull Type armorType) {
         return switch (armorType) {
            case HELMET -> MekanismConfig.gear.mekaSuitHelmetArmor.getOrDefault();
            case CHESTPLATE -> MekanismConfig.gear.mekaSuitBodyArmorArmor.getOrDefault();
            case LEGGINGS -> MekanismConfig.gear.mekaSuitPantsArmor.getOrDefault();
            case BOOTS -> MekanismConfig.gear.mekaSuitBootsArmor.getOrDefault();
            default -> throw new IncompatibleClassChangeError();
         };
      }

      @Override
      public float m_6651_() {
         return MekanismConfig.gear.mekaSuitToughness.getOrDefault();
      }

      @Override
      public float m_6649_() {
         return MekanismConfig.gear.mekaSuitKnockbackResistance.getOrDefault();
      }

      @NotNull
      public String m_6082_() {
         return "mekanism:mekasuit";
      }
   }
}

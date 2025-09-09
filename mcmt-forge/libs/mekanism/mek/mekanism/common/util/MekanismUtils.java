package mekanism.common.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MekanismUtils {
   public static final float ONE_OVER_ROOT_TWO = (float)(1.0 / Math.sqrt(2.0));
   public static final NonNullSupplier<IllegalStateException> MISSING_CAP_ERROR = () -> new IllegalStateException(
      "Capability is somehow not present after isPresent checks"
   );
   private static final ItemStack MILK = new ItemStack(Items.f_42455_);
   private static final List<UUID> warnedFails = new ArrayList<>();

   public static void logMismatchedStackSize(long actual, long expected) {
      if (expected != actual) {
         Mekanism.logger.error("Stack size changed by a different amount ({}) than requested ({}).", new Object[]{actual, expected, new Exception()});
      }
   }

   public static void logExpectedZero(FloatingLong actual) {
      if (!actual.isZero()) {
         Mekanism.logger.error("Energy value changed by a different amount ({}) than requested (zero).", actual, new Exception());
      }
   }

   public static Component logFormat(Object message) {
      return logFormat(EnumColor.GRAY, message);
   }

   public static Component logFormat(EnumColor messageColor, Object message) {
      return MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, new Object[]{MekanismLang.MEKANISM, messageColor, message});
   }

   @Nullable
   public static Player tryGetClientPlayer() {
      return FMLEnvironment.dist.isClient() ? MekanismClient.tryGetClientPlayer() : null;
   }

   @NotNull
   public static String getModId(@NotNull ItemStack stack) {
      Item item = stack.m_41720_();
      String modid = item.getCreatorModId(stack);
      if (modid == null) {
         ResourceLocation registryName = RegistryUtils.getName(item);
         if (registryName == null) {
            Mekanism.logger.error("Unexpected null registry name for item of class type: {}", item.getClass().getSimpleName());
            return "";
         } else {
            return registryName.m_135827_();
         }
      } else {
         return modid;
      }
   }

   public static ItemStack getItemInHand(LivingEntity entity, HumanoidArm side) {
      if (entity instanceof Player player) {
         return getItemInHand(player, side);
      } else {
         return side == HumanoidArm.RIGHT ? entity.m_21205_() : entity.m_21206_();
      }
   }

   public static ItemStack getItemInHand(Player player, HumanoidArm side) {
      return player.m_5737_() == side ? player.m_21205_() : player.m_21206_();
   }

   public static Direction getLeft(Direction orientation) {
      return orientation.m_122427_();
   }

   public static Direction getRight(Direction orientation) {
      return orientation.m_122428_();
   }

   public static double fractionUpgrades(IUpgradeTile tile, Upgrade type) {
      return tile.supportsUpgrade(type) ? (double)tile.getComponent().getUpgrades(type) / type.getMax() : 0.0;
   }

   public static float getScale(float prevScale, IExtendedFluidTank tank) {
      return getScale(prevScale, tank.getFluidAmount(), tank.getCapacity(), tank.isEmpty());
   }

   public static float getScale(float prevScale, IChemicalTank<?, ?> tank) {
      return getScale(prevScale, tank.getStored(), tank.getCapacity(), tank.isEmpty());
   }

   public static float getScale(float prevScale, int stored, int capacity, boolean empty) {
      return getScale(prevScale, capacity == 0 ? 0.0F : (float)stored / capacity, empty, stored == capacity);
   }

   public static float getScale(float prevScale, long stored, long capacity, boolean empty) {
      return getScale(prevScale, capacity == 0L ? 0.0F : (float)((double)stored / capacity), empty, stored == capacity);
   }

   public static float getScale(float prevScale, IEnergyContainer container) {
      FloatingLong stored = container.getEnergy();
      FloatingLong capacity = container.getMaxEnergy();
      float targetScale;
      if (capacity.isZero()) {
         targetScale = 0.0F;
      } else {
         targetScale = stored.divide(capacity).floatValue();
      }

      return getScale(prevScale, targetScale, container.isEmpty(), stored.equals(capacity));
   }

   public static float getScale(float prevScale, float targetScale, boolean empty, boolean full) {
      float difference = Math.abs(prevScale - targetScale);
      if (difference > 0.01) {
         return (9.0F * prevScale + targetScale) / 10.0F;
      } else if (!empty && full && difference > 0.0F) {
         return targetScale;
      } else if (!empty && prevScale == 0.0F) {
         return targetScale;
      } else {
         return empty && prevScale < 0.01 ? 0.0F : prevScale;
      }
   }

   public static long getBaseUsage(IUpgradeTile tile, int def) {
      return tile.supportsUpgrades() && tile.supportsUpgrade(Upgrade.GAS)
         ? Math.round(
            def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.GAS))
         )
         : def;
   }

   public static int getTicks(IUpgradeTile tile, int def) {
      return tile.supportsUpgrades()
         ? MathUtils.clampToInt(def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(tile, Upgrade.SPEED)))
         : def;
   }

   public static FloatingLong getEnergyPerTick(IUpgradeTile tile, FloatingLong def) {
      return tile.supportsUpgrades()
         ? def.multiply(
            Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2.0 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.ENERGY))
         )
         : def;
   }

   public static double getGasPerTickMeanMultiplier(IUpgradeTile tile) {
      if (tile.supportsUpgrades()) {
         return tile.supportsUpgrade(Upgrade.GAS)
            ? Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2.0 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.GAS))
            : Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.SPEED));
      } else {
         return 1.0;
      }
   }

   public static FloatingLong getMaxEnergy(IUpgradeTile tile, FloatingLong def) {
      return tile.supportsUpgrades() ? def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.ENERGY))) : def;
   }

   public static FloatingLong getMaxEnergy(ItemStack stack, FloatingLong def) {
      float numUpgrades = 0.0F;
      if (ItemDataUtils.hasData(stack, "componentUpgrade", 10)) {
         Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, "componentUpgrade"));
         numUpgrades = upgrades.getOrDefault(Upgrade.ENERGY, 0).intValue();
      }

      return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), numUpgrades / Upgrade.ENERGY.getMax()));
   }

   public static ResourceLocation getResource(MekanismUtils.ResourceType type, String name) {
      return Mekanism.rl(type.getPrefix() + name);
   }

   public static boolean canFunction(TileEntityMekanism tile) {
      if (!tile.supportsRedstone()) {
         return true;
      } else {
         return switch (tile.getControlType()) {
            case DISABLED -> true;
            case HIGH -> tile.isPowered();
            case LOW -> !tile.isPowered();
            case PULSE -> tile.isPowered() && !tile.wasPowered();
         };
      }
   }

   public static boolean lighterThanAirGas(FluidStack stack) {
      return stack.getFluid().m_205067_(Fluids.GASEOUS) && stack.getFluid().getFluidType().getDensity(stack) <= 0;
   }

   public static int getEnchantmentLevel(ListTag enchantments, Enchantment enchantment) {
      ResourceLocation enchantmentId = EnchantmentHelper.m_182432_(enchantment);

      for (int i = 0; i < enchantments.size(); i++) {
         CompoundTag compoundtag = enchantments.m_128728_(i);
         ResourceLocation id = EnchantmentHelper.m_182446_(compoundtag);
         if (id != null && id.equals(enchantmentId)) {
            return EnchantmentHelper.m_182438_(compoundtag);
         }
      }

      return 0;
   }

   public static boolean isLiquidBlock(Block block) {
      return block instanceof LiquidBlock || block instanceof BubbleColumnBlock || block instanceof IFluidBlock;
   }

   public static BlockHitResult rayTrace(Player player) {
      return rayTrace(player, Fluid.NONE);
   }

   public static BlockHitResult rayTrace(Player player, Fluid fluidMode) {
      return rayTrace(player, player.getBlockReach(), fluidMode);
   }

   public static BlockHitResult rayTrace(Player player, double reach) {
      return rayTrace(player, reach, Fluid.NONE);
   }

   public static BlockHitResult rayTrace(Player player, double reach, Fluid fluidMode) {
      Vec3 headVec = getHeadVec(player);
      Vec3 lookVec = player.m_20252_(1.0F);
      Vec3 endVec = headVec.m_82520_(lookVec.f_82479_ * reach, lookVec.f_82480_ * reach, lookVec.f_82481_ * reach);
      return player.m_9236_().m_45547_(new ClipContext(headVec, endVec, net.minecraft.world.level.ClipContext.Block.OUTLINE, fluidMode, player));
   }

   private static Vec3 getHeadVec(Player player) {
      double posY = player.m_20186_() + player.m_20192_();
      if (player.m_6047_()) {
         posY -= 0.08;
      }

      return new Vec3(player.m_20185_(), posY, player.m_20189_());
   }

   public static void addFrequencyToTileTooltip(ItemStack stack, FrequencyType<?> frequencyType, List<Component> tooltip) {
      ItemDataUtils.setCompoundIfPresent(
         stack,
         "componentFrequency",
         frequencyComponent -> NBTUtils.setCompoundIfPresent(
            frequencyComponent,
            frequencyType.getName(),
            frequencyCompound -> {
               Frequency frequency = frequencyType.create(frequencyCompound);
               frequency.setValid(false);
               tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, frequency.getName()}));
               if (frequency.getOwner() != null) {
                  String owner = OwnerDisplay.getOwnerName(tryGetClientPlayer(), frequency.getOwner(), frequency.getClientOwner());
                  if (owner != null) {
                     tooltip.add(MekanismLang.OWNER.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, owner}));
                  }
               }

               tooltip.add(
                  MekanismLang.MODE.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, frequency.isPublic() ? APILang.PUBLIC : APILang.PRIVATE})
               );
            }
         )
      );
   }

   public static void addFrequencyItemTooltip(ItemStack stack, List<Component> tooltip) {
      Frequency.FrequencyIdentity frequency = ((IFrequencyItem)stack.m_41720_()).getFrequencyIdentity(stack);
      if (frequency != null) {
         tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, frequency.key()}));
         CompoundTag frequencyCompound = ItemDataUtils.getCompound(stack, "frequency");
         if (frequencyCompound.m_128403_("owner")) {
            String owner = OwnerDisplay.getOwnerName(tryGetClientPlayer(), frequencyCompound.m_128342_("owner"), null);
            if (owner != null) {
               tooltip.add(MekanismLang.OWNER.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, owner}));
            }
         }

         tooltip.add(
            MekanismLang.MODE.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, frequency.isPublic() ? APILang.PUBLIC : APILang.PRIVATE})
         );
      }
   }

   public static void addUpgradesToTooltip(ItemStack stack, List<Component> tooltip) {
      ItemDataUtils.setCompoundIfPresent(
         stack,
         "componentUpgrade",
         upgradeComponent -> Upgrade.buildMap(upgradeComponent).forEach((upgrade, level) -> tooltip.add(UpgradeDisplay.of(upgrade, level).getTextComponent()))
      );
   }

   public static Component getEnergyDisplayShort(FloatingLong energy) {
      UnitDisplayUtils.EnergyUnit configured = UnitDisplayUtils.EnergyUnit.getConfigured();
      return UnitDisplayUtils.getDisplayShort(configured.convertTo(energy), configured);
   }

   public static FloatingLong convertToJoules(FloatingLong energy) {
      return UnitDisplayUtils.EnergyUnit.getConfigured().convertFrom(energy);
   }

   public static FloatingLong convertToDisplay(FloatingLong energy) {
      return UnitDisplayUtils.EnergyUnit.getConfigured().convertTo(energy);
   }

   public static Component getTemperatureDisplay(double temp, UnitDisplayUtils.TemperatureUnit unit, boolean shift) {
      double tempKelvin = unit.convertToK(temp, true);
      return UnitDisplayUtils.getDisplayShort(tempKelvin, MekanismConfig.common.tempUnit.get(), shift);
   }

   public static CraftingContainer getDummyCraftingInv() {
      AbstractContainerMenu tempContainer = new AbstractContainerMenu(MenuType.f_39968_, 1) {
         @NotNull
         public ItemStack m_7648_(@NotNull Player player, int slotID) {
            return ItemStack.f_41583_;
         }

         public boolean m_6875_(@NotNull Player player) {
            return false;
         }
      };
      return new TransientCraftingContainer(tempContainer, 3, 3);
   }

   public static boolean canUseAsWrench(ItemStack stack) {
      if (stack.m_41619_()) {
         return false;
      } else {
         return stack.m_41720_() instanceof ItemConfigurator configurator
            ? configurator.getMode(stack) == ItemConfigurator.ConfiguratorMode.WRENCH
            : stack.m_204117_(MekanismTags.Items.CONFIGURATORS);
      }
   }

   @NotNull
   public static String getLastKnownUsername(@Nullable UUID uuid) {
      if (uuid == null) {
         return "<???>";
      } else {
         String ret = UsernameCache.getLastKnownUsername(uuid);
         if (ret == null && !warnedFails.contains(uuid) && EffectiveSide.get().isServer()) {
            Optional<GameProfile> gp = ServerLifecycleHooks.getCurrentServer().m_129927_().m_11002_(uuid);
            if (gp.isPresent()) {
               ret = gp.get().getName();
            }
         }

         if (ret == null && !warnedFails.contains(uuid)) {
            Mekanism.logger.warn("Failed to retrieve username for UUID {}, you might want to add it to the JSON cache", uuid);
            warnedFails.add(uuid);
         }

         return ret == null ? "<" + uuid + ">" : ret;
      }
   }

   public static void speedUpEffectSafely(LivingEntity entity, MobEffectInstance effectInstance) {
      if (effectInstance.m_19557_() > 0) {
         int remainingDuration = effectInstance.m_19579_();
         if (remainingDuration == 0 && effectInstance.f_19510_ != null) {
            effectInstance.m_19548_(effectInstance.f_19510_);
            effectInstance.f_19510_ = effectInstance.f_19510_.f_19510_;
            onChangedPotionEffect(entity, effectInstance, true);
         }
      }
   }

   public static boolean shouldSpeedUpEffect(MobEffectInstance effectInstance) {
      return effectInstance.isCurativeItem(MILK) && !MekanismTags.MobEffects.SPEED_UP_BLACKLIST_LOOKUP.contains(effectInstance.m_19544_());
   }

   private static void onChangedPotionEffect(LivingEntity entity, MobEffectInstance effectInstance, boolean reapply) {
      entity.f_20948_ = true;
      if (reapply && !entity.m_9236_().f_46443_) {
         MobEffect effect = effectInstance.m_19544_();
         effect.m_6386_(entity, entity.m_21204_(), effectInstance.m_19564_());
         effect.m_6385_(entity, entity.m_21204_(), effectInstance.m_19564_());
      }

      if (entity instanceof ServerPlayer player) {
         player.f_8906_.m_9829_(new ClientboundUpdateMobEffectPacket(entity.m_19879_(), effectInstance));
         CriteriaTriggers.f_10550_.m_149262_(player, null);
      }
   }

   public static boolean isSameTypeFactory(Block block, BlockEntityType<?> factoryTileType) {
      return Attribute.matches(block, AttributeFactoryType.class, attribute -> {
         FactoryType factoryType = attribute.getFactoryType();

         for (FactoryTier factoryTier : EnumUtils.FACTORY_TIERS) {
            if (MekanismTileEntityTypes.getFactoryTile(factoryTier, factoryType).get() == factoryTileType) {
               return true;
            }
         }

         return false;
      });
   }

   @SafeVarargs
   public static InteractionResult performActions(InteractionResult firstAction, Supplier<InteractionResult>... secondaryActions) {
      if (firstAction.m_19077_()) {
         return firstAction;
      } else {
         boolean hasFailed = firstAction == InteractionResult.FAIL;

         for (Supplier<InteractionResult> secondaryAction : secondaryActions) {
            InteractionResult result = secondaryAction.get();
            if (result.m_19077_()) {
               return result;
            }

            hasFailed &= result == InteractionResult.FAIL;
         }

         return hasFailed ? InteractionResult.FAIL : InteractionResult.PASS;
      }
   }

   public static int redstoneLevelFromContents(long amount, long capacity) {
      double fractionFull = capacity == 0L ? 0.0 : (double)amount / capacity;
      return Mth.m_14143_((float)(fractionFull * 14.0)) + (fractionFull > 0.0 ? 1 : 0);
   }

   public static int redstoneLevelFromContents(FloatingLong amount, FloatingLong capacity) {
      return !capacity.isZero() && !amount.isZero() ? 1 + amount.divide(capacity).multiply(14L).intValue() : 0;
   }

   public static int redstoneLevelFromContents(List<IInventorySlot> slots) {
      long totalCount = 0L;
      long totalLimit = 0L;

      for (IInventorySlot slot : slots) {
         if (slot.isEmpty()) {
            totalLimit += slot.getLimit(ItemStack.f_41583_);
         } else {
            totalCount += slot.getCount();
            totalLimit += slot.getLimit(slot.getStack());
         }
      }

      return redstoneLevelFromContents(totalCount, totalLimit);
   }

   public static boolean isPlayingMode(Player player) {
      return !player.m_7500_() && !player.m_5833_();
   }

   public static List<String> getParameterNames(@Nullable JsonObject classMethods, String method, String signature) {
      if (classMethods != null) {
         JsonObject signatures = classMethods.getAsJsonObject(method);
         if (signatures != null) {
            JsonElement params = signatures.get(signature);
            if (params != null) {
               if (!params.isJsonArray()) {
                  return Collections.singletonList(params.getAsString());
               }

               JsonArray paramArray = params.getAsJsonArray();
               List<String> paramNames = new ArrayList<>(paramArray.size());

               for (JsonElement param : paramArray) {
                  paramNames.add(param.getAsString());
               }

               return Collections.unmodifiableList(paramNames);
            }
         }
      }

      return Collections.emptyList();
   }

   public static Map<FluidType, MekanismUtils.FluidInDetails> getFluidsIn(Player player, UnaryOperator<AABB> modifyBoundingBox) {
      AABB bb = modifyBoundingBox.apply(player.m_20191_().m_82406_(0.001));
      int xMin = Mth.m_14107_(bb.f_82288_);
      int xMax = Mth.m_14165_(bb.f_82291_);
      int yMin = Mth.m_14107_(bb.f_82289_);
      int yMax = Mth.m_14165_(bb.f_82292_);
      int zMin = Mth.m_14107_(bb.f_82290_);
      int zMax = Mth.m_14165_(bb.f_82293_);
      if (!player.m_9236_().m_46812_(xMin, yMin, zMin, xMax, yMax, zMax)) {
         return Collections.emptyMap();
      } else {
         Map<FluidType, MekanismUtils.FluidInDetails> fluidsIn = new IdentityHashMap<>();
         MutableBlockPos mutablePos = new MutableBlockPos();

         for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
               for (int z = zMin; z < zMax; z++) {
                  mutablePos.m_122178_(x, y, z);
                  FluidState fluidState = player.m_9236_().m_6425_(mutablePos);
                  if (!fluidState.m_76178_()) {
                     double fluidY = y + fluidState.m_76155_(player.m_9236_(), mutablePos);
                     if (bb.f_82289_ <= fluidY) {
                        MekanismUtils.FluidInDetails details = fluidsIn.computeIfAbsent(fluidState.getFluidType(), f -> new MekanismUtils.FluidInDetails());
                        details.positions.put(mutablePos.m_7949_(), fluidState);
                        double actualFluidHeight;
                        if (fluidY > bb.f_82292_) {
                           actualFluidHeight = bb.f_82292_ - Math.max(bb.f_82289_, (double)y);
                        } else {
                           actualFluidHeight = fluidY - Math.max(bb.f_82289_, (double)y);
                        }

                        details.heights.merge(ChunkPos.m_45589_(x, z), actualFluidHeight, Double::sum);
                     }
                  }
               }
            }
         }

         return fluidsIn;
      }
   }

   public static void veinMineArea(
      IEnergyContainer energyContainer,
      FloatingLong energyRequired,
      Level world,
      BlockPos pos,
      ServerPlayer player,
      ItemStack stack,
      Item usedTool,
      Object2IntMap<BlockPos> found,
      MekanismUtils.BlastEnergyFunction blastEnergy,
      MekanismUtils.VeinEnergyFunction veinEnergy
   ) {
      FloatingLong energyUsed = FloatingLong.ZERO;
      FloatingLong energyAvailable = energyContainer.getEnergy();
      energyAvailable = energyAvailable.subtract(energyRequired);
      ObjectIterator var12 = found.object2IntEntrySet().iterator();

      while (var12.hasNext()) {
         Entry<BlockPos> foundEntry = (Entry<BlockPos>)var12.next();
         BlockPos foundPos = (BlockPos)foundEntry.getKey();
         if (!pos.equals(foundPos)) {
            BlockState targetState = world.m_8055_(foundPos);
            if (!targetState.m_60795_()) {
               float hardness = targetState.m_60800_(world, foundPos);
               if (hardness != -1.0F) {
                  int distance = foundEntry.getIntValue();
                  FloatingLong destroyEnergy = distance == 0 ? blastEnergy.calc(hardness) : veinEnergy.calc(hardness, distance, targetState);
                  if (!energyUsed.add(destroyEnergy).greaterThan(energyAvailable)) {
                     int exp = ForgeHooks.onBlockBreakEvent(world, player.f_8941_.m_9290_(), player, foundPos);
                     if (exp != -1) {
                        Block block = targetState.m_60734_();
                        BlockEntity tileEntity = WorldUtils.getTileEntity(world, foundPos);
                        if (targetState.onDestroyedByPlayer(world, foundPos, player, true, targetState.m_60819_())) {
                           block.m_6786_(world, foundPos, targetState);
                           block.m_6240_(world, player, foundPos, targetState, tileEntity, stack);
                           player.m_36246_(Stats.f_12982_.m_12902_(usedTool));
                           if (exp > 0) {
                              block.m_49805_((ServerLevel)world, foundPos, exp);
                           }

                           energyUsed = energyUsed.plusEqual(destroyEnergy);
                        }
                     }
                  }
               }
            }
         }
      }

      energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
   }

   @FunctionalInterface
   public interface BlastEnergyFunction {
      FloatingLong calc(float hardness);
   }

   public static class FluidInDetails {
      private final Map<BlockPos, FluidState> positions = new HashMap<>();
      private final Long2DoubleMap heights = new Long2DoubleArrayMap();

      public Map<BlockPos, FluidState> getPositions() {
         return this.positions;
      }

      public double getMaxHeight() {
         return this.heights.values().doubleStream().max().orElse(0.0);
      }
   }

   public static enum ResourceType {
      GUI("gui"),
      GUI_BUTTON("gui/button"),
      GUI_BAR("gui/bar"),
      GUI_GAUGE("gui/gauge"),
      GUI_HUD("gui/hud"),
      GUI_ICONS("gui/icons"),
      GUI_PROGRESS("gui/progress"),
      GUI_RADIAL("gui/radial"),
      GUI_SLOT("gui/slot"),
      GUI_TAB("gui/tabs"),
      SOUND("sound"),
      RENDER("render"),
      TEXTURE_BLOCKS("textures/block"),
      TEXTURE_ITEMS("textures/item"),
      MODEL("models"),
      INFUSE("infuse"),
      PIGMENT("pigment"),
      SLURRY("slurry");

      private final String prefix;

      private ResourceType(String s) {
         this.prefix = s;
      }

      public String getPrefix() {
         return this.prefix + "/";
      }
   }

   @FunctionalInterface
   public interface VeinEnergyFunction {
      FloatingLong calc(float hardness, int distance, BlockState state);
   }
}

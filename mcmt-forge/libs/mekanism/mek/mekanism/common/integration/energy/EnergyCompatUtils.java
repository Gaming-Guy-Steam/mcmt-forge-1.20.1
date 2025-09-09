package mekanism.common.integration.energy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.listener.ConfigBasedCachedSupplier;
import mekanism.common.config.value.CachedValue;
import mekanism.common.integration.energy.fluxnetworks.FNEnergyCompat;
import mekanism.common.integration.energy.forgeenergy.ForgeEnergyCompat;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCompatUtils {
   private static final List<IEnergyCompat> energyCompats = List.of(new StrictEnergyCompat(), new FNEnergyCompat(), new ForgeEnergyCompat());
   private static Supplier<List<Capability<?>>> ENABLED_ENERGY_CAPS = () -> List.of(Capabilities.STRICT_ENERGY);

   private EnergyCompatUtils() {
   }

   public static void initLoadedCache() {
      Set<CachedValue<?>> configs = new HashSet<>();

      for (IEnergyCompat energyCompat : energyCompats) {
         configs.addAll(energyCompat.getBackingConfigs());
      }

      ENABLED_ENERGY_CAPS = new ConfigBasedCachedSupplier<>(
         () -> energyCompats.stream().filter(IEnergyCompat::isUsable).map(IEnergyCompat::getCapability).toList(), configs.toArray(new CachedValue[0])
      );
   }

   public static List<IEnergyCompat> getCompats() {
      return energyCompats;
   }

   public static boolean isEnergyCapability(@NotNull Capability<?> capability) {
      if (capability.isRegistered()) {
         for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isMatchingCapability(capability)) {
               return energyCompat.isUsable();
            }
         }
      }

      return false;
   }

   public static List<Capability<?>> getEnabledEnergyCapabilities() {
      return ENABLED_ENERGY_CAPS.get();
   }

   private static boolean isTileValid(@Nullable BlockEntity tile) {
      return tile != null && !tile.m_58901_() && tile.m_58898_();
   }

   public static boolean hasStrictEnergyHandler(@NotNull ItemStack stack) {
      return !stack.m_41619_() && hasStrictEnergyHandler(stack, null);
   }

   public static boolean hasStrictEnergyHandler(@Nullable BlockEntity tile, Direction side) {
      return isTileValid(tile) && hasStrictEnergyHandler((ICapabilityProvider)tile, side);
   }

   private static boolean hasStrictEnergyHandler(ICapabilityProvider provider, Direction side) {
      for (IEnergyCompat energyCompat : energyCompats) {
         if (energyCompat.isUsable() && energyCompat.isCapabilityPresent(provider, side)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static IStrictEnergyHandler getStrictEnergyHandler(@NotNull ItemStack stack) {
      return (IStrictEnergyHandler)getLazyStrictEnergyHandler(stack).resolve().orElse(null);
   }

   @NotNull
   public static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(@NotNull ItemStack stack) {
      return stack.m_41619_() ? LazyOptional.empty() : getLazyStrictEnergyHandler(stack, null);
   }

   @NotNull
   public static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(@Nullable BlockEntity tile, Direction side) {
      return isTileValid(tile) ? getLazyStrictEnergyHandler((ICapabilityProvider)tile, side) : LazyOptional.empty();
   }

   @NotNull
   private static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, Direction side) {
      for (IEnergyCompat energyCompat : energyCompats) {
         if (energyCompat.isUsable()) {
            LazyOptional<IStrictEnergyHandler> handler = energyCompat.getLazyStrictEnergyHandler(provider, side);
            if (handler.isPresent()) {
               return handler;
            }
         }
      }

      return LazyOptional.empty();
   }

   @NotNull
   public static <T> LazyOptional<T> getEnergyCapability(@NotNull Capability<T> capability, @NotNull IStrictEnergyHandler handler) {
      if (capability.isRegistered()) {
         for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isUsable() && energyCompat.isMatchingCapability(capability)) {
               return energyCompat.getHandlerAs(handler).cast();
            }
         }
      }

      return LazyOptional.empty();
   }

   public static boolean useIC2() {
      return false;
   }
}

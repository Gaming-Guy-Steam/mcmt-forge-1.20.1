package mekanism.common.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import mekanism.api.Coord4D;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;

@ParametersAreNotNullByDefault
public class NBTUtils {
   private NBTUtils() {
   }

   public static void setByteIfPresent(CompoundTag nbt, String key, ByteConsumer setter) {
      if (nbt.m_128425_(key, 1)) {
         setter.accept(nbt.m_128445_(key));
      }
   }

   public static void setBooleanIfPresent(CompoundTag nbt, String key, BooleanConsumer setter) {
      if (nbt.m_128425_(key, 1)) {
         setter.accept(nbt.m_128471_(key));
      }
   }

   public static void setBooleanIfPresentElse(CompoundTag nbt, String key, boolean fallback, BooleanConsumer setter) {
      if (nbt.m_128425_(key, 1)) {
         setter.accept(nbt.m_128471_(key));
      } else {
         setter.accept(fallback);
      }
   }

   public static void setShortIfPresent(CompoundTag nbt, String key, ShortConsumer setter) {
      if (nbt.m_128425_(key, 2)) {
         setter.accept(nbt.m_128448_(key));
      }
   }

   public static void setIntIfPresent(CompoundTag nbt, String key, IntConsumer setter) {
      if (nbt.m_128425_(key, 3)) {
         setter.accept(nbt.m_128451_(key));
      }
   }

   public static void setLongIfPresent(CompoundTag nbt, String key, LongConsumer setter) {
      if (nbt.m_128425_(key, 4)) {
         setter.accept(nbt.m_128454_(key));
      }
   }

   public static void setFloatIfPresent(CompoundTag nbt, String key, FloatConsumer setter) {
      if (nbt.m_128425_(key, 5)) {
         setter.accept(nbt.m_128457_(key));
      }
   }

   public static void setDoubleIfPresent(CompoundTag nbt, String key, DoubleConsumer setter) {
      if (nbt.m_128425_(key, 6)) {
         setter.accept(nbt.m_128459_(key));
      }
   }

   public static void setByteArrayIfPresent(CompoundTag nbt, String key, Consumer<byte[]> setter) {
      if (nbt.m_128425_(key, 7)) {
         setter.accept(nbt.m_128463_(key));
      }
   }

   public static void setStringIfPresent(CompoundTag nbt, String key, Consumer<String> setter) {
      if (nbt.m_128425_(key, 8)) {
         setter.accept(nbt.m_128461_(key));
      }
   }

   public static void setListIfPresent(CompoundTag nbt, String key, int type, Consumer<ListTag> setter) {
      if (nbt.m_128425_(key, 9)) {
         setter.accept(nbt.m_128437_(key, type));
      }
   }

   public static void setCompoundIfPresent(CompoundTag nbt, String key, Consumer<CompoundTag> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(nbt.m_128469_(key));
      }
   }

   public static void setIntArrayIfPresent(CompoundTag nbt, String key, Consumer<int[]> setter) {
      if (nbt.m_128425_(key, 11)) {
         setter.accept(nbt.m_128465_(key));
      }
   }

   public static void setLongArrayIfPresent(CompoundTag nbt, String key, Consumer<long[]> setter) {
      if (nbt.m_128425_(key, 12)) {
         setter.accept(nbt.m_128467_(key));
      }
   }

   public static void setUUIDIfPresent(CompoundTag nbt, String key, Consumer<UUID> setter) {
      if (nbt.m_128403_(key)) {
         setter.accept(nbt.m_128342_(key));
      }
   }

   public static void setUUIDIfPresentElse(CompoundTag nbt, String key, Consumer<UUID> setter, Runnable notPresent) {
      if (nbt.m_128403_(key)) {
         setter.accept(nbt.m_128342_(key));
      } else {
         notPresent.run();
      }
   }

   public static void setBlockPosIfPresent(CompoundTag nbt, String key, Consumer<BlockPos> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(NbtUtils.m_129239_(nbt.m_128469_(key)));
      }
   }

   public static void setCoord4DIfPresent(CompoundTag nbt, String key, Consumer<Coord4D> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(Coord4D.read(nbt.m_128469_(key)));
      }
   }

   public static void setFluidStackIfPresent(CompoundTag nbt, String key, Consumer<FluidStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(FluidStack.loadFluidStackFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setBoxedChemicalIfPresent(CompoundTag nbt, String key, Consumer<BoxedChemical> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(BoxedChemical.read(nbt.m_128469_(key)));
      }
   }

   public static void setGasIfPresent(CompoundTag nbt, String key, Consumer<Gas> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(Gas.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setGasStackIfPresent(CompoundTag nbt, String key, Consumer<GasStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(GasStack.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setInfuseTypeIfPresent(CompoundTag nbt, String key, Consumer<InfuseType> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(InfuseType.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setInfusionStackIfPresent(CompoundTag nbt, String key, Consumer<InfusionStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(InfusionStack.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setPigmentIfPresent(CompoundTag nbt, String key, Consumer<Pigment> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(Pigment.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setPigmentStackIfPresent(CompoundTag nbt, String key, Consumer<PigmentStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(PigmentStack.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setSlurryIfPresent(CompoundTag nbt, String key, Consumer<Slurry> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(Slurry.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setSlurryStackIfPresent(CompoundTag nbt, String key, Consumer<SlurryStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(SlurryStack.readFromNBT(nbt.m_128469_(key)));
      }
   }

   public static void setFloatingLongIfPresent(CompoundTag nbt, String key, FloatingLongConsumer setter) {
      if (nbt.m_128425_(key, 8)) {
         try {
            setter.accept(FloatingLong.parseFloatingLong(nbt.m_128461_(key)));
         } catch (NumberFormatException var4) {
            setter.accept(FloatingLong.ZERO);
         }
      }
   }

   public static void setItemStackIfPresent(CompoundTag nbt, String key, Consumer<ItemStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(ItemStack.m_41712_(nbt.m_128469_(key)));
      }
   }

   public static void setItemStackOrEmpty(CompoundTag nbt, String key, Consumer<ItemStack> setter) {
      if (nbt.m_128425_(key, 10)) {
         setter.accept(ItemStack.m_41712_(nbt.m_128469_(key)));
      } else {
         setter.accept(ItemStack.f_41583_);
      }
   }

   public static void setResourceLocationIfPresent(CompoundTag nbt, String key, Consumer<ResourceLocation> setter) {
      if (nbt.m_128425_(key, 8)) {
         ResourceLocation value = ResourceLocation.m_135820_(nbt.m_128461_(key));
         if (value != null) {
            setter.accept(value);
         }
      }
   }

   public static void setResourceLocationIfPresentElse(CompoundTag nbt, String key, Consumer<ResourceLocation> setter, Runnable notPresent) {
      if (nbt.m_128425_(key, 8)) {
         ResourceLocation value = ResourceLocation.m_135820_(nbt.m_128461_(key));
         if (value == null) {
            notPresent.run();
         } else {
            setter.accept(value);
         }
      }
   }

   public static <REG> void setRegistryEntryIfPresentElse(CompoundTag nbt, String key, IForgeRegistry<REG> registry, Consumer<REG> setter, Runnable notPresent) {
      setResourceLocationIfPresentElse(nbt, key, rl -> {
         REG reg = (REG)registry.getValue(rl);
         if (reg == null) {
            notPresent.run();
         } else {
            setter.accept(reg);
         }
      }, notPresent);
   }

   public static <REG> void setResourceKeyIfPresentElse(
      CompoundTag nbt, String key, ResourceKey<? extends Registry<REG>> registryName, Consumer<ResourceKey<REG>> setter, Runnable notPresent
   ) {
      setResourceLocationIfPresentElse(nbt, key, rl -> setter.accept(ResourceKey.m_135785_(registryName, rl)), notPresent);
   }

   public static <ENUM extends Enum<ENUM>> void setEnumIfPresent(CompoundTag nbt, String key, Int2ObjectFunction<ENUM> indexLookup, Consumer<ENUM> setter) {
      if (nbt.m_128425_(key, 3)) {
         setter.accept((ENUM)indexLookup.apply(nbt.m_128451_(key)));
      }
   }

   public static void writeEnum(CompoundTag nbt, String key, Enum<?> e) {
      nbt.m_128405_(key, e.ordinal());
   }

   public static <V> V readRegistryEntry(CompoundTag nbt, String key, IForgeRegistry<V> registry, V fallback) {
      if (nbt.m_128425_(key, 8)) {
         ResourceLocation rl = ResourceLocation.m_135820_(nbt.m_128461_(key));
         if (rl != null) {
            V result = (V)registry.getValue(rl);
            if (result != null) {
               return result;
            }
         }
      }

      return fallback;
   }

   public static <V> void writeRegistryEntry(CompoundTag nbt, String key, IForgeRegistry<V> registry, V entry) {
      ResourceLocation registryName = registry.getKey(entry);
      if (registryName != null) {
         nbt.m_128359_(key, registryName.toString());
      }
   }

   public static void writeResourceKey(CompoundTag nbt, String key, ResourceKey<?> entry) {
      nbt.m_128359_(key, entry.m_135782_().toString());
   }
}

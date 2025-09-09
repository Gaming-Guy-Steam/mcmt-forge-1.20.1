package mekanism.api.radiation;

import com.google.common.collect.Table;
import java.util.List;
import java.util.ServiceLoader;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@NothingNullByDefault
public interface IRadiationManager {
   IRadiationManager INSTANCE = ServiceLoader.load(IRadiationManager.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IRadiationManager found"));

   boolean isRadiationEnabled();

   DamageSource getRadiationDamageSource(RegistryAccess var1);

   ResourceKey<DamageType> getRadiationDamageTypeKey();

   double getRadiationLevel(Coord4D var1);

   double getRadiationLevel(Entity var1);

   Table<Chunk3D, Coord4D, IRadiationSource> getRadiationSources();

   void removeRadiationSources(Chunk3D var1);

   void removeRadiationSource(Coord4D var1);

   void radiate(Coord4D var1, double var2);

   void radiate(LivingEntity var1, double var2);

   void dumpRadiation(Coord4D var1, IGasHandler var2, boolean var3);

   void dumpRadiation(Coord4D var1, List<IGasTank> var2, boolean var3);

   boolean dumpRadiation(Coord4D var1, GasStack var2);
}

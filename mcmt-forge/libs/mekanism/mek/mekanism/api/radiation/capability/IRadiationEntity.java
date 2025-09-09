package mekanism.api.radiation.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

@AutoRegisterCapability
public interface IRadiationEntity extends INBTSerializable<CompoundTag> {
   double getRadiation();

   void radiate(double var1);

   void decay();

   void update(@NotNull LivingEntity var1);

   void set(double var1);
}

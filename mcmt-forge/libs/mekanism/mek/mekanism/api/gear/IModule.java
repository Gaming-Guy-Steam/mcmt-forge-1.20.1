package mekanism.api.gear;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IModule<MODULE extends ICustomModule<MODULE>> {
   ModuleData<MODULE> getData();

   MODULE getCustomInstance();

   int getInstalledCount();

   boolean isEnabled();

   boolean handlesModeChange();

   boolean handlesRadialModeChange();

   boolean renderHUD();

   void displayModeChange(Player var1, Component var2, IHasTextComponent var3);

   void toggleEnabled(Player var1, Component var2);

   ItemStack getContainer();

   @Nullable
   IEnergyContainer getEnergyContainer();

   FloatingLong getContainerEnergy();

   boolean hasEnoughEnergy(FloatingLongSupplier var1);

   boolean hasEnoughEnergy(FloatingLong var1);

   boolean canUseEnergy(LivingEntity var1, FloatingLong var2);

   boolean canUseEnergy(LivingEntity var1, FloatingLong var2, boolean var3);

   boolean canUseEnergy(LivingEntity var1, @Nullable IEnergyContainer var2, FloatingLong var3, boolean var4);

   FloatingLong useEnergy(LivingEntity var1, FloatingLong var2);

   FloatingLong useEnergy(LivingEntity var1, FloatingLong var2, boolean var3);

   FloatingLong useEnergy(LivingEntity var1, @Nullable IEnergyContainer var2, FloatingLong var3, boolean var4);
}

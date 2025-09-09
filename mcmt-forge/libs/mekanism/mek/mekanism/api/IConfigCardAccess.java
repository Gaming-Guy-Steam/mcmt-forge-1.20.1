package mekanism.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IConfigCardAccess {
   String getConfigCardName();

   BlockEntityType<?> getConfigurationDataType();

   default boolean isConfigurationDataCompatible(BlockEntityType<?> type) {
      return type == this.getConfigurationDataType();
   }

   CompoundTag getConfigurationData(Player var1);

   void setConfigurationData(Player var1, CompoundTag var2);

   void configurationDataSet();
}

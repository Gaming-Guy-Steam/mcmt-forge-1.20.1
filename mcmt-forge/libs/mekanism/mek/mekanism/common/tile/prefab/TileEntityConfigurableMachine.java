package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityConfigurableMachine extends TileEntityMekanism implements ISideConfiguration {
   public TileComponentEjector ejectorComponent;
   public TileComponentConfig configComponent;

   public TileEntityConfigurableMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @Override
   public TileComponentConfig getConfig() {
      return this.configComponent;
   }

   @Override
   public TileComponentEjector getEjector() {
      return this.ejectorComponent;
   }

   @Override
   public CompoundTag getConfigurationData(Player player) {
      CompoundTag data = super.getConfigurationData(player);
      this.getConfig().write(data);
      this.getEjector().write(data);
      return data;
   }

   @Override
   public void setConfigurationData(Player player, CompoundTag data) {
      super.setConfigurationData(player, data);
      this.getConfig().read(data);
      this.getEjector().read(data);
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.ejectorComponent != null) {
         this.ejectorComponent.tickServer();
      }
   }
}

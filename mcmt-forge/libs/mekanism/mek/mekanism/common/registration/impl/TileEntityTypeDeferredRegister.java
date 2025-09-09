package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedDeferredRegister;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeDeferredRegister extends WrappedDeferredRegister<BlockEntityType<?>> {
   public TileEntityTypeDeferredRegister(String modid) {
      super(modid, ForgeRegistries.BLOCK_ENTITY_TYPES);
   }

   public <BE extends TileEntityMekanism> TileEntityTypeRegistryObject<BE> register(
      BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory, BlockEntityTicker<BE> serverTicker, BlockEntityTicker<BE> clientTicker
   ) {
      return this.<BE>builder(block, factory).clientTicker(clientTicker).serverTicker(serverTicker).build();
   }

   @Deprecated
   public <BE extends TileEntityMekanism> TileEntityTypeRegistryObject<BE> register(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
      return this.this_is_not_a_mekanism_tile(block, factory);
   }

   private <BE extends TileEntityMekanism> TileEntityTypeRegistryObject<BE> this_is_not_a_mekanism_tile(
      BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory
   ) {
      return this.register(block, factory, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
   }

   public <BE extends BlockEntity> TileEntityTypeDeferredRegister.BlockEntityTypeBuilder<BE> builder(
      BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory
   ) {
      return new TileEntityTypeDeferredRegister.BlockEntityTypeBuilder<>(block, factory);
   }

   public class BlockEntityTypeBuilder<BE extends BlockEntity> {
      private final BlockRegistryObject<?, ?> block;
      private final BlockEntitySupplier<? extends BE> factory;
      @Nullable
      private BlockEntityTicker<BE> clientTicker;
      @Nullable
      private BlockEntityTicker<BE> serverTicker;

      private BlockEntityTypeBuilder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
         this.block = block;
         this.factory = factory;
      }

      public TileEntityTypeDeferredRegister.BlockEntityTypeBuilder<BE> clientTicker(BlockEntityTicker<BE> ticker) {
         if (this.clientTicker != null) {
            throw new IllegalStateException("Client ticker may only be set once.");
         } else {
            this.clientTicker = ticker;
            return this;
         }
      }

      public TileEntityTypeDeferredRegister.BlockEntityTypeBuilder<BE> serverTicker(BlockEntityTicker<BE> ticker) {
         if (this.serverTicker != null) {
            throw new IllegalStateException("Server ticker may only be set once.");
         } else {
            this.serverTicker = ticker;
            return this;
         }
      }

      public TileEntityTypeDeferredRegister.BlockEntityTypeBuilder<BE> commonTicker(BlockEntityTicker<BE> ticker) {
         return this.clientTicker(ticker).serverTicker(ticker);
      }

      public TileEntityTypeRegistryObject<BE> build() {
         TileEntityTypeRegistryObject<BE> registryObject = new TileEntityTypeRegistryObject<>(null);
         registryObject.clientTicker(this.clientTicker).serverTicker(this.serverTicker);
         return TileEntityTypeDeferredRegister.this.register(
            this.block.getInternalRegistryName(),
            () -> Builder.m_155273_(this.factory, new Block[]{this.block.getBlock()}).m_58966_(null),
            registryObject::setRegistryObject
         );
      }
   }
}

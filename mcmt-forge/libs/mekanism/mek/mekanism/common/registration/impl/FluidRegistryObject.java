package mekanism.common.registration.impl;

import java.util.Objects;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.providers.IFluidProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class FluidRegistryObject<TYPE extends FluidType, STILL extends Fluid, FLOWING extends Fluid, BLOCK extends LiquidBlock, BUCKET extends BucketItem>
   implements IFluidProvider {
   private RegistryObject<TYPE> fluidTypeRO;
   private RegistryObject<STILL> stillRO;
   private RegistryObject<FLOWING> flowingRO;
   private RegistryObject<BLOCK> blockRO;
   private RegistryObject<BUCKET> bucketRO;

   public TYPE getFluidType() {
      return (TYPE)this.fluidTypeRO.get();
   }

   public STILL getStillFluid() {
      return (STILL)this.stillRO.get();
   }

   public FLOWING getFlowingFluid() {
      return (FLOWING)this.flowingRO.get();
   }

   public BLOCK getBlock() {
      return (BLOCK)this.blockRO.get();
   }

   public BUCKET getBucket() {
      return (BUCKET)this.bucketRO.get();
   }

   void updateFluidType(RegistryObject<TYPE> fluidTypeRO) {
      this.fluidTypeRO = Objects.requireNonNull(fluidTypeRO);
   }

   void updateStill(RegistryObject<STILL> stillRO) {
      this.stillRO = Objects.requireNonNull(stillRO);
   }

   void updateFlowing(RegistryObject<FLOWING> flowingRO) {
      this.flowingRO = Objects.requireNonNull(flowingRO);
   }

   void updateBlock(RegistryObject<BLOCK> blockRO) {
      this.blockRO = Objects.requireNonNull(blockRO);
   }

   void updateBucket(RegistryObject<BUCKET> bucketRO) {
      this.bucketRO = Objects.requireNonNull(bucketRO);
   }

   @Override
   public STILL getFluid() {
      return this.getStillFluid();
   }
}

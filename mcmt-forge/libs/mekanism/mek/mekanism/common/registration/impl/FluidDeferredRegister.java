package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalConstant;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidType.Properties;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidDeferredRegister {
   private static final ResourceLocation OVERLAY = new ResourceLocation("block/water_overlay");
   private static final ResourceLocation RENDER_OVERLAY = new ResourceLocation("misc/underwater");
   private static final ResourceLocation LIQUID = Mekanism.rl("liquid/liquid");
   private static final ResourceLocation LIQUID_FLOW = Mekanism.rl("liquid/liquid_flow");
   private static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
      @NotNull
      public ItemStack m_7498_(@NotNull BlockSource source, @NotNull ItemStack stack) {
         Level world = source.m_7727_();
         DispensibleContainerItem bucket = (DispensibleContainerItem)stack.m_41720_();
         BlockPos pos = source.m_7961_().m_121945_((Direction)source.m_6414_().m_61143_(DispenserBlock.f_52659_));
         if (bucket.emptyContents(null, world, pos, null, stack)) {
            bucket.m_142131_(null, world, stack, pos);
            return new ItemStack(Items.f_42446_);
         } else {
            return super.m_7498_(source, stack);
         }
      }
   };
   private final List<FluidRegistryObject<? extends FluidDeferredRegister.MekanismFluidType, ?, ?, ?, ?>> allFluids = new ArrayList<>();
   private final DeferredRegister<FluidType> fluidTypeRegister;
   private final DeferredRegister<Fluid> fluidRegister;
   private final DeferredRegister<Block> blockRegister;
   private final DeferredRegister<Item> itemRegister;
   private final String modid;

   public static Properties getMekBaseBuilder() {
      return Properties.create().sound(SoundActions.BUCKET_FILL, SoundEvents.f_11781_).sound(SoundActions.BUCKET_EMPTY, SoundEvents.f_11778_);
   }

   public FluidDeferredRegister(String modid) {
      this.modid = modid;
      this.blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
      this.fluidRegister = DeferredRegister.create(ForgeRegistries.FLUIDS, modid);
      this.fluidTypeRegister = DeferredRegister.create(Keys.FLUID_TYPES, modid);
      this.itemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
   }

   public FluidRegistryObject<FluidDeferredRegister.MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> registerLiquidChemical(
      IChemicalConstant constants
   ) {
      int density = Math.round(constants.getDensity());
      return this.register(
         constants.getName(),
         properties -> properties.temperature(Math.round(constants.getTemperature())).density(density).viscosity(density).lightLevel(constants.getLightLevel()),
         renderProperties -> renderProperties.tint(constants.getColor())
      );
   }

   public FluidRegistryObject<FluidDeferredRegister.MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> register(
      String name, UnaryOperator<FluidDeferredRegister.FluidTypeRenderProperties> renderProperties
   ) {
      return this.register(name, UnaryOperator.identity(), renderProperties);
   }

   public FluidRegistryObject<FluidDeferredRegister.MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> register(
      String name, UnaryOperator<Properties> properties, UnaryOperator<FluidDeferredRegister.FluidTypeRenderProperties> renderProperties
   ) {
      return this.register(name, BucketItem::new, properties, renderProperties);
   }

   public <BUCKET extends BucketItem> FluidRegistryObject<FluidDeferredRegister.MekanismFluidType, Source, Flowing, LiquidBlock, BUCKET> register(
      String name,
      FluidDeferredRegister.BucketCreator<BUCKET> bucketCreator,
      UnaryOperator<Properties> fluidProperties,
      UnaryOperator<FluidDeferredRegister.FluidTypeRenderProperties> renderProperties
   ) {
      return this.register(
         name,
         fluidProperties.apply(getMekBaseBuilder()),
         renderProperties.apply(FluidDeferredRegister.FluidTypeRenderProperties.builder()),
         bucketCreator,
         FluidDeferredRegister.MekanismFluidType::new
      );
   }

   public <TYPE extends FluidDeferredRegister.MekanismFluidType, BUCKET extends BucketItem> FluidRegistryObject<TYPE, Source, Flowing, LiquidBlock, BUCKET> register(
      String name,
      Properties properties,
      FluidDeferredRegister.FluidTypeRenderProperties renderProperties,
      FluidDeferredRegister.BucketCreator<BUCKET> bucketCreator,
      BiFunction<Properties, FluidDeferredRegister.FluidTypeRenderProperties, TYPE> fluidTypeCreator
   ) {
      String flowingName = "flowing_" + name;
      String bucketName = name + "_bucket";
      properties.descriptionId(Util.m_137492_("block", new ResourceLocation(this.modid, name)));
      FluidRegistryObject<TYPE, Source, Flowing, LiquidBlock, BUCKET> fluidRegistryObject = new FluidRegistryObject<>();
      net.minecraftforge.fluids.ForgeFlowingFluid.Properties fluidProperties = new net.minecraftforge.fluids.ForgeFlowingFluid.Properties(
            fluidRegistryObject::getFluidType, fluidRegistryObject::getStillFluid, fluidRegistryObject::getFlowingFluid
         )
         .bucket(fluidRegistryObject::getBucket)
         .block(fluidRegistryObject::getBlock);
      fluidRegistryObject.updateFluidType(this.fluidTypeRegister.register(name, () -> fluidTypeCreator.apply(properties, renderProperties)));
      fluidRegistryObject.updateStill(this.fluidRegister.register(name, () -> new Source(fluidProperties)));
      fluidRegistryObject.updateFlowing(this.fluidRegister.register(flowingName, () -> new Flowing(fluidProperties)));
      fluidRegistryObject.updateBucket(
         this.itemRegister
            .register(
               bucketName,
               () -> bucketCreator.create(
                  fluidRegistryObject::getStillFluid, new net.minecraft.world.item.Item.Properties().m_41487_(1).m_41495_(Items.f_42446_)
               )
            )
      );
      MapColor color = getClosestColor(renderProperties.color);
      fluidRegistryObject.updateBlock(
         this.blockRegister
            .register(
               name,
               () -> new LiquidBlock(
                  fluidRegistryObject::getStillFluid,
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.m_284310_()
                     .m_60910_()
                     .m_60978_(100.0F)
                     .m_222994_()
                     .m_280170_()
                     .m_278166_(PushReaction.DESTROY)
                     .m_278788_()
                     .m_284180_(color)
               )
            )
      );
      this.allFluids.add(fluidRegistryObject);
      return fluidRegistryObject;
   }

   private static MapColor getClosestColor(int tint) {
      if (tint == -1) {
         return MapColor.f_283808_;
      } else {
         int red = ARGB32.m_13665_(tint);
         int green = ARGB32.m_13667_(tint);
         int blue = ARGB32.m_13669_(tint);
         MapColor color = MapColor.f_283808_;
         double minDistance = Double.MAX_VALUE;

         for (MapColor toTest : MapColor.f_283862_) {
            if (toTest != null && toTest != MapColor.f_283808_) {
               int testRed = ARGB32.m_13665_(toTest.f_283871_);
               int testGreen = ARGB32.m_13667_(toTest.f_283871_);
               int testBlue = ARGB32.m_13669_(toTest.f_283871_);
               double distanceSquare = perceptualColorDistanceSquared(red, green, blue, testRed, testGreen, testBlue);
               if (distanceSquare < minDistance) {
                  minDistance = distanceSquare;
                  color = toTest;
               }
            }
         }

         return color;
      }
   }

   private static double perceptualColorDistanceSquared(int red1, int green1, int blue1, int red2, int green2, int blue2) {
      int redMean = red1 + red2 >> 1;
      int r = red1 - red2;
      int g = green1 - green2;
      int b = blue1 - blue2;
      return ((512 + redMean) * r * r >> 8) + 4 * g * g + ((767 - redMean) * b * b >> 8);
   }

   public void register(IEventBus bus) {
      this.blockRegister.register(bus);
      this.fluidRegister.register(bus);
      this.fluidTypeRegister.register(bus);
      this.itemRegister.register(bus);
   }

   public List<FluidRegistryObject<? extends FluidDeferredRegister.MekanismFluidType, ?, ?, ?, ?>> getAllFluids() {
      return Collections.unmodifiableList(this.allFluids);
   }

   public void registerBucketDispenserBehavior() {
      for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : this.getAllFluids()) {
         DispenserBlock.m_52672_(fluidRO.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
      }
   }

   @FunctionalInterface
   public interface BucketCreator<BUCKET extends BucketItem> {
      BUCKET create(Supplier<? extends Fluid> supplier, net.minecraft.world.item.Item.Properties builder);
   }

   public static class FluidTypeRenderProperties {
      private ResourceLocation stillTexture = FluidDeferredRegister.LIQUID;
      private ResourceLocation flowingTexture = FluidDeferredRegister.LIQUID_FLOW;
      private ResourceLocation overlayTexture = FluidDeferredRegister.OVERLAY;
      private ResourceLocation renderOverlayTexture = FluidDeferredRegister.RENDER_OVERLAY;
      private int color = -1;

      private FluidTypeRenderProperties() {
      }

      public static FluidDeferredRegister.FluidTypeRenderProperties builder() {
         return new FluidDeferredRegister.FluidTypeRenderProperties();
      }

      public FluidDeferredRegister.FluidTypeRenderProperties texture(ResourceLocation still, ResourceLocation flowing) {
         this.stillTexture = still;
         this.flowingTexture = flowing;
         return this;
      }

      public FluidDeferredRegister.FluidTypeRenderProperties texture(ResourceLocation still, ResourceLocation flowing, ResourceLocation overlay) {
         this.stillTexture = still;
         this.flowingTexture = flowing;
         this.overlayTexture = overlay;
         return this;
      }

      public FluidDeferredRegister.FluidTypeRenderProperties renderOverlay(ResourceLocation renderOverlay) {
         this.renderOverlayTexture = renderOverlay;
         return this;
      }

      public FluidDeferredRegister.FluidTypeRenderProperties tint(int color) {
         this.color = color;
         return this;
      }
   }

   public static class MekanismFluidType extends FluidType {
      public final ResourceLocation stillTexture;
      public final ResourceLocation flowingTexture;
      public final ResourceLocation overlayTexture;
      public final ResourceLocation renderOverlayTexture;
      private final int color;

      public MekanismFluidType(Properties properties, FluidDeferredRegister.FluidTypeRenderProperties renderProperties) {
         super(properties);
         this.stillTexture = renderProperties.stillTexture;
         this.flowingTexture = renderProperties.flowingTexture;
         this.overlayTexture = renderProperties.overlayTexture;
         this.renderOverlayTexture = renderProperties.renderOverlayTexture;
         this.color = renderProperties.color;
      }

      public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
         return false;
      }

      public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
         consumer.accept(new IClientFluidTypeExtensions() {
            public ResourceLocation getStillTexture() {
               return MekanismFluidType.this.stillTexture;
            }

            public ResourceLocation getFlowingTexture() {
               return MekanismFluidType.this.flowingTexture;
            }

            public ResourceLocation getOverlayTexture() {
               return MekanismFluidType.this.overlayTexture;
            }

            @Nullable
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
               return MekanismFluidType.this.renderOverlayTexture;
            }

            public int getTintColor() {
               return MekanismFluidType.this.color;
            }
         });
      }
   }
}

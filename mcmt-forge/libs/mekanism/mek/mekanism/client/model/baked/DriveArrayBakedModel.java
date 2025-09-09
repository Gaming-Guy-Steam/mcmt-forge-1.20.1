package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.recipe.upgrade.ItemRecipeData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DriveArrayBakedModel extends ExtensionOverrideBakedModel<byte[]> {
   private static final BiPredicate<byte[], byte[]> DATA_EQUALITY_CHECK = Arrays::equals;
   private static final float[][] DRIVE_PLACEMENTS = new float[][]{
      {0.0F, 0.375F},
      {-0.125F, 0.375F},
      {-0.25F, 0.375F},
      {-0.4375F, 0.375F},
      {-0.5625F, 0.375F},
      {-0.6875F, 0.375F},
      {0.0F, 0.0F},
      {-0.125F, 0.0F},
      {-0.25F, 0.0F},
      {-0.4375F, 0.0F},
      {-0.5625F, 0.0F},
      {-0.6875F, 0.0F}
   };

   public DriveArrayBakedModel(BakedModel original) {
      super(original, DriveArrayBakedModel.DriveArrayOverrideList::new);
   }

   @Override
   public List<BakedQuad> createQuads(ExtensionBakedModel.QuadsKey<byte[]> key) {
      byte[] driveStatus = Objects.requireNonNull(key.getData());
      BlockState blockState = Objects.requireNonNull(key.getBlockState());
      RenderType renderType = key.getLayer();
      QuadTransformation rotation = QuadTransformation.rotate(Attribute.getFacing(blockState));
      Direction side = key.getSide();
      List<BakedQuad> driveQuads = new ArrayList<>();

      for (int i = 0; i < driveStatus.length; i++) {
         TileEntityQIODriveArray.DriveStatus status = TileEntityQIODriveArray.DriveStatus.STATUSES[driveStatus[i]];
         if (status != TileEntityQIODriveArray.DriveStatus.NONE) {
            float[] translation = DRIVE_PLACEMENTS[i];
            QuadTransformation transformation = QuadTransformation.translate(translation[0], translation[1], 0.0);

            for (BakedQuad bakedQuad : MekanismModelCache.INSTANCE.QIO_DRIVES[status.ordinal()]
               .getQuads(blockState, side, key.getRandom(), ModelData.EMPTY, renderType)) {
               Quad quad = new Quad(bakedQuad);
               if (quad.transform(transformation, rotation)) {
                  driveQuads.add(quad.bake());
               } else {
                  driveQuads.add(bakedQuad);
               }
            }
         }
      }

      if (!driveQuads.isEmpty()) {
         List<BakedQuad> ret = new ArrayList<>(key.getQuads());
         ret.addAll(driveQuads);
         return ret;
      } else {
         return key.getQuads();
      }
   }

   @Nullable
   @Override
   public ExtensionBakedModel.QuadsKey<byte[]> createKey(ExtensionBakedModel.QuadsKey<byte[]> key, ModelData data) {
      if (key.getBlockState() != null && key.getSide() == null) {
         byte[] driveStatus = (byte[])data.get(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY);
         if (driveStatus != null) {
            return key.data(driveStatus, Arrays.hashCode(driveStatus), DATA_EQUALITY_CHECK);
         }
      }

      return null;
   }

   protected DriveArrayBakedModel wrapModel(BakedModel model) {
      return new DriveArrayBakedModel(model);
   }

   private static class DriveArrayOverrideList extends ExtensionOverrideBakedModel.ExtendedItemOverrides {
      DriveArrayOverrideList(ItemOverrides original) {
         super(original);
      }

      @Nullable
      @Override
      public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
         if (!stack.m_41619_() && stack.m_41720_() == MekanismBlocks.QIO_DRIVE_ARRAY.m_5456_()) {
            ListTag inventory = ((IItemSustainedInventory)stack.m_41720_()).getSustainedInventory(stack);
            List<IInventorySlot> inventorySlots = ItemRecipeData.readContents(inventory);
            byte[] driveStatus = new byte[12];
            boolean hasFrequency = this.hasFrequency(stack);
            boolean allEmpty = true;

            for (int i = 0; i < driveStatus.length; i++) {
               ItemStack driveStack;
               if (i < inventorySlots.size()) {
                  driveStack = inventorySlots.get(i).getStack();
               } else {
                  driveStack = ItemStack.f_41583_;
               }

               TileEntityQIODriveArray.DriveStatus status;
               if (!(!driveStack.m_41619_() && driveStack.m_41720_() instanceof IQIODriveItem driveItem)) {
                  status = TileEntityQIODriveArray.DriveStatus.NONE;
               } else if (hasFrequency) {
                  allEmpty = false;
                  IQIODriveItem.DriveMetadata metadata = IQIODriveItem.DriveMetadata.load(driveStack);
                  long countCapacity = driveItem.getCountCapacity(driveStack);
                  if (metadata.count() == countCapacity) {
                     status = TileEntityQIODriveArray.DriveStatus.FULL;
                  } else if (metadata.types() != driveItem.getTypeCapacity(driveStack) && !(metadata.count() >= countCapacity * 0.75)) {
                     status = TileEntityQIODriveArray.DriveStatus.READY;
                  } else {
                     status = TileEntityQIODriveArray.DriveStatus.NEAR_FULL;
                  }
               } else {
                  allEmpty = false;
                  status = TileEntityQIODriveArray.DriveStatus.OFFLINE;
               }

               driveStatus[i] = status.status();
            }

            if (!allEmpty) {
               ModelData modelData = ModelData.builder().with(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY, driveStatus).build();
               return this.wrap(model, stack, world, entity, seed, modelData, DriveArrayBakedModel.DriveArrayOverrideList.DriveStatusBakedModel::new);
            }
         }

         return this.original.m_173464_(model, stack, world, entity, seed);
      }

      private boolean hasFrequency(ItemStack stack) {
         if (ItemDataUtils.hasData(stack, "componentFrequency", 10)) {
            CompoundTag frequencyComponent = ItemDataUtils.getCompound(stack, "componentFrequency");
            if (frequencyComponent.m_128425_(FrequencyType.QIO.getName(), 10)) {
               CompoundTag frequencyCompound = frequencyComponent.m_128469_(FrequencyType.QIO.getName());
               Frequency.FrequencyIdentity identity = Frequency.FrequencyIdentity.load(FrequencyType.QIO, frequencyCompound);
               return identity != null && frequencyCompound.m_128403_("owner");
            }
         }

         return false;
      }

      private static class DriveStatusBakedModel extends ModelDataBakedModel {
         private final BlockState targetState = MekanismBlocks.QIO_DRIVE_ARRAY.getBlock().m_49966_();

         public DriveStatusBakedModel(BakedModel original, ModelData data) {
            super(original, data);
         }

         @NotNull
         public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType
         ) {
            return super.getQuads(state == null ? this.targetState : state, side, rand, data, renderType);
         }
      }
   }
}

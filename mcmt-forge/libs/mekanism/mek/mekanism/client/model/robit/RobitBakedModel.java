package mekanism.client.model.robit;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.robit.RobitSkin;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.model.baked.ExtensionBakedModel;
import mekanism.client.model.baked.ExtensionOverrideBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.ItemRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RobitBakedModel extends ExtensionOverrideBakedModel<ResourceLocation> {
   private static final BiPredicate<ResourceLocation, ResourceLocation> DATA_EQUALITY_CHECK = ResourceLocation::equals;

   public RobitBakedModel(BakedModel original) {
      super(original, RobitBakedModel.RobitItemOverrideList::new);
   }

   @Override
   public List<BakedQuad> createQuads(ExtensionBakedModel.QuadsKey<ResourceLocation> key) {
      List<BakedQuad> quads = key.getQuads();
      if (RobitSpriteUploader.UPLOADER != null) {
         ResourceLocation selectedTexture = key.getData();
         QuadTransformation transformation = QuadTransformation.texture(RobitSpriteUploader.UPLOADER.m_118901_(selectedTexture));
         QuadTransformation var5 = QuadTransformation.TextureFilteredTransformation.of(transformation, rl -> rl.m_135815_().equals("missingno"));
         quads = QuadUtils.transformBakedQuads(quads, var5);
      }

      return quads;
   }

   @Nullable
   @Override
   public ExtensionBakedModel.QuadsKey<ResourceLocation> createKey(ExtensionBakedModel.QuadsKey<ResourceLocation> key, ModelData data) {
      ResourceLocation skinTexture = (ResourceLocation)data.get(EntityRobit.SKIN_TEXTURE_PROPERTY);
      return skinTexture == null ? null : key.data(skinTexture, skinTexture.hashCode(), DATA_EQUALITY_CHECK);
   }

   protected RobitBakedModel wrapModel(BakedModel model) {
      return new RobitBakedModel(model);
   }

   private static class RobitItemOverrideList extends ExtensionOverrideBakedModel.ExtendedItemOverrides {
      RobitItemOverrideList(ItemOverrides original) {
         super(original);
      }

      @Nullable
      @Override
      public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
         if (!stack.m_41619_() && stack.m_41720_() instanceof ItemRobit robit) {
            RegistryAccess registryAccess;
            if (world != null) {
               registryAccess = world.m_9598_();
            } else if (entity != null) {
               registryAccess = entity.m_9236_().m_9598_();
            } else {
               ClientLevel level = Minecraft.m_91087_().f_91073_;
               if (level == null) {
                  return this.original.m_173464_(model, stack, null, null, seed);
               }

               registryAccess = level.m_9598_();
            }

            ResourceKey<RobitSkin> skinKey = robit.getRobitSkin(stack);
            MekanismRobitSkins.SkinLookup skinLookup = MekanismRobitSkins.lookup(registryAccess, skinKey);
            RobitSkin skin = skinLookup.skin();
            if (skin.customModel() != null) {
               BakedModel customModel = MekanismModelCache.INSTANCE.getRobitSkin(skinLookup);
               if (customModel != null && customModel != model) {
                  return customModel.m_7343_().m_173464_(customModel, stack, world, entity, seed);
               }
            }

            List<ResourceLocation> textures = skin.textures();
            if (!textures.isEmpty()) {
               ModelData modelData = ModelData.builder().with(EntityRobit.SKIN_TEXTURE_PROPERTY, textures.get(0)).build();
               return this.wrap(model, stack, world, entity, seed, modelData, RobitModelDataBakedModel::new);
            }
         }

         return this.original.m_173464_(model, stack, world, entity, seed);
      }
   }
}

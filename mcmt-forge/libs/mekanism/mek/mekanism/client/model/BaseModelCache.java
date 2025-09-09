package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelBakery.ModelBakerImpl;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import net.minecraftforge.client.model.obj.ObjModel.ModelSettings;
import org.jetbrains.annotations.Nullable;

public class BaseModelCache {
   private final Map<ResourceLocation, BaseModelCache.MekanismModelData> modelMap = new Object2ObjectOpenHashMap();
   private final String modid;

   protected BaseModelCache(String modid) {
      this.modid = modid;
   }

   private ResourceLocation rl(String path) {
      return new ResourceLocation(this.modid, path);
   }

   public void onBake(BakingCompleted evt) {
      this.modelMap.values().forEach(m -> m.reload(evt));
   }

   public void setup(RegisterAdditional event) {
      this.modelMap.values().forEach(mekanismModelData -> mekanismModelData.setup(event));
   }

   protected BaseModelCache.OBJModelData registerOBJ(String path) {
      return this.registerOBJ(this.rl(path));
   }

   protected BaseModelCache.OBJModelData registerOBJ(ResourceLocation rl) {
      return this.register(rl, BaseModelCache.OBJModelData::new);
   }

   protected BaseModelCache.JSONModelData registerJSON(String path) {
      return this.registerJSON(this.rl(path));
   }

   protected BaseModelCache.JSONModelData registerJSON(ResourceLocation rl) {
      return this.register(rl, BaseModelCache.JSONModelData::new);
   }

   protected BaseModelCache.JSONModelData registerJSONAndBake(ResourceLocation rl) {
      ModelManager modelManager = Minecraft.m_91087_().m_91304_();
      ModelBakery modelBakery = modelManager.getModelBakery();
      Objects.requireNonNull(modelBakery);
      ModelBaker baker = new ModelBakerImpl(modelBakery, (modelLoc, material) -> material.m_119204_(), rl);
      BaseModelCache.JSONModelData data = this.registerJSON(rl);
      data.bakedModel = baker.bake(rl, BlockModelRotation.X0_Y0, Material::m_119204_);
      if (modelBakery.m_119341_(rl) instanceof BlockModel blockModel) {
         data.model = blockModel.customData.getCustomGeometry();
      }

      return data;
   }

   protected <DATA extends BaseModelCache.MekanismModelData> DATA register(ResourceLocation rl, Function<ResourceLocation, DATA> creator) {
      DATA data = (DATA)creator.apply(rl);
      this.modelMap.put(rl, data);
      return data;
   }

   public static BakedModel getBakedModel(BakingCompleted evt, ResourceLocation rl) {
      BakedModel bakedModel = (BakedModel)evt.getModels().get(rl);
      if (bakedModel == null) {
         Mekanism.logger.error("Baked model doesn't exist: {}", rl.toString());
         return evt.getModelManager().m_119409_();
      } else {
         return bakedModel;
      }
   }

   public static class JSONModelData extends BaseModelCache.MekanismModelData {
      private BakedModel bakedModel;

      private JSONModelData(ResourceLocation rl) {
         super(rl);
      }

      @Override
      protected void reload(BakingCompleted evt) {
         super.reload(evt);
         this.bakedModel = BaseModelCache.getBakedModel(evt, this.rl);
         if (evt.getModelBakery().m_119341_(this.rl) instanceof BlockModel blockModel) {
            this.model = blockModel.customData.getCustomGeometry();
         }
      }

      @Override
      protected void setup(RegisterAdditional event) {
         event.register(this.rl);
      }

      public void collectQuadVertices(List<Vertex[]> vertices, RandomSource random) {
         for (Quad quad : QuadUtils.unpack(this.getQuads(random))) {
            vertices.add(quad.getVertices());
         }
      }

      public List<BakedQuad> getQuads(RandomSource random) {
         return this.getBakedModel().m_213637_(null, null, random);
      }

      public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
         return this.getBakedModel().getQuads(state, side, rand, data, renderType);
      }

      public BakedModel getBakedModel() {
         return this.bakedModel;
      }
   }

   public static class MekanismModelData {
      protected IUnbakedGeometry<?> model;
      protected final ResourceLocation rl;
      private final Map<IGeometryBakingContext, BakedModel> bakedMap = new Object2ObjectOpenHashMap();

      protected MekanismModelData(ResourceLocation rl) {
         this.rl = rl;
      }

      protected void reload(BakingCompleted evt) {
         this.bakedMap.clear();
      }

      protected void setup(RegisterAdditional event) {
      }

      public BakedModel bake(IGeometryBakingContext config) {
         return this.bakedMap.computeIfAbsent(config, c -> {
            ModelBakery var10002 = Minecraft.m_91087_().m_91304_().getModelBakery();
            Objects.requireNonNull(var10002);
            ModelBaker baker = new ModelBakerImpl(var10002, (modelLoc, material) -> material.m_119204_(), this.rl);
            return this.model.bake(c, baker, Material::m_119204_, BlockModelRotation.X0_Y0, ItemOverrides.f_111734_, this.rl);
         });
      }

      public IUnbakedGeometry<?> getModel() {
         return this.model;
      }
   }

   public static class OBJModelData extends BaseModelCache.MekanismModelData {
      protected OBJModelData(ResourceLocation rl) {
         super(rl);
      }

      @Override
      protected void reload(BakingCompleted evt) {
         super.reload(evt);
         this.model = ObjLoader.INSTANCE.loadModel(new ModelSettings(this.rl, true, this.useDiffuseLighting(), true, true, null));
      }

      public ObjModel getModel() {
         return (ObjModel)super.getModel();
      }

      protected boolean useDiffuseLighting() {
         return true;
      }
   }
}

package mekanism.client.render.obj;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class TransmitterBakedModel extends BakedModelWrapper<BakedModel> {
   private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(new RenderType[]{RenderType.m_110463_()});
   private static final ChunkRenderTypeSet FULL = ChunkRenderTypeSet.of(new RenderType[]{RenderType.m_110463_(), RenderType.m_110466_()});
   private final IGeometryBakingContext owner;
   private final ModelBaker baker;
   private final Function<Material, TextureAtlasSprite> spriteGetter;
   private final ModelState modelTransform;
   private final ItemOverrides overrides;
   private final ResourceLocation modelLocation;
   private final LoadingCache<TransmitterBakedModel.SidedConnection, List<BakedQuad>> internalPartsCache;
   @Nullable
   private final LoadingCache<TransmitterBakedModel.SidedConnection, List<BakedQuad>> glassPartsCache;
   private final LoadingCache<TransmitterBakedModel.TransmitterDataKey, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      .build(
         new CacheLoader<TransmitterBakedModel.TransmitterDataKey, List<BakedQuad>>() {
            @NotNull
            public List<BakedQuad> load(@NotNull TransmitterBakedModel.TransmitterDataKey key) {
               LoadingCache<TransmitterBakedModel.SidedConnection, List<BakedQuad>> partsCache = key.renderGlass
                  ? Objects.requireNonNull(TransmitterBakedModel.this.glassPartsCache)
                  : TransmitterBakedModel.this.internalPartsCache;
               List<BakedQuad> quads = new ArrayList<>();

               for (Direction side : EnumUtils.DIRECTIONS) {
                  ConnectionType connectionType = key.data.getConnectionType(side);
                  TransmitterModelConfiguration.IconStatus iconStatus = TransmitterModelConfiguration.getIconStatus(key.data, side, connectionType);
                  TransmitterBakedModel.SidedConnection sidedConnection = new TransmitterBakedModel.SidedConnection(side, connectionType, iconStatus);
                  quads.addAll((Collection<? extends BakedQuad>)partsCache.getUnchecked(sidedConnection));
               }

               return quads;
            }
         }
      );

   public TransmitterBakedModel(
      ObjModel internal,
      @Nullable ObjModel glass,
      IGeometryBakingContext owner,
      ModelBaker baker,
      Function<Material, TextureAtlasSprite> spriteGetter,
      ModelState modelTransform,
      ItemOverrides overrides,
      ResourceLocation modelLocation
   ) {
      super(
         internal.bake(
            new VisibleModelConfiguration(owner, Arrays.stream(EnumUtils.DIRECTIONS).map(side -> getPartName(side, ConnectionType.NONE)).toList()),
            baker,
            spriteGetter,
            modelTransform,
            overrides,
            modelLocation
         )
      );
      this.owner = owner;
      this.baker = baker;
      this.spriteGetter = spriteGetter;
      this.modelTransform = modelTransform;
      this.overrides = overrides;
      this.modelLocation = modelLocation;
      this.internalPartsCache = CacheBuilder.newBuilder().build(this.createPartCacheLoader(internal));
      this.glassPartsCache = glass == null ? null : CacheBuilder.newBuilder().build(this.createPartCacheLoader(glass));
   }

   public List<BakedQuad> m_213637_(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      return this.getQuads(state, side, rand, ModelData.EMPTY, null);
   }

   @NotNull
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType
   ) {
      if (side != null) {
         return Collections.emptyList();
      } else {
         TransmitterModelData data = (TransmitterModelData)extraData.get(TileEntityTransmitter.TRANSMITTER_PROPERTY);
         if (data != null) {
            boolean renderGlass = renderType == RenderType.m_110466_();
            return !renderGlass || this.glassPartsCache != null && data.getHasColor()
               ? (List)this.cache.getUnchecked(new TransmitterBakedModel.TransmitterDataKey(data, renderGlass))
               : Collections.emptyList();
         } else {
            return super.getQuads(state, null, rand, extraData, renderType);
         }
      }
   }

   public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
      return this.glassPartsCache == null ? CUTOUT : FULL;
   }

   public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
      return this.glassPartsCache == null ? List.of(Sheets.m_110790_()) : List.of(Sheets.m_110790_(), fabulous ? Sheets.m_110792_() : Sheets.m_110791_());
   }

   public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
      return Collections.singletonList(this);
   }

   private static String getPartName(Direction side, ConnectionType connectionType) {
      return side.m_7912_() + connectionType.name();
   }

   private CacheLoader<TransmitterBakedModel.SidedConnection, List<BakedQuad>> createPartCacheLoader(ObjModel model) {
      return new CacheLoader<TransmitterBakedModel.SidedConnection, List<BakedQuad>>() {
         @NotNull
         public List<BakedQuad> load(@NotNull TransmitterBakedModel.SidedConnection key) {
            Direction side = key.side();
            ConnectionType connectionType = key.connection();
            String part = TransmitterBakedModel.getPartName(side, connectionType);
            if (!model.getRootComponentNames().contains(part)) {
               return Collections.emptyList();
            } else {
               TransmitterModelConfiguration.IconStatus iconStatus = key.status();
               ModelState transform = TransmitterBakedModel.this.modelTransform;
               if (connectionType == ConnectionType.NONE && iconStatus.getAngle() > 0.0F) {
                  Vector3f vecForDirection = Vec3.m_82528_(side.m_122436_()).m_252839_();
                  vecForDirection.mul(-1.0F);
                  Quaternionf quaternion = new Quaternionf().setAngleAxis(iconStatus.getAngle(), vecForDirection.x, vecForDirection.y, vecForDirection.z);
                  Transformation matrix = new Transformation(null, quaternion, null, null);
                  transform = new SimpleModelState(transform.m_6189_().m_121096_(matrix), transform.m_7538_());
               }

               BakedModel bakedModel = model.bake(
                  new TransmitterModelConfiguration(TransmitterBakedModel.this.owner, part, iconStatus),
                  TransmitterBakedModel.this.baker,
                  TransmitterBakedModel.this.spriteGetter,
                  transform,
                  TransmitterBakedModel.this.overrides,
                  TransmitterBakedModel.this.modelLocation
               );
               return bakedModel.getQuads(null, null, RandomSource.m_216327_(), ModelData.EMPTY, null);
            }
         }
      };
   }

   private record SidedConnection(Direction side, ConnectionType connection, TransmitterModelConfiguration.IconStatus status) {
   }

   private static class TransmitterDataKey {
      private final TransmitterModelData data;
      private final boolean renderGlass;
      private final int hash;

      public TransmitterDataKey(TransmitterModelData data, boolean renderGlass) {
         this.data = data;
         this.renderGlass = renderGlass;
         this.hash = Objects.hash(this.data.getConnectionsMap(), this.renderGlass);
      }

      @Override
      public int hashCode() {
         return this.hash;
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this
            ? true
            : obj instanceof TransmitterBakedModel.TransmitterDataKey other
               && this.renderGlass == other.renderGlass
               && this.data.getConnectionsMap().equals(other.data.getConnectionsMap());
      }
   }
}

package mekanism.client.model.energycube;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import mekanism.api.RelativeSide;
import mekanism.client.model.baked.ExtensionBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.EnumUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCubeBakedModel implements IDynamicBakedModel {
   private static final TileEntityEnergyCube.CubeSideState[] INACTIVE = (TileEntityEnergyCube.CubeSideState[])Util.m_137469_(
      new TileEntityEnergyCube.CubeSideState[EnumUtils.DIRECTIONS.length], sideStates -> Arrays.fill(sideStates, TileEntityEnergyCube.CubeSideState.INACTIVE)
   );
   private static final QuadTransformation LED_TRANSFORMS = QuadTransformation.list(QuadTransformation.fullbright, QuadTransformation.uvShift(-2.0F, 0.0F));
   private static final BiPredicate<TileEntityEnergyCube.CubeSideState[], TileEntityEnergyCube.CubeSideState[]> DATA_EQUALITY_CHECK = Arrays::equals;
   private final LoadingCache<ExtensionBakedModel.QuadsKey<TileEntityEnergyCube.CubeSideState[]>, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      .build(new CacheLoader<ExtensionBakedModel.QuadsKey<TileEntityEnergyCube.CubeSideState[]>, List<BakedQuad>>() {
         @NotNull
         public List<BakedQuad> load(@NotNull ExtensionBakedModel.QuadsKey<TileEntityEnergyCube.CubeSideState[]> key) {
            return EnergyCubeBakedModel.this.createQuads(key);
         }
      });
   private final EnergyCubeGeometry.FaceData frame;
   private final Map<RelativeSide, EnergyCubeGeometry.FaceData> leds;
   private final Map<RelativeSide, EnergyCubeGeometry.FaceData> activeLEDs;
   private final Map<RelativeSide, EnergyCubeGeometry.FaceData> ports;
   private final Map<RelativeSide, EnergyCubeGeometry.FaceData> activePorts;
   private final ChunkRenderTypeSet blockRenderTypes;
   private final List<RenderType> itemRenderTypes;
   private final List<RenderType> fabulousItemRenderTypes;
   private final boolean isAmbientOcclusion;
   private final boolean usesBlockLight;
   private final boolean isGui3d;
   private final TextureAtlasSprite particle;
   private final ItemOverrides overrides;
   private final ItemTransforms transforms;

   EnergyCubeBakedModel(
      boolean useAmbientOcclusion,
      boolean usesBlockLight,
      boolean isGui3d,
      ItemTransforms transforms,
      ItemOverrides overrides,
      TextureAtlasSprite particle,
      EnergyCubeGeometry.FaceData frame,
      Map<RelativeSide, EnergyCubeGeometry.FaceData> leds,
      Map<RelativeSide, EnergyCubeGeometry.FaceData> ports,
      RenderTypeGroup renderTypes
   ) {
      this.isAmbientOcclusion = useAmbientOcclusion;
      this.usesBlockLight = usesBlockLight;
      this.isGui3d = isGui3d;
      this.overrides = overrides;
      this.transforms = transforms;
      this.particle = particle;
      this.frame = frame;
      this.leds = leds;
      this.ports = ports;
      this.activeLEDs = new EnumMap<>(RelativeSide.class);
      this.activePorts = new EnumMap<>(RelativeSide.class);

      for (Entry<RelativeSide, EnergyCubeGeometry.FaceData> entry : this.leds.entrySet()) {
         this.activeLEDs.put(entry.getKey(), entry.getValue().transform(LED_TRANSFORMS));
      }

      for (Entry<RelativeSide, EnergyCubeGeometry.FaceData> entry : this.ports.entrySet()) {
         this.activePorts.put(entry.getKey(), entry.getValue().transform(QuadTransformation.filtered_fullbright));
      }

      if (renderTypes.isEmpty()) {
         this.blockRenderTypes = null;
         this.itemRenderTypes = null;
         this.fabulousItemRenderTypes = null;
      } else {
         this.blockRenderTypes = ChunkRenderTypeSet.of(new RenderType[]{renderTypes.block()});
         this.itemRenderTypes = Collections.singletonList(renderTypes.entity());
         this.fabulousItemRenderTypes = Collections.singletonList(renderTypes.entityFabulous());
      }
   }

   @NotNull
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType
   ) {
      TileEntityEnergyCube.CubeSideState[] sideStates = (TileEntityEnergyCube.CubeSideState[])data.get(TileEntityEnergyCube.SIDE_STATE_PROPERTY);
      if (sideStates == null || sideStates.length != EnumUtils.SIDES.length) {
         sideStates = INACTIVE;
      }

      ExtensionBakedModel.QuadsKey<TileEntityEnergyCube.CubeSideState[]> key = new ExtensionBakedModel.QuadsKey<>(
         null, side, rand, renderType, this.frame.getFaces(side)
      );
      key.data(sideStates, Arrays.hashCode((Object[])sideStates), DATA_EQUALITY_CHECK);
      return (List<BakedQuad>)this.cache.getUnchecked(key);
   }

   private List<BakedQuad> createQuads(ExtensionBakedModel.QuadsKey<TileEntityEnergyCube.CubeSideState[]> key) {
      Direction side = key.getSide();
      TileEntityEnergyCube.CubeSideState[] data = Objects.requireNonNull(key.getData());
      List<BakedQuad> quads = new ArrayList<>(key.getQuads());

      for (int i = 0; i < EnumUtils.SIDES.length; i++) {
         RelativeSide dir = EnumUtils.SIDES[i];
         TileEntityEnergyCube.CubeSideState sideState = data[i];
         if (sideState == TileEntityEnergyCube.CubeSideState.ACTIVE_LIT) {
            quads.addAll(this.activeLEDs.get(dir).getFaces(side));
            quads.addAll(this.activePorts.get(dir).getFaces(side));
         } else {
            quads.addAll(this.leds.get(dir).getFaces(side));
            if (sideState == TileEntityEnergyCube.CubeSideState.ACTIVE_UNLIT) {
               quads.addAll(this.ports.get(dir).getFaces(side));
            }
         }
      }

      return quads;
   }

   public boolean m_7541_() {
      return this.isAmbientOcclusion;
   }

   public boolean m_7539_() {
      return this.isGui3d;
   }

   public boolean m_7547_() {
      return this.usesBlockLight;
   }

   public boolean m_7521_() {
      return false;
   }

   @Deprecated
   @NotNull
   public TextureAtlasSprite m_6160_() {
      return this.particle;
   }

   @Deprecated
   @NotNull
   public ItemOverrides m_7343_() {
      return this.overrides;
   }

   @Deprecated
   @NotNull
   public ItemTransforms m_7442_() {
      return this.transforms;
   }

   @NotNull
   public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
      return this.blockRenderTypes == null ? super.getRenderTypes(state, rand, data) : this.blockRenderTypes;
   }

   @NotNull
   public List<RenderType> getRenderTypes(@NotNull ItemStack stack, boolean fabulous) {
      if (fabulous) {
         if (this.fabulousItemRenderTypes != null) {
            return this.fabulousItemRenderTypes;
         }
      } else if (this.itemRenderTypes != null) {
         return this.itemRenderTypes;
      }

      return super.getRenderTypes(stack, fabulous);
   }
}

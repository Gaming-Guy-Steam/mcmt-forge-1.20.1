package mekanism.client.render.obj;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrapperModelConfiguration implements IGeometryBakingContext {
   protected final IGeometryBakingContext internal;

   protected WrapperModelConfiguration(IGeometryBakingContext internal) {
      this.internal = internal;
   }

   @NotNull
   public String getModelName() {
      return this.internal.getModelName();
   }

   public boolean hasMaterial(@NotNull String name) {
      return this.internal.hasMaterial(name);
   }

   @NotNull
   public Material getMaterial(@NotNull String name) {
      return this.internal.getMaterial(name);
   }

   public boolean isGui3d() {
      return this.internal.isGui3d();
   }

   public boolean useBlockLight() {
      return false;
   }

   public boolean useAmbientOcclusion() {
      return this.internal.useAmbientOcclusion();
   }

   @NotNull
   public ItemTransforms getTransforms() {
      return this.internal.getTransforms();
   }

   public Transformation getRootTransform() {
      return this.internal.getRootTransform();
   }

   public boolean isComponentVisible(String component, boolean fallback) {
      return this.internal.isComponentVisible(component, fallback);
   }

   @Nullable
   public ResourceLocation getRenderTypeHint() {
      return this.internal.getRenderTypeHint();
   }

   public RenderTypeGroup getRenderType(ResourceLocation name) {
      return this.internal.getRenderType(name);
   }
}

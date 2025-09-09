package mekanism.client.render.armor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.model.BaseModelCache;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.QuickHash;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekaSuitArmor implements ICustomArmor {
   private static final String LED_TAG = "led";
   private static final String INACTIVE_TAG = "inactive_";
   private static final String OVERRIDDEN_TAG = "override_";
   private static final String EXCLUSIVE_TAG = "excl_";
   private static final String SHARED_TAG = "shared_";
   private static final String GLASS_TAG = "glass";
   public static final MekaSuitArmor HELMET = new MekaSuitArmor(EquipmentSlot.HEAD, EquipmentSlot.CHEST);
   public static final MekaSuitArmor BODYARMOR = new MekaSuitArmor(EquipmentSlot.CHEST, EquipmentSlot.HEAD);
   public static final MekaSuitArmor PANTS = new MekaSuitArmor(EquipmentSlot.LEGS, EquipmentSlot.FEET);
   public static final MekaSuitArmor BOOTS = new MekaSuitArmor(EquipmentSlot.FEET, EquipmentSlot.LEGS);
   private static final Table<EquipmentSlot, ModuleData<?>, MekaSuitArmor.ModuleModelSpec> moduleModelSpec = HashBasedTable.create();
   private static final Map<UUID, BoltRenderer> boltRenderMap = new Object2ObjectOpenHashMap();
   private static final QuadTransformation BASE_TRANSFORM = QuadTransformation.list(
      QuadTransformation.rotate(0.0, 0.0, 180.0), QuadTransformation.translate(-1.0, 0.5, 0.0)
   );
   private final LoadingCache<QuickHash, MekaSuitArmor.ArmorQuads> cache = CacheBuilder.newBuilder()
      .build(
         new CacheLoader<QuickHash, MekaSuitArmor.ArmorQuads>() {
            @NotNull
            public MekaSuitArmor.ArmorQuads load(@NotNull QuickHash key) {
               return MekaSuitArmor.this.createQuads(
                  (Object2BooleanMap<MekaSuitArmor.ModuleModelSpec>)key.objs()[0],
                  (Set<EquipmentSlot>)key.objs()[1],
                  (Boolean)key.objs()[2],
                  (Boolean)key.objs()[3]
               );
            }
         }
      );
   private final EquipmentSlot type;
   private final EquipmentSlot adjacentType;

   private MekaSuitArmor(EquipmentSlot type, EquipmentSlot adjacentType) {
      this.type = type;
      this.adjacentType = adjacentType;
      MekanismModelCache.INSTANCE.reloadCallback(this.cache::invalidateAll);
   }

   private static Color getColor(ItemStack stack) {
      if (!stack.m_41619_()) {
         IModule<ModuleColorModulationUnit> colorModulation = IModuleHelper.INSTANCE.load(stack, MekanismModules.COLOR_MODULATION_UNIT);
         if (colorModulation != null) {
            return colorModulation.getCustomInstance().getColor();
         }
      }

      return Color.WHITE;
   }

   public void renderArm(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      LivingEntity entity,
      ItemStack stack,
      boolean rightHand
   ) {
      MekaSuitArmor.ModelPos armPos = rightHand ? MekaSuitArmor.ModelPos.RIGHT_ARM : MekaSuitArmor.ModelPos.LEFT_ARM;
      MekaSuitArmor.ArmorQuads armorQuads = (MekaSuitArmor.ArmorQuads)this.cache.getUnchecked(this.key(entity));
      boolean hasOpaqueArm = armorQuads.opaqueQuads().containsKey(armPos);
      boolean hasTransparentArm = armorQuads.transparentQuads().containsKey(armPos);
      if (hasOpaqueArm || hasTransparentArm) {
         matrix.m_85836_();
         armPos.translate(baseModel, matrix, entity);
         Pose last = matrix.m_85850_();
         if (hasOpaqueArm) {
            VertexConsumer builder = ItemRenderer.m_115222_(renderer, MekanismRenderType.MEKASUIT, false, stack.m_41790_());
            this.putQuads(armorQuads.opaqueQuads().get(armPos), builder, last, light, overlayLight, getColor(stack));
         }

         if (hasTransparentArm) {
            VertexConsumer builder = ItemRenderer.m_115222_(renderer, RenderType.m_110473_(TextureAtlas.f_118259_), false, stack.m_41790_());
            this.putQuads(armorQuads.transparentQuads().get(armPos), builder, last, light, overlayLight, Color.WHITE);
         }

         matrix.m_85849_();
      }
   }

   @Override
   public void render(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      float partialTicks,
      boolean hasEffect,
      LivingEntity entity,
      ItemStack stack
   ) {
      if (baseModel.f_102610_) {
         matrix.m_85836_();
         float f1 = 1.0F / baseModel.f_102011_;
         matrix.m_85841_(f1, f1, f1);
         matrix.m_85837_(0.0, baseModel.f_102012_ / 16.0F, 0.0);
         this.renderMekaSuit(baseModel, matrix, renderer, light, overlayLight, getColor(stack), partialTicks, hasEffect, entity);
         matrix.m_85849_();
      } else {
         this.renderMekaSuit(baseModel, matrix, renderer, light, overlayLight, getColor(stack), partialTicks, hasEffect, entity);
      }
   }

   private void renderMekaSuit(
      HumanoidModel<? extends LivingEntity> baseModel,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight,
      Color color,
      float partialTicks,
      boolean hasEffect,
      LivingEntity entity
   ) {
      MekaSuitArmor.ArmorQuads armorQuads = (MekaSuitArmor.ArmorQuads)this.cache.getUnchecked(this.key(entity));
      this.render(baseModel, renderer, matrix, light, overlayLight, color, hasEffect, entity, armorQuads.opaqueQuads(), false);
      if (this.type == EquipmentSlot.CHEST) {
         BoltRenderer boltRenderer = boltRenderMap.computeIfAbsent(entity.m_20148_(), id -> new BoltRenderer());
         if (IModuleHelper.INSTANCE.isEnabled(entity.m_6844_(EquipmentSlot.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT)) {
            BoltEffect leftBolt = new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, new Vec3(-0.01, 0.35, 0.37), new Vec3(-0.01, 0.15, 0.37), 10)
               .size(0.012F)
               .lifespan(6)
               .spawn(BoltEffect.SpawnFunction.noise(3.0F, 1.0F));
            BoltEffect rightBolt = new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, new Vec3(0.025, 0.35, 0.37), new Vec3(0.025, 0.15, 0.37), 10)
               .size(0.012F)
               .lifespan(6)
               .spawn(BoltEffect.SpawnFunction.noise(3.0F, 1.0F));
            boltRenderer.update(0, leftBolt, partialTicks);
            boltRenderer.update(1, rightBolt, partialTicks);
         }

         matrix.m_85836_();
         MekaSuitArmor.ModelPos.BODY.translate(baseModel, matrix, entity);
         boltRenderer.render(partialTicks, matrix, renderer);
         matrix.m_85849_();
      }

      this.render(baseModel, renderer, matrix, light, overlayLight, Color.WHITE, hasEffect, entity, armorQuads.transparentQuads(), true);
   }

   private void render(
      HumanoidModel<? extends LivingEntity> baseModel,
      MultiBufferSource renderer,
      PoseStack matrix,
      int light,
      int overlayLight,
      Color color,
      boolean hasEffect,
      LivingEntity entity,
      Map<MekaSuitArmor.ModelPos, List<BakedQuad>> quadMap,
      boolean transparent
   ) {
      if (!quadMap.isEmpty()) {
         RenderType renderType = transparent ? RenderType.m_110473_(TextureAtlas.f_118259_) : MekanismRenderType.MEKASUIT;
         VertexConsumer builder = ItemRenderer.m_115222_(renderer, renderType, false, hasEffect);

         for (Entry<MekaSuitArmor.ModelPos, List<BakedQuad>> entry : quadMap.entrySet()) {
            matrix.m_85836_();
            entry.getKey().translate(baseModel, matrix, entity);
            this.putQuads(entry.getValue(), builder, matrix.m_85850_(), light, overlayLight, color);
            matrix.m_85849_();
         }
      }
   }

   private void putQuads(List<BakedQuad> quads, VertexConsumer builder, Pose pose, int light, int overlayLight, Color color) {
      for (BakedQuad quad : quads) {
         builder.putBulkData(pose, quad, color.rf(), color.gf(), color.bf(), color.af(), light, overlayLight, false);
      }
   }

   private static List<BakedQuad> getQuads(
      BaseModelCache.MekanismModelData data, Set<String> parts, Set<String> ledParts, @Nullable QuadTransformation transform
   ) {
      RandomSource random = Minecraft.m_91087_().f_91073_.m_213780_();
      List<BakedQuad> quads = new ArrayList<>();
      if (!parts.isEmpty()) {
         quads.addAll(data.bake(new MekaSuitArmor.MekaSuitModelConfiguration(parts)).getQuads(null, null, random, ModelData.EMPTY, null));
      }

      if (!ledParts.isEmpty()) {
         List<BakedQuad> ledQuads = data.bake(new MekaSuitArmor.MekaSuitModelConfiguration(ledParts)).getQuads(null, null, random, ModelData.EMPTY, null);
         quads.addAll(QuadUtils.transformBakedQuads(ledQuads, QuadTransformation.fullbright));
      }

      if (transform != null) {
         quads = QuadUtils.transformBakedQuads(quads, transform);
      }

      return quads;
   }

   private static void processMekaTool(BaseModelCache.OBJModelData mekaToolModel, Set<String> ignored) {
      for (String name : mekaToolModel.getModel().getRootComponentNames()) {
         if (name.contains("override_")) {
            ignored.add(processOverrideName(name, "mekatool"));
         }
      }
   }

   private MekaSuitArmor.ArmorQuads createQuads(
      Object2BooleanMap<MekaSuitArmor.ModuleModelSpec> modules, Set<EquipmentSlot> wornParts, boolean hasMekaToolLeft, boolean hasMekaToolRight
   ) {
      Map<BaseModelCache.MekanismModelData, Map<MekaSuitArmor.ModelPos, Set<String>>> specialQuadsToRender = new Object2ObjectOpenHashMap();
      Map<BaseModelCache.MekanismModelData, Map<MekaSuitArmor.ModelPos, Set<String>>> specialLEDQuadsToRender = new Object2ObjectOpenHashMap();
      Map<String, MekaSuitArmor.OverrideData> overrides = new Object2ObjectOpenHashMap();
      Set<String> ignored = new HashSet<>();
      if (!modules.isEmpty()) {
         Map<BaseModelCache.MekanismModelData, Set<String>> allMatchedParts = new Object2ObjectOpenHashMap();

         for (MekaSuitArmor.ModuleOBJModelData modelData : MekanismModelCache.INSTANCE.MEKASUIT_MODULES) {
            Set<String> matchedParts = allMatchedParts.computeIfAbsent(modelData, d -> new HashSet<>());
            ObjectIterator pos = modules.object2BooleanEntrySet().iterator();

            while (pos.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<MekaSuitArmor.ModuleModelSpec> entry = (it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<MekaSuitArmor.ModuleModelSpec>)pos.next();
               MekaSuitArmor.ModuleModelSpec spec = (MekaSuitArmor.ModuleModelSpec)entry.getKey();

               for (String name : modelData.getPartsForSpec(spec, entry.getBooleanValue())) {
                  if (name.contains("override_")) {
                     overrides.put(spec.processOverrideName(name), new MekaSuitArmor.OverrideData(modelData, name));
                  }

                  if (this.type == spec.slotType) {
                     matchedParts.add(name);
                  }
               }
            }
         }

         for (Entry<BaseModelCache.MekanismModelData, Set<String>> entry : allMatchedParts.entrySet()) {
            Set<String> matchedParts = entry.getValue();
            if (!matchedParts.isEmpty()) {
               BaseModelCache.MekanismModelData modelData = entry.getKey();
               Map<MekaSuitArmor.ModelPos, Set<String>> quadsToRender = specialQuadsToRender.computeIfAbsent(
                  modelData, d -> new EnumMap<>(MekaSuitArmor.ModelPos.class)
               );
               Map<MekaSuitArmor.ModelPos, Set<String>> ledQuadsToRender = specialLEDQuadsToRender.computeIfAbsent(
                  modelData, d -> new EnumMap<>(MekaSuitArmor.ModelPos.class)
               );

               for (String name : matchedParts) {
                  MekaSuitArmor.ModelPos pos = MekaSuitArmor.ModelPos.get(name);
                  if (pos == null) {
                     Mekanism.logger.warn("MekaSuit part '{}' is invalid from modules model. Ignoring.", name);
                  } else {
                     addQuadsToRender(pos, name, overrides, quadsToRender, ledQuadsToRender, specialQuadsToRender, specialLEDQuadsToRender);
                  }
               }
            }
         }
      }

      if (this.type == EquipmentSlot.CHEST) {
         if (hasMekaToolLeft) {
            processMekaTool(MekanismModelCache.INSTANCE.MEKATOOL_LEFT_HAND, ignored);
         }

         if (hasMekaToolRight) {
            processMekaTool(MekanismModelCache.INSTANCE.MEKATOOL_RIGHT_HAND, ignored);
         }
      }

      Map<MekaSuitArmor.ModelPos, Set<String>> armorQuadsToRender = new EnumMap<>(MekaSuitArmor.ModelPos.class);
      Map<MekaSuitArmor.ModelPos, Set<String>> armorLEDQuadsToRender = new EnumMap<>(MekaSuitArmor.ModelPos.class);

      for (String namex : MekanismModelCache.INSTANCE.MEKASUIT.getModel().getRootComponentNames()) {
         if (checkEquipment(this.type, namex)
            && (
               namex.startsWith("excl_")
                  ? !wornParts.contains(this.adjacentType)
                  : !namex.startsWith("shared_") || !wornParts.contains(this.adjacentType) || this.adjacentType.ordinal() <= this.type.ordinal()
            )) {
            MekaSuitArmor.ModelPos pos = MekaSuitArmor.ModelPos.get(namex);
            if (pos == null) {
               Mekanism.logger.warn("MekaSuit part '{}' is invalid. Ignoring.", namex);
            } else if (!ignored.contains(namex)) {
               addQuadsToRender(pos, namex, overrides, armorQuadsToRender, armorLEDQuadsToRender, specialQuadsToRender, specialLEDQuadsToRender);
            }
         }
      }

      Map<MekaSuitArmor.ModelPos, List<BakedQuad>> opaqueMap = new EnumMap<>(MekaSuitArmor.ModelPos.class);
      Map<MekaSuitArmor.ModelPos, List<BakedQuad>> transparentMap = new EnumMap<>(MekaSuitArmor.ModelPos.class);

      for (MekaSuitArmor.ModelPos pos : MekaSuitArmor.ModelPos.VALUES) {
         for (BaseModelCache.MekanismModelData modelData : MekanismModelCache.INSTANCE.MEKASUIT_MODULES) {
            parseTransparency(
               modelData,
               pos,
               opaqueMap,
               transparentMap,
               specialQuadsToRender.getOrDefault(modelData, Collections.emptyMap()),
               specialLEDQuadsToRender.getOrDefault(modelData, Collections.emptyMap())
            );
         }

         parseTransparency(MekanismModelCache.INSTANCE.MEKASUIT, pos, opaqueMap, transparentMap, armorQuadsToRender, armorLEDQuadsToRender);
      }

      return new MekaSuitArmor.ArmorQuads(opaqueMap, transparentMap);
   }

   private static void addQuadsToRender(
      MekaSuitArmor.ModelPos pos,
      String name,
      Map<String, MekaSuitArmor.OverrideData> overrides,
      Map<MekaSuitArmor.ModelPos, Set<String>> quadsToRender,
      Map<MekaSuitArmor.ModelPos, Set<String>> ledQuadsToRender,
      Map<BaseModelCache.MekanismModelData, Map<MekaSuitArmor.ModelPos, Set<String>>> specialQuadsToRender,
      Map<BaseModelCache.MekanismModelData, Map<MekaSuitArmor.ModelPos, Set<String>>> specialLEDQuadsToRender
   ) {
      MekaSuitArmor.OverrideData override = overrides.get(name);
      if (override != null) {
         name = override.name();
         BaseModelCache.MekanismModelData overrideData = override.modelData();
         quadsToRender = specialQuadsToRender.computeIfAbsent(overrideData, d -> new EnumMap<>(MekaSuitArmor.ModelPos.class));
         ledQuadsToRender = specialLEDQuadsToRender.computeIfAbsent(overrideData, d -> new EnumMap<>(MekaSuitArmor.ModelPos.class));
      }

      if (name.contains("led")) {
         ledQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
      } else {
         quadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
      }
   }

   private static void parseTransparency(
      BaseModelCache.MekanismModelData modelData,
      MekaSuitArmor.ModelPos pos,
      Map<MekaSuitArmor.ModelPos, List<BakedQuad>> opaqueMap,
      Map<MekaSuitArmor.ModelPos, List<BakedQuad>> transparentMap,
      Map<MekaSuitArmor.ModelPos, Set<String>> regularQuads,
      Map<MekaSuitArmor.ModelPos, Set<String>> ledQuads
   ) {
      Set<String> opaqueRegularQuads = new HashSet<>();
      Set<String> opaqueLEDQuads = new HashSet<>();
      Set<String> transparentRegularQuads = new HashSet<>();
      Set<String> transparentLEDQuads = new HashSet<>();
      parseTransparency(pos, opaqueRegularQuads, transparentRegularQuads, regularQuads);
      parseTransparency(pos, opaqueLEDQuads, transparentLEDQuads, ledQuads);
      addParsedQuads(modelData, pos, opaqueMap, opaqueRegularQuads, opaqueLEDQuads);
      addParsedQuads(modelData, pos, transparentMap, transparentRegularQuads, transparentLEDQuads);
   }

   private static void addParsedQuads(
      BaseModelCache.MekanismModelData modelData,
      MekaSuitArmor.ModelPos pos,
      Map<MekaSuitArmor.ModelPos, List<BakedQuad>> map,
      Set<String> quads,
      Set<String> ledQuads
   ) {
      List<BakedQuad> bakedQuads = getQuads(modelData, quads, ledQuads, pos.getTransform());
      if (!bakedQuads.isEmpty()) {
         map.computeIfAbsent(pos, p -> new ArrayList<>()).addAll(bakedQuads);
      }
   }

   private static void parseTransparency(
      MekaSuitArmor.ModelPos pos, Set<String> opaqueQuads, Set<String> transparentQuads, Map<MekaSuitArmor.ModelPos, Set<String>> quads
   ) {
      for (String quad : quads.getOrDefault(pos, Collections.emptySet())) {
         if (quad.contains("glass")) {
            transparentQuads.add(quad);
         } else {
            opaqueQuads.add(quad);
         }
      }
   }

   private static boolean checkEquipment(EquipmentSlot type, String text) {
      return switch (type) {
         case HEAD -> text.contains("helmet");
         case CHEST -> text.contains("chest");
         case LEGS -> text.contains("leggings");
         case FEET -> text.contains("boots");
         default -> false;
      };
   }

   private static String processOverrideName(String part, String name) {
      return part.replaceFirst("override_", "").replaceFirst(name + "_", "");
   }

   public static void registerModule(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType, Predicate<LivingEntity> isActive) {
      ModuleData<?> module = moduleDataProvider.getModuleData();
      moduleModelSpec.put(slotType, module, new MekaSuitArmor.ModuleModelSpec(module, slotType, name, isActive));
   }

   public QuickHash key(LivingEntity player) {
      Object2BooleanMap<MekaSuitArmor.ModuleModelSpec> modules = new Object2BooleanOpenHashMap();
      Set<EquipmentSlot> wornParts = EnumSet.noneOf(EquipmentSlot.class);
      IModuleHelper moduleHelper = IModuleHelper.INSTANCE;

      for (EquipmentSlot slotType : EnumUtils.ARMOR_SLOTS) {
         ItemStack wornItem = player.m_6844_(slotType);
         if (!wornItem.m_41619_() && wornItem.m_41720_() instanceof ItemMekaSuitArmor) {
            wornParts.add(slotType);

            for (Entry<ModuleData<?>, MekaSuitArmor.ModuleModelSpec> entry : moduleModelSpec.row(slotType).entrySet()) {
               if (moduleHelper.isEnabled(wornItem, entry.getKey())) {
                  MekaSuitArmor.ModuleModelSpec spec = entry.getValue();
                  modules.put(spec, spec.isActive(player));
               }
            }
         }
      }

      return new QuickHash(
         modules.isEmpty() ? Object2BooleanMaps.emptyMap() : modules,
         wornParts.isEmpty() ? Collections.emptySet() : wornParts,
         MekanismUtils.getItemInHand(player, HumanoidArm.LEFT).m_41720_() instanceof ItemMekaTool,
         MekanismUtils.getItemInHand(player, HumanoidArm.RIGHT).m_41720_() instanceof ItemMekaTool
      );
   }

   private record ArmorQuads(Map<MekaSuitArmor.ModelPos, List<BakedQuad>> opaqueQuads, Map<MekaSuitArmor.ModelPos, List<BakedQuad>> transparentQuads) {
      public ArmorQuads(Map<MekaSuitArmor.ModelPos, List<BakedQuad>> opaqueQuads, Map<MekaSuitArmor.ModelPos, List<BakedQuad>> transparentQuads) {
         if (opaqueQuads.isEmpty()) {
            opaqueQuads = Collections.emptyMap();
         }

         if (transparentQuads.isEmpty()) {
            transparentQuads = Collections.emptyMap();
         }

         this.opaqueQuads = opaqueQuads;
         this.transparentQuads = transparentQuads;
      }
   }

   private record MekaSuitModelConfiguration(Set<String> parts) implements IGeometryBakingContext {
      private static final Material NO_MATERIAL = new Material(TextureAtlas.f_118259_, MissingTextureAtlasSprite.m_118071_());

      private MekaSuitModelConfiguration(Set<String> parts) {
         parts = parts.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(parts);
         this.parts = parts;
      }

      @NotNull
      public String getModelName() {
         return "mekanism:mekasuit";
      }

      public boolean hasMaterial(@NotNull String name) {
         return false;
      }

      @NotNull
      public Material getMaterial(@NotNull String name) {
         return NO_MATERIAL;
      }

      public boolean isGui3d() {
         return false;
      }

      public boolean useBlockLight() {
         return false;
      }

      public boolean useAmbientOcclusion() {
         return true;
      }

      @Deprecated
      @NotNull
      public ItemTransforms getTransforms() {
         return ItemTransforms.f_111786_;
      }

      @NotNull
      public Transformation getRootTransform() {
         return Transformation.m_121093_();
      }

      @Nullable
      public ResourceLocation getRenderTypeHint() {
         return null;
      }

      public boolean isComponentVisible(String component, boolean fallback) {
         return this.parts.contains(component);
      }
   }

   public static enum ModelPos {
      HEAD(MekaSuitArmor.BASE_TRANSFORM, s -> s.contains("head")),
      BODY(MekaSuitArmor.BASE_TRANSFORM, s -> s.contains("body")),
      LEFT_ARM(MekaSuitArmor.BASE_TRANSFORM.and(QuadTransformation.translate(-0.3125, -0.125, 0.0)), s -> s.contains("left_arm")),
      RIGHT_ARM(MekaSuitArmor.BASE_TRANSFORM.and(QuadTransformation.translate(0.3125, -0.125, 0.0)), s -> s.contains("right_arm")),
      LEFT_LEG(MekaSuitArmor.BASE_TRANSFORM.and(QuadTransformation.translate(-0.125, -0.75, 0.0)), s -> s.contains("left_leg")),
      RIGHT_LEG(MekaSuitArmor.BASE_TRANSFORM.and(QuadTransformation.translate(0.125, -0.75, 0.0)), s -> s.contains("right_leg")),
      LEFT_WING(MekaSuitArmor.BASE_TRANSFORM, s -> s.contains("left_wing")),
      RIGHT_WING(MekaSuitArmor.BASE_TRANSFORM, s -> s.contains("right_wing"));

      private static final float EXPANDED_WING_X = 1.0F;
      private static final float EXPANDED_WING_Y = -2.5F;
      private static final float EXPANDED_WING_Z = 5.0F;
      private static final float EXPANDED_WING_Y_ROT = 45.0F;
      private static final float EXPANDED_WING_Z_ROT = 25.0F;
      public static final MekaSuitArmor.ModelPos[] VALUES = values();
      private final QuadTransformation transform;
      private final Predicate<String> modelSpec;

      private ModelPos(QuadTransformation transform, Predicate<String> modelSpec) {
         this.transform = transform;
         this.modelSpec = modelSpec;
      }

      public QuadTransformation getTransform() {
         return this.transform;
      }

      public boolean contains(String s) {
         return this.modelSpec.test(s);
      }

      public static MekaSuitArmor.ModelPos get(String name) {
         name = name.toLowerCase(Locale.ROOT);

         for (MekaSuitArmor.ModelPos pos : VALUES) {
            if (pos.contains(name)) {
               return pos;
            }
         }

         return null;
      }

      public void translate(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, LivingEntity entity) {
         switch (this) {
            case HEAD:
               baseModel.f_102808_.m_104299_(matrix);
               break;
            case BODY:
               baseModel.f_102810_.m_104299_(matrix);
               break;
            case LEFT_ARM:
               baseModel.f_102812_.m_104299_(matrix);
               break;
            case RIGHT_ARM:
               baseModel.f_102811_.m_104299_(matrix);
               break;
            case LEFT_LEG:
               baseModel.f_102814_.m_104299_(matrix);
               break;
            case RIGHT_LEG:
               baseModel.f_102813_.m_104299_(matrix);
               break;
            case LEFT_WING:
            case RIGHT_WING:
               this.translateWings(baseModel, matrix, entity);
         }
      }

      private void translateWings(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, LivingEntity entity) {
         baseModel.f_102810_.m_104299_(matrix);
         float x = 0.0F;
         float y = 0.0F;
         float z = 0.0F;
         float yRot = 0.0F;
         float zRot = 0.0F;
         if (entity.m_21255_() && entity.m_146909_() < 45.0F) {
            float scale = 0.0F;
            if (entity.m_146909_() > -45.0F || entity.m_20184_().f_82480_ > 1.0) {
               scale = 1.0F;
            } else if (entity.m_20184_().f_82480_ > 0.0) {
               scale = (float)entity.m_20184_().f_82480_;
            }

            x = 1.0F * scale;
            y = -2.5F * scale;
            z = 5.0F * scale;
            yRot = 45.0F * scale;
            zRot = 25.0F * scale;
         }

         if (entity instanceof AbstractClientPlayer player) {
            player.f_108542_ = 0.0F;
            yRot = player.f_108543_ = player.f_108543_ + (yRot - player.f_108543_) * 0.01F;
            float scale = player.f_108543_ / 45.0F;
            x = 1.0F * scale;
            y = -2.5F * scale;
            z = 5.0F * scale;
            zRot = player.f_108544_ = 25.0F * scale;
         }

         if (this == RIGHT_WING) {
            x = -x;
            yRot = -yRot;
            zRot = -zRot;
         }

         matrix.m_252880_(x / 16.0F, y / 16.0F, z / 16.0F);
         if (yRot != 0.0F) {
            matrix.m_252781_(Axis.f_252436_.m_252977_(yRot));
         }

         if (zRot != 0.0F) {
            matrix.m_252781_(Axis.f_252403_.m_252977_(zRot));
         }
      }
   }

   private record ModuleModelSpec(ModuleData<?> module, EquipmentSlot slotType, String name, Predicate<LivingEntity> isActive) {
      public int score(String name) {
         return name.indexOf(this.name + "_");
      }

      public boolean isActive(LivingEntity entity) {
         return this.isActive.test(entity);
      }

      public String processOverrideName(String part) {
         return MekaSuitArmor.processOverrideName(part, this.name);
      }
   }

   public static class ModuleOBJModelData extends BaseModelCache.OBJModelData {
      private final Map<MekaSuitArmor.ModuleModelSpec, MekaSuitArmor.ModuleOBJModelData.SpecData> specParts = new Object2ObjectOpenHashMap();

      public ModuleOBJModelData(ResourceLocation rl) {
         super(rl);
      }

      public Set<String> getPartsForSpec(MekaSuitArmor.ModuleModelSpec spec, boolean active) {
         MekaSuitArmor.ModuleOBJModelData.SpecData specData = this.specParts.get(spec);
         if (specData == null) {
            return Collections.emptySet();
         } else {
            return active ? specData.active() : specData.inactive();
         }
      }

      @Override
      protected void reload(BakingCompleted evt) {
         super.reload(evt);
         Collection<MekaSuitArmor.ModuleModelSpec> modules = MekaSuitArmor.moduleModelSpec.values();

         for (String name : this.getModel().getRootComponentNames()) {
            MekaSuitArmor.ModuleModelSpec matchingSpec = null;
            int bestScore = -1;

            for (MekaSuitArmor.ModuleModelSpec spec : modules) {
               int score = spec.score(name);
               if (score != -1 && (bestScore == -1 || score < bestScore)) {
                  bestScore = score;
                  matchingSpec = spec;
               }
            }

            if (matchingSpec != null) {
               MekaSuitArmor.ModuleOBJModelData.SpecData specData = this.specParts
                  .computeIfAbsent(matchingSpec, specx -> new MekaSuitArmor.ModuleOBJModelData.SpecData(new HashSet<>(), new HashSet<>()));
               if (name.contains("inactive_" + matchingSpec.name + "_")) {
                  specData.inactive().add(name);
               } else {
                  specData.active().add(name);
               }
            }
         }

         for (Entry<MekaSuitArmor.ModuleModelSpec, MekaSuitArmor.ModuleOBJModelData.SpecData> entry : this.specParts.entrySet()) {
            MekaSuitArmor.ModuleOBJModelData.SpecData specData = entry.getValue();
            if (specData.active().isEmpty()) {
               entry.setValue(new MekaSuitArmor.ModuleOBJModelData.SpecData(Collections.emptySet(), specData.inactive()));
            } else if (specData.inactive().isEmpty()) {
               entry.setValue(new MekaSuitArmor.ModuleOBJModelData.SpecData(specData.active(), Collections.emptySet()));
            }
         }
      }

      private record SpecData(Set<String> active, Set<String> inactive) {
      }
   }

   private record OverrideData(BaseModelCache.MekanismModelData modelData, String name) {
   }
}

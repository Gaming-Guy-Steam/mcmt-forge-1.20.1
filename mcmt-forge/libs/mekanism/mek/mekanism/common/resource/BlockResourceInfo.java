package mekanism.common.resource;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public enum BlockResourceInfo implements IResource {
   OSMIUM("osmium", 7.5F, 12.0F, MapColor.f_283772_),
   RAW_OSMIUM("raw_osmium", 7.5F, 12.0F, MapColor.f_283772_, NoteBlockInstrument.BASEDRUM),
   TIN("tin", 5.0F, 6.0F, MapColor.f_283919_),
   RAW_TIN("raw_tin", 5.0F, 6.0F, MapColor.f_283919_, NoteBlockInstrument.BASEDRUM),
   LEAD("lead", 5.0F, 9.0F, MapColor.f_283779_),
   RAW_LEAD("raw_lead", 5.0F, 9.0F, MapColor.f_283779_, NoteBlockInstrument.BASEDRUM),
   URANIUM("uranium", 5.0F, 9.0F, MapColor.f_283824_),
   RAW_URANIUM("raw_uranium", 5.0F, 9.0F, MapColor.f_283824_, NoteBlockInstrument.BASEDRUM),
   CHARCOAL("charcoal", 5.0F, 6.0F, MapColor.f_283927_, NoteBlockInstrument.BASEDRUM, 16000),
   FLUORITE("fluorite", 5.0F, 9.0F, MapColor.f_283811_),
   BRONZE("bronze", 5.0F, 9.0F, MapColor.f_283750_),
   STEEL("steel", 5.0F, 9.0F, MapColor.f_283875_),
   REFINED_OBSIDIAN("refined_obsidian", 50.0F, 2400.0F, MapColor.f_283889_, NoteBlockInstrument.BASEDRUM, -1, 8, false, true, PushReaction.BLOCK),
   REFINED_GLOWSTONE("refined_glowstone", 5.0F, 6.0F, MapColor.f_283832_, NoteBlockInstrument.BASEDRUM, -1, 15);

   private final String registrySuffix;
   private final MapColor mapColor;
   private final PushReaction pushReaction;
   private final boolean portalFrame;
   private final boolean burnsInFire;
   private final NoteBlockInstrument instrument;
   private final float resistance;
   private final float hardness;
   private final int burnTime;
   private final int lightValue;

   private BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor) {
      this(registrySuffix, hardness, resistance, mapColor, null);
   }

   private BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor, @Nullable NoteBlockInstrument instrument) {
      this(registrySuffix, hardness, resistance, mapColor, instrument, -1);
   }

   private BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor, @Nullable NoteBlockInstrument instrument, int burnTime) {
      this(registrySuffix, hardness, resistance, mapColor, instrument, burnTime, 0);
   }

   private BlockResourceInfo(
      String registrySuffix, float hardness, float resistance, MapColor mapColor, @Nullable NoteBlockInstrument instrument, int burnTime, int lightValue
   ) {
      this(registrySuffix, hardness, resistance, mapColor, instrument, burnTime, lightValue, true, false, PushReaction.NORMAL);
   }

   private BlockResourceInfo(
      String registrySuffix,
      float hardness,
      float resistance,
      MapColor mapColor,
      @Nullable NoteBlockInstrument instrument,
      int burnTime,
      int lightValue,
      boolean burnsInFire,
      boolean portalFrame,
      PushReaction pushReaction
   ) {
      this.registrySuffix = registrySuffix;
      this.pushReaction = pushReaction;
      this.portalFrame = portalFrame;
      this.burnsInFire = burnsInFire;
      this.burnTime = burnTime;
      this.lightValue = lightValue;
      this.resistance = resistance;
      this.hardness = hardness;
      this.instrument = instrument;
      this.mapColor = mapColor;
   }

   @Override
   public String getRegistrySuffix() {
      return this.registrySuffix;
   }

   public boolean isPortalFrame() {
      return this.portalFrame;
   }

   public int getBurnTime() {
      return this.burnTime;
   }

   public boolean burnsInFire() {
      return this.burnsInFire;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   public Properties modifyProperties(Properties properties) {
      if (this.instrument != null) {
         properties.m_280658_(this.instrument);
      }

      return properties.m_284180_(this.mapColor).m_60913_(this.hardness, this.resistance).m_60953_(state -> this.lightValue).m_278166_(this.pushReaction);
   }
}

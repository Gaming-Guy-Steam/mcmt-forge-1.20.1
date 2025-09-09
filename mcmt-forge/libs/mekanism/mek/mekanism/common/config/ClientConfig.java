package mekanism.common.config;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ClientConfig extends BaseMekanismConfig {
   private static final String PARTICLE_CATEGORY = "particle";
   private static final String GUI_CATEGORY = "gui";
   private static final String GUI_WINDOW_CATEGORY = "window";
   private static final String QIO_CATEGORY = "qio";
   private final ForgeConfigSpec configSpec;
   public final CachedBooleanValue enablePlayerSounds;
   public final CachedBooleanValue enableMachineSounds;
   public final CachedBooleanValue whiteRadialText;
   public final CachedBooleanValue holidays;
   public final CachedFloatValue baseSoundVolume;
   public final CachedBooleanValue opaqueTransmitters;
   public final CachedBooleanValue allowModeScroll;
   public final CachedBooleanValue reverseHUD;
   public final CachedFloatValue hudScale;
   public final CachedBooleanValue enableHUD;
   public final CachedIntValue energyColor;
   public final CachedIntValue terRange;
   public final CachedBooleanValue enableMultiblockFormationParticles;
   public final CachedBooleanValue machineEffects;
   public final CachedIntValue radiationParticleRadius;
   public final CachedIntValue radiationParticleCount;
   public final CachedBooleanValue renderMagneticAttractionParticles;
   public final CachedBooleanValue renderToolAOEParticles;
   public final CachedFloatValue hudOpacity;
   public final CachedIntValue hudColor;
   public final CachedIntValue hudWarningColor;
   public final CachedIntValue hudDangerColor;
   public final CachedFloatValue hudJitter;
   public final CachedBooleanValue hudCompassEnabled;
   public final Map<String, SelectedWindowData.CachedWindowPosition> lastWindowPositions = new HashMap<>();
   public final CachedEnumValue<QIOItemViewerContainer.ListSortType> qioItemViewerSortType;
   public final CachedEnumValue<QIOItemViewerContainer.SortDirection> qioItemViewerSortDirection;
   public final CachedIntValue qioItemViewerSlotsX;
   public final CachedIntValue qioItemViewerSlotsY;

   ClientConfig() {
      Builder builder = new Builder();
      builder.comment("Client Config. This config only exists on the client").push("client");
      this.enablePlayerSounds = CachedBooleanValue.wrap(
         this, builder.comment("Play sounds for Jetpack/Gas Mask/Flamethrower/Radiation (all players).").define("enablePlayerSounds", true)
      );
      this.enableMachineSounds = CachedBooleanValue.wrap(
         this, builder.comment("If enabled machines play their sounds while running.").define("enableMachineSounds", true)
      );
      this.whiteRadialText = CachedBooleanValue.wrap(
         this, builder.comment("If enabled tries to force all radial menu text to be white.").define("whiteRadialText", false)
      );
      this.holidays = CachedBooleanValue.wrap(
         this, builder.comment("Should holiday greetings and easter eggs play for holidays (ex: Christmas and New Years).").define("holidays", true)
      );
      this.baseSoundVolume = CachedFloatValue.wrap(
         this, builder.comment("Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder.").defineInRange("baseSoundVolume", 1.0, 0.0, 10.0)
      );
      this.opaqueTransmitters = CachedBooleanValue.wrap(
         this, builder.comment("If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents.").define("opaqueTransmitters", false)
      );
      this.allowModeScroll = CachedBooleanValue.wrap(this, builder.comment("Allow sneak + scroll to change item modes.").define("allowModeScroll", true));
      this.reverseHUD = CachedBooleanValue.wrap(
         this,
         builder.comment(
               "If true will move HUD text alignment and compass rendering to the right side of the screen, and move the MekaSuit module rendering to the left side."
            )
            .define("reverseHUD", false)
      );
      this.hudScale = CachedFloatValue.wrap(this, builder.comment("Scale of the text displayed on the HUD.").defineInRange("hudScale", 0.6, 0.25, 1.0));
      this.enableHUD = CachedBooleanValue.wrap(this, builder.comment("Enable item information HUD during gameplay").define("enableHUD", true));
      this.energyColor = CachedIntValue.wrap(this, builder.comment("Color of energy in item durability display.").define("energyColor", 3997338));
      this.terRange = CachedIntValue.wrap(
         this,
         builder.comment(
               "Range at which Tile Entity Renderer's added by Mekanism can render at, for example the contents of multiblocks. Vanilla defaults the rendering range for TERs to 64 for most blocks, but uses a range of 256 for beacons and end gateways."
            )
            .defineInRange("terRange", 256, 1, 1024)
      );
      builder.comment("Particle Config").push("particle");
      this.enableMultiblockFormationParticles = CachedBooleanValue.wrap(
         this,
         builder.comment("Set to false to prevent particle spam when loading multiblocks (notification message will display instead).")
            .define("enableMultiblockFormationParticles", true)
      );
      this.machineEffects = CachedBooleanValue.wrap(this, builder.comment("Show particles when machines active.").define("machineEffects", true));
      this.radiationParticleRadius = CachedIntValue.wrap(
         this, builder.comment("How far (in blocks) from the player radiation particles can spawn.").defineInRange("radiationParticleRadius", 30, 2, 64)
      );
      this.radiationParticleCount = CachedIntValue.wrap(
         this,
         builder.comment("How many particles spawn when rendering radiation effects (scaled by radiation level).")
            .defineInRange("radiationParticleCount", 100, 0, 1000)
      );
      this.renderMagneticAttractionParticles = CachedBooleanValue.wrap(
         this, builder.comment("Show bolts when the Magnetic Attraction Unit is pulling items.").define("magneticAttraction", true)
      );
      this.renderToolAOEParticles = CachedBooleanValue.wrap(
         this, builder.comment("Show bolts for various AOE tool behaviors such as tilling, debarking, and vein mining.").define("toolAOE", true)
      );
      builder.pop();
      builder.comment("GUI Config").push("gui");
      this.hudOpacity = CachedFloatValue.wrap(this, builder.comment("Opacity of HUD used by MekaSuit.").defineInRange("hudOpacity", 0.4F, 0.0, 1.0));
      this.hudColor = CachedIntValue.wrap(this, builder.comment("Color (RGB) of HUD used by MekaSuit.").defineInRange("hudColor", 4257264, 0, 16777215));
      this.hudWarningColor = CachedIntValue.wrap(
         this, builder.comment("Color (RGB) of warning HUD elements used by MekaSuit.").defineInRange("hudWarningColor", 16768335, 0, 16777215)
      );
      this.hudDangerColor = CachedIntValue.wrap(
         this, builder.comment("Color (RGB) of danger HUD elements used by MekaSuit.").defineInRange("hudDangerColor", 16726076, 0, 16777215)
      );
      this.hudJitter = CachedFloatValue.wrap(
         this,
         builder.comment("Visual jitter of MekaSuit HUD, seen when moving the player's head. Bigger value = more jitter.")
            .defineInRange("hudJitter", 6.0, 1.0, 100.0)
      );
      this.hudCompassEnabled = CachedBooleanValue.wrap(
         this, builder.comment("Display a fancy compass when the MekaSuit is worn.").define("mekaSuitHelmetCompass", true)
      );
      builder.comment("Last Window Positions. In general these values should not be modified manually.").push("window");

      for (SelectedWindowData.WindowType windowType : SelectedWindowData.WindowType.values()) {
         for (String savePath : windowType.getSavePaths()) {
            builder.push(savePath);
            this.lastWindowPositions
               .put(
                  savePath,
                  new SelectedWindowData.CachedWindowPosition(
                     CachedIntValue.wrap(this, builder.define("x", Integer.MAX_VALUE)), CachedIntValue.wrap(this, builder.define("y", Integer.MAX_VALUE))
                  )
               );
            builder.pop();
         }
      }

      builder.pop(2);
      builder.comment("QIO Config").push("qio");
      this.qioItemViewerSortType = CachedEnumValue.wrap(
         this,
         builder.comment("Sorting strategy when viewing items in a QIO Item Viewer.")
            .defineEnum("itemViewerSortType", QIOItemViewerContainer.ListSortType.NAME)
      );
      this.qioItemViewerSortDirection = CachedEnumValue.wrap(
         this,
         builder.comment("Sorting direction when viewing items in a QIO Item Viewer.")
            .defineEnum("itemViewerSortDirection", QIOItemViewerContainer.SortDirection.ASCENDING)
      );
      this.qioItemViewerSlotsX = CachedIntValue.wrap(
         this, builder.comment("Number of slots to view horizontally on a QIO Item Viewer.").defineInRange("itemViewerSlotsX", 8, 8, 16)
      );
      this.qioItemViewerSlotsY = CachedIntValue.wrap(
         this, builder.comment("Number of slots to view vertically on a QIO Item Viewer.").defineInRange("itemViewerSlotsY", 4, 2, 48)
      );
      builder.pop();
      builder.pop();
      this.configSpec = builder.build();
   }

   @Override
   public String getFileName() {
      return "client";
   }

   @Override
   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   @Override
   public Type getConfigType() {
      return Type.CLIENT;
   }
}

package mekanism.common.content.gear.mekatool;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleBlastingUnit implements ICustomModule<ModuleBlastingUnit> {
   private IModuleConfigItem<ModuleBlastingUnit.BlastRadius> blastRadius;
   private static final ResourceLocation RADIAL_ID = Mekanism.rl("blasting_mode");
   private static final Int2ObjectMap<Lazy<NestedRadialMode>> RADIAL_DATAS = (Int2ObjectMap<Lazy<NestedRadialMode>>)Util.m_137537_(
      () -> {
         int types = ModuleBlastingUnit.BlastRadius.values().length - 1;
         Int2ObjectMap<Lazy<NestedRadialMode>> map = new Int2ObjectArrayMap(types);

         for (int type = 1; type <= types; type++) {
            int accessibleValues = type + 1;
            map.put(
               type,
               Lazy.of(
                  () -> new NestedRadialMode(
                     IRadialDataHelper.INSTANCE.dataForTruncated(RADIAL_ID, accessibleValues, ModuleBlastingUnit.BlastRadius.LOW),
                     MekanismLang.RADIAL_BLASTING_POWER,
                     ModuleBlastingUnit.BlastRadius.LOW.icon(),
                     EnumColor.DARK_BLUE
                  )
               )
            );
         }

         return map;
      }
   );

   @Override
   public void init(IModule<ModuleBlastingUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.blastRadius = configItemCreator.createConfigItem(
         "blast_radius", MekanismLang.MODULE_BLAST_RADIUS, new ModuleEnumData<>(ModuleBlastingUnit.BlastRadius.LOW, module.getInstalledCount() + 1)
      );
   }

   private NestedRadialMode getNestedData(IModule<ModuleBlastingUnit> module) {
      return (NestedRadialMode)((Lazy)RADIAL_DATAS.get(module.getInstalledCount())).get();
   }

   private RadialData<?> getRadialData(IModule<ModuleBlastingUnit> module) {
      return this.getNestedData(module).nestedData();
   }

   @Override
   public void addRadialModes(IModule<ModuleBlastingUnit> module, @NotNull ItemStack stack, Consumer<NestedRadialMode> adder) {
      adder.accept(this.getNestedData(module));
   }

   @Nullable
   @Override
   public <MODE extends IRadialMode> MODE getMode(IModule<ModuleBlastingUnit> module, ItemStack stack, RadialData<MODE> radialData) {
      return (MODE)(radialData == this.getRadialData(module) ? this.blastRadius.get() : null);
   }

   @Override
   public <MODE extends IRadialMode> boolean setMode(IModule<ModuleBlastingUnit> module, Player player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
      if (radialData == this.getRadialData(module)) {
         ModuleBlastingUnit.BlastRadius newMode = (ModuleBlastingUnit.BlastRadius)mode;
         if (this.blastRadius.get() != newMode) {
            this.blastRadius.set(newMode);
         }
      }

      return false;
   }

   public int getBlastRadius() {
      return this.blastRadius.get().getRadius();
   }

   @Override
   public void addHUDStrings(IModule<ModuleBlastingUnit> module, Player player, Consumer<Component> hudStringAdder) {
      if (module.isEnabled()) {
         hudStringAdder.accept(
            MekanismLang.MODULE_BLASTING_ENABLED.translateColored(EnumColor.DARK_GRAY, new Object[]{EnumColor.INDIGO, this.blastRadius.get()})
         );
      }
   }

   @NothingNullByDefault
   public static enum BlastRadius implements IHasTextComponent, IRadialMode {
      OFF(0, MekanismLang.RADIAL_BLASTING_POWER_OFF, EnumColor.WHITE, "blasting_off"),
      LOW(1, MekanismLang.RADIAL_BLASTING_POWER_LOW, EnumColor.BRIGHT_GREEN, "blasting_low"),
      MED(2, MekanismLang.RADIAL_BLASTING_POWER_MED, EnumColor.YELLOW, "blasting_med"),
      HIGH(3, MekanismLang.RADIAL_BLASTING_POWER_HIGH, EnumColor.ORANGE, "blasting_high"),
      EXTREME(4, MekanismLang.RADIAL_BLASTING_POWER_EXTREME, EnumColor.RED, "blasting_extreme");

      private final int radius;
      private final Component label;
      private final EnumColor color;
      private final ResourceLocation icon;
      private final ILangEntry langEntry;

      private BlastRadius(int radius, ILangEntry langEntry, EnumColor color, String texture) {
         this.radius = radius;
         this.label = MekanismLang.MODULE_BLAST_AREA.translate(new Object[]{2 * radius + 1});
         this.langEntry = langEntry;
         this.color = color;
         this.icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, texture + ".png");
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public int getRadius() {
         return this.radius;
      }

      @NotNull
      @Override
      public Component sliceName() {
         return this.langEntry.translateColored(this.color);
      }

      @NotNull
      @Override
      public ResourceLocation icon() {
         return this.icon;
      }

      @Override
      public EnumColor color() {
         return this.color;
      }
   }
}

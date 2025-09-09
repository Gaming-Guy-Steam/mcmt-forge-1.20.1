package mekanism.common.util;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.IDisableableEnum;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.listener.ConfigBasedCachedFLSupplier;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnitDisplayUtils {
   public static Component getDisplay(FloatingLong value, UnitDisplayUtils.EnergyUnit unit, int decimalPlaces, boolean isShort) {
      ILangEntry label = unit.pluralLangEntry;
      if (isShort) {
         label = unit.shortLangEntry;
      } else if (value.equals(FloatingLong.ONE)) {
         label = unit.singularLangEntry;
      }

      if (value.isZero()) {
         return TextComponentUtil.build(value + " ", label);
      } else {
         for (int i = 0; i < EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS.length; i++) {
            UnitDisplayUtils.FloatingLongMeasurementUnit lowerMeasure = EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS[i];
            if (i == 0 && lowerMeasure.below(value)
               || i + 1 >= EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS.length
               || lowerMeasure.aboveEqual(value) && EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS[i + 1].below(value)) {
               return TextComponentUtil.build(lowerMeasure.process(value).toString(decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
            }
         }

         return TextComponentUtil.build(value.toString(decimalPlaces), label);
      }
   }

   public static Component getDisplayShort(FloatingLong value, UnitDisplayUtils.EnergyUnit unit) {
      return getDisplay(value, unit, 2, true);
   }

   public static Component getDisplay(
      double temp, UnitDisplayUtils.TemperatureUnit unit, int decimalPlaces, boolean shift, boolean isShort, boolean spaceBetweenSymbol
   ) {
      return getDisplayBase(unit.convertFromK(temp, shift), unit, decimalPlaces, isShort, spaceBetweenSymbol);
   }

   public static Component getDisplayBase(double value, UnitDisplayUtils.Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol) {
      if (value == 0.0) {
         if (isShort) {
            String spaceStr = spaceBetweenSymbol ? " " : "";
            return TextComponentUtil.getString(value + spaceStr + unit.getSymbol());
         } else {
            return TextComponentUtil.build(value, unit.getLabel());
         }
      } else {
         boolean negative = value < 0.0;
         if (negative) {
            value = Math.abs(value);
         }

         for (int i = 0; i < EnumUtils.MEASUREMENT_UNITS.length; i++) {
            UnitDisplayUtils.MeasurementUnit lowerMeasure = EnumUtils.MEASUREMENT_UNITS[i];
            if (i == 0 && lowerMeasure.below(value)
               || i + 1 >= EnumUtils.MEASUREMENT_UNITS.length
               || lowerMeasure.aboveEqual(value) && EnumUtils.MEASUREMENT_UNITS[i + 1].below(value)) {
               return lowerMeasure.getDisplay(value, unit, decimalPlaces, isShort, spaceBetweenSymbol, negative);
            }
         }

         return EnumUtils.MEASUREMENT_UNITS[EnumUtils.MEASUREMENT_UNITS.length - 1]
            .getDisplay(value, unit, decimalPlaces, isShort, spaceBetweenSymbol, negative);
      }
   }

   public static Component getDisplayShort(double value, UnitDisplayUtils.TemperatureUnit unit) {
      return getDisplayShort(value, unit, true);
   }

   public static Component getDisplayShort(double value, UnitDisplayUtils.TemperatureUnit unit, boolean shift) {
      return getDisplayShort(value, unit, shift, 2);
   }

   public static Component getDisplayShort(double value, UnitDisplayUtils.TemperatureUnit unit, boolean shift, int decimalPlaces) {
      return getDisplay(value, unit, decimalPlaces, shift, true, false);
   }

   public static Component getDisplayShort(double value, UnitDisplayUtils.RadiationUnit unit, int decimalPlaces) {
      return getDisplayBase(value, unit, decimalPlaces, true, true);
   }

   public static double roundDecimals(boolean negative, double d, int decimalPlaces) {
      return negative ? roundDecimals(-d, decimalPlaces) : roundDecimals(d, decimalPlaces);
   }

   public static double roundDecimals(double d, int decimalPlaces) {
      double multiplier = Math.pow(10.0, decimalPlaces);
      long j = (long)(d * multiplier);
      return j / multiplier;
   }

   public static double roundDecimals(double d) {
      return roundDecimals(d, 2);
   }

   @NothingNullByDefault
   public static enum EnergyUnit implements IDisableableEnum<UnitDisplayUtils.EnergyUnit>, IEnergyConversion {
      JOULES(MekanismLang.ENERGY_JOULES, MekanismLang.ENERGY_JOULES_PLURAL, MekanismLang.ENERGY_JOULES_SHORT, "j", null, () -> true) {
         @Override
         protected FloatingLong getConversion() {
            return FloatingLong.ONE;
         }

         @Override
         protected FloatingLong getInverseConversion() {
            return FloatingLong.ONE;
         }

         @Override
         public FloatingLong convertFrom(FloatingLong joules) {
            return joules;
         }

         @Override
         public FloatingLong convertInPlaceFrom(FloatingLong joules) {
            return joules;
         }

         @Override
         public FloatingLong convertTo(FloatingLong joules) {
            return joules;
         }

         @Override
         public FloatingLong convertInPlaceTo(FloatingLong joules) {
            return joules;
         }
      },
      FORGE_ENERGY(
         MekanismLang.ENERGY_FORGE,
         MekanismLang.ENERGY_FORGE,
         MekanismLang.ENERGY_FORGE_SHORT,
         "fe",
         () -> MekanismConfig.general.forgeConversionRate,
         () -> !MekanismConfig.general.blacklistForge.getOrDefault()
      ),
      ELECTRICAL_UNITS(
         MekanismLang.ENERGY_EU,
         MekanismLang.ENERGY_EU_PLURAL,
         MekanismLang.ENERGY_EU_SHORT,
         "eu",
         () -> MekanismConfig.general.ic2ConversionRate,
         EnergyCompatUtils::useIC2
      );

      private static final UnitDisplayUtils.EnergyUnit[] TYPES = values();
      private final Supplier<CachedFloatingLongValue> conversion;
      private final Supplier<FloatingLongSupplier> inverseConversion;
      private final BooleanSupplier checkEnabled;
      private final ILangEntry singularLangEntry;
      private final ILangEntry pluralLangEntry;
      private final ILangEntry shortLangEntry;
      private final String tabName;

      private EnergyUnit(
         ILangEntry singularLangEntry,
         ILangEntry pluralLangEntry,
         ILangEntry shortLangEntry,
         String tabName,
         @Nullable Supplier<CachedFloatingLongValue> conversionRate,
         BooleanSupplier checkEnabled
      ) {
         this.singularLangEntry = singularLangEntry;
         this.pluralLangEntry = pluralLangEntry;
         this.shortLangEntry = shortLangEntry;
         this.checkEnabled = checkEnabled;
         this.tabName = tabName;
         this.conversion = conversionRate;
         if (this.conversion == null) {
            this.inverseConversion = null;
         } else {
            this.inverseConversion = Lazy.of(() -> new ConfigBasedCachedFLSupplier(() -> FloatingLong.ONE.divide(this.getConversion()), this.conversion.get()));
         }
      }

      protected FloatingLong getConversion() {
         return this.conversion.get().getOrDefault();
      }

      protected FloatingLong getInverseConversion() {
         return this.inverseConversion.get().get();
      }

      @Override
      public FloatingLong convertFrom(FloatingLong energy) {
         return energy.multiply(this.getConversion());
      }

      @Override
      public FloatingLong convertInPlaceFrom(FloatingLong energy) {
         return energy.timesEqual(this.getConversion());
      }

      @Override
      public FloatingLong convertTo(FloatingLong joules) {
         return joules.isZero() ? FloatingLong.ZERO : joules.multiply(this.getInverseConversion());
      }

      @Override
      public FloatingLong convertInPlaceTo(FloatingLong joules) {
         return joules.isZero() ? joules : joules.timesEqual(this.getInverseConversion());
      }

      @Override
      public String getTranslationKey() {
         return this.shortLangEntry.getTranslationKey();
      }

      @NotNull
      public UnitDisplayUtils.EnergyUnit byIndex(int index) {
         return MathUtils.getByIndexMod(TYPES, index);
      }

      public String getTabName() {
         return this.tabName;
      }

      @Override
      public boolean isEnabled() {
         return this.checkEnabled.getAsBoolean();
      }

      public static UnitDisplayUtils.EnergyUnit getConfigured() {
         UnitDisplayUtils.EnergyUnit type = MekanismConfig.common.energyUnit.get();
         return type.isEnabled() ? type : JOULES;
      }
   }

   public static enum FloatingLongMeasurementUnit {
      MILLI("Milli", "m", FloatingLong.createConst(0.001)),
      BASE("", "", FloatingLong.ONE),
      KILO("Kilo", "k", FloatingLong.createConst(1000L)),
      MEGA("Mega", "M", FloatingLong.createConst(1000000L)),
      GIGA("Giga", "G", FloatingLong.createConst(1000000000L)),
      TERA("Tera", "T", FloatingLong.createConst(1000000000000L)),
      PETA("Peta", "P", FloatingLong.createConst(1000000000000000L)),
      EXA("Exa", "E", FloatingLong.createConst(1000000000000000000L));

      private final String name;
      private final String symbol;
      private final FloatingLong value;

      private FloatingLongMeasurementUnit(String name, String symbol, FloatingLong value) {
         this.name = name;
         this.symbol = symbol;
         this.value = value;
      }

      public String getName(boolean getShort) {
         return getShort ? this.symbol : this.name;
      }

      public FloatingLong process(FloatingLong d) {
         return d.divide(this.value);
      }

      public boolean aboveEqual(FloatingLong d) {
         return d.greaterOrEqual(this.value);
      }

      public boolean below(FloatingLong d) {
         return d.smallerThan(this.value);
      }
   }

   public static enum MeasurementUnit {
      FEMTO("Femto", "f", 1.0E-15),
      PICO("Pico", "p", 1.0E-12),
      NANO("Nano", "n", 1.0E-9),
      MICRO("Micro", "µ", 1.0E-6),
      MILLI("Milli", "m", 0.001),
      BASE("", "", 1.0),
      KILO("Kilo", "k", 1000.0),
      MEGA("Mega", "M", 1000000.0),
      GIGA("Giga", "G", 1.0E9),
      TERA("Tera", "T", 1.0E12),
      PETA("Peta", "P", 1.0E15),
      EXA("Exa", "E", 1.0E18),
      ZETTA("Zetta", "Z", 1.0E21),
      YOTTA("Yotta", "Y", 1.0E24);

      private final String name;
      private final String symbol;
      private final double value;

      private MeasurementUnit(String name, String symbol, double value) {
         this.name = name;
         this.symbol = symbol;
         this.value = value;
      }

      public String getName(boolean isShort) {
         return isShort ? this.symbol : this.name;
      }

      public double process(double d) {
         return d / this.value;
      }

      public boolean aboveEqual(double d) {
         return d >= this.value;
      }

      public boolean below(double d) {
         return d < this.value;
      }

      private Component getDisplay(double value, UnitDisplayUtils.Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol, boolean negative) {
         double rounded = UnitDisplayUtils.roundDecimals(negative, this.process(value), decimalPlaces);
         String name = this.getName(isShort);
         if (isShort) {
            if (spaceBetweenSymbol) {
               name = " " + name;
            }

            return TextComponentUtil.getString(rounded + name + unit.getSymbol());
         } else {
            return TextComponentUtil.build(rounded + " " + name, unit.getLabel());
         }
      }
   }

   public static enum RadiationUnit implements UnitDisplayUtils.Unit {
      SV("Sv"),
      SVH("Sv/h");

      private final String symbol;

      private RadiationUnit(String symbol) {
         this.symbol = symbol;
      }

      @Override
      public String getSymbol() {
         return this.symbol;
      }

      @Override
      public ILangEntry getLabel() {
         return MekanismLang.ERROR;
      }
   }

   @NothingNullByDefault
   public static enum TemperatureUnit implements IIncrementalEnum<UnitDisplayUtils.TemperatureUnit>, IHasTranslationKey, UnitDisplayUtils.Unit {
      KELVIN(MekanismLang.TEMPERATURE_KELVIN, MekanismLang.TEMPERATURE_KELVIN_SHORT, "K", "k", 0.0, 1.0),
      CELSIUS(MekanismLang.TEMPERATURE_CELSIUS, MekanismLang.TEMPERATURE_CELSIUS_SHORT, "°C", "c", 273.15, 1.0),
      RANKINE(MekanismLang.TEMPERATURE_RANKINE, MekanismLang.TEMPERATURE_RANKINE_SHORT, "R", "r", 0.0, 1.8),
      FAHRENHEIT(MekanismLang.TEMPERATURE_FAHRENHEIT, MekanismLang.TEMPERATURE_FAHRENHEIT_SHORT, "°F", "f", 459.67, 1.8),
      AMBIENT(MekanismLang.TEMPERATURE_AMBIENT, MekanismLang.TEMPERATURE_AMBIENT_SHORT, "+STP", "stp", 300.0, 1.0);

      private static final UnitDisplayUtils.TemperatureUnit[] TYPES = values();
      private final ILangEntry langEntry;
      private final ILangEntry shortName;
      private final String symbol;
      private final String tabName;
      public final double zeroOffset;
      public final double intervalSize;

      private TemperatureUnit(ILangEntry langEntry, ILangEntry shortName, String symbol, String tabName, double offset, double size) {
         this.langEntry = langEntry;
         this.shortName = shortName;
         this.symbol = symbol;
         this.tabName = tabName;
         this.zeroOffset = offset;
         this.intervalSize = size;
      }

      public double convertFromK(double temp, boolean shift) {
         return temp * this.intervalSize - (shift ? this.zeroOffset : 0.0);
      }

      public double convertToK(double temp, boolean shift) {
         return (temp + (shift ? this.zeroOffset : 0.0)) / this.intervalSize;
      }

      @Override
      public String getSymbol() {
         return this.symbol;
      }

      @Override
      public ILangEntry getLabel() {
         return this.langEntry;
      }

      @Override
      public String getTranslationKey() {
         return this.shortName.getTranslationKey();
      }

      public String getTabName() {
         return this.tabName;
      }

      public UnitDisplayUtils.TemperatureUnit byIndex(int index) {
         return MathUtils.getByIndexMod(TYPES, index);
      }
   }

   private interface Unit {
      String getSymbol();

      ILangEntry getLabel();
   }
}

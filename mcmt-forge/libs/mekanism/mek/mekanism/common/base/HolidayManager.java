package mekanism.common.base;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class HolidayManager {
   public static final HolidayManager.Holiday CHRISTMAS = new HolidayManager.Christmas();
   public static final HolidayManager.Holiday NEW_YEAR = new HolidayManager.NewYear();
   public static final HolidayManager.Holiday MAY_4 = new HolidayManager.May4();
   public static final HolidayManager.Holiday APRIL_FOOLS = new HolidayManager.AprilFools();
   private static final Set<HolidayManager.Holiday> holidays = Set.of(CHRISTMAS, NEW_YEAR, MAY_4, APRIL_FOOLS);

   public static void init() {
      LocalDate time = LocalDate.now();
      HolidayManager.YearlyDate date = new HolidayManager.YearlyDate(time.getMonth(), time.getDayOfMonth());

      for (HolidayManager.Holiday holiday : holidays) {
         holiday.updateIsToday(date);
      }

      Mekanism.logger.info("Initialized HolidayManager.");
   }

   public static void notify(Player player) {
      if (MekanismConfig.client.holidays.get()) {
         for (HolidayManager.Holiday holiday : holidays) {
            if (holiday.isToday() && !holiday.hasNotified()) {
               holiday.notify(player);
            }
         }
      }
   }

   public static SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
      if (MekanismConfig.client.isLoaded() && MekanismConfig.client.holidays.get()) {
         for (HolidayManager.Holiday holiday : holidays) {
            if (holiday.isToday()) {
               return holiday.filterSound(sound);
            }
         }
      }

      return sound;
   }

   private static String getThemedLines(int amount, EnumColor... colors) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < amount; i++) {
         builder.append(colors[i % colors.length]).append("-");
      }

      return builder.toString();
   }

   private static class AprilFools extends HolidayManager.Holiday {
      private AprilFools() {
         super(new HolidayManager.YearlyDate(Month.APRIL, 1));
      }
   }

   private static class Christmas extends HolidayManager.Holiday {
      private Christmas() {
         super(new HolidayManager.YearlyDate(Month.DECEMBER, 25));
      }

      @Nullable
      @Override
      protected HolidayManager.HolidayMessage getMessage(Player player) {
         return new HolidayManager.HolidayMessage(
            HolidayManager.getThemedLines(13, EnumColor.DARK_GREEN, EnumColor.DARK_RED),
            MekanismLang.CHRISTMAS_LINE_ONE.translateColored(EnumColor.RED, new Object[]{EnumColor.DARK_BLUE, player.m_7755_()}),
            MekanismLang.CHRISTMAS_LINE_TWO.translateColored(EnumColor.RED, new Object[0]),
            MekanismLang.CHRISTMAS_LINE_THREE.translateColored(EnumColor.RED, new Object[0]),
            MekanismLang.CHRISTMAS_LINE_FOUR.translateColored(EnumColor.RED, new Object[0]),
            MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY, new Object[0])
         );
      }

      @Override
      public SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
         if (sound == MekanismSounds.ENRICHMENT_CHAMBER) {
            return MekanismSounds.CHRISTMAS1;
         } else if (sound == MekanismSounds.METALLURGIC_INFUSER) {
            return MekanismSounds.CHRISTMAS2;
         } else if (sound == MekanismSounds.PURIFICATION_CHAMBER) {
            return MekanismSounds.CHRISTMAS3;
         } else if (sound == MekanismSounds.ENERGIZED_SMELTER) {
            return MekanismSounds.CHRISTMAS4;
         } else {
            return sound == MekanismSounds.CRUSHER ? MekanismSounds.CHRISTMAS5 : super.filterSound(sound);
         }
      }
   }

   public abstract static class Holiday {
      private final HolidayManager.YearlyDate date;
      private boolean hasNotified;
      private boolean isToday;

      protected Holiday(HolidayManager.YearlyDate date) {
         this.date = date;
      }

      public HolidayManager.YearlyDate getDate() {
         return this.date;
      }

      protected boolean checkIsToday(HolidayManager.YearlyDate date) {
         return this.getDate().equals(date);
      }

      @Nullable
      protected HolidayManager.HolidayMessage getMessage(Player player) {
         return null;
      }

      public SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
         return sound;
      }

      private boolean hasNotified() {
         return this.hasNotified;
      }

      private void notify(Player player) {
         HolidayManager.HolidayMessage message = this.getMessage(player);
         if (message != null) {
            player.m_213846_(
               MekanismLang.HOLIDAY_BORDER
                  .translate(
                     new Object[]{message.themedLines, EnumColor.DARK_BLUE, MekanismLang.GENERIC_SQUARE_BRACKET.translate(new Object[]{MekanismLang.MEKANISM})}
                  )
            );

            for (Component line : message.lines) {
               player.m_213846_(line);
            }

            player.m_213846_(MekanismLang.HOLIDAY_BORDER.translate(new Object[]{message.themedLines, EnumColor.DARK_BLUE, "[=======]"}));
         }

         this.hasNotified = true;
      }

      private void updateIsToday(HolidayManager.YearlyDate date) {
         this.isToday = this.checkIsToday(date);
         if (!this.isToday) {
            this.hasNotified = false;
         }
      }

      public boolean isToday() {
         return this.isToday;
      }
   }

   private record HolidayMessage(String themedLines, Component... lines) {
      @Override
      public boolean equals(Object o) {
         return this == o
            ? true
            : o instanceof HolidayManager.HolidayMessage other
               && this.themedLines.equals(other.themedLines)
               && Arrays.equals((Object[])this.lines, (Object[])other.lines);
      }

      @Override
      public int hashCode() {
         int result = this.themedLines.hashCode();
         return 31 * result + Arrays.hashCode((Object[])this.lines);
      }
   }

   private static class May4 extends HolidayManager.Holiday {
      private May4() {
         super(new HolidayManager.YearlyDate(Month.MAY, 4));
      }

      @Nullable
      @Override
      protected HolidayManager.HolidayMessage getMessage(Player player) {
         return new HolidayManager.HolidayMessage(
            HolidayManager.getThemedLines(15, EnumColor.BLACK, EnumColor.GRAY, EnumColor.BLACK, EnumColor.YELLOW, EnumColor.BLACK),
            MekanismLang.MAY_4_LINE_ONE.translateColored(EnumColor.GRAY, new Object[]{EnumColor.DARK_BLUE, player.m_7755_()})
         );
      }
   }

   private static class NewYear extends HolidayManager.Holiday {
      private NewYear() {
         super(new HolidayManager.YearlyDate(Month.JANUARY, 1));
      }

      @Nullable
      @Override
      protected HolidayManager.HolidayMessage getMessage(Player player) {
         return new HolidayManager.HolidayMessage(
            HolidayManager.getThemedLines(13, EnumColor.WHITE, EnumColor.YELLOW),
            MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, new Object[]{EnumColor.DARK_BLUE, player.m_7755_()}),
            MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA, new Object[0]),
            MekanismLang.NEW_YEAR_LINE_THREE.translateColored(EnumColor.AQUA, new Object[]{LocalDate.now().getYear()}),
            MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY, new Object[0])
         );
      }
   }

   public record YearlyDate(Month month, @Range(from = 1L,to = 31L) int day) {
   }
}

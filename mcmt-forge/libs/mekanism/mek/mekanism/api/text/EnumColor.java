package mekanism.api.text;

import mekanism.api.IIncrementalEnum;
import mekanism.api.SupportsColorMap;
import mekanism.api.math.MathUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EnumColor implements IIncrementalEnum<EnumColor>, SupportsColorMap {
   BLACK("§0", APILang.COLOR_BLACK, "Black", "black", new int[]{64, 64, 64}, DyeColor.BLACK),
   DARK_BLUE("§1", APILang.COLOR_DARK_BLUE, "Blue", "blue", new int[]{54, 107, 208}, DyeColor.BLUE),
   DARK_GREEN("§2", APILang.COLOR_DARK_GREEN, "Green", "green", new int[]{89, 193, 95}, DyeColor.GREEN),
   DARK_AQUA("§3", APILang.COLOR_DARK_AQUA, "Cyan", "cyan", new int[]{0, 243, 208}, DyeColor.CYAN),
   DARK_RED("§4", APILang.COLOR_DARK_RED, "Dark Red", "dark_red", new int[]{201, 7, 31}, MapColor.f_283820_, null),
   PURPLE("§5", APILang.COLOR_PURPLE, "Purple", "purple", new int[]{164, 96, 217}, DyeColor.PURPLE),
   ORANGE("§6", APILang.COLOR_ORANGE, "Orange", "orange", new int[]{255, 161, 96}, DyeColor.ORANGE),
   GRAY("§7", APILang.COLOR_GRAY, "Light Gray", "light_gray", new int[]{207, 207, 207}, DyeColor.LIGHT_GRAY),
   DARK_GRAY("§8", APILang.COLOR_DARK_GRAY, "Gray", "gray", new int[]{122, 122, 122}, DyeColor.GRAY),
   INDIGO("§9", APILang.COLOR_INDIGO, "Light Blue", "light_blue", new int[]{85, 158, 255}, DyeColor.LIGHT_BLUE),
   BRIGHT_GREEN("§a", APILang.COLOR_BRIGHT_GREEN, "Lime", "lime", new int[]{117, 255, 137}, DyeColor.LIME),
   AQUA("§b", APILang.COLOR_AQUA, "Aqua", "aqua", new int[]{48, 255, 249}, MapColor.f_283869_, null),
   RED("§c", APILang.COLOR_RED, "Red", "red", new int[]{255, 56, 60}, DyeColor.RED),
   PINK("§d", APILang.COLOR_PINK, "Magenta", "magenta", new int[]{213, 94, 203}, DyeColor.MAGENTA),
   YELLOW("§e", APILang.COLOR_YELLOW, "Yellow", "yellow", new int[]{255, 221, 79}, DyeColor.YELLOW),
   WHITE("§f", APILang.COLOR_WHITE, "White", "white", new int[]{255, 255, 255}, DyeColor.WHITE),
   BROWN("§6", APILang.COLOR_BROWN, "Brown", "brown", new int[]{161, 118, 73}, DyeColor.BROWN),
   BRIGHT_PINK("§d", APILang.COLOR_BRIGHT_PINK, "Pink", "pink", new int[]{255, 188, 196}, DyeColor.PINK);

   private static final EnumColor[] COLORS = values();
   public final String code;
   private int[] rgbCode;
   private TextColor color;
   private final APILang langEntry;
   private final String englishName;
   private final String registryPrefix;
   @Nullable
   private final DyeColor dyeColor;
   private final MapColor mapColor;

   private EnumColor(String s, APILang langEntry, String englishName, String registryPrefix, int[] rgbCode, DyeColor dyeColor) {
      this(s, langEntry, englishName, registryPrefix, rgbCode, dyeColor.m_284406_(), dyeColor);
   }

   private EnumColor(String code, APILang langEntry, String englishName, String registryPrefix, int[] rgbCode, MapColor mapColor, @Nullable DyeColor dyeColor) {
      this.code = code;
      this.langEntry = langEntry;
      this.englishName = englishName;
      this.dyeColor = dyeColor;
      this.registryPrefix = registryPrefix;
      this.setColorFromAtlas(rgbCode);
      this.mapColor = mapColor;
   }

   public String getRegistryPrefix() {
      return this.registryPrefix;
   }

   public String getEnglishName() {
      return this.englishName;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   @Nullable
   public DyeColor getDyeColor() {
      return this.dyeColor;
   }

   public Component getColoredName() {
      return TextComponentUtil.build(this, this.getName());
   }

   public MutableComponent getName() {
      return this.langEntry.translate(new Object[0]);
   }

   public APILang getLangEntry() {
      return this.langEntry;
   }

   public TextColor getColor() {
      return this.color;
   }

   @Override
   public String toString() {
      return this.code;
   }

   public static EnumColor byIndexStatic(int index) {
      return MathUtils.getByIndexMod(COLORS, index);
   }

   @NotNull
   public EnumColor byIndex(int index) {
      return byIndexStatic(index);
   }

   @Override
   public void setColorFromAtlas(int[] color) {
      this.rgbCode = color;
      this.color = TextColor.m_131266_(this.rgbCode[0] << 16 | this.rgbCode[1] << 8 | this.rgbCode[2]);
   }

   @Override
   public int[] getRgbCode() {
      return this.rgbCode;
   }
}

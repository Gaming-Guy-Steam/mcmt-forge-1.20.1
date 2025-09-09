package mekanism.api.tier;

import java.util.Locale;
import mekanism.api.SupportsColorMap;
import mekanism.api.math.MathUtils;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public enum BaseTier implements StringRepresentable, SupportsColorMap {
   BASIC("Basic", new int[]{95, 255, 184}, MapColor.f_283916_),
   ADVANCED("Advanced", new int[]{255, 128, 106}, MapColor.f_283870_),
   ELITE("Elite", new int[]{75, 248, 255}, MapColor.f_283821_),
   ULTIMATE("Ultimate", new int[]{247, 135, 255}, MapColor.f_283931_),
   CREATIVE("Creative", new int[]{88, 88, 88}, MapColor.f_283846_);

   private static final BaseTier[] TIERS = values();
   private final String name;
   private final MapColor mapColor;
   private TextColor textColor;
   private int[] rgbCode;

   private BaseTier(String name, int[] rgbCode, MapColor mapColor) {
      this.name = name;
      this.mapColor = mapColor;
      this.setColorFromAtlas(rgbCode);
   }

   public String getSimpleName() {
      return this.name;
   }

   public String getLowerName() {
      return this.getSimpleName().toLowerCase(Locale.ROOT);
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   @Override
   public int[] getRgbCode() {
      return this.rgbCode;
   }

   @Override
   public void setColorFromAtlas(int[] color) {
      this.rgbCode = color;
      this.textColor = TextColor.m_131266_(this.rgbCode[0] << 16 | this.rgbCode[1] << 8 | this.rgbCode[2]);
   }

   public TextColor getColor() {
      return this.textColor;
   }

   @NotNull
   public String m_7912_() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public static BaseTier byIndexStatic(int index) {
      return MathUtils.getByIndexMod(TIERS, index);
   }
}

package mekanism.common.lib;

import java.util.Objects;
import mekanism.common.util.StatUtils;

public class Color {
   public static final Color WHITE = rgbad(1.0, 1.0, 1.0, 1.0);
   public static final Color BLACK = rgbad(0.0, 0.0, 0.0, 1.0);
   private final double r;
   private final double g;
   private final double b;
   private final double a;

   private Color(double r, double g, double b, double a) {
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
   }

   public int r() {
      return (int)Math.round(this.r * 255.0);
   }

   public int g() {
      return (int)Math.round(this.g * 255.0);
   }

   public int b() {
      return (int)Math.round(this.b * 255.0);
   }

   public int a() {
      return (int)Math.round(this.a * 255.0);
   }

   public float rf() {
      return (float)this.r;
   }

   public float gf() {
      return (float)this.g;
   }

   public float bf() {
      return (float)this.b;
   }

   public float af() {
      return (float)this.a;
   }

   public double rd() {
      return this.r;
   }

   public double gd() {
      return this.g;
   }

   public double bd() {
      return this.b;
   }

   public double ad() {
      return this.a;
   }

   public Color alpha(double alpha) {
      return new Color(this.r, this.g, this.b, alpha);
   }

   public int rgba() {
      return (this.r() & 0xFF) << 24 | (this.g() & 0xFF) << 16 | (this.b() & 0xFF) << 8 | this.a() & 0xFF;
   }

   public int argb() {
      return (this.a() & 0xFF) << 24 | this.rgb();
   }

   public int rgb() {
      return (this.r() & 0xFF) << 16 | (this.g() & 0xFF) << 8 | this.b() & 0xFF;
   }

   public int[] rgbaArray() {
      return new int[]{this.r(), this.g(), this.b(), this.a()};
   }

   public int[] argbArray() {
      return new int[]{this.a(), this.r(), this.g(), this.b()};
   }

   public int[] rgbArray() {
      return new int[]{this.r(), this.g(), this.b()};
   }

   public Color blend(Color to, double scale) {
      return rgbad(this.r + (to.r - this.r) * scale, this.g + (to.g - this.g) * scale, this.b + (to.b - this.b) * scale, this.a + (to.a - this.a) * scale);
   }

   public Color blendOnto(Color baseColor) {
      double sR = this.rd();
      double sG = this.gd();
      double sB = this.bd();
      double sA = this.ad();
      double dR = baseColor.rd();
      double dG = baseColor.gd();
      double dB = baseColor.bd();
      double dA = baseColor.ad();
      double rR = sR * sA + dR * (1.0 - sA);
      double rG = sG * sA + dG * (1.0 - sA);
      double rB = sB * sA + dB * (1.0 - sA);
      double rA = dA + sA * (1.0 - dA);
      return rgbad(rR, rG, rB, rA);
   }

   public Color darken(double amount) {
      double scale = 1.0 - amount;
      return rgbad(this.r * scale, this.g * scale, this.b * scale, this.a);
   }

   public static Color blend(Color src, Color dest) {
      return src.blendOnto(dest);
   }

   public static Color rgbai(int r, int g, int b, int a) {
      return new Color(r / 255.0, g / 255.0, b / 255.0, a / 255.0);
   }

   public static Color rgbad(double r, double g, double b, double a) {
      return new Color(r, g, b, a);
   }

   public static Color rgba(int color) {
      return rgbai(color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF);
   }

   public static Color rgba(int[] color) {
      return rgbai(color[0], color[1], color[2], color[3]);
   }

   public static Color argbi(int a, int r, int g, int b) {
      return rgbai(r, g, b, a);
   }

   public static Color argbd(double a, double r, double g, double b) {
      return rgbad(r, g, b, a);
   }

   public static Color argb(int color) {
      return argbi(color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF);
   }

   public static Color argb(int[] color) {
      return argbi(color[0], color[1], color[2], color[3]);
   }

   public static Color rgbi(int r, int g, int b) {
      return rgbai(r, g, b, 255);
   }

   public static Color rgbd(double r, double g, double b) {
      return rgbad(r, g, b, 1.0);
   }

   public static Color rgb(int color) {
      return rgbi(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF);
   }

   public static Color rgb(int[] color) {
      return rgbi(color[0], color[1], color[2]);
   }

   public static Color hsv(double h, double s, double v) {
      double hueIndex = h % 360.0 / 60.0;
      int i = (int)hueIndex;
      double diff = hueIndex - i;
      double p = v * (1.0 - s);
      double q = v * (1.0 - s * diff);
      double t = v * (1.0 - s * (1.0 - diff));

      return switch (i) {
         case 0 -> rgbd(v, t, p);
         case 1 -> rgbd(q, v, p);
         case 2 -> rgbd(p, v, t);
         case 3 -> rgbd(p, q, v);
         case 4 -> rgbd(t, p, v);
         default -> rgbd(v, p, q);
      };
   }

   public static int argbToFromABGR(int argb) {
      int red = argb >> 16 & 0xFF;
      int blue = argb & 0xFF;
      return argb & -16711936 | blue << 16 | red;
   }

   public double[] hsvArray() {
      double min = StatUtils.min(this.r, this.g, this.b);
      double max = StatUtils.max(this.r, this.g, this.b);
      double delta = max - min;
      double[] ret = new double[3];
      if (delta == 0.0) {
         ret[0] = 0.0;
      } else if (max == this.r) {
         ret[0] = (this.g - this.b) / delta % 6.0;
      } else if (max == this.g) {
         ret[0] = (this.b - this.r) / delta + 2.0;
      } else {
         ret[0] = (this.r - this.g) / delta + 4.0;
      }

      ret[0] *= 60.0;
      if (ret[0] < 0.0) {
         ret[0] += 360.0;
      }

      ret[1] = max == 0.0 ? 0.0 : delta / max;
      ret[2] = max;
      return ret;
   }

   public static int packOpaque(int rgb) {
      return rgb | 0xFF000000;
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this ? true : obj instanceof Color other && this.r == other.r && this.g == other.g && this.b == other.b && this.a == other.a;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.r, this.g, this.b, this.a);
   }

   @Override
   public String toString() {
      return "[Color: " + this.r + ", " + this.g + ", " + this.b + ", " + this.a + "]";
   }

   public interface ColorFunction {
      Color.ColorFunction HEAT = level -> Color.rgbai(
         (int)Math.min(200.0F, 400.0F * level), (int)Math.max(0.0F, 200.0F - Math.max(0.0F, -200.0F + 400.0F * level)), 0, 255
      );

      static Color.ColorFunction scale(Color from, Color to) {
         return level -> from.blend(to, level);
      }

      Color getColor(float level);
   }
}

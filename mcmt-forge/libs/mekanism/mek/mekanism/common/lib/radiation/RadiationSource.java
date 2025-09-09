package mekanism.common.lib.radiation;

import java.util.Objects;
import mekanism.api.Coord4D;
import mekanism.api.radiation.IRadiationSource;
import mekanism.common.config.MekanismConfig;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class RadiationSource implements IRadiationSource {
   private final Coord4D pos;
   private double magnitude;

   public RadiationSource(Coord4D pos, double magnitude) {
      this.pos = pos;
      this.magnitude = magnitude;
   }

   @NotNull
   @Override
   public Coord4D getPos() {
      return this.pos;
   }

   @Override
   public double getMagnitude() {
      return this.magnitude;
   }

   @Override
   public void radiate(double magnitude) {
      this.magnitude += magnitude;
   }

   @Override
   public boolean decay() {
      this.magnitude = this.magnitude * MekanismConfig.general.radiationSourceDecayRate.get();
      return this.magnitude < 1.0E-5;
   }

   public static RadiationSource load(CompoundTag tag) {
      return new RadiationSource(Coord4D.read(tag), tag.m_128459_("radiation"));
   }

   public void write(CompoundTag tag) {
      this.pos.write(tag);
      tag.m_128347_("radiation", this.magnitude);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RadiationSource other = (RadiationSource)o;
         return this.magnitude == other.magnitude && this.pos.equals(other.pos);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.pos, this.magnitude);
   }
}

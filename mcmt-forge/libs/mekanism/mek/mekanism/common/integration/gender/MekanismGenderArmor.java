package mekanism.common.integration.gender;

import com.wildfire.api.IGenderArmor;
import java.util.function.Consumer;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class MekanismGenderArmor extends ItemCapabilityWrapper.ItemCapability implements IGenderArmor {
   private static final Capability<IGenderArmor> GENDER_ARMOR_CAPABILITY = CapabilityManager.get(new CapabilityToken<IGenderArmor>() {});
   public static final MekanismGenderArmor OPEN_FRONT = new MekanismGenderArmor(false, false, 0.0F, 0.0F);
   public static final MekanismGenderArmor HIDES_BREASTS = new MekanismGenderArmor(true, true, 0.0F, 0.0F);
   static final MekanismGenderArmor HAZMAT = new MekanismGenderArmor(0.5F, 0.25F);
   private final boolean coversBreasts;
   private final boolean alwaysHidesBreasts;
   private final float physicsResistance;
   private final float tightness;

   public MekanismGenderArmor(float physicsResistance) {
      this(physicsResistance, 0.0F);
   }

   public MekanismGenderArmor(float physicsResistance, float tightness) {
      this(true, false, physicsResistance, tightness);
   }

   private MekanismGenderArmor(boolean coversBreasts, boolean alwaysHidesBreasts, float physicsResistance, float tightness) {
      if (physicsResistance < 0.0F || physicsResistance > 1.0F) {
         throw new IllegalArgumentException("Physics resistance must be between zero and one inclusive.");
      } else if (!(tightness < 0.0F) && !(tightness > 1.0F)) {
         this.coversBreasts = coversBreasts;
         this.alwaysHidesBreasts = alwaysHidesBreasts;
         this.physicsResistance = physicsResistance;
         this.tightness = tightness;
      } else {
         throw new IllegalArgumentException("Armor tightness must be between zero and one inclusive.");
      }
   }

   public boolean coversBreasts() {
      return this.coversBreasts;
   }

   public boolean alwaysHidesBreasts() {
      return this.alwaysHidesBreasts;
   }

   public float physicsResistance() {
      return this.physicsResistance;
   }

   public float tightness() {
      return this.tightness;
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(GENDER_ARMOR_CAPABILITY, this));
   }
}

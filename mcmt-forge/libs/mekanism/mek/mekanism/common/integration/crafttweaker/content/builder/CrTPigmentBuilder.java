package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;

@ZenRegister(
   loaders = {"mekanismcontent"}
)
@Name("mods.mekanism.content.builder.PigmentBuilder")
public class CrTPigmentBuilder extends CrTChemicalBuilder<Pigment, PigmentBuilder, CrTPigmentBuilder> {
   @Method
   public static CrTPigmentBuilder builder(@Optional ResourceLocation textureLocation) {
      return new CrTPigmentBuilder(textureLocation == null ? PigmentBuilder.builder() : PigmentBuilder.builder(textureLocation));
   }

   protected CrTPigmentBuilder(PigmentBuilder builder) {
      super(builder);
   }

   @Override
   protected void build(ResourceLocation registryName) {
      Pigment pigment = ChemicalUtil.pigment(this.getInternal(), this.colorRepresentation);
      CrTContentUtils.queuePigmentForRegistration(registryName, pigment);
   }
}

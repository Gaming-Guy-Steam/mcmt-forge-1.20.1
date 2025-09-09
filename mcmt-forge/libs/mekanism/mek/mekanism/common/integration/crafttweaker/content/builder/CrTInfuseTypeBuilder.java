package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;

@ZenRegister(
   loaders = {"mekanismcontent"}
)
@Name("mods.mekanism.content.builder.InfuseTypeBuilder")
public class CrTInfuseTypeBuilder extends CrTChemicalBuilder<InfuseType, InfuseTypeBuilder, CrTInfuseTypeBuilder> {
   @Method
   public static CrTInfuseTypeBuilder builder(@Optional ResourceLocation textureLocation) {
      return new CrTInfuseTypeBuilder(textureLocation == null ? InfuseTypeBuilder.builder() : InfuseTypeBuilder.builder(textureLocation));
   }

   protected CrTInfuseTypeBuilder(InfuseTypeBuilder builder) {
      super(builder);
   }

   @Override
   protected void build(ResourceLocation registryName) {
      InfuseType infuseType = ChemicalUtil.infuseType(this.getInternal(), this.colorRepresentation);
      CrTContentUtils.queueInfuseTypeForRegistration(registryName, infuseType);
   }
}

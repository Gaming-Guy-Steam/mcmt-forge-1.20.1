package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;

@ZenRegister(
   loaders = {"mekanismcontent"}
)
@Name("mods.mekanism.content.builder.GasBuilder")
public class CrTGasBuilder extends CrTChemicalBuilder<Gas, GasBuilder, CrTGasBuilder> {
   @Method
   public static CrTGasBuilder builder(@Optional ResourceLocation textureLocation) {
      return new CrTGasBuilder(textureLocation == null ? GasBuilder.builder() : GasBuilder.builder(textureLocation));
   }

   protected CrTGasBuilder(GasBuilder builder) {
      super(builder);
   }

   @Override
   protected void build(ResourceLocation registryName) {
      Gas gas = ChemicalUtil.gas(this.getInternal(), this.colorRepresentation);
      CrTContentUtils.queueGasForRegistration(registryName, gas);
   }
}

package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister(
   loaders = {"mekanismcontent"}
)
@Name("mods.mekanism.content.builder.ChemicalBuilder")
public abstract class CrTChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>, CRT_BUILDER extends CrTChemicalBuilder<CHEMICAL, BUILDER, CRT_BUILDER>> {
   private final BUILDER builder;
   @Nullable
   protected Integer colorRepresentation;

   protected CrTChemicalBuilder(BUILDER builder) {
      this.builder = builder;
   }

   @Method
   public CRT_BUILDER with(ChemicalAttribute attribute) {
      this.getInternal().with(attribute);
      return this.self();
   }

   @Method
   public CRT_BUILDER tint(int tint) {
      this.getInternal().tint(tint);
      return this.self();
   }

   @Method
   public CRT_BUILDER colorRepresentation(int color) {
      this.colorRepresentation = color;
      return this.self();
   }

   @Method
   public CRT_BUILDER hidden() {
      this.getInternal().hidden();
      return this.self();
   }

   @Method
   public void build(String name) {
      this.build(CrTUtils.rl(name));
   }

   protected abstract void build(ResourceLocation registryName);

   protected BUILDER getInternal() {
      return this.builder;
   }

   protected CRT_BUILDER self() {
      return (CRT_BUILDER)this;
   }
}

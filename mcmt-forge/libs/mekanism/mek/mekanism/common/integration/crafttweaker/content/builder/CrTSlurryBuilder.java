package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister(
   loaders = {"mekanismcontent"}
)
@Name("mods.mekanism.content.builder.SlurryBuilder")
public class CrTSlurryBuilder extends CrTChemicalBuilder<Slurry, SlurryBuilder, CrTSlurryBuilder> {
   @Method
   public static CrTSlurryBuilder clean() {
      return new CrTSlurryBuilder(SlurryBuilder.clean());
   }

   @Method
   public static CrTSlurryBuilder dirty() {
      return new CrTSlurryBuilder(SlurryBuilder.dirty());
   }

   @Method
   public static CrTSlurryBuilder builder(ResourceLocation textureLocation) {
      return new CrTSlurryBuilder(SlurryBuilder.builder(textureLocation));
   }

   protected CrTSlurryBuilder(SlurryBuilder builder) {
      super(builder);
   }

   @Method
   public CrTSlurryBuilder ore(ResourceLocation oreTagLocation) {
      this.getInternal().ore(oreTagLocation);
      return this;
   }

   @Method
   public CrTSlurryBuilder ore(KnownTag<Item> oreTag) {
      this.getInternal().ore(oreTag.getTagKey());
      return this;
   }

   @Override
   protected void build(ResourceLocation registryName) {
      Slurry slurry = ChemicalUtil.slurry(this.getInternal(), this.colorRepresentation);
      CrTContentUtils.queueSlurryForRegistration(registryName, slurry);
   }
}

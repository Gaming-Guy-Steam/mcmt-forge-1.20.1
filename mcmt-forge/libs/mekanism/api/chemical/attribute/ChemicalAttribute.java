package mekanism.api.chemical.attribute;

import java.util.List;
import net.minecraft.network.chat.Component;

public abstract class ChemicalAttribute {
   public boolean needsValidation() {
      return false;
   }

   public List<Component> addTooltipText(List<Component> list) {
      return list;
   }
}

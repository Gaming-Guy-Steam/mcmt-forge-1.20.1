package mekanism.common.integration.lookingat.wthit;

import java.util.ArrayList;
import java.util.List;
import mcp.mobius.waila.api.IData;
import mcp.mobius.waila.api.IData.Serializer;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

public class WTHITLookingAtHelper implements LookingAtHelper, IData {
   public static final Serializer<WTHITLookingAtHelper> SERIALIZER = buffer -> {
      WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
      int count = buffer.m_130242_();

      for (int i = 0; i < count; i++) {
         WTHITLookingAtHelper.LookingAtTypes type = (WTHITLookingAtHelper.LookingAtTypes)buffer.m_130066_(WTHITLookingAtHelper.LookingAtTypes.class);

         Object element = switch (type) {
            case ENERGY -> new EnergyElement(FloatingLong.readFromBuffer(buffer), FloatingLong.readFromBuffer(buffer));
            case FLUID -> new FluidElement(buffer.readFluidStack(), buffer.m_130242_());
            case GAS -> new ChemicalElement(ChemicalUtils.readGasStack(buffer), buffer.m_130258_());
            case INFUSION -> new ChemicalElement(ChemicalUtils.readInfusionStack(buffer), buffer.m_130258_());
            case PIGMENT -> new ChemicalElement(ChemicalUtils.readPigmentStack(buffer), buffer.m_130258_());
            case SLURRY -> new ChemicalElement(ChemicalUtils.readSlurryStack(buffer), buffer.m_130258_());
            case COMPONENT -> buffer.m_130238_();
            case UNKNOWN -> null;
         };
         if (element != null) {
            helper.elements.add(element);
         }
      }

      return helper;
   };
   final List<Object> elements = new ArrayList<>();

   public void write(FriendlyByteBuf buffer) {
      buffer.m_236828_(this.elements, (buf, object) -> {
         WTHITLookingAtHelper.LookingAtTypes type = WTHITLookingAtHelper.LookingAtTypes.getType(object);
         buf.m_130068_(type);
         switch (type) {
            case ENERGY:
               EnergyElement energyElement = (EnergyElement)object;
               energyElement.getEnergy().writeToBuffer(buf);
               energyElement.getMaxEnergy().writeToBuffer(buf);
               break;
            case FLUID:
               FluidElement fluidElement = (FluidElement)object;
               buf.writeFluidStack(fluidElement.getStored());
               buf.m_130130_(fluidElement.getCapacity());
               break;
            case GAS:
            case INFUSION:
            case PIGMENT:
            case SLURRY:
               ChemicalElement chemicalElement = (ChemicalElement)object;
               ChemicalUtils.writeChemicalStack(buf, chemicalElement.getStored());
               buf.m_130103_(chemicalElement.getCapacity());
               break;
            case COMPONENT:
               buf.m_130083_((Component)object);
         }
      });
   }

   @Override
   public void addText(Component text) {
      this.elements.add(text);
   }

   @Override
   public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
      this.elements.add(new EnergyElement(energy, maxEnergy));
   }

   @Override
   public void addFluidElement(FluidStack stored, int capacity) {
      this.elements.add(new FluidElement(stored, capacity));
   }

   @Override
   public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
      this.elements.add(new ChemicalElement(stored, capacity));
   }

   private static enum LookingAtTypes {
      UNKNOWN,
      ENERGY,
      FLUID,
      GAS,
      INFUSION,
      PIGMENT,
      SLURRY,
      COMPONENT;

      public static WTHITLookingAtHelper.LookingAtTypes getType(Object element) {
         if (element instanceof Component) {
            return COMPONENT;
         } else if (element instanceof EnergyElement) {
            return ENERGY;
         } else if (element instanceof FluidElement) {
            return FLUID;
         } else if (element instanceof ChemicalElement chemicalElement) {
            return switch (chemicalElement.getChemicalType()) {
               case GAS -> GAS;
               case INFUSION -> INFUSION;
               case PIGMENT -> PIGMENT;
               case SLURRY -> SLURRY;
            };
         } else {
            return UNKNOWN;
         }
      }
   }
}

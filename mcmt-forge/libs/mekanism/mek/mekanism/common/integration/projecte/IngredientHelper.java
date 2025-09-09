package mekanism.common.integration.projecte;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class IngredientHelper {
   private final IMappingCollector<NormalizedSimpleStack, Long> mapper;
   private Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
   private boolean isValid = true;

   public IngredientHelper(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
      this.mapper = mapper;
   }

   public void resetHelper() {
      this.isValid = true;
      this.ingredientMap = new HashMap<>();
   }

   public void put(NormalizedSimpleStack stack, int amount) {
      if (this.isValid) {
         if (this.ingredientMap.containsKey(stack)) {
            long newAmount = (long)this.ingredientMap.get(stack).intValue() + amount;
            if (newAmount <= 2147483647L && newAmount >= -2147483648L) {
               this.ingredientMap.put(stack, (int)newAmount);
            } else {
               this.isValid = false;
            }
         } else {
            this.ingredientMap.put(stack, amount);
         }
      }
   }

   public void put(NormalizedSimpleStack stack, long amount) {
      if (amount <= 2147483647L && amount >= -2147483648L) {
         this.put(stack, (int)amount);
      } else {
         this.isValid = false;
      }
   }

   public void put(ChemicalStack<?> chemicalStack) {
      if (chemicalStack instanceof GasStack stack) {
         this.put(stack);
      } else if (chemicalStack instanceof InfusionStack stack) {
         this.put(stack);
      } else if (chemicalStack instanceof PigmentStack stack) {
         this.put(stack);
      } else if (chemicalStack instanceof SlurryStack stack) {
         this.put(stack);
      }
   }

   public void put(GasStack stack) {
      this.put(NSSGas.createGas(stack), stack.getAmount());
   }

   public void put(InfusionStack stack) {
      this.put(NSSInfuseType.createInfuseType(stack), stack.getAmount());
   }

   public void put(PigmentStack stack) {
      this.put(NSSPigment.createPigment(stack), stack.getAmount());
   }

   public void put(SlurryStack stack) {
      this.put(NSSSlurry.createSlurry(stack), stack.getAmount());
   }

   public void put(FluidStack stack) {
      this.put(NSSFluid.createFluid(stack), stack.getAmount());
   }

   public void put(ItemStack stack) {
      this.put(NSSItem.createItem(stack), stack.m_41613_());
   }

   public boolean addAsConversion(NormalizedSimpleStack output, int outputAmount) {
      if (this.isValid) {
         this.mapper.addConversion(outputAmount, output, this.ingredientMap);
         return true;
      } else {
         return false;
      }
   }

   public boolean addAsConversion(NormalizedSimpleStack output, long outputAmount) {
      return outputAmount > 2147483647L ? false : this.addAsConversion(output, (int)outputAmount);
   }

   public boolean addAsConversion(ChemicalStack<?> chemicalStack) {
      if (chemicalStack instanceof GasStack stack) {
         return this.addAsConversion(stack);
      } else if (chemicalStack instanceof InfusionStack stack) {
         return this.addAsConversion(stack);
      } else if (chemicalStack instanceof PigmentStack stack) {
         return this.addAsConversion(stack);
      } else {
         return chemicalStack instanceof SlurryStack stack ? this.addAsConversion(stack) : false;
      }
   }

   public boolean addAsConversion(GasStack stack) {
      return this.addAsConversion(NSSGas.createGas(stack), stack.getAmount());
   }

   public boolean addAsConversion(InfusionStack stack) {
      return this.addAsConversion(NSSInfuseType.createInfuseType(stack), stack.getAmount());
   }

   public boolean addAsConversion(PigmentStack stack) {
      return this.addAsConversion(NSSPigment.createPigment(stack), stack.getAmount());
   }

   public boolean addAsConversion(SlurryStack stack) {
      return this.addAsConversion(NSSSlurry.createSlurry(stack), stack.getAmount());
   }

   public boolean addAsConversion(FluidStack stack) {
      return this.addAsConversion(NSSFluid.createFluid(stack), stack.getAmount());
   }

   public boolean addAsConversion(ItemStack stack) {
      return this.addAsConversion(NSSItem.createItem(stack), stack.m_41613_());
   }
}

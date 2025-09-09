package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiModIDFilter<FILTER extends IModIDFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
   extends GuiTextFilter<FILTER, TILE> {
   protected GuiModIDFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, @Nullable FILTER origFilter) {
      super(gui, x, y, width, height, MekanismLang.MODID_FILTER.translate(new Object[0]), tile, origFilter);
   }

   @Override
   protected CharPredicate getInputValidator() {
      return InputValidator.RL_NAMESPACE.or(InputValidator.WILDCARD_CHARS);
   }

   @Override
   protected List<Component> getScreenText() {
      List<Component> list = super.getScreenText();
      list.add(MekanismLang.MODID_FILTER_ID.translate(new Object[]{this.filter.getModID()}));
      return list;
   }

   @Override
   protected ILangEntry getNoFilterSaveError() {
      return MekanismLang.MODID_FILTER_NO_ID;
   }

   @Override
   protected boolean setText() {
      return this.setFilterName(this.text.getText(), false);
   }

   @NotNull
   @Override
   protected List<ItemStack> getRenderStacks() {
      return this.filter.hasFilter() ? TagCache.getItemModIDStacks(this.filter.getModID()) : Collections.emptyList();
   }

   @Nullable
   @Override
   protected IJEIGhostTarget.IGhostIngredientConsumer getGhostHandler() {
      return new IJEIGhostTarget.IGhostIngredientConsumer() {
         @Override
         public boolean supportsIngredient(Object ingredient) {
            if (ingredient instanceof ItemStack stack) {
               return !stack.m_41619_();
            } else if (ingredient instanceof FluidStack stack) {
               return !stack.isEmpty();
            } else {
               return ingredient instanceof ChemicalStack<?> stack ? !stack.isEmpty() : RegistryUtils.getNameGeneric(ingredient) != null;
            }
         }

         @Override
         public void accept(Object ingredient) {
            if (ingredient instanceof ItemStack stack) {
               GuiModIDFilter.this.setFilterName(stack, true);
            } else if (ingredient instanceof FluidStack stack) {
               GuiModIDFilter.this.setFilterName(RegistryUtils.getName(stack.getFluid()));
            } else if (ingredient instanceof ChemicalStack<?> stack) {
               GuiModIDFilter.this.setFilterName(stack.getTypeRegistryName());
            } else {
               ResourceLocation registryName = RegistryUtils.getNameGeneric(ingredient);
               if (registryName != null) {
                  GuiModIDFilter.this.setFilterName(registryName);
               }
            }
         }
      };
   }

   @Nullable
   @Override
   protected GuiElement.IClickable getSlotClickHandler() {
      return (element, mouseX, mouseY) -> {
         if (!Screen.m_96638_()) {
            ItemStack stack = this.gui().getCarriedItem();
            if (!stack.m_41619_()) {
               this.setFilterName(stack.m_255036_(1), false);
               return true;
            }
         }

         return false;
      };
   }

   private void setFilterName(ItemStack stack, boolean click) {
      this.setFilterName(MekanismUtils.getModId(stack), click);
   }

   private void setFilterName(ResourceLocation registryName) {
      this.setFilterName(registryName.m_135827_(), true);
   }

   private boolean setFilterName(String name, boolean click) {
      boolean success = false;
      if (name.isEmpty()) {
         this.filterSaveFailed(this.getNoFilterSaveError(), new Object[0]);
      } else if (name.equals(this.filter.getModID())) {
         this.filterSaveFailed(MekanismLang.MODID_FILTER_SAME_ID, new Object[0]);
      } else if (!this.hasMatchingTargets(name)) {
         this.filterSaveFailed(MekanismLang.TEXT_FILTER_NO_MATCHES, new Object[0]);
      } else {
         this.filter.setModID(name);
         this.slotDisplay.updateStackList();
         this.text.setText("");
         this.filterSaveSuccess();
         success = true;
      }

      if (click) {
         playClickSound(SoundEvents.f_12490_);
      }

      return success;
   }

   protected boolean hasMatchingTargets(String name) {
      return !TagCache.getItemModIDStacks(name).isEmpty();
   }
}

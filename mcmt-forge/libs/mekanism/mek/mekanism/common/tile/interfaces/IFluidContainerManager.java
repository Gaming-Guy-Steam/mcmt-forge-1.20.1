package mekanism.common.tile.interfaces;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;

public interface IFluidContainerManager extends IHasMode {
   IFluidContainerManager.ContainerEditMode getContainerEditMode();

   @NothingNullByDefault
   public static enum ContainerEditMode implements IIncrementalEnum<IFluidContainerManager.ContainerEditMode>, IHasTextComponent {
      BOTH(MekanismLang.FLUID_CONTAINER_BOTH),
      FILL(MekanismLang.FLUID_CONTAINER_FILL),
      EMPTY(MekanismLang.FLUID_CONTAINER_EMPTY);

      private static final IFluidContainerManager.ContainerEditMode[] MODES = values();
      private final ILangEntry langEntry;

      private ContainerEditMode(ILangEntry langEntry) {
         this.langEntry = langEntry;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translate();
      }

      public IFluidContainerManager.ContainerEditMode byIndex(int index) {
         return byIndexStatic(index);
      }

      public static IFluidContainerManager.ContainerEditMode byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}

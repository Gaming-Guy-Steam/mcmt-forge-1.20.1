package mekanism.common.block.attribute;

import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Nullable;

public class AttributeGui implements Attribute {
   private final Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar;
   @Nullable
   private final ILangEntry customName;

   public AttributeGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar, @Nullable ILangEntry customName) {
      this.containerRegistrar = containerRegistrar;
      this.customName = customName;
   }

   public <TILE extends TileEntityMekanism> MenuProvider getProvider(TILE tile) {
      return this.containerRegistrar.get().getProvider((Component)(this.customName == null ? tile.m_5446_() : this.customName.translate()), tile);
   }

   public boolean hasCustomName() {
      return this.customName != null;
   }
}

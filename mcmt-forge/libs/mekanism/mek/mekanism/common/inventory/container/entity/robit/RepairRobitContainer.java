package mekanism.common.inventory.container.entity.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairRobitContainer extends AnvilMenu implements IEntityContainer<EntityRobit>, ISecurityContainer {
   private final EntityRobit entity;

   public RepairRobitContainer(int id, Inventory inv, EntityRobit robit) {
      super(id, inv, robit.getWorldPosCallable());
      this.entity = robit;
      this.entity.open(inv.f_35978_);
   }

   public boolean m_6875_(@NotNull Player player) {
      return this.entity.m_6084_();
   }

   @NotNull
   public EntityRobit getEntity() {
      return this.entity;
   }

   @NotNull
   public MenuType<?> m_6772_() {
      return (MenuType<?>)MekanismContainerTypes.REPAIR_ROBIT.get();
   }

   public void m_6877_(@NotNull Player player) {
      super.m_6877_(player);
      this.entity.close(player);
   }

   @Nullable
   @Override
   public ICapabilityProvider getSecurityObject() {
      return this.entity;
   }
}

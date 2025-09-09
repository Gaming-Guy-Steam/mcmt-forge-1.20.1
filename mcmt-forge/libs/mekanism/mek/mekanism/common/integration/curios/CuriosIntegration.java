package mekanism.common.integration.curios;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.client.render.MekanismCurioRenderer;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.RegistryUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CuriosIntegration {
   public static void addListeners(IEventBus bus) {
      bus.addListener(event -> registerRenderers(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
   }

   private static void registerRenderers(ItemLike... items) {
      for (ItemLike item : items) {
         if (item.m_5456_() instanceof ArmorItem armor && IClientItemExtensions.of(armor) instanceof ISpecialGear gear) {
            CuriosRendererRegistry.register(armor, () -> new MekanismCurioRenderer(gear.getGearModel(armor.m_266204_())));
         } else {
            Mekanism.logger.warn("Attempted to register Curios renderer for non-special gear item: {}.", RegistryUtils.getName(item.m_5456_()));
         }
      }
   }

   public static Optional<? extends IItemHandler> getCuriosInventory(LivingEntity entity) {
      return CuriosApi.getCuriosHelper().getEquippedCurios(entity).resolve();
   }

   public static Optional<SlotResult> findFirstCurioAsResult(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
      return CuriosApi.getCuriosHelper().findFirstCurio(entity, filter);
   }

   public static ItemStack findFirstCurio(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
      return findFirstCurioAsResult(entity, filter).<ItemStack>map(SlotResult::stack).orElse(ItemStack.f_41583_);
   }

   public static ItemStack getCurioStack(@NotNull LivingEntity entity, String slotType, int slot) {
      return CuriosApi.getCuriosHelper()
         .getCuriosHandler(entity)
         .resolve()
         .flatMap(handler -> handler.getStacksHandler(slotType))
         .map(handler -> handler.getStacks().getStackInSlot(slot))
         .orElse(ItemStack.f_41583_);
   }
}

package mekanism.api.recipes;

import java.util.Objects;
import mekanism.api.inventory.IgnoredIInventory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismRecipe implements Recipe<IgnoredIInventory> {
   private final ResourceLocation id;

   protected MekanismRecipe(ResourceLocation id) {
      this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
   }

   public abstract void write(FriendlyByteBuf var1);

   @NotNull
   public ResourceLocation m_6423_() {
      return this.id;
   }

   public boolean matches(@NotNull IgnoredIInventory inv, @NotNull Level world) {
      return !this.m_142505_();
   }

   public boolean m_5598_() {
      return true;
   }

   public abstract boolean m_142505_();

   public void logMissingTags() {
   }

   @NotNull
   public ItemStack assemble(@NotNull IgnoredIInventory inv, @NotNull RegistryAccess registryAccess) {
      return ItemStack.f_41583_;
   }

   public boolean m_8004_(int width, int height) {
      return true;
   }

   @NotNull
   public ItemStack m_8043_(@NotNull RegistryAccess registryAccess) {
      return ItemStack.f_41583_;
   }
}

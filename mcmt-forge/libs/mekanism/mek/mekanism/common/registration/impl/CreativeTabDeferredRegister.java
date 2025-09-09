package mekanism.common.registration.impl;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.common.block.BlockBounding;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class CreativeTabDeferredRegister extends WrappedDeferredRegister<CreativeModeTab> {
   private final Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs;
   private final String modid;

   public CreativeTabDeferredRegister(String modid) {
      this(modid, event -> {});
   }

   public CreativeTabDeferredRegister(String modid, Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs) {
      super(modid, Registries.f_279569_);
      this.modid = modid;
      this.addToExistingTabs = addToExistingTabs;
   }

   @Override
   public void register(IEventBus bus) {
      super.register(bus);
      bus.addListener(this.addToExistingTabs);
   }

   public CreativeTabRegistryObject registerMain(ILangEntry title, IItemProvider icon, UnaryOperator<Builder> operator) {
      return this.register(this.modid, title, icon, operator);
   }

   public CreativeTabRegistryObject register(String name, ILangEntry title, IItemProvider icon, UnaryOperator<Builder> operator) {
      return this.register(
         name,
         () -> {
            Builder builder = CreativeModeTab.builder()
               .m_257941_(title.translate())
               .m_257737_(icon::getItemStack)
               .withTabFactory(CreativeTabDeferredRegister.MekanismCreativeTab::new);
            return operator.apply(builder).m_257652_();
         },
         CreativeTabRegistryObject::new
      );
   }

   public static void addToDisplay(Output output, ItemLike... items) {
      for (ItemLike item : items) {
         addToDisplay(output, item);
      }
   }

   public static void addToDisplay(Output output, ItemLike itemLike) {
      if (itemLike.m_5456_() instanceof CreativeTabDeferredRegister.ICustomCreativeTabContents contents) {
         if (contents.addDefault()) {
            output.m_246326_(itemLike);
         }

         contents.addItems(output);
      } else {
         output.m_246326_(itemLike);
      }
   }

   public static void addToDisplay(ItemDeferredRegister register, Output output) {
      for (IItemProvider itemProvider : register.getAllItems()) {
         addToDisplay(output, itemProvider);
      }
   }

   public static void addToDisplay(BlockDeferredRegister register, Output output) {
      for (IBlockProvider itemProvider : register.getAllBlocks()) {
         if (!(itemProvider.getBlock() instanceof BlockBounding)) {
            addToDisplay(output, itemProvider);
         }
      }
   }

   public static void addToDisplay(FluidDeferredRegister register, Output output) {
      for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : register.getAllFluids()) {
         addToDisplay(output, fluidRO.getBucket());
      }
   }

   public interface ICustomCreativeTabContents {
      void addItems(Output tabOutput);

      default boolean addDefault() {
         return true;
      }
   }

   public static class MekanismCreativeTab extends CreativeModeTab {
      protected MekanismCreativeTab(Builder builder) {
         super(builder);
      }

      public int getLabelColor() {
         return SpecialColors.TEXT_TITLE.argb();
      }
   }
}

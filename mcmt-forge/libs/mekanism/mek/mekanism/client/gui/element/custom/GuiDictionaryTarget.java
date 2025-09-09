package mekanism.client.gui.element.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.item.GuiDictionary;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiDictionaryTarget extends GuiElement implements IJEIGhostTarget {
   private final Map<GuiDictionary.DictionaryTagType, List<String>> tags = new EnumMap<>(GuiDictionary.DictionaryTagType.class);
   private final Consumer<Set<GuiDictionary.DictionaryTagType>> tagSetter;
   @Nullable
   private Object target;

   public GuiDictionaryTarget(IGuiWrapper gui, int x, int y, Consumer<Set<GuiDictionary.DictionaryTagType>> tagSetter) {
      super(gui, x, y, 16, 16);
      this.tagSetter = tagSetter;
   }

   public boolean hasTarget() {
      return this.target != null;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.target instanceof ItemStack stack) {
         this.gui().renderItem(guiGraphics, stack, this.relativeX, this.relativeY);
      } else if (this.target instanceof FluidStack stack) {
         MekanismRenderer.color(guiGraphics, stack);
         this.drawTiledSprite(
            guiGraphics,
            this.relativeX,
            this.relativeY,
            this.f_93619_,
            this.f_93618_,
            this.f_93619_,
            MekanismRenderer.getFluidTexture(stack, MekanismRenderer.FluidTextureType.STILL),
            GuiUtils.TilingDirection.DOWN_RIGHT
         );
         MekanismRenderer.resetColor(guiGraphics);
      } else if (this.target instanceof ChemicalStack<?> stack) {
         MekanismRenderer.color(guiGraphics, stack);
         this.drawTiledSprite(
            guiGraphics,
            this.relativeX,
            this.relativeY,
            this.f_93619_,
            this.f_93618_,
            this.f_93619_,
            MekanismRenderer.getChemicalTexture(stack.getType()),
            GuiUtils.TilingDirection.DOWN_RIGHT
         );
         MekanismRenderer.resetColor(guiGraphics);
      }
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (this.target instanceof ItemStack stack) {
         this.gui().renderItemTooltip(guiGraphics, stack, mouseX, mouseY);
      } else if (this.target != null) {
         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{TextComponentUtil.build(this.target)});
      }
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      if (Screen.m_96638_()) {
         if (this.target != null) {
            this.setTargetSlot(null);
         }
      } else {
         ItemStack stack = this.gui().getCarriedItem();
         if (!stack.m_41619_()) {
            this.setTargetSlot(stack);
         }
      }
   }

   public List<String> getTags(GuiDictionary.DictionaryTagType type) {
      return this.tags.getOrDefault(type, Collections.emptyList());
   }

   public void setTargetSlot(@Nullable Object newTarget) {
      this.tags.clear();
      if (newTarget == null) {
         this.target = null;
      } else if (newTarget instanceof ItemStack itemStack) {
         if (itemStack.m_41619_()) {
            this.target = null;
         } else {
            ItemStack stack = itemStack.m_255036_(1);
            this.target = stack;
            Item item = stack.m_41720_();
            this.tags.put(GuiDictionary.DictionaryTagType.ITEM, TagCache.getItemTags(stack));
            if (item instanceof BlockItem blockItem) {
               Block block = blockItem.m_40614_();
               this.tags.put(GuiDictionary.DictionaryTagType.BLOCK, TagCache.getTagsAsStrings(TagUtils.tagsStream(ForgeRegistries.BLOCKS, block)));
               if (block instanceof IHasTileEntity || block.m_49966_().m_155947_()) {
                  this.tags.put(GuiDictionary.DictionaryTagType.BLOCK_ENTITY_TYPE, TagCache.getTileEntityTypeTags(block));
               }
            }

            if (item instanceof SpawnEggItem spawnEggItem) {
               this.tags.put(GuiDictionary.DictionaryTagType.ENTITY_TYPE, TagCache.getTagsAsStrings(spawnEggItem.m_43228_(stack.m_41783_()).getTags()));
            }

            Map<Enchantment, Integer> enchantments = EnchantmentHelper.m_44831_(stack);
            if (!enchantments.isEmpty()) {
               ITagManager<Enchantment> manager = TagUtils.manager(ForgeRegistries.ENCHANTMENTS);
               this.tags
                  .put(
                     GuiDictionary.DictionaryTagType.ENCHANTMENT,
                     TagCache.getTagsAsStrings(enchantments.keySet().stream().flatMap(enchantment -> TagUtils.tagsStream(manager, enchantment)).distinct())
                  );
            }

            Potion potion = PotionUtils.m_43579_(itemStack);
            if (potion != Potions.f_43598_) {
               this.tags.put(GuiDictionary.DictionaryTagType.POTION, TagCache.getTagsAsStrings(TagUtils.tagsStream(ForgeRegistries.POTIONS, potion)));
               ITagManager<MobEffect> effectManager = TagUtils.manager(ForgeRegistries.MOB_EFFECTS);
               this.tags
                  .put(
                     GuiDictionary.DictionaryTagType.MOB_EFFECT,
                     TagCache.getTagsAsStrings(potion.m_43488_().stream().flatMap(effect -> TagUtils.tagsStream(effectManager, effect.m_19544_())).distinct())
                  );
            }

            Set<Attribute> attributes = Arrays.stream(EnumUtils.EQUIPMENT_SLOT_TYPES)
               .flatMap(slot -> itemStack.m_41638_(slot).keySet().stream())
               .collect(Collectors.toSet());
            if (!attributes.isEmpty()) {
               ITagManager<Attribute> attributeManager = TagUtils.manager(ForgeRegistries.ATTRIBUTES);
               this.tags
                  .put(
                     GuiDictionary.DictionaryTagType.ATTRIBUTE,
                     TagCache.getTagsAsStrings(attributes.stream().flatMap(attribute -> TagUtils.tagsStream(attributeManager, attribute)).distinct())
                  );
            }

            FluidUtil.getFluidHandler(stack)
               .ifPresent(
                  fluidHandler -> {
                     ITagManager<Fluid> fluidManager = TagUtils.manager(ForgeRegistries.FLUIDS);
                     this.tags
                        .put(
                           GuiDictionary.DictionaryTagType.FLUID,
                           TagCache.getTagsAsStrings(
                              IntStream.range(0, fluidHandler.getTanks())
                                 .<FluidStack>mapToObj(fluidHandler::getFluidInTank)
                                 .filter(fluidInTank -> !fluidInTank.isEmpty())
                                 .flatMap(fluidInTank -> TagUtils.tagsStream(fluidManager, fluidInTank.getFluid()))
                                 .distinct()
                           )
                        );
                  }
               );
            this.addChemicalTags(GuiDictionary.DictionaryTagType.GAS, stack, Capabilities.GAS_HANDLER);
            this.addChemicalTags(GuiDictionary.DictionaryTagType.INFUSE_TYPE, stack, Capabilities.INFUSION_HANDLER);
            this.addChemicalTags(GuiDictionary.DictionaryTagType.PIGMENT, stack, Capabilities.PIGMENT_HANDLER);
            this.addChemicalTags(GuiDictionary.DictionaryTagType.SLURRY, stack, Capabilities.SLURRY_HANDLER);
         }
      } else if (newTarget instanceof FluidStack fluidStack) {
         if (fluidStack.isEmpty()) {
            this.target = null;
         } else {
            this.target = fluidStack.copy();
            this.tags
               .put(
                  GuiDictionary.DictionaryTagType.FLUID,
                  TagCache.getTagsAsStrings(TagUtils.tagsStream(ForgeRegistries.FLUIDS, ((FluidStack)this.target).getFluid()))
               );
         }
      } else {
         if (!(newTarget instanceof ChemicalStack<?> chemicalStack)) {
            Mekanism.logger.warn("Unable to get tags for unknown type: {}", newTarget);
            return;
         }

         if (chemicalStack.isEmpty()) {
            this.target = null;
         } else {
            this.target = chemicalStack.copy();
            List<String> chemicalTags = TagCache.getTagsAsStrings(((ChemicalStack)this.target).getType().getTags());
            if (this.target instanceof GasStack) {
               this.tags.put(GuiDictionary.DictionaryTagType.GAS, chemicalTags);
            } else if (this.target instanceof InfusionStack) {
               this.tags.put(GuiDictionary.DictionaryTagType.INFUSE_TYPE, chemicalTags);
            } else if (this.target instanceof PigmentStack) {
               this.tags.put(GuiDictionary.DictionaryTagType.PIGMENT, chemicalTags);
            } else if (this.target instanceof SlurryStack) {
               this.tags.put(GuiDictionary.DictionaryTagType.SLURRY, chemicalTags);
            }
         }
      }

      this.tagSetter.accept(this.tags.keySet());
      playClickSound(SoundEvents.f_12490_);
   }

   private <STACK extends ChemicalStack<?>, HANDLER extends IChemicalHandler<?, STACK>> void addChemicalTags(
      GuiDictionary.DictionaryTagType tagType, ItemStack stack, Capability<HANDLER> capability
   ) {
      stack.getCapability(capability)
         .ifPresent(
            handler -> this.tags
               .put(
                  tagType,
                  TagCache.getTagsAsStrings(
                     IntStream.range(0, handler.getTanks())
                        .mapToObj(handler::getChemicalInTank)
                        .filter(chemicalInTank -> !chemicalInTank.isEmpty())
                        .flatMap(chemicalInTank -> chemicalInTank.getType().getTags())
                        .distinct()
                  )
               )
         );
   }

   @Override
   public boolean hasPersistentData() {
      return true;
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      GuiDictionaryTarget old = (GuiDictionaryTarget)element;
      this.target = old.target;
      this.tags.putAll(old.tags);
   }

   @Nullable
   @Override
   public IJEIGhostTarget.IGhostIngredientConsumer getGhostHandler() {
      return new IJEIGhostTarget.IGhostIngredientConsumer() {
         @Override
         public boolean supportsIngredient(Object ingredient) {
            if (ingredient instanceof ItemStack stack) {
               return !stack.m_41619_();
            } else if (ingredient instanceof FluidStack stack) {
               return !stack.isEmpty();
            } else {
               return ingredient instanceof ChemicalStack<?> stack ? !stack.isEmpty() : false;
            }
         }

         @Override
         public void accept(Object ingredient) {
            GuiDictionaryTarget.this.setTargetSlot(ingredient);
         }
      };
   }
}

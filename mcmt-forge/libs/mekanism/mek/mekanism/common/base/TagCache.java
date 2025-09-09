package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

public final class TagCache {
   private static final Map<String, TagCache.MatchingStacks> blockTagStacks = new Object2ObjectOpenHashMap();
   private static final Map<String, List<ItemStack>> itemTagStacks = new Object2ObjectOpenHashMap();
   private static final Map<String, List<ItemStack>> itemModIDStacks = new Object2ObjectOpenHashMap();
   private static final Map<String, TagCache.MatchingStacks> blockModIDStacks = new Object2ObjectOpenHashMap();
   private static final Map<Block, List<String>> tileEntityTypeTagCache = new IdentityHashMap<>();
   private static final Object2BooleanMap<String> blockTagBlacklistedElements = new Object2BooleanOpenHashMap();
   private static final Object2BooleanMap<String> modIDBlacklistedElements = new Object2BooleanOpenHashMap();

   private TagCache() {
   }

   public static void resetTagCaches() {
      blockTagStacks.clear();
      itemTagStacks.clear();
      tileEntityTypeTagCache.clear();
      blockTagBlacklistedElements.clear();
      modIDBlacklistedElements.clear();
   }

   public static List<String> getItemTags(@NotNull ItemStack check) {
      return getTagsAsStrings(check.m_204131_());
   }

   public static List<String> getTileEntityTypeTags(@NotNull Block block) {
      return tileEntityTypeTagCache.computeIfAbsent(
         block,
         b -> {
            if (b instanceof IHasTileEntity<?> hasTileEntity) {
               return getTagsAsStrings(TagUtils.tagsStream(ForgeRegistries.BLOCK_ENTITY_TYPES, hasTileEntity.getTileType().get()));
            } else {
               BlockState state = b.m_49966_();
               if (state.m_155947_()) {
                  ITagManager<BlockEntityType<?>> manager = TagUtils.manager(ForgeRegistries.BLOCK_ENTITY_TYPES);
                  return getTagsAsStrings(
                     StreamSupport.<BlockEntityType>stream(ForgeRegistries.BLOCK_ENTITY_TYPES.spliterator(), false)
                        .filter(type -> type.m_155262_(state))
                        .flatMap(type -> TagUtils.tagsStream(manager, type))
                        .distinct()
                  );
               } else {
                  return Collections.emptyList();
               }
            }
         }
      );
   }

   public static <TYPE> List<String> getTagsAsStrings(@NotNull Stream<TagKey<TYPE>> tags) {
      return tags.map(tag -> tag.f_203868_().toString()).toList();
   }

   public static List<ItemStack> getItemTagStacks(@NotNull String tagName) {
      return itemTagStacks.computeIfAbsent(tagName, name -> {
         Set<Item> items = collectTagStacks(TagUtils.manager(ForgeRegistries.ITEMS), name, item -> item != MekanismBlocks.BOUNDING_BLOCK.m_5456_());
         return items.stream().<ItemStack>map(ItemStack::new).filter(stack -> !stack.m_41619_()).toList();
      });
   }

   public static TagCache.MatchingStacks getBlockTagStacks(@NotNull String tagName) {
      return blockTagStacks.computeIfAbsent(tagName, name -> {
         Set<Block> blocks = collectTagStacks(TagUtils.manager(ForgeRegistries.BLOCKS), name, block -> block != MekanismBlocks.BOUNDING_BLOCK.getBlock());
         return getMatching(blocks);
      });
   }

   private static <TYPE extends ItemLike> Set<TYPE> collectTagStacks(ITagManager<TYPE> tagManager, String tagName, Predicate<TYPE> validElement) {
      return tagManager.stream()
         .filter(tag -> WildcardMatcher.matches(tagName, tag.getKey()))
         .<TYPE>flatMap(ITag::stream)
         .filter(validElement)
         .collect(Collectors.toSet());
   }

   private static TagCache.MatchingStacks getMatching(Set<Block> blocks) {
      return blocks.isEmpty()
         ? TagCache.MatchingStacks.NONE
         : new TagCache.MatchingStacks(true, blocks.stream().<ItemStack>map(ItemStack::new).filter(stack -> !stack.m_41619_()).toList());
   }

   public static List<ItemStack> getItemModIDStacks(@NotNull String modName) {
      return itemModIDStacks.computeIfAbsent(modName, name -> {
         List<ItemStack> stacks = new ArrayList<>();

         for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item != MekanismBlocks.BOUNDING_BLOCK.m_5456_()) {
               ItemStack stack = new ItemStack(item);
               if (!stack.m_41619_() && WildcardMatcher.matches(name, MekanismUtils.getModId(stack))) {
                  stacks.add(stack);
               }
            }
         }

         return stacks;
      });
   }

   public static TagCache.MatchingStacks getBlockModIDStacks(@NotNull String modName) {
      return blockModIDStacks.computeIfAbsent(modName, name -> {
         Set<Block> blocks = new ReferenceOpenHashSet();

         for (Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            Block block = entry.getValue();
            if (block != MekanismBlocks.BOUNDING_BLOCK.getBlock() && WildcardMatcher.matches(name, entry.getKey().m_135782_().m_135827_())) {
               blocks.add(block);
            }
         }

         return getMatching(blocks);
      });
   }

   public static boolean tagHasMinerBlacklisted(@NotNull String tag) {
      return MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty()
         ? false
         : blockTagBlacklistedElements.computeIfAbsent(
            tag,
            t -> TagUtils.manager(ForgeRegistries.BLOCKS)
               .stream()
               .anyMatch(
                  blockTag -> WildcardMatcher.matches(t, blockTag.getKey()) && blockTag.stream().anyMatch(MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP::contains)
               )
         );
   }

   public static boolean modIDHasMinerBlacklisted(@NotNull String modName) {
      return MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty() ? false : modIDBlacklistedElements.computeIfAbsent(modName, name -> {
         for (Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            Block block = entry.getValue();
            if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.contains(block) && WildcardMatcher.matches(name, entry.getKey().m_135782_().m_135827_())) {
               return true;
            }
         }

         return false;
      });
   }

   public record MatchingStacks(boolean hasMatch, List<ItemStack> stacks) {
      private static final TagCache.MatchingStacks NONE = new TagCache.MatchingStacks(false, Collections.emptyList());
   }
}

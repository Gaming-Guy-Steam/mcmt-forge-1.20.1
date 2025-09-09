package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import mekanism.common.base.TagCache;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import org.jetbrains.annotations.Nullable;

public class SearchQueryParser {
   private static final SearchQueryParser.ISearchQuery INVALID = stack -> false;
   private static final Set<Character> TERMINATORS = Set.of('|', '(', '"', '\'');

   public static SearchQueryParser.ISearchQuery parse(String query) {
      List<SearchQueryParser.SearchQuery> ret = new ArrayList<>();
      SearchQueryParser.SearchQuery curQuery = new SearchQueryParser.SearchQuery();

      for (int i = 0; i < query.length(); i++) {
         char c = query.charAt(i);
         if (c == '|') {
            if (!curQuery.isEmpty()) {
               ret.add(curQuery);
            }

            curQuery = new SearchQueryParser.SearchQuery();
         } else if (c != ' ') {
            SearchQueryParser.QueryType type = SearchQueryParser.QueryType.get(c);
            if (type != null) {
               i++;
            } else {
               type = SearchQueryParser.QueryType.NAME;
            }

            SearchQueryParser.KeyListResult keyListResult = readKeyList(query, i, type, curQuery);
            if (!keyListResult.hasResult()) {
               return INVALID;
            }

            i = keyListResult.index();
         }
      }

      if (!curQuery.isEmpty()) {
         ret.add(curQuery);
      }

      return new SearchQueryParser.SearchQueryList(ret);
   }

   private static SearchQueryParser.KeyListResult readKeyList(String query, int start, SearchQueryParser.QueryType type, SearchQueryParser.SearchQuery curQuery) {
      if (start >= query.length()) {
         return new SearchQueryParser.KeyListResult(true, start);
      } else {
         char qc = query.charAt(start);
         int newIndex;
         List<String> keys;
         if (qc == '(') {
            SearchQueryParser.ListResult<String> listResult = readList(query, start);
            if (listResult == null) {
               return SearchQueryParser.KeyListResult.INVALID;
            }

            keys = listResult.result();
            newIndex = listResult.index();
         } else if (qc != '"' && qc != '\'') {
            SearchQueryParser.Result textResult = readUntilTermination(query, start, type != SearchQueryParser.QueryType.NAME);
            keys = Collections.singletonList(textResult.result());
            newIndex = textResult.index();
         } else {
            SearchQueryParser.Result quoteResult = readQuote(query, start);
            if (quoteResult == null) {
               return SearchQueryParser.KeyListResult.INVALID;
            }

            keys = Collections.singletonList(quoteResult.result());
            newIndex = quoteResult.index();
         }

         if (!keys.isEmpty()) {
            curQuery.queryStrings.put(type, keys);
         }

         return new SearchQueryParser.KeyListResult(true, newIndex);
      }
   }

   @Nullable
   private static SearchQueryParser.ListResult<String> readList(String query, int start) {
      List<String> ret = new ArrayList<>();
      StringBuilder sb = new StringBuilder();

      for (int i = start + 1; i < query.length(); i++) {
         char qc = query.charAt(i);
         switch (qc) {
            case '"':
            case '\'':
               SearchQueryParser.Result quoteResult = readQuote(query, i);
               if (quoteResult == null) {
                  return null;
               }

               ret.add(quoteResult.result());
               i = quoteResult.index();
               break;
            case ')':
               String key = sb.toString().trim();
               if (!key.isEmpty()) {
                  ret.add(key);
               }

               return new SearchQueryParser.ListResult<>(ret, i);
            case '|':
               String key = sb.toString().trim();
               if (!key.isEmpty()) {
                  ret.add(key);
               }

               sb = new StringBuilder();
               break;
            default:
               sb.append(qc);
         }
      }

      return null;
   }

   @Nullable
   private static SearchQueryParser.Result readQuote(String text, int start) {
      char quoteChar = text.charAt(start);
      StringBuilder ret = new StringBuilder();

      for (int i = start + 1; i < text.length(); i++) {
         char tc = text.charAt(i);
         if (tc == quoteChar) {
            return new SearchQueryParser.Result(ret.toString(), i);
         }

         ret.append(tc);
      }

      return null;
   }

   private static SearchQueryParser.Result readUntilTermination(String text, int start, boolean spaceTerminate) {
      StringBuilder sb = new StringBuilder();

      int i;
      for (i = start; i < text.length(); i++) {
         char tc = text.charAt(i);
         if (TERMINATORS.contains(tc) || SearchQueryParser.QueryType.get(tc) != null || spaceTerminate && tc == ' ') {
            i--;
            break;
         }

         sb.append(tc);
      }

      return new SearchQueryParser.Result(sb.toString().trim(), i);
   }

   public interface ISearchQuery {
      boolean matches(ItemStack stack);

      default boolean isInvalid() {
         return this == SearchQueryParser.INVALID;
      }
   }

   private record KeyListResult(boolean hasResult, int index) {
      public static final SearchQueryParser.KeyListResult INVALID = new SearchQueryParser.KeyListResult(false, -1);
   }

   private record ListResult<TYPE>(List<TYPE> result, int index) {
   }

   public static enum QueryType {
      NAME('~', (key, stack) -> stack.m_41786_().getString().toLowerCase(Locale.ROOT).contains(key.toLowerCase(Locale.ROOT))),
      MOD_ID('@', (key, stack) -> MekanismUtils.getModId(stack).toLowerCase(Locale.ROOT).contains(key.toLowerCase(Locale.ROOT))),
      TOOLTIP(
         '$',
         (key, stack) -> stack.m_41651_(null, Default.f_256752_)
            .stream()
            .map(t -> t.getString().toLowerCase(Locale.ROOT))
            .anyMatch(tooltip -> tooltip.contains(key.toLowerCase(Locale.ROOT)))
      ),
      TAG(
         '#', (key, stack) -> TagCache.getItemTags(stack).stream().anyMatch(itemTag -> itemTag.toLowerCase(Locale.ROOT).contains(key.toLowerCase(Locale.ROOT)))
      );

      private static final Char2ObjectMap<SearchQueryParser.QueryType> charLookupMap;
      private final char prefix;
      private final BiPredicate<String, ItemStack> checker;

      public static SearchQueryParser.QueryType get(char prefix) {
         return (SearchQueryParser.QueryType)charLookupMap.get(prefix);
      }

      public static CharSet getPrefixChars() {
         return charLookupMap.keySet();
      }

      private QueryType(char prefix, BiPredicate<String, ItemStack> checker) {
         this.prefix = prefix;
         this.checker = checker;
      }

      public boolean matches(String key, ItemStack stack) {
         return this.checker.test(key, stack);
      }

      static {
         SearchQueryParser.QueryType[] values = values();
         charLookupMap = new Char2ObjectArrayMap(values.length);

         for (SearchQueryParser.QueryType type : values) {
            charLookupMap.put(type.prefix, type);
         }
      }
   }

   private record Result(String result, int index) {
   }

   public static class SearchQuery implements SearchQueryParser.ISearchQuery {
      private final Map<SearchQueryParser.QueryType, List<String>> queryStrings = new LinkedHashMap<>();

      @Override
      public boolean matches(ItemStack stack) {
         return this.queryStrings
            .entrySet()
            .stream()
            .allMatch(entry -> entry.getValue().stream().anyMatch(key -> ((SearchQueryParser.QueryType)entry.getKey()).matches(key, stack)));
      }

      private boolean isEmpty() {
         return this.queryStrings.isEmpty();
      }

      protected Map<SearchQueryParser.QueryType, List<String>> getQueryMap() {
         return this.queryStrings;
      }

      @Override
      public String toString() {
         return this.queryStrings.toString();
      }
   }

   public static class SearchQueryList implements SearchQueryParser.ISearchQuery {
      private final List<SearchQueryParser.SearchQuery> queries;

      private SearchQueryList(List<SearchQueryParser.SearchQuery> queries) {
         this.queries = queries;
      }

      @Override
      public boolean matches(ItemStack stack) {
         return this.queries.isEmpty() || this.queries.stream().anyMatch(query -> query.matches(stack));
      }

      @Override
      public String toString() {
         return this.queries.toString();
      }

      protected List<SearchQueryParser.SearchQuery> getQueries() {
         return this.queries;
      }
   }
}

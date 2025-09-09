package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.ingredient.type.IIngredientList;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister
@NativeTypeRegistration(
   value = ItemStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.ItemStackIngredient"
)
public class CrTItemStackIngredient {
   private CrTItemStackIngredient() {
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(IItemStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty stack.");
      } else {
         return from(stack, stack.getAmount());
      }
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(Item item) {
      return from(item, 1);
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(Item item, int amount) {
      CrTIngredientHelper.assertValidAmount("ItemStackIngredients", amount);
      return IngredientCreatorAccess.item().from(item, amount);
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(KnownTag<Item> itemTag, int amount) {
      TagKey<Item> tag = CrTIngredientHelper.assertValidAndGet(itemTag, amount, "ItemStackIngredients");
      return IngredientCreatorAccess.item().from(tag, amount);
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(KnownTag<Item> itemTag) {
      return from(itemTag, 1);
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(Many<KnownTag<Item>> itemTag) {
      return from((KnownTag<Item>)itemTag.getData(), itemTag.getAmount());
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(IIngredient ingredient) {
      return from(ingredient, 1);
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(IIngredientWithAmount ingredient) {
      return from(ingredient.getIngredient(), ingredient.getAmount());
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(IIngredient ingredient, int amount) {
      CrTIngredientHelper.assertValidAmount("ItemStackIngredients", amount);
      Ingredient vanillaIngredient = ingredient.asVanillaIngredient();
      if (vanillaIngredient == Ingredient.f_43901_) {
         throw new IllegalArgumentException("ItemStackIngredients cannot be made using the empty ingredient: " + amount);
      } else {
         return IngredientCreatorAccess.item().from(vanillaIngredient, amount);
      }
   }

   @StaticExpansionMethod
   public static ItemStackIngredient from(IIngredientList ingredientList) {
      IIngredient[] ingredients = ingredientList.getIngredients();
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty ingredient list!");
      } else {
         List<ItemStackIngredient> itemStackIngredients = new ArrayList<>();
         addIngredients(itemStackIngredients, ingredients);
         return createMulti(itemStackIngredients.toArray(new ItemStackIngredient[0]));
      }
   }

   private static void addIngredients(List<ItemStackIngredient> itemStackIngredients, IIngredient[] ingredients) {
      for (IIngredient ingredient : ingredients) {
         if (ingredient instanceof IItemStack stack) {
            itemStackIngredients.add(from(stack));
         } else if (ingredient instanceof IIngredientList ingredientList) {
            addIngredients(itemStackIngredients, ingredientList.getIngredients());
         } else {
            itemStackIngredients.add(from(ingredient));
         }
      }
   }

   @StaticExpansionMethod
   public static ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
      return CrTIngredientHelper.createMulti("ItemStackIngredients", IngredientCreatorAccess.item(), ingredients);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static IData asIData(ItemStackIngredient _this) {
      return JSONConverter.convert(_this.serialize());
   }

   @Method
   public static boolean testType(ItemStackIngredient _this, IItemStack type) {
      return _this.testType(type.getInternal());
   }

   @Method
   public static boolean test(ItemStackIngredient _this, IItemStack stack) {
      return _this.test(stack.getInternal());
   }

   @Method
   @Getter("representations")
   public static List<IItemStack> getRepresentations(ItemStackIngredient _this) {
      return CrTUtils.convertItems(_this.getRepresentations());
   }

   @Method
   @Operator(OperatorType.OR)
   public static ItemStackIngredient or(ItemStackIngredient _this, ItemStackIngredient other) {
      return IngredientCreatorAccess.item().createMulti(new ItemStackIngredient[]{_this, other});
   }
}

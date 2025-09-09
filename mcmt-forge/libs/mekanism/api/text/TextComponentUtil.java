package mekanism.api.text;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class TextComponentUtil {
   private TextComponentUtil() {
   }

   public static MutableComponent color(MutableComponent component, int color) {
      return component.m_6270_(component.m_7383_().m_131148_(TextColor.m_131266_(color)));
   }

   public static MutableComponent build(Object... components) {
      MutableComponent result = null;
      Style cachedStyle = Style.f_131099_;

      for (Object component : components) {
         if (component != null) {
            MutableComponent current = null;
            if (component instanceof IHasTextComponent hasTextComponent) {
               current = hasTextComponent.getTextComponent().m_6881_();
            } else if (component instanceof IHasTranslationKey hasTranslationKey) {
               current = translate(hasTranslationKey.getTranslationKey());
            } else if (component instanceof EnumColor color) {
               cachedStyle = cachedStyle.m_131148_(color.getColor());
            } else if (component instanceof TextColor color) {
               cachedStyle = cachedStyle.m_131148_(color);
            } else if (component instanceof Component c) {
               current = c.m_6881_();
            } else if (component instanceof ChatFormatting formatting) {
               cachedStyle = cachedStyle.m_131157_(formatting);
            } else if (component instanceof ClickEvent event) {
               cachedStyle = cachedStyle.m_131142_(event);
            } else if (component instanceof HoverEvent event) {
               cachedStyle = cachedStyle.m_131144_(event);
            } else if (component instanceof Block block) {
               current = translate(block.m_7705_());
            } else if (component instanceof Item item) {
               current = translate(item.m_5524_());
            } else if (component instanceof ItemStack stack) {
               current = stack.m_41786_().m_6881_();
            } else if (component instanceof FluidStack stack) {
               current = stack.getDisplayName().m_6881_();
            } else if (component instanceof Fluid fluid) {
               current = translate(fluid.getFluidType().getDescriptionId());
            } else if (component instanceof Direction direction) {
               current = getTranslatedDirection(direction);
            } else if (component instanceof Boolean bool) {
               current = getTranslatedBoolean(bool);
            } else {
               current = getString(component.toString());
            }

            if (current != null) {
               if (!cachedStyle.m_131179_()) {
                  current.m_6270_(cachedStyle);
                  cachedStyle = Style.f_131099_;
               }

               if (result == null) {
                  result = current;
               } else {
                  result.m_7220_(current);
               }
            }
         }
      }

      return result;
   }

   private static MutableComponent getTranslatedBoolean(boolean bool) {
      return (bool ? APILang.TRUE_LOWER : APILang.FALSE_LOWER).translate(new Object[0]);
   }

   private static MutableComponent getTranslatedDirection(Direction direction) {
      return (switch (direction) {
         case DOWN -> APILang.DOWN;
         case UP -> APILang.UP;
         case NORTH -> APILang.NORTH;
         case SOUTH -> APILang.SOUTH;
         case WEST -> APILang.WEST;
         case EAST -> APILang.EAST;
         default -> throw new IncompatibleClassChangeError();
      }).translate(new Object[0]);
   }

   public static MutableComponent getString(String component) {
      return Component.m_237113_(cleanString(component));
   }

   private static String cleanString(String component) {
      return component.replace(" ", " ").replace(" ", " ");
   }

   public static MutableComponent translate(String key, Object... args) {
      return Component.m_237110_(key, args);
   }

   public static MutableComponent smartTranslate(String key, Object... components) {
      if (components.length == 0) {
         return translate(key);
      } else {
         List<Object> args = new ArrayList<>();
         Style cachedStyle = Style.f_131099_;

         for (Object component : components) {
            if (component == null) {
               args.add(null);
               cachedStyle = Style.f_131099_;
            } else {
               MutableComponent current = null;
               if (component instanceof IHasTextComponent hasTextComponent) {
                  current = hasTextComponent.getTextComponent().m_6881_();
               } else if (component instanceof IHasTranslationKey hasTranslationKey) {
                  current = translate(hasTranslationKey.getTranslationKey());
               } else if (component instanceof Block block) {
                  current = translate(block.m_7705_());
               } else if (component instanceof Item item) {
                  current = translate(item.m_5524_());
               } else if (component instanceof ItemStack stack) {
                  current = stack.m_41786_().m_6881_();
               } else if (component instanceof FluidStack stack) {
                  current = stack.getDisplayName().m_6881_();
               } else if (component instanceof Fluid fluid) {
                  current = translate(fluid.getFluidType().getDescriptionId());
               } else if (component instanceof Direction direction) {
                  current = getTranslatedDirection(direction);
               } else if (component instanceof Boolean bool) {
                  current = getTranslatedBoolean(bool);
               } else {
                  if (component instanceof EnumColor color && cachedStyle.m_131135_() == null) {
                     cachedStyle = cachedStyle.m_131148_(color.getColor());
                     continue;
                  }

                  if (component instanceof TextColor color && cachedStyle.m_131135_() == null) {
                     cachedStyle = cachedStyle.m_131148_(color);
                     continue;
                  }

                  if (component instanceof ChatFormatting formatting && !hasStyleType(cachedStyle, formatting)) {
                     cachedStyle = cachedStyle.m_131157_(formatting);
                     continue;
                  }

                  if (component instanceof ClickEvent event && cachedStyle.m_131182_() == null) {
                     cachedStyle = cachedStyle.m_131142_(event);
                     continue;
                  }

                  if (component instanceof HoverEvent event && cachedStyle.m_131186_() == null) {
                     cachedStyle = cachedStyle.m_131144_(event);
                     continue;
                  }

                  if (!cachedStyle.m_131179_()) {
                     if (component instanceof Component) {
                        current = ((Component)component).m_6881_();
                     } else if (component instanceof EnumColor) {
                        current = ((EnumColor)component).getName();
                     } else {
                        current = getString(component.toString());
                     }
                  } else if (component instanceof String) {
                     component = cleanString((String)component);
                  }
               }

               if (!cachedStyle.m_131179_()) {
                  if (current == null) {
                     args.add(component);
                  } else {
                     args.add(current.m_6270_(cachedStyle));
                  }

                  cachedStyle = Style.f_131099_;
               } else if (current == null) {
                  args.add(component);
               } else {
                  args.add(current);
               }
            }
         }

         if (!cachedStyle.m_131179_()) {
            Object lastComponent = components[components.length - 1];
            if (lastComponent instanceof EnumColor color) {
               args.add(color.getName());
            } else {
               args.add(lastComponent);
            }
         }

         return translate(key, args.toArray());
      }
   }

   private static boolean hasStyleType(Style current, ChatFormatting formatting) {
      return switch (formatting) {
         case OBFUSCATED -> current.m_131176_();
         case BOLD -> current.m_131154_();
         case STRIKETHROUGH -> current.m_131168_();
         case UNDERLINE -> current.m_131171_();
         case ITALIC -> current.m_131161_();
         case RESET -> current.m_131179_();
         default -> current.m_131135_() != null;
      };
   }
}

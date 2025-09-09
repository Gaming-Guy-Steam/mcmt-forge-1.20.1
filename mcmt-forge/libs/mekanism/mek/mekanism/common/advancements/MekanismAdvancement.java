package mekanism.common.advancements;

import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record MekanismAdvancement(ResourceLocation name, String title, String description, @Nullable MekanismAdvancement parent) {
   public MekanismAdvancement(@Nullable MekanismAdvancement parent, ResourceLocation name) {
      this(parent, name, getSubName(name.m_135815_()));
   }

   private MekanismAdvancement(@Nullable MekanismAdvancement parent, ResourceLocation name, String subName) {
      this(name, subName, subName, parent);
   }

   public MekanismAdvancement(ResourceLocation name, String title, String description, @Nullable MekanismAdvancement parent) {
      title = Util.m_137492_("advancements", new ResourceLocation(name.m_135827_(), title + ".title"));
      description = Util.m_137492_("advancements", new ResourceLocation(name.m_135827_(), description + ".description"));
      this.name = name;
      this.title = title;
      this.description = description;
      this.parent = parent;
   }

   public Component translateTitle() {
      return TextComponentUtil.translate(this.title);
   }

   public Component translateDescription() {
      return TextComponentUtil.translate(this.description);
   }

   private static String getSubName(String path) {
      int lastSeparator = path.lastIndexOf(47);
      if (lastSeparator == -1) {
         return path;
      } else if (lastSeparator + 1 == path.length()) {
         throw new IllegalArgumentException("Unexpected name portion");
      } else {
         return path.substring(lastSeparator + 1);
      }
   }
}

package mekanism.client.model;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

public record ModelPartData(String name, CubeListBuilder cubes, PartPose pose, List<ModelPartData> children) {
   public ModelPartData(String name, CubeListBuilder cubes, PartPose pose, ModelPartData... children) {
      this(name, cubes, pose, List.of(children));
   }

   public ModelPartData(String name, CubeListBuilder cubes, ModelPartData... children) {
      this(name, cubes, PartPose.f_171404_, children);
   }

   public void addToDefinition(PartDefinition definition) {
      PartDefinition subDefinition = definition.m_171599_(this.name, this.cubes, this.pose);

      for (ModelPartData child : this.children) {
         child.addToDefinition(subDefinition);
      }
   }

   public ModelPart getFromRoot(ModelPart part) {
      return part.m_171324_(this.name);
   }
}

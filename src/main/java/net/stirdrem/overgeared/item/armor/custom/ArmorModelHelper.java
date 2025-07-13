package net.stirdrem.overgeared.item.armor.custom;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArmorModelHelper {
    public static HumanoidModel withPart(String partName, ModelPart customPart) {
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("body", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()));

        parts.put(partName, customPart);

        return new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));
    }
}

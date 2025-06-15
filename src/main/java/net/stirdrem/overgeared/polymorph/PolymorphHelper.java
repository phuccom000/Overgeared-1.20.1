/*
package net.stirdrem.overgeared.polymorph;

import java.util.Optional;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.screen.SmithingAnvilMenu;


public class PolymorphHelper {

    public static <C extends Container, T extends Recipe<C>> Optional<T> getRecipe(SmithingAnvilBlockEntity be, RecipeType<T> type, C inventory, Level world) {
        return RecipeSelection.getBlockEntityRecipe(type, inventory, world, be);
    }

    public static void init() {
        IPolymorphCommon commonApi = PolymorphApi.common();
        commonApi.registerBlockEntity2RecipeData(pTileEntity -> {
            if (pTileEntity instanceof SmithingAnvilBlockEntity) {
                return new SmithingAnvilRecipeData((SmithingAnvilBlockEntity) pTileEntity);
            }
            return null;
        });
        commonApi.registerContainer2BlockEntity(pContainer -> {
            if (pContainer instanceof SmithingAnvilMenu cnt) {
                return cnt.getBlockEntity();
            }
            return null;
        });
    }
}
*/

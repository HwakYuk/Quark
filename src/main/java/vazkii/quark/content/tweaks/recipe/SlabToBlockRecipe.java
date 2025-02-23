package vazkii.quark.content.tweaks.recipe;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import vazkii.quark.content.tweaks.module.SlabsToBlocksModule;

public class SlabToBlockRecipe extends CustomRecipe {

	public static final SimpleRecipeSerializer<?> SERIALIZER = new SimpleRecipeSerializer<>(SlabToBlockRecipe::new);
	private ThreadLocal<Boolean> locked = ThreadLocal.withInitial(() -> false);

	public SlabToBlockRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if(locked.get())
			return false;

		Item target = null;

		boolean checked = false;
		boolean result = false;

		for(int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				Item item = stack.getItem();

				if(target != null) {
					if(checked)
						return false;

					result = item == target && checkForOtherRecipes(container, level);
					checked = true;
				} else {
					if(SlabsToBlocksModule.recipes.containsKey(item)) {
						target = item;
					} else return false;
				}
			}
		}

		return result;
	}

	private boolean checkForOtherRecipes(CraftingContainer container, Level level) {
		locked.set(true);
		boolean ret = false;
		Optional<CraftingRecipe> optional = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, container, level);
		ret = !optional.isPresent();
		locked.set(false);
		return ret;
	}

	@Override
	public ItemStack assemble(CraftingContainer container) {
		for(int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				Item item = stack.getItem();

				if(SlabsToBlocksModule.recipes.containsKey(item))
					return new ItemStack(SlabsToBlocksModule.recipes.get(item));
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return (width * height) >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}



}

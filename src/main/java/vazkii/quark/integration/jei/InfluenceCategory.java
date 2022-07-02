package vazkii.quark.integration.jei;

import javax.annotation.Nonnull;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.base.Quark;

public class InfluenceCategory implements IRecipeCategory<InfluenceEntry> {

	public static final ResourceLocation UID = new ResourceLocation(Quark.MOD_ID, "influence");

	public static final ResourceLocation TEXTURE = new ResourceLocation(Quark.MOD_ID, "textures/gui/jei_influence.png");

	private final IDrawable icon;
	private final IDrawableStatic background;
	private final Component localizedName;

	public InfluenceCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MatrixEnchantingModule.matrixEnchanter));
		this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 72, 36).setTextureSize(128, 128).build();
		this.localizedName = Component.translatable("quark.jei.influence");
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public RecipeType<InfluenceEntry> getRecipeType() {
		return QuarkJeiPlugin.INFLUENCING;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Nonnull
	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull InfluenceEntry recipe, @Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 10)
			 .addItemStack(recipe.getCandleStack());

		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 55, 1)
			 .addItemStack(recipe.getBoostBook());

		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 55, 19)
			 .addItemStack(recipe.getDampenBook());

		builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
			 .addItemStacks(recipe.getAssociatedBooks());
	}
}

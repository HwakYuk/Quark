package vazkii.quark.content.world.module;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Lists;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraftforge.common.Tags;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.type.CompoundBiomeConfig;
import vazkii.quark.base.module.config.type.DimensionConfig;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.content.world.config.AirStoneClusterConfig;
import vazkii.quark.content.world.config.BigStoneClusterConfig;
import vazkii.quark.content.world.gen.BigStoneClusterGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class BigStoneClustersModule extends QuarkModule {

	@Config public static BigStoneClusterConfig calcite = new BigStoneClusterConfig(BiomeTags.IS_MOUNTAIN);
	@Config public static BigStoneClusterConfig limestone = new BigStoneClusterConfig(Tags.Biomes.IS_SWAMP, BiomeTags.IS_OCEAN);
	@Config public static BigStoneClusterConfig jasper = new BigStoneClusterConfig(BiomeTags.IS_BADLANDS, Tags.Biomes.IS_SANDY);
	@Config public static BigStoneClusterConfig shale = new BigStoneClusterConfig(Tags.Biomes.IS_SNOWY);
	
	@Config public static BigStoneClusterConfig myalite = new AirStoneClusterConfig(DimensionConfig.end(false), 20, 6, 100, 58, 62, 
			CompoundBiomeConfig.fromBiomeReslocs(false, "minecraft:end_highlands"))
			.setVertical(40, 10);

	@Config(description = "Blocks that stone clusters can replace. If you want to make it so it only replaces in one dimension,\n"
			+ "do \"block|dimension\", as we do for netherrack and end stone by default.") 
	public static List<String> blocksToReplace = Lists.newArrayList(
			"minecraft:stone", "minecraft:andesite", "minecraft:diorite", "minecraft:granite",
			"minecraft:netherrack|minecraft:the_nether", "minecraft:end_stone|minecraft:the_end",
			"quark:marble", "quark:limestone", "quark:jasper", "quark:slate");
	
	public static BiPredicate<Level, Block> blockReplacePredicate = (w, b) -> false;
	
	@Override
	public void setup() {
		BooleanSupplier alwaysTrue = () -> true;
		add(calcite, Blocks.CALCITE, alwaysTrue);
		
		add(limestone, NewStoneTypesModule.limestoneBlock, () -> NewStoneTypesModule.enabledWithLimestone);
		add(jasper, NewStoneTypesModule.jasperBlock, () -> NewStoneTypesModule.enabledWithJasper);
		add(shale, NewStoneTypesModule.shaleBlock, () -> NewStoneTypesModule.enabledWithShale);
		add(myalite, NewStoneTypesModule.myaliteBlock, () -> NewStoneTypesModule.enabledWithMyalite);
	}
	
	private void add(BigStoneClusterConfig config, Block block, BooleanSupplier condition) {
		WorldGenHandler.addGenerator(this, new BigStoneClusterGenerator(config, block.defaultBlockState(), condition), Decoration.UNDERGROUND_DECORATION, WorldGenWeights.BIG_STONE_CLUSTERS);
	}
	
	@Override
	public void configChanged() {
		blockReplacePredicate = (b, w) -> false;
		
		for(String s : blocksToReplace) {
			String bname = s;
			String dimension = null;
			
			if(bname.contains("|")) {
				String[] toks = bname.split("\\|");
				bname = toks[0];
				dimension = toks[1];
			}
			
			String dimFinal = dimension;
			Registry.BLOCK.getOptional(new ResourceLocation(bname)).ifPresent(blockObj -> {
				if(blockObj != Blocks.AIR) {
					if(dimFinal == null)
						blockReplacePredicate = blockReplacePredicate.or((w, b) -> blockObj == b);
					else {
						blockReplacePredicate = blockReplacePredicate.or((w, b) -> {
							if(blockObj != b)
								return false;
							if(w == null)
								return false;
							
							return w.dimension().location().toString().equals(dimFinal);
						});
					}
				}
			});
		}
	}
	
}

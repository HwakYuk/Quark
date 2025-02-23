package vazkii.quark.base.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Month;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.IngameConfigHandler;
import vazkii.quark.base.client.config.external.ExternalConfigHandler;
import vazkii.quark.base.client.config.screen.QuarkConfigHomeScreen;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.DyeHandler;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.WoodSetHandler;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.IConfigCallback;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

	public static boolean jingleBellsMotherfucker = false;

	@Override
	public void start() {
		LocalDateTime now = LocalDateTime.now();
		if(now.getMonth() == Month.DECEMBER && now.getDayOfMonth() >= 16 || now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 6)
			jingleBellsMotherfucker = true;

		super.start();

		ModuleLoader.INSTANCE.clientStart();

		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((minecraft, screen) -> new QuarkConfigHomeScreen(screen)));

		copyProgrammerArtIfMissing();

		(new ExternalConfigHandler()).setAPIHandler();
	}

	@Override
	public void registerListeners(IEventBus bus) {
		super.registerListeners(bus);

		bus.addListener(this::clientSetup);
		bus.addListener(this::registerReloadListeners);
		bus.addListener(this::modelBake);
		bus.addListener(this::modelLayers);
		bus.addListener(this::textureStitch);
		bus.addListener(this::postTextureStitch);
		bus.addListener(this::registerKeybinds);
		bus.addListener(this::registerAdditionalModels);
		bus.addListener(this::registerClientTooltipComponentFactories);
	}

	public void clientSetup(FMLClientSetupEvent event) {
		RenderLayerHandler.init();
		WoodSetHandler.clientSetup(event);
		DyeHandler.clientSetup(event);

		ModuleLoader.INSTANCE.clientSetup(event);
	}

	public void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		ModuleLoader.INSTANCE.registerReloadListeners(event);
	}

	public void modelBake(BakingCompleted event) {
		ModuleLoader.INSTANCE.modelBake(event);
	}

	public void modelLayers(EntityRenderersEvent.AddLayers event) {
		ModuleLoader.INSTANCE.modelLayers(event);
	}

	public void textureStitch(TextureStitchEvent.Pre event) {
		ModuleLoader.INSTANCE.textureStitch(event);
	}

	public void postTextureStitch(TextureStitchEvent.Post event) {
		ModuleLoader.INSTANCE.postTextureStitch(event);
	}
	
	public void registerKeybinds(RegisterKeyMappingsEvent event) {
		ModuleLoader.INSTANCE.registerKeybinds(event);
	}
	
	public void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		ModuleLoader.INSTANCE.registerAdditionalModels(event);
	}

	@OnlyIn(Dist.CLIENT)
	public void registerClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
		ModuleLoader.INSTANCE.registerClientTooltipComponentFactories(event);
	}
	
	@Override
	public void handleQuarkConfigChange() {
		super.handleQuarkConfigChange();

		ModuleLoader.INSTANCE.configChangedClient();
		IngameConfigHandler.INSTANCE.refresh();

		Minecraft mc = Minecraft.getInstance();
		mc.submit(() -> {
			if(mc.hasSingleplayerServer() && mc.player != null && mc.getSingleplayerServer() != null)
				for(int i = 0; i < 3; i++)
					mc.player.sendSystemMessage(Component.translatable("quark.misc.reloaded" + i).withStyle(i == 0 ? ChatFormatting.AQUA : ChatFormatting.WHITE));
		});
	}

	@Override
	public InteractionResult clientUseItem(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		if (player instanceof LocalPlayer lPlayer) {
			var mc = Minecraft.getInstance();
			if (mc.gameMode != null && mc.level != null)
				return mc.gameMode.useItemOn(lPlayer, hand, hit);
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void initContributorRewards() {
		ContributorRewardHandler.getLocalName();
		super.initContributorRewards();
	}

	@Override
	public IConfigCallback getConfigCallback() {
		return IngameConfigHandler.INSTANCE;
	}

	@Override
	public boolean isClientPlayerHoldingShift() {
		return Screen.hasShiftDown();
	}

	private static void copyProgrammerArtIfMissing() {
		File dir = new File(".", "resourcepacks");
		File target = new File(dir, "Quark Programmer Art.zip");

		if(!target.exists())
			try {
				dir.mkdirs();
				InputStream in = Quark.class.getResourceAsStream("/assets/quark/programmer_art.zip");
				FileOutputStream out = new FileOutputStream(target);

				byte[] buf = new byte[16384];
				int len;
				while((len = in.read(buf)) > 0)
					out.write(buf, 0, len);

				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}


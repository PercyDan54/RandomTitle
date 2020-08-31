package me.PercyDan.RandomTitle.Mixins;

import me.PercyDan.RandomTitle.RandomTitleUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.integrated.IntegratedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.text.SimpleDateFormat;


@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {
	@Shadow
	public abstract boolean isModded();
	@Shadow
	private IntegratedServer server;
	@Shadow
	public abstract ClientPlayNetworkHandler getNetworkHandler();
	@Shadow
	public abstract boolean isConnectedToRealms();
	@Shadow
	public Screen currentScreen;
	@Shadow
	public ClientWorld world;
	@Shadow
	private ClientConnection connection;
	@Shadow
	private Window window;
	@Shadow
	private ServerInfo currentServerEntry;
	public String Randtitle= RandomTitleUtil.getTitle();

	@Inject(method = "getWindowTitle", at = @At("HEAD"),cancellable = true)
	private void inject_getWindowTitle(CallbackInfoReturnable<String> ci) {
		final Logger LOGGER = LogManager.getLogger("RandomTitle");
		String title = RandomTitleUtil.getFormat();
		StringBuilder stringBuilder = new StringBuilder(SharedConstants.getGameVersion().getName());
		stringBuilder.append(" ");
		ClientPlayNetworkHandler clientPlayNetworkHandler = this.getNetworkHandler();
		if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
			stringBuilder.append(" - ");
			if (this.server != null && !this.server.isRemote()) {
				stringBuilder.append(I18n.translate("title.singleplayer", new Object[0]));
			} else if (this.isConnectedToRealms()) {
				stringBuilder.append(I18n.translate("title.multiplayer.realms", new Object[0]));
			} else if (this.server == null && (this.currentServerEntry == null || !this.currentServerEntry.isLocal())) {
				stringBuilder.append(I18n.translate("title.multiplayer.other", new Object[0]));
			} else {
				stringBuilder.append(I18n.translate("title.multiplayer.lan", new Object[0]));
			}

		}
		String date = new SimpleDateFormat(RandomTitleUtil.getDateFormat()).format((System.currentTimeMillis()));
		title=title.replace("%date%", date);
		title=title.replace("%prefix%",RandomTitleUtil.getPrefix().replace("%version%", stringBuilder.toString()));
		title=title.replace("%title%", Randtitle);
		LOGGER.info("Title set to \"" + title + "\"");
		ci.setReturnValue(title);
		ci.cancel();
	}
}

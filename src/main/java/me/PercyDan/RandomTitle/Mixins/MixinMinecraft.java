package me.PercyDan.RandomTitle.Mixins;

import me.PercyDan.RandomTitle.ConfigManager;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.SimpleDateFormat;


@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {
    @Shadow
    public Screen currentScreen;
    @Shadow
    public ClientWorld world;
    ConfigManager config = new ConfigManager();
    public String RandTitle = config.getTitle();
    @Shadow
    private IntegratedServer server;
    @Shadow
    private ClientConnection connection;
    @Shadow
    private Window window;
    @Shadow
    private ServerInfo currentServerEntry;
    
    @Shadow
    public abstract boolean isModded();

    @Shadow
    public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow
    public abstract boolean isConnectedToRealms();

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> ci) {
        String title = config.getFormat();
        StringBuilder stringBuilder = new StringBuilder(SharedConstants.getGameVersion().getName());
        stringBuilder.append(" ");
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.getNetworkHandler();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
            stringBuilder.append(" - ");
            if (this.server != null && !this.server.isRemote()) {
                stringBuilder.append(I18n.translate("title.singleplayer"));
            } else if (this.isConnectedToRealms()) {
                stringBuilder.append(I18n.translate("title.multiplayer.realms"));
            } else if (this.server == null && (this.currentServerEntry == null || !this.currentServerEntry.isLocal())) {
                stringBuilder.append(I18n.translate("title.multiplayer.other"));
            } else {
                stringBuilder.append(I18n.translate("title.multiplayer.lan"));
            }

        }
        String date = new SimpleDateFormat(config.getDateFormat()).format((System.currentTimeMillis()));
        title = title.replace("%date%", date);
        title = title.replace("%prefix%", config.getPrefix().replace("%version%", stringBuilder.toString()));
        title = title.replace("%title%", RandTitle);
        ci.setReturnValue(title);
        ci.cancel();
    }
}

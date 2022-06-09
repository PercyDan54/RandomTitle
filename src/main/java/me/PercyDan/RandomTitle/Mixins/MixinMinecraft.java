package me.percydan.RandomTitle.mixins;

import me.percydan.RandomTitle.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.SimpleDateFormat;


@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {
    ConfigManager config = new ConfigManager();
    private final String randomTitle = config.getTitle();

    @Shadow
    private IntegratedServer server;
    @Shadow
    private ServerInfo currentServerEntry;

    @Shadow
    public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow
    public abstract boolean isConnectedToRealms();

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> ci) {
        String title = config.Get("format");
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
        String date = new SimpleDateFormat(config.Get("dateformat")).format((System.currentTimeMillis()));
        title = title.replace("%date%", date);
        title = title.replace("%title%", randomTitle);
        title = title.replace("%version%", stringBuilder.toString());
        title = title.replace("%mod%", String.valueOf(FabricLoader.getInstance().getAllMods().size()));
        ci.setReturnValue(title);
        ci.cancel();
    }
}

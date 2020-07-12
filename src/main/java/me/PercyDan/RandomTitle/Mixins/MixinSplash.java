package me.PercyDan.RandomTitle.Mixins;
import me.PercyDan.RandomTitle.RandomTitleUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.text.SimpleDateFormat;
@Mixin(TitleScreen.class)
public abstract class MixinSplash {
    @Shadow
    private String splashText;
    @Inject(method = "init", at = @At("RETURN"))
    protected void init(CallbackInfo ci){
        this.splashText=RandomTitleUtil.getTitle();
    }

}

package creo.mod.koreanchat;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KoreanChatScreen extends ChatScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean isSleep = false;

    public KoreanChatScreen(String defaultText, boolean isSleep) {
        super(defaultText);
        this.isSleep = isSleep;
        LOGGER.info("KOREAN CHAT defaultText:" + defaultText + ", isSleep: " + isSleep);
    }

    @Override
    protected void init() {
        super.init();

        if (this.isSleep) {
            this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 40, 200, 20, new TranslatableComponent("multiplayer.stopSleeping"), (p_96074_) -> {
                this.sendWakeUp();
            }));
        }
    }

    @Override
    public void onClose() {
        if (this.isSleep) {
            this.sendWakeUp();
        }
        super.onClose();
    }

    private void sendWakeUp() {
        ClientPacketListener clientpacketlistener = this.minecraft.player.connection;
        clientpacketlistener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }

    @Override
    public boolean keyPressed(int p1, int p2, int p3) {
        LOGGER.info("KEY 1: " + p1 + ", KEY 2: " + p2 + ", KEY 3: " + p3);

        if (this.isSleep) {
            if (p1 == 256) {
                this.sendWakeUp();
            } else if (p1 == 257 || p1 == 335) {
                String s = this.input.getValue().trim();
                if (!s.isEmpty()) {
                    this.sendMessage(s);
                }

                this.input.setValue("");
                this.minecraft.gui.getChat().resetChatScroll();
                return true;
            }
        }

        return super.keyPressed(p1, p2, p3);
    }
}

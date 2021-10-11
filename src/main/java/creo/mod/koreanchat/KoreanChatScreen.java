package creo.mod.koreanchat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KoreanChatScreen extends ChatScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private String historyBuffer = "";
    private int historyPos = -1;

    private final String initial;
    private boolean isSleep = false;

    private CommandSuggestions commandSuggestions;

    public KoreanChatScreen(String initial, boolean isSleep) {
        super(initial);
        this.initial = initial;
        this.isSleep = isSleep;
        LOGGER.info("KOREAN CHAT defaultText:" + initial + ", isSleep: " + isSleep);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new KoreanEditBox(this.font, 4, this.height - 12, this.width - 4, 12, new TranslatableComponent("chat.editBox")) {
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(KoreanChatScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.addWidget(this.input);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        this.setInitialFocus(this.input);

        if (this.isSleep) {
            this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 40, 200, 20, new TranslatableComponent("multiplayer.stopSleeping"), (p_96074_) -> {
                this.sendWakeUp();
            }));
        }
    }

    @Override
    public void resize(Minecraft p1, int p2, int p3) {
        String s = this.input.getValue();
        this.init(p1, p2, p3);
        this.setChatLine(s);
        this.commandSuggestions.updateCommandInfo();
    }

    private void onEdited(String p_95611_) {
        String s = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!s.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
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

        if (this.commandSuggestions.keyPressed(p1, p2, p3)) {
            return true;
        } else if (superKeyPressed(p1, p2, p3)) {
            return true;
        } else if (p1 == 256) {
            this.minecraft.setScreen((Screen) null);
            return true;
        } else if (p1 != 257 && p1 != 335) {
            if (p1 == 265) {
                this.moveInHistory(-1);
                return true;
            } else if (p1 == 264) {
                this.moveInHistory(1);
                return true;
            } else if (p1 == 266) {
                this.minecraft.gui.getChat().scrollChat((double) (this.minecraft.gui.getChat().getLinesPerPage() - 1));
                return true;
            } else if (p1 == 267) {
                this.minecraft.gui.getChat().scrollChat((double) (-this.minecraft.gui.getChat().getLinesPerPage() + 1));
                return true;
            } else {
                return false;
            }
        } else {
            String s = this.input.getValue().trim();
            if (!s.isEmpty()) {
                this.sendMessage(s);
            }

            this.minecraft.setScreen((Screen) null);
            return true;
        }
    }

    public boolean superKeyPressed(int p1, int p2, int p3) {
        if (p1 == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        } else if (p1 == 258) {
            boolean flag = !hasShiftDown();
            if (!this.changeFocus(flag)) {
                this.changeFocus(flag);
            }

            return false;
        } else {
            return this.getFocused() != null && this.getFocused().keyPressed(p1, p2, p3);
        }
    }

    @Override
    public boolean mouseScrolled(double p_95581_, double p_95582_, double p_95583_) {
        if (p_95583_ > 1.0D) {
            p_95583_ = 1.0D;
        }

        if (p_95583_ < -1.0D) {
            p_95583_ = -1.0D;
        }

        if (this.commandSuggestions.mouseScrolled(p_95583_)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                p_95583_ *= 7.0D;
            }

            this.minecraft.gui.getChat().scrollChat(p_95583_);
            return true;
        }
    }

    @Override
    public boolean mouseClicked(double p_95585_, double p_95586_, int p_95587_) {
        if (this.commandSuggestions.mouseClicked((double) ((int) p_95585_), (double) ((int) p_95586_), p_95587_)) {
            return true;
        } else {
            if (p_95587_ == 0) {
                ChatComponent chatcomponent = this.minecraft.gui.getChat();
                if (chatcomponent.handleChatQueueClicked(p_95585_, p_95586_)) {
                    return true;
                }

                Style style = chatcomponent.getClickedComponentStyleAt(p_95585_, p_95586_);
                if (style != null && this.handleComponentClicked(style)) {
                    return true;
                }
            }

            if (this.input.mouseClicked(p_95585_, p_95586_, p_95587_)) {
                return true;
            } else {
                for (GuiEventListener guieventlistener : this.children()) {
                    if (guieventlistener.mouseClicked(p_95585_, p_95586_, p_95587_)) {
                        this.setFocused(guieventlistener);
                        if (p_95587_ == 0) {
                            this.setDragging(true);
                        }

                        return true;
                    }
                }

                return false;
            }
        }
    }

    @Override
    public void moveInHistory(int p_95589_) {
        int i = this.historyPos + p_95589_;
        int j = this.minecraft.gui.getChat().getRecentChat().size();
        i = Mth.clamp(i, 0, j);
        if (i != this.historyPos) {
            if (i == j) {
                this.historyPos = j;
                this.input.setValue(this.historyBuffer);
            } else {
                if (this.historyPos == j) {
                    this.historyBuffer = this.input.getValue();
                }

                this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(i));
                this.commandSuggestions.setAllowSuggestions(false);
                this.historyPos = i;
            }
        }
    }

    @Override
    public void render(PoseStack p1, int p2, int p3, float p4) {
        this.setFocused(this.input);
        this.input.setFocus(true);
        fill(p1, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(p1, p2, p3, p4);
        this.commandSuggestions.render(p1, p2, p3);
        Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt((double) p2, (double) p3);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(p1, style, p2, p3);
        }

        for (Widget widget : this.renderables) {
            widget.render(p1, p2, p3, p4);
        }
    }

    private void setChatLine(String p_95613_) {
        this.input.setValue(p_95613_);
    }
}

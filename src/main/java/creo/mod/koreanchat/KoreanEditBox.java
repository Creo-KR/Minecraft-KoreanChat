package creo.mod.koreanchat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class KoreanEditBox extends EditBox {
    private final Font font;
    private String value = "";
    private int maxLength = 32;
    private int frame;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private boolean shiftPressed;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = 14737632;
    private int textColorUneditable = 7368816;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (p_94147_, p_94148_) -> {
        return FormattedCharSequence.forward(p_94147_, Style.EMPTY);
    };

    public KoreanEditBox(Font p1, int p2, int p3, int p4, int p5, Component p6) {
        this(p1, p2, p3, p4, p5, null, p6);
    }

    public KoreanEditBox(Font p1, int p2, int p3, int p4, int p5, @Nullable EditBox p6, Component p7) {
        super(p1, p2, p3, p4, p5, p6, p7);
        this.font = p1;
        if (p6 != null) {
            this.setValue(p6.getValue());
        }
    }

    public void deleteChars(int p_94181_) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(p_94181_);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j > 0 && this.value.charAt(j - 1) == KoreanChat.colorChar) {
                    j--;
                }
                if (k > 0 && this.value.charAt(k - 1) == KoreanChat.colorChar) {
                    k++;
                }
                if (j != k) {
                    String s = (new StringBuilder(this.value)).delete(j, k).toString();
                    if (this.filter.test(s)) {
                        this.value = s;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }


    @Override
    public boolean keyPressed(int p1, int p2, int p3) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(p1)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(p1)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(p1)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(p1)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {

                /**
                 * Korean Chat
                 */
                if ((p1 > 64 && p1 < 91)) {
                    // A~Z
                } else if (p1 != 259 && p1 != 340 && p1 != 344) {
                    // backspace Lshift Rshift
                    Converter.resetStatus();
                }

                switch (p1) {
                    case 259: // backspace
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            if (Converter.lastTyped.length() > 0) {
                                this.deleteText(-1);
                                this.insertText(Converter.deleteInput());
                            } else
                                this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }
                        return true;
                    case 260: // insert
                        KoreanChat.isEffect = !KoreanChat.isEffect;
                        return true;
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                    case 341:
                        KoreanChat.korean = !KoreanChat.korean;
                        return true;
                }
            }
        }
    }

    public boolean charTyped(char p1, int p2) {
        if (this.canConsumeInput()) {
            if (KoreanChat.isEffect) {
                KoreanChat.isEffect = false;
                String color = "1234567890abcdefklmnor";
                if (color.indexOf(p1) != -1)
                    this.insertText("" + KoreanChat.colorChar + p1);
                return true;
            }
            if (SharedConstants.isAllowedChatCharacter(p1)) {
                if (this.isEditable) {
                    String input = Character.toString(p1);
                    String convert = null;
                    if (KoreanChat.korean) {
                        switch (Converter.typed) {
                            case 4:
                                if ((convert = Converter.getComplexVowel(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(convert);
                                    return true;
                                }
                                if ((convert = Converter.getInital(input)) != null) {
                                    this.insertText(convert);
                                    return true;
                                }
                            case 3:
                                if ((convert = Converter.getVowel(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(Converter.getCurrentInput());
                                    Converter.lastTyped = Converter.lastTyped
                                            .substring(Converter.lastTyped.length() - 2);
                                    return true;
                                }
                                if ((convert = Converter.getComplexConsonant(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(Converter.getCurrentInput());
                                    return true;
                                }
                                if ((convert = Converter.getInital(input)) != null) {
                                    this.insertText(convert);
                                    return true;
                                }
                            case 2:
                                if ((convert = Converter.getComplexVowel(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(Converter.getCurrentInput());
                                    return true;
                                }
                                if ((convert = Converter.getComplexConsonant(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(Converter.getCurrentInput());
                                    return true;
                                }
                                if ((convert = Converter.getFinalConsonant(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(Converter.getCurrentInput());
                                    return true;
                                }
                                if ((convert = Converter.getInital(input)) != null) {
                                    this.insertText(convert);
                                    return true;
                                }
                            case 1:
                                if ((convert = Converter.getComplexConsonant(input)) != null) {
                                    this.deleteChars(-1);
                                    this.insertText(convert);
                                    return true;
                                }
                                if ((convert = Converter.getVowel(input)) != null) {
                                    if (Converter.lastTyped.length() > 2) {
                                        this.deleteChars(-1);
                                        String o = Converter.lastTyped.charAt(0) + "";
                                        String n = Converter.lastTyped.substring(1);
                                        this.insertText(Converter.getInital(o));
                                        Converter.lastTyped = n;
                                        this.insertText(Converter.getCurrentInput());
                                        return true;
                                    } else {
                                        this.deleteChars(-1);
                                        this.insertText(Converter.getCurrentInput());
                                        return true;
                                    }
                                }

                            default:
                                if ((convert = Converter.getInital(input)) != null) {
                                    this.insertText(convert);
                                    return true;
                                }
                        }
                        this.insertText(input);
                    } else
                        this.insertText(input);
                }
                return true;
            }
        }

        return false;
    }

    public boolean mouseClicked(double p_94125_, double p_94126_, int p_94127_) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean flag = p_94125_ >= (double) this.x && p_94125_ < (double) (this.x + this.width) && p_94126_ >= (double) this.y && p_94126_ < (double) (this.y + this.height);
            if (this.canLoseFocus) {
                this.setFocus(flag);
            }

            if (this.isFocused() && flag && p_94127_ == 0) {
                int i = Mth.floor(p_94125_) - this.x;
                if (this.bordered) {
                    i -= 4;
                }

                String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                String[] t1 = this.font.plainSubstrByWidth(s, i).split(KoreanChat.colorChar+"");
                int t2 = (t1.length - 1) * 2;
                this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + t2 + this.displayPos);
                return true;
            } else {
                return false;
            }
        }
    }

    public void renderButton(PoseStack p_94160_, int p_94161_, int p_94162_, float p_94163_) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                int i = this.isFocused() ? -1 : -6250336;
                fill(p_94160_, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
                fill(p_94160_, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            /**
             * Korean Chat
             */
            drawKoreanState(p_94160_);
            drawStringEffect(p_94160_);

            int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;

            String convText = this.value.replace(KoreanChat.colorChar, '§');
            String s = this.font.plainSubstrByWidth(convText.substring(this.displayPos), this.getInnerWidth());

            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int l = this.bordered ? this.x + 4 : this.x;
            int i1 = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                FormattedCharSequence formattedCharSequence = this.formatter.apply(s1, this.displayPos);
                j1 = this.font.drawShadow(p_94160_, s1, (float) l, (float) i1, i2);
            }

            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                FormattedCharSequence formattedCharSequence = this.formatter.apply(s.substring(j), this.cursorPos);
                String[] t1 = s.substring(0, j).split("§");
                String prevStyle = "";

                for (int i = 1; i < t1.length; i++) {
                    prevStyle += "§";
                    prevStyle += t1[i].charAt(0);
                }

                if (j > 0 && s.charAt(j - 1) == '§') {
                    prevStyle += "§";
                }

                this.font.drawShadow(p_94160_, prevStyle + s.substring(j), (float) j1, (float) i1, i2);
            }

            if (!flag2 && this.suggestion != null) {
                this.font.drawShadow(p_94160_, this.suggestion, (float) (k1 - 1), (float) i1, -8355712);
            }

            if (flag1) {
                if (flag2) {
                    GuiComponent.fill(p_94160_, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                } else {
                    this.font.drawShadow(p_94160_, "_", (float) k1, (float) i1, i2);
                }
            }

            if (k != j) {
                int l1 = l + this.font.width(s.substring(0, k));
                this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }

        }
    }

    public boolean changeFocus(boolean p_94172_) {
        return this.visible && this.isEditable ? superChangeFocus(p_94172_) : false;
    }

    private boolean superChangeFocus(boolean p1) {
        if (this.active && this.visible) {
            this.setFocused(!this.isFocused());
            this.onFocusedChanged(this.isFocused());
            return this.isFocused();
        } else {
            return false;
        }
    }

    private void drawKoreanState(PoseStack p1) {
        fill(p1, this.x - 3, this.y - 2 - height, this.x + 10, this.y - 2, 0x22FF0000);
        fill(p1, this.x - 2, this.y - 1 - height, this.x + 9, this.y - 3, Integer.MIN_VALUE);

        this.font.draw(p1, KoreanChat.korean ? "한" : "영", x, y - height, 0xFFFFFF);
    }

    private void drawStringEffect(PoseStack p1) {
        if (!KoreanChat.isEffect)
            return;

        fill(p1, this.x - 3 + 14, this.y - 2 - height, this.x + 10 + 158, this.y - 2, 0xAA44FFFF);
        fill(p1, this.x - 2 + 14, this.y - 1 - height, this.x + 9 + 158, this.y - 3, 0xAA333333);

        char c = 167;
        String color = "1234567890abcdefklmnor";
        for (int i = 0; i < color.length(); i++)
            this.font.draw(p1, String.valueOf(c) + color.charAt(i) + color.charAt(i),
                    x + 14 + 7 * i, y - height, 0xFF88CC);
    }


    ///////////////////////////////////


    public void setResponder(Consumer<String> p_94152_) {
        this.responder = p_94152_;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> p_94150_) {
        this.formatter = p_94150_;
    }

    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return new TranslatableComponent("gui.narrate.editBox", component, this.value);
    }

    public void setValue(String p_94145_) {
        if (this.filter.test(p_94145_)) {
            if (p_94145_.length() > this.maxLength) {
                this.value = p_94145_.substring(0, this.maxLength);
            } else {
                this.value = p_94145_;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(p_94145_);
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public void setFilter(Predicate<String> p_94154_) {
        this.filter = p_94154_;
    }

    public void insertText(String p_94165_) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        String s = SharedConstants.filterText(p_94165_);
        int l = s.length();
        if (k < l) {
            s = s.substring(0, k);
            l = k;
        }

        String s1 = (new StringBuilder(this.value)).replace(i, j, s).toString();
        if (this.filter.test(s1)) {
            this.value = s1;
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String p_94175_) {
        if (this.responder != null) {
            this.responder.accept(p_94175_);
        }
    }

    private void deleteText(int p_94218_) {
        if (Screen.hasControlDown()) {
            this.deleteWords(p_94218_);
        } else {
            this.deleteChars(p_94218_);
        }
    }

    public void deleteWords(int p_94177_) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(p_94177_) - this.cursorPos);
            }
        }
    }

    public int getWordPosition(int p_94185_) {
        return this.getWordPosition(p_94185_, this.getCursorPosition());
    }

    private int getWordPosition(int p_94129_, int p_94130_) {
        return this.getWordPosition(p_94129_, p_94130_, true);
    }

    private int getWordPosition(int p_94141_, int p_94142_, boolean p_94143_) {
        int i = p_94142_;
        boolean flag = p_94141_ < 0;
        int j = Math.abs(p_94141_);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (p_94143_ && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (p_94143_ && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int p_94189_) {
        this.moveCursorTo(this.getCursorPos(p_94189_));
    }

    private int getCursorPos(int p_94221_) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, p_94221_);
    }

    public void moveCursorTo(int p_94193_) {
        this.setCursorPosition(p_94193_);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int p_94197_) {
        this.cursorPos = Mth.clamp(p_94197_, 0, this.value.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public void setFocus(boolean p_94179_) {
        this.setFocused(p_94179_);
    }

    private void renderHighlight(int p_94136_, int p_94137_, int p_94138_, int p_94139_) {
        if (p_94136_ < p_94138_) {
            int i = p_94136_;
            p_94136_ = p_94138_;
            p_94138_ = i;
        }

        if (p_94137_ < p_94139_) {
            int j = p_94137_;
            p_94137_ = p_94139_;
            p_94139_ = j;
        }

        if (p_94138_ > this.x + this.width) {
            p_94138_ = this.x + this.width;
        }

        if (p_94136_ > this.x + this.width) {
            p_94136_ = this.x + this.width;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex((double) p_94136_, (double) p_94139_, 0.0D).endVertex();
        bufferbuilder.vertex((double) p_94138_, (double) p_94139_, 0.0D).endVertex();
        bufferbuilder.vertex((double) p_94138_, (double) p_94137_, 0.0D).endVertex();
        bufferbuilder.vertex((double) p_94136_, (double) p_94137_, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    public void setMaxLength(int p_94200_) {
        this.maxLength = p_94200_;
        if (this.value.length() > p_94200_) {
            this.value = this.value.substring(0, p_94200_);
            this.onValueChange(this.value);
        }

    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean p_94183_) {
        this.bordered = p_94183_;
    }

    public void setTextColor(int p_94203_) {
        this.textColor = p_94203_;
    }

    public void setTextColorUneditable(int p_94206_) {
        this.textColorUneditable = p_94206_;
    }

    public boolean isMouseOver(double p_94157_, double p_94158_) {
        return this.visible && p_94157_ >= (double) this.x && p_94157_ < (double) (this.x + this.width) && p_94158_ >= (double) this.y && p_94158_ < (double) (this.y + this.height);
    }

    protected void onFocusedChanged(boolean p_94170_) {
        if (p_94170_) {
            this.frame = 0;
        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean p_94187_) {
        this.isEditable = p_94187_;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int p_94209_) {
        int i = this.value.length();
        this.highlightPos = Mth.clamp(p_94209_, 0, i);
        if (this.font != null) {
            if (this.displayPos > i) {
                this.displayPos = i;
            }

            int j = this.getInnerWidth();
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), j);
            int k = s.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, j, true).length();
            }

            if (this.highlightPos > k) {
                this.displayPos += this.highlightPos - k;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, i);
        }

    }

    public void setCanLoseFocus(boolean p_94191_) {
        this.canLoseFocus = p_94191_;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean p_94195_) {
        this.visible = p_94195_;
    }

    public void setSuggestion(@Nullable String p_94168_) {
        this.suggestion = p_94168_;
    }

    public int getScreenX(int p_94212_) {
        return p_94212_ > this.value.length() ? this.x : this.x + this.font.width(this.value.substring(0, p_94212_));
    }

    public void setX(int p_94215_) {
        this.x = p_94215_;
    }

    public void updateNarration(NarrationElementOutput p_169009_) {
        p_169009_.add(NarratedElementType.TITLE, new TranslatableComponent("narration.edit_box", this.getValue()));
    }
}

package creo.mod.koreanchat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class KoreanEditBox extends EditBox {

    public KoreanEditBox(Font p1, int p2, int p3, int p4, int p5, Component p6) {
        super(p1, p2, p3, p4, p5, p6);
    }

    public KoreanEditBox(Font p1, int p2, int p3, int p4, int p5, @Nullable EditBox p6, Component p7) {
        super(p1, p2, p3, p4, p5, p6, p7);
    }
}

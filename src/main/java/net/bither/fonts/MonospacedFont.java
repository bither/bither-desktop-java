package net.bither.fonts;

import com.google.common.base.Preconditions;
import net.bither.exception.UIException;

import java.awt.*;
import java.io.InputStream;

/**
 * Created by songchenwen on 14/12/18.
 */
public class MonospacedFont {
    private static Font F;

    static {

        try {
            InputStream in = AwesomeDecorator.class.getResourceAsStream("/fonts/DroidSansMono.ttf");
            F = Font.createFont(Font.TRUETYPE_FONT, in);

            Preconditions.checkNotNull(F, "Font Awesome not loaded");

        } catch (Exception e) {
            throw new UIException(e);
        }
    }

    public static Font instance() {
        return F;
    }

    public static Font fontWithSize(float size) {
        return F.deriveFont(size);
    }
}

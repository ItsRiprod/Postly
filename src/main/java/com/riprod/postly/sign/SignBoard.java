package com.riprod.postly.sign;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record SignBoard(@Nonnull String board, @Nonnull String outline, @Nonnull String bevel) {

    private static final double MAX_LUMINANCE = 105;
    private static final double OUTLINE_SHADE = 0.55;
    private static final double BEVEL_SHADE = 1.4;

    @Nullable
    public static SignBoard of(@Nonnull BlockType blockType) {
        final var color = blockType.getTextureComputedColor();
        if (color == null) return null;

        final int red = color.red & 0xFF;
        final int green = color.green & 0xFF;
        final int blue = color.blue & 0xFF;

        final double luminance = 0.299 * red + 0.587 * green + 0.114 * blue;
        final double scale = luminance > MAX_LUMINANCE ? MAX_LUMINANCE / luminance : 1;

        return new SignBoard(
            shade(red, green, blue, scale),
            shade(red, green, blue, scale * OUTLINE_SHADE),
            shade(red, green, blue, scale * BEVEL_SHADE));
    }

    public void apply(@Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.setObject("#Board.Background", new PatchStyle().setColor(Value.of(board)));
        commandBuilder.set("#Board.OutlineColor", outline);
        commandBuilder.set("#Bevel.OutlineColor", bevel);
    }

    @Nonnull
    private static String shade(int red, int green, int blue, double scale) {
        return ColorParseUtil.toHexString(channel(red, scale), channel(green, scale), channel(blue, scale));
    }

    private static byte channel(int value, double scale) {
        return (byte) Math.min(255, Math.round(value * scale));
    }
}

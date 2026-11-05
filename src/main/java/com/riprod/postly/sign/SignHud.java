package com.riprod.postly.sign;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class SignHud extends CustomUIHud {

    @Nonnull
    public static final String KEY = "Postly.Sign";

    @Nullable
    private String text;

    @Nullable
    private String blockId;

    @Nullable
    private SignBoard board;

    public SignHud(@Nonnull PlayerRef playerRef) {
        super(playerRef, KEY);
    }

    public boolean track(@Nullable SignSight.Sighting sighting) {
        final var nextText = sighting == null ? null : sighting.signText().getText();
        final var nextBlockId = sighting == null ? null : sighting.blockType().getId();
        if (Objects.equals(text, nextText) && Objects.equals(blockId, nextBlockId)) return false;

        text = nextText;
        blockId = nextBlockId;
        board = sighting == null ? null : SignBoard.of(sighting.blockType());
        return true;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder commandBuilder) {
        if (text == null) return;

        commandBuilder.append("Hud/Postly/SignHud.ui");
        commandBuilder.set("#Text.Text", text);
        if (board != null) {
            board.apply(commandBuilder);
        }
    }
}

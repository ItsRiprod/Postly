package com.riprod.postly.sign;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.riprod.postly.PostlyPlugin;

import javax.annotation.Nonnull;

public class SignText implements Component<ChunkStore> {

    @Nonnull
    public static final BuilderCodec<SignText> CODEC = BuilderCodec.builder(SignText.class, SignText::new)
        .append(
            new KeyedCodec<>("Text", Codec.STRING),
            (sign, text) -> sign.text = text,
            sign -> sign.text
        )
        .add()
        .build();

    @Nonnull
    private String text = "";

    public SignText() {
    }

    private SignText(@Nonnull String text) {
        this.text = text;
    }

    @Nonnull
    public static ComponentType<ChunkStore, SignText> getComponentType() {
        return PostlyPlugin.get().getSignTextComponentType();
    }

    @Nonnull
    public String getText() {
        return text;
    }

    public void setText(@Nonnull String text) {
        this.text = text;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        return new SignText(text);
    }
}

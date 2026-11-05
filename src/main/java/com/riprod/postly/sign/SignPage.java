package com.riprod.postly.sign;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SignPage extends InteractiveCustomUIPage<SignPage.PageData> {

    private static final int MAX_TEXT_LENGTH = 1024;
    private static final String WRITTEN_STATE = "Written";

    @Nonnull
    private final Ref<ChunkStore> blockRef;

    @Nullable
    private final BlockType blockType;

    public SignPage(@Nonnull PlayerRef playerRef, @Nonnull Ref<ChunkStore> blockRef, @Nullable BlockType blockType) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageData.CODEC);
        this.blockRef = blockRef;
        this.blockType = blockType;
    }

    @Nonnull
    public static SignPage tryCreate(@Nonnull PlayerRef playerRef, @Nonnull Ref<ChunkStore> blockRef) {
        blockRef.getStore().ensureComponent(blockRef, SignText.getComponentType());
        return new SignPage(playerRef, blockRef, resolveBlockType(blockRef));
    }

    @Nullable
    private static BlockType resolveBlockType(@Nonnull Ref<ChunkStore> blockRef) {
        final var chunkStore = blockRef.getStore();

        final var info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info == null) return null;

        final var blockSection = chunkStore.getComponent(info.getSectionRef(), BlockSection.getComponentType());
        if (blockSection == null) return null;

        return BlockType.getAssetMap().getAsset(blockSection.get(info.getIndex()));
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/Postly/SignPage.ui");

        if (blockType != null) {
            final var item = blockType.getItem();
            final var translations = item == null ? null : item.getTranslationProperties();
            if (translations != null && translations.getName() != null) {
                commandBuilder.set("#PageTitle.Text", Message.translation(translations.getName()));
            }

            final var board = SignBoard.of(blockType);
            if (board != null) {
                board.apply(commandBuilder);
            }
        }

        final var signText = blockRef.getStore().getComponent(blockRef, SignText.getComponentType());
        commandBuilder.set("#TextInput.Value", signText == null ? "" : signText.getText());

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", new EventData()
            .append(PageData.TEXT, "#TextInput.Value"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageData data) {
        if (blockRef.isValid()) {
            save(data.text);
        }
        close();
    }

    private void save(@Nonnull String text) {
        final var chunkStore = blockRef.getStore();

        final var signText = chunkStore.ensureAndGetComponent(blockRef, SignText.getComponentType());
        signText.setText(text.codePointCount(0, text.length()) > MAX_TEXT_LENGTH
            ? text.substring(0, text.offsetByCodePoints(0, MAX_TEXT_LENGTH))
            : text);

        final var info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info != null) {
            info.markNeedsSaving();
            applyWrittenState(info, !signText.getText().isBlank());
        }
    }

    private void applyWrittenState(@Nonnull BlockModule.BlockStateInfo info, boolean written) {
        final var currentType = resolveBlockType(blockRef);
        if (currentType == null) return;

        final var targetState = written ? WRITTEN_STATE : StateData.NULL_STATE_ID;
        final var currentState = currentType.getCurrentInteractionState();
        if (targetState.equals(currentState == null ? StateData.NULL_STATE_ID : currentState)) return;

        final var chunkStore = blockRef.getStore();
        final var position = new Vector3i();
        if (!info.fillWorldPos(chunkStore, position)) return;

        BlockOperations.setBlockInteractionState(chunkStore.getExternalData(), info.getSectionRef(),
            position.x, position.y, position.z, currentType, targetState, false);
    }

    public static class PageData {
        static final String TEXT = "@Text";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
            .append(
                new KeyedCodec<>(TEXT, Codec.STRING),
                (data, text) -> data.text = text,
                data -> data.text
            )
            .addValidator(Validators.nonNull())
            .add()
            .build();

        private String text;
    }
}

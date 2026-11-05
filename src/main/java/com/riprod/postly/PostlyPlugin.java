package com.riprod.postly;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.riprod.patchly.PatchManager;
import com.riprod.postly.sign.SignHudSystem;
import com.riprod.postly.sign.SignPage;
import com.riprod.postly.sign.SignText;

import javax.annotation.Nonnull;

public final class PostlyPlugin extends JavaPlugin {

    private static PostlyPlugin instance;

    private final PatchManager patchManager;

    private ComponentType<ChunkStore, SignText> signTextComponentType;

    public PostlyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this);
    }

    public static PostlyPlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;
        patchManager.install();

        signTextComponentType = getChunkStoreRegistry().registerComponent(SignText.class, "PostlySignText", SignText.CODEC);

        OpenCustomUIInteraction.registerBlockEntityCustomPage(
            this, SignPage.class, "PostlySign", SignPage::tryCreate, ChunkStore.REGISTRY::newHolder);

        getEntityStoreRegistry().registerSystem(new SignHudSystem());
    }

    public ComponentType<ChunkStore, SignText> getSignTextComponentType() {
        return signTextComponentType;
    }
}

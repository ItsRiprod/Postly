package com.riprod.postly.sign;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SignHudSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query = Query.and(Player.getComponentType(), PlayerRef.getComponentType());

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        final var player = archetypeChunk.getComponent(index, Player.getComponentType());
        final var playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        final var sighting = player.getPageManager().getCustomPage() == null
            ? SignSight.find(archetypeChunk.getReferenceTo(index), store)
            : null;

        final var hudManager = player.getHudManager();
        if (hudManager.getCustomHud(SignHud.KEY) instanceof SignHud hud) {
            if (hud.track(sighting)) {
                hud.show();
            }
            return;
        }

        final var hud = new SignHud(playerRef);
        hud.track(sighting);
        hudManager.addCustomHud(playerRef, hud);
    }
}

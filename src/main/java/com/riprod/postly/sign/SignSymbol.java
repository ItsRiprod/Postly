package com.riprod.postly.sign;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSpawner;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record SignSymbol(@Nonnull String state, @Nonnull String name, @Nullable String iconPath) {

    public static final String STATE_PREFIX = "Written_";
    public static final String DEFAULT_STATE = STATE_PREFIX + "Exclamation";

    @Nonnull
    public static List<SignSymbol> of(@Nonnull BlockType blockType) {
        final var symbols = new ArrayList<SignSymbol>();
        final var stateData = blockType.getState();
        final var stateNames = stateData == null ? null : stateData.getStateNames();
        if (stateNames == null) return symbols;

        for (final var state : stateNames.stream().filter(name -> name.startsWith(STATE_PREFIX)).sorted().toList()) {
            final var stateType = blockType.getBlockForState(state);
            if (stateType == null) continue;
            symbols.add(new SignSymbol(state, state.substring(STATE_PREFIX.length()), iconPath(stateType)));
        }
        return symbols;
    }

    @Nonnull
    public static String select(@Nonnull List<SignSymbol> symbols, @Nullable String state) {
        for (final var symbol : symbols) {
            if (symbol.state().equals(state)) return state;
        }
        for (final var symbol : symbols) {
            if (symbol.state().equals(DEFAULT_STATE)) return DEFAULT_STATE;
        }
        return symbols.isEmpty() ? DEFAULT_STATE : symbols.getFirst().state();
    }

    @Nullable
    private static String iconPath(@Nonnull BlockType stateType) {
        final var particles = stateType.getParticles();
        if (particles == null || particles.length == 0) return null;

        final var system = ParticleSystem.getAssetMap().getAsset(particles[0].getSystemId());
        if (system == null || system.getSpawners() == null || system.getSpawners().length == 0) return null;

        final var spawner = ParticleSpawner.getAssetMap().getAsset(system.getSpawners()[0].getSpawnerId());
        if (spawner == null || spawner.getParticle() == null) return null;

        return spawner.getParticle().getTexture();
    }
}

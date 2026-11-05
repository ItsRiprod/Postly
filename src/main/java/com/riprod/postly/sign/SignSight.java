package com.riprod.postly.sign;

import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.iterator.BlockIterator;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector2d;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SignSight {

    private SignSight() {
    }

    public record Sighting(@Nonnull SignText signText, @Nonnull BlockType blockType) {
    }

    @Nullable
    public static Sighting find(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
        final var world = accessor.getExternalData().getWorld();
        final var look = TargetUtil.getLook(ref, accessor);
        final var origin = look.getPosition();
        final var direction = look.getDirection();

        final var probe = new Probe(world, origin, direction);
        BlockIterator.iterate(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, InteractionManager.MAX_REACH_DISTANCE, probe);

        if (probe.blockType == null) return null;

        final var signText = BlockModule.getComponent(SignText.getComponentType(), world, probe.baseX, probe.baseY, probe.baseZ);
        if (signText == null || signText.getText().isBlank()) return null;

        return new Sighting(signText, probe.blockType);
    }

    private static final class Probe implements BlockIterator.BlockIteratorProcedure {

        @Nonnull
        private final World world;

        @Nonnull
        private final Vector3d origin;

        @Nonnull
        private final Vector3d direction;

        @Nonnull
        private final Vector2d minMax = new Vector2d();

        @Nullable
        private BlockType blockType;

        private int baseX;
        private int baseY;
        private int baseZ;

        private Probe(@Nonnull World world, @Nonnull Vector3d origin, @Nonnull Vector3d direction) {
            this.world = world;
            this.origin = origin;
            this.direction = direction;
        }

        @Override
        public boolean accept(int x, int y, int z, double px, double py, double pz, double qx, double qy, double qz) {
            final var chunkStore = world.getChunkStore();

            final var sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
            if (sectionRef == null || !sectionRef.isValid()) return false;

            final var blockSection = chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
            if (blockSection == null) return false;

            final int blockId = blockSection.get(x, y, z);
            if (blockId == BlockType.EMPTY_ID) return true;

            final var type = BlockType.getAssetMap().getAsset(blockId);
            if (type == null) return true;

            final int filler = blockSection.getFiller(x, y, z);
            final int originX = x - FillerBlockUtil.unpackX(filler);
            final int originY = y - FillerBlockUtil.unpackY(filler);
            final int originZ = z - FillerBlockUtil.unpackZ(filler);

            final int hitboxIndex = type.getInteractionHitboxTypeIndex() == AssetMapWithIndexes.NOT_FOUND
                ? type.getHitboxTypeIndex()
                : type.getInteractionHitboxTypeIndex();
            final var hitbox = BlockBoundingBoxes.getAssetMap().getAsset(hitboxIndex);
            if (hitbox == null) return true;

            for (final var box : hitbox.get(blockSection.getRotationIndex(x, y, z)).getDetailBoxes()) {
                if (CollisionMath.intersectRayAABB(origin, direction, originX, originY, originZ, box, minMax)
                    && minMax.x <= InteractionManager.MAX_REACH_DISTANCE) {
                    blockType = type;
                    baseX = originX;
                    baseY = originY;
                    baseZ = originZ;
                    return false;
                }
            }
            return true;
        }
    }
}

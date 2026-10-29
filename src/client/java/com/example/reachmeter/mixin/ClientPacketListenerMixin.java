package com.example.reachmeter.mixin;

import com.example.reachmeter.ReachStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    private void reachmeter$onDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread() || mc.player == null || mc.level == null) return;

        // Only hits on ME
        if (packet.entityId() != mc.player.getId()) return;

        // Only direct melee hits (not arrows, tridents, etc.)
        int causeId = packet.sourceCauseId();
        if (causeId < 0 || causeId != packet.sourceDirectId()) return;

        Entity cause = mc.level.getEntity(causeId);
        if (!(cause instanceof Player attacker) || attacker == mc.player) return;

        // Distance from the attacker's eyes to the closest point of MY hitbox
        // (this is how the game measures reach)
        Vec3 eye = attacker.getEyePosition();
        AABB box = mc.player.getBoundingBox();
        double cx = Mth.clamp(eye.x, box.minX, box.maxX);
        double cy = Mth.clamp(eye.y, box.minY, box.maxY);
        double cz = Mth.clamp(eye.z, box.minZ, box.maxZ);
        double distance = eye.distanceTo(new Vec3(cx, cy, cz));

        ReachStats.record(attacker.getName().getString(), distance);
    }
}

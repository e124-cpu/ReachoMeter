package com.example.reachmeter.mixin;

import com.example.reachmeter.ReachHistory;
import com.example.reachmeter.ReachStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
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

        if (packet.entityId() != mc.player.getId()) return;

        int causeId = packet.sourceCauseId();
        if (causeId < 0 || causeId != packet.sourceDirectId()) return;

        Entity cause = mc.level.getEntity(causeId);
        if (!(cause instanceof Player attacker) || attacker == mc.player) return;

        Vec3 eye = attacker.getEyePosition();

        double raw = distanceToBox(eye, mc.player.getBoundingBox());

        int lagTicks = 0;
        ClientPacketListener connection = (ClientPacketListener) (Object) this;
        PlayerInfo info = connection.getPlayerInfo(mc.player.getUUID());
        if (info != null) {
            lagTicks = Mth.clamp(Math.round(info.getLatency() / 50.0f), 0, 10);
        }
        AABB myBox = ReachHistory.get(lagTicks, mc.player.getBoundingBox());
        double compensated = distanceToBox(eye, myBox);

        ReachStats.record(attacker.getName().getString(), compensated, raw, lagTicks);
    }

    private static double distanceToBox(Vec3 point, AABB box) {
        double cx = Mth.clamp(point.x, box.minX, box.maxX);
        double cy = Mth.clamp(point.y, box.minY, box.maxY);
        double cz = Mth.clamp(point.z, box.minZ, box.maxZ);
        return point.distanceTo(new Vec3(cx, cy, cz));
    }
}

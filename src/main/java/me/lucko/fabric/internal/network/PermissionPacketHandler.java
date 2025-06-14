package me.lucko.fabric.internal.network;

import me.lucko.fabric.FabricPermissionsApi;
import me.lucko.fabric.api.permissions.v0.PermissionCheckRequestEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PermissionPacketHandler {
    private static final Logger LOGGER = FabricPermissionsApi.LOGGER;
    private static final HashMap<UUID, CompletableFuture<TriState>> callbacks = new HashMap<>();

    /**
     * Sends a permission check packet to the server and registers a callback for the response.
     *
     * @param packet The permission check packet to send
     * @param callback The callback to execute when the server responds
     */
    public static void send(PermissionPacket.Check packet, CompletableFuture<TriState> callback) {
        try {
            if (FabricPermissionsApi.ENVIRONMENT != EnvType.CLIENT) {
                throw new IllegalStateException("Cannot send permission check packet from server");
            }

            UUID id = UUID.randomUUID();
            while (callbacks.containsKey(id)) id = UUID.randomUUID();

            PacketByteBuf buf = packet.build();
            buf.writeUuid(id);

            callbacks.put(id, callback);
            ClientPlayNetworking.send(packet.getChannel(), buf);
        } catch (IllegalStateException e) {
            LOGGER.error("Failed to send permission check packet: {}", e.getMessage());
        }
    }

    private static void handleServer(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf packet, PacketSender sender) {
        PermissionPacket.Check checkPacket = PermissionPacket.Check.from(packet);
        UUID id = packet.readUuid();

        TriState result = TriState.DEFAULT;
        if (checkPacket != null && checkPacket.permission() != null) {
            result = PermissionCheckRequestEvent.EVENT.invoker().onPermissionCheck(player, checkPacket.permission());
        }

        PermissionPacket.Response responsePacket = new PermissionPacket.Response(result);
        PacketByteBuf responseBuf = responsePacket.build().writeUuid(id);
        sender.sendPacket(responsePacket.getChannel(), responseBuf);
    }

    private static void handleClient(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf packet, PacketSender sender) {
        PermissionPacket.Response responsePacket = PermissionPacket.Response.from(packet);
        UUID id = packet.readUuid();
        CompletableFuture<TriState> callback = callbacks.remove(id);

        try {
            if (responsePacket == null) {
                LOGGER.error("Received null permission response packet");
                if (callback != null) {
                    callback.completeExceptionally(new IllegalArgumentException("Null permission response packet."));
                }
                return;
            } else if (callback == null) {
                LOGGER.warn("Received permission response for unknown request ID: {}", id);
                return;
            }

            TriState result = responsePacket.value();
            callback.complete(result);
        } catch (Exception e) {
            LOGGER.error("Exception handling permission response packet", e);
            if (callback != null && !callback.isDone()) {
                callback.completeExceptionally(e);
            }
        }

    }
    
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(PermissionPacket.Check.CHANNEL, PermissionPacketHandler::handleServer);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(PermissionPacket.Response.CHANNEL, PermissionPacketHandler::handleClient);
        }
    }
}

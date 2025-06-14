package me.lucko.fabric.internal.network;

import io.netty.handler.codec.DecoderException;
import me.lucko.fabric.FabricPermissionsApi;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PermissionPacket {
    /**
     * Represents a permission check request packet.
     *
     * @param permission The permission to check
     */
    public record Check(String permission) implements Packet {
        public static final Identifier CHANNEL = new Identifier("fabric-permissions-api", "permission_check");

        public PacketByteBuf build() {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(permission);
            return buf;
        }

        public static @Nullable PermissionPacket.Check from(PacketByteBuf buf) {
            try {
                String permission = buf.readString();
                return new Check(permission);
            } catch (DecoderException e) {
                FabricPermissionsApi.LOGGER.error("Failed to read permission check request packet", e);
                return null;
            }
        }

        @Override
        public Identifier getChannel() {
            return CHANNEL;
        }

        @Override
        public @NotNull String toString() {
            return "Check { permission='" + permission + "' }";
        }
    }

    /**
     * Represents a permission check response packet.
     *
     * @param value The result of the permission check
     */
    public record Response(TriState value) implements Packet {
        public static final Identifier CHANNEL = new Identifier("fabric-permissions-api", "permission_response");

        public PacketByteBuf build() {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeEnumConstant(value);
            return buf;
        }

        public static @Nullable Response from(PacketByteBuf buf) {
            try {
                TriState value = buf.readEnumConstant(TriState.class);
                return new Response(value);
            } catch (DecoderException e) {
                FabricPermissionsApi.LOGGER.error("Failed to read permission check response packet", e);
                return null;
            }
        }

        @Override
        public Identifier getChannel() {
            return CHANNEL;
        }

        @Override
        public @NotNull String toString() {
            return "Response { value=" + value + " }";
        }
    }
}

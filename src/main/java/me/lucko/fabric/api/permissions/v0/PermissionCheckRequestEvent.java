package me.lucko.fabric.api.permissions.v0;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PermissionCheckRequestEvent {

    Event<PermissionCheckRequestEvent> EVENT = EventFactory.createArrayBacked(PermissionCheckRequestEvent.class, (callbacks) -> (player, permission) -> {
        for (PermissionCheckRequestEvent callback : callbacks) {
            TriState state = callback.onPermissionCheck(player, permission);
            if (state != TriState.DEFAULT) {
                return state;
            }
        }
        return TriState.DEFAULT;
    });

    @NotNull TriState onPermissionCheck(@NotNull ServerPlayerEntity source, @NotNull String permission);
}

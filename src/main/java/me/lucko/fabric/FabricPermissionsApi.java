package me.lucko.fabric;

import me.lucko.fabric.internal.network.PermissionPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricPermissionsApi implements ModInitializer, ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Fabric-Permissions-API");
    public static final EnvType ENVIRONMENT = FabricLoader.getInstance().getEnvironmentType();

    @Override
    public void onInitialize() {
        PermissionPacketHandler.init();
        LOGGER.info("Fabric Permissions API initialized");
    }

    @Override
    public void onInitializeClient() {
        PermissionPacketHandler.init();
    }
}

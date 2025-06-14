package me.lucko.fabric.internal.network;

import net.minecraft.util.Identifier;

public interface Packet {
    Identifier getChannel();
}

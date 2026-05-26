package com.jimm.serverdoctor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

/**
 * Sends Adventure {@link Component} messages using the server's native API when available
 * (Paper / Spigot 1.21+), otherwise falls back to legacy {@code &} color codes.
 */
public final class AdventureMessages {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Method SEND_COMPONENT_METHOD = findSendComponentMethod();

    private AdventureMessages() {
    }

    private static Method findSendComponentMethod() {
        try {
            return CommandSender.class.getMethod("sendMessage", Component.class);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    public static void send(CommandSender sender, Component message) {
        if (SEND_COMPONENT_METHOD != null) {
            try {
                SEND_COMPONENT_METHOD.invoke(sender, message);
                return;
            } catch (ReflectiveOperationException ignored) {
                // Fall back to legacy text below
            }
        }
        sender.sendMessage(LEGACY.serialize(message));
    }
}

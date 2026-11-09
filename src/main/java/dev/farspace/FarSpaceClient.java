package dev.farspace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class FarSpaceClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        dev.farspace.mixin.ConditionalItemPropertiesAccessor.farspace$mapper().put(
                Identifier.fromNamespaceAndPath("farspace", "tether_has_oxygen"), TetherOxygenCondition.CODEC);
        dev.farspace.mixin.ConditionalItemPropertiesAccessor.farspace$mapper().put(
                Identifier.fromNamespaceAndPath("farspace", "tether_warning"), TetherWarningCondition.CODEC);
        TankRenderLayer.initialize();
        FirstPersonOxygenCable.initialize();
        TetherCableRenderer.initialize();
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("farspace", "oxygen"), (graphics, delta) -> {
            var p = Minecraft.getInstance().player;
            if (p == null || !Oxygen.onMoon(p) || p.isCreative() || p.isSpectator()) return;
            var slots = TankSlots.forPlayer(p);
            int row = 0;
            for (int i = 0; i < slots.getContainerSize(); i++) {
                var tank = slots.getItem(i);
                int capacity = Oxygen.capacity(tank);
                if (capacity == 0) continue;
                int seconds = Math.clamp(tank.getOrDefault(Oxygen.REMAINING, 0), 0, capacity);
                int x = graphics.guiWidth() / 2 - 181, y = graphics.guiHeight() - 36 - row++ * 10;
                graphics.fill(x, y, x + 80, y + 7, 0xB0000000);
                graphics.fill(x + 1, y + 1, x + 79, y + 6, 0xFF404040);
                graphics.fill(x + 1, y + 1, x + 1 + (int)(78L * seconds / capacity), y + 6, 0xFF42BFFF);
            }
        });
    }
}



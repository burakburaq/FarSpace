package dev.farspace;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import java.util.WeakHashMap;

public final class Oxygen {
    public static final DataComponentType<Integer> REMAINING = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            id("oxygen_seconds"), DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Item TANK_1 = tank("oxygen_tank_1", 12 * 60);
    public static final Item TANK_2 = tank("oxygen_tank_2", 24 * 60);
    public static final Item TANK_3 = tank("oxygen_tank_3", 48 * 60);
    private static final WeakHashMap<ServerPlayer, AirState> AIR = new WeakHashMap<>();
    private static final class AirState { int air, ticks, frozen; AirState(int air, int frozen) { this.air = air; this.frozen = frozen; } }
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath("farspace", name); }
    private static Item tank(String name, int seconds) {
        var key = ResourceKey.create(Registries.ITEM, id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, new OxygenTankItem(new Item.Properties().setId(key).stacksTo(1).component(REMAINING, seconds), seconds));
    }
    public static boolean onMoon(Player p) { return p.level().dimension().identifier().equals(id("moon")); }
    public static int capacity(ItemStack s) {
        return s.is(TANK_1) ? 720 : s.is(TANK_2) ? 1440 : s.is(TANK_3) ? 2880 : 0;
    }
    public static ItemStack activeTank(Player p) {
        var inv = TankSlots.forPlayer(p);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (capacity(stack) > 0 && stack.getOrDefault(REMAINING, 0) > 0) return stack;
        }
        return ItemStack.EMPTY;
    }
    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(TANK_1); entries.accept(TANK_2); entries.accept(TANK_3);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) { TankSlots.forPlayer(p).migrateLegacy(); tick(p); }
        });
    }
    private static void tick(ServerPlayer p) {
        if (!onMoon(p) || p.isCreative() || p.isSpectator() || !p.isAlive()) { OxygenNetwork.update(p, false); AIR.remove(p); return; }
        AirState state = AIR.computeIfAbsent(p, player -> new AirState(Math.max(0, player.getAirSupply()), player.getTicksFrozen()));
        boolean protectedBySuit = SpaceSuit.complete(p);
        if (!protectedBySuit) {
            state.frozen = Math.min(p.getTicksRequiredToFreeze(), Math.max(state.frozen, p.getTicksFrozen()) + 1);
            p.setTicksFrozen(state.frozen);
        } else {
            state.frozen = p.getTicksFrozen();
        }
        boolean supplied = OxygenNetwork.update(p, protectedBySuit);
        ItemStack tank = protectedBySuit && !supplied ? activeTank(p) : ItemStack.EMPTY;
        boolean second = ++state.ticks >= 20;
        if (second) state.ticks = 0;
        if (supplied || !tank.isEmpty()) {
            state.air = supplied ? Math.min(p.getMaxAirSupply(), state.air + 1) : p.getMaxAirSupply();
            if (second && !supplied) {
                tank.set(REMAINING, tank.getOrDefault(REMAINING, 0) - 1);
                TankSlots.forPlayer(p).setChanged();
            }
        } else {
            if (second) {
                if (state.air > 0) state.air = Math.max(0, state.air - (protectedBySuit ? 15 : 30));
                else p.hurtServer(p.level(), p.damageSources().generic(), 2.0f);
            }
        }
        p.setAirSupply(state.air);
    }
}




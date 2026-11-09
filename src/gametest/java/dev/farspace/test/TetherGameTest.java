package dev.farspace.test;

import dev.farspace.OxygenBlock;
import dev.farspace.OxygenNetwork;
import dev.farspace.TetherBlock;
import dev.farspace.TetherNode;
import dev.farspace.TetherOxygenCondition;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public final class TetherGameTest implements FabricClientGameTest {
    private static final BlockPos A = new BlockPos(0, 110, 0), B = A.offset(34, 0, 0), C = A.offset(68, 0, 0), D = A.offset(103, 0, 0);
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext ctx) {
        try (var world = ctx.worldBuilder().create()) {
            var server = world.getServer();
            ctx.waitTicks(30);
            server.runCommand("gamemode survival @a");
            server.runOnServer(s -> {
                var p=s.getPlayerList().getPlayers().getFirst(); var level=p.level();
                for (var pos : new BlockPos[]{A,B,C,D,A.offset(-4,0,0)}) level.getChunk(pos.getX() >> 4,pos.getZ() >> 4);
                level.setBlock(A.offset(-4,0,0), OxygenBlock.BLOCK.defaultBlockState(), 3);
                for(var pos:new BlockPos[]{A,B,C,D}) level.setBlock(pos,TetherBlock.BLOCK.defaultBlockState(),3);
            });
            ctx.waitTicks(25);
            server.runOnServer(s -> {
                var p=s.getPlayerList().getPlayers().getFirst(); var level=p.level();
                var a=(TetherNode)level.getBlockEntity(A); var b=(TetherNode)level.getBlockEntity(B);
                var c=(TetherNode)level.getBlockEntity(C); var d=(TetherNode)level.getBlockEntity(D);
                check(a.links().contains(B) && b.links().contains(A),"34-block edge missing");
                check(level.getBlockState(A).getValue(TetherBlock.CONNECTABLE),"connected tether should be blue");
                check(!level.getBlockState(D).getValue(TetherBlock.CONNECTABLE),"isolated tether should be gray");
                check(Math.abs(level.getBlockState(A).getShape(level,A).bounds().maxY-1.5)<1.0e-6,"tether is not 1.5 blocks tall");
                level.setBlock(D.offset(4,0,0), OxygenBlock.BLOCK.defaultBlockState(), 3);
                check(b.links().size()==2 && b.links().contains(C) && c.links().contains(B),"chain or two-connection limit broken");
                check(d.links().isEmpty(),"35-block edge incorrectly connected");
                check(OxygenNetwork.powered(level,C),"oxygen did not travel down chain");
                p.setPos(15.5,110.5,0.5); check(OxygenNetwork.update(p,true) && A.equals(OxygenNetwork.connection(p)),"initial tether failed");
                p.setPos(17.5,110.5,0.5); check(OxygenNetwork.update(p,true) && A.equals(OxygenNetwork.connection(p)),"connection did not persist in range");
                p.setPos(19.5,110.5,0.5); check(OxygenNetwork.update(p,true) && B.equals(OxygenNetwork.connection(p)),"did not switch after range");
                level.setBlock(B,Blocks.AIR.defaultBlockState(),3);
                p.teleportTo(D.getX()+0.5, D.getY()+0.5, D.getZ()+2.5); p.setNoGravity(true);
            });
            ctx.waitTicks(25);
            server.runOnServer(s -> {
                var level=s.getPlayerList().getPlayers().getFirst().level();
                check(level.getBlockState(D).getValue(TetherBlock.CONNECTABLE),"nearby oxygen block should turn tether blue");
                level.setBlock(D.offset(4,0,0),Blocks.AIR.defaultBlockState(),3);
            });
            ctx.waitTicks(25);
            server.runOnServer(s -> {
                var level=s.getPlayerList().getPlayers().getFirst().level();
                check(!level.getBlockState(D).getValue(TetherBlock.CONNECTABLE),"removed source should turn tether gray");
                check(!OxygenNetwork.powered(level,C),"oxygen still passes broken edge");
                check(((TetherNode)level.getBlockEntity(C)).links().isEmpty(),"broken edge was not removed");
                level.setBlock(D.offset(20,0,0),TetherBlock.BLOCK.defaultBlockState(),3);
            });
            ctx.waitTicks(25);
            server.runOnServer(s -> {
                var level=s.getPlayerList().getPlayers().getFirst().level();
                var d=(TetherNode)level.getBlockEntity(D); var e=(TetherNode)level.getBlockEntity(D.offset(20,0,0));
                check(d.links().contains(e.getBlockPos()),"unpowered pair did not connect");
                check(!level.getBlockState(D).getValue(TetherBlock.CONNECTABLE) && !level.getBlockState(e.getBlockPos()).getValue(TetherBlock.CONNECTABLE),"unpowered linked tethers should remain gray");
                level.setBlock(D.offset(4,0,0), OxygenBlock.BLOCK.defaultBlockState(),3);
            });
            ctx.waitTicks(25);
            server.runOnServer(s -> {
                var p=s.getPlayerList().getPlayers().getFirst();var level=p.level();
                check(level.getBlockState(D).getValue(TetherBlock.CONNECTABLE) && level.getBlockState(D.offset(20,0,0)).getValue(TetherBlock.CONNECTABLE),"oxygen indicator did not propagate");
                p.teleportTo(D.getX()+16.5,D.getY()+0.5,D.getZ()+0.5);
            });
            ctx.waitFor(c -> c.level.getBlockState(D.offset(20,0,0)).is(TetherBlock.BLOCK)
                    && c.level.getBlockState(D.offset(20,0,0)).getValue(TetherBlock.CONNECTABLE),100);
            ctx.runOnClient(c -> {
                var condition=new TetherOxygenCondition();var stack=new ItemStack(TetherBlock.ITEM);
                check(condition.get(stack,c.level,c.player,0,ItemDisplayContext.FIRST_PERSON_RIGHT_HAND),"held tether did not detect powered network");
                check(!condition.get(stack,c.level,c.player,0,ItemDisplayContext.GUI),"inventory model should stay neutral");
            });
            System.out.println("FARSPACE_TETHER_PASS");
        }
    }
}


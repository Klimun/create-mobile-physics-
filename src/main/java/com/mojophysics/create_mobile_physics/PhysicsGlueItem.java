package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Fizik tutkali: blogu hedef al, BFS ile araci bagla.
 * Create Super Glue ile yapini kur; sonra bu tutkal ile "ucusa hazir" kaydet.
 */
public class PhysicsGlueItem extends Item {
    public PhysicsGlueItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;

        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();

        // Shift: coz
        if (player != null && player.isShiftKeyDown()) {
            AssemblyManager.unglueNear(server, pos);
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.create_mobile_physics.unglued"), true);
            }
            return InteractionResult.SUCCESS;
        }

        AssemblyManager.Assembly a = AssemblyManager.glue(server, pos);
        if (player != null) {
            player.displayClientMessage(Component.translatable(
                    "message.create_mobile_physics.glued",
                    a.blockCount, a.balloons, a.wings), true);
        }
        return InteractionResult.SUCCESS;
    }
}

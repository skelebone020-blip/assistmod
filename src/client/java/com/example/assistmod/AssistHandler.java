package com.example.assistmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Runs every client tick and applies whichever assists are switched on. */
public final class AssistHandler {
    // Track keys we pressed ourselves, so we only release those and never fight the real keyboard.
    private static boolean forcedSneak, forcedSprint, forcedWalk, forcedAttack;
    private static boolean autoJumpWasOn, autoJumpOriginal;
    private static boolean nightVisionWasOn;

    private AssistHandler() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) return;

        handleAutoJump(client);
        handleFullbright(player);

        boolean noScreen = client.gui.screen() == null;

        handleHeldKey(Assist.AUTO_WALK.isEnabled() && noScreen, client.options.keyUp,
                () -> forcedWalk, v -> forcedWalk = v);
        handleHeldKey(Assist.AUTO_MINE.isEnabled() && noScreen, client.options.keyAttack,
                () -> forcedAttack, v -> forcedAttack = v);

        boolean sprint = Assist.AUTO_SPRINT.isEnabled() && noScreen
                && client.options.keyUp.isDown()
                && !player.isShiftKeyDown()
                && player.getFoodData().getFoodLevel() > 6;
        handleHeldKey(sprint, client.options.keySprint, () -> forcedSprint, v -> forcedSprint = v);

        boolean sneak = Assist.EDGE_GUARD.isEnabled() && noScreen && shouldSneakAtEdge(client, player, level);
        handleHeldKey(sneak, client.options.keyShift, () -> forcedSneak, v -> forcedSneak = v);

        if (Assist.AUTO_BRIDGE.isEnabled() && noScreen) autoBridge(client, player, level);
    }

    /** Presses {@code key} while wanted; releases it only if we were the one holding it. */
    private static void handleHeldKey(boolean wanted, net.minecraft.client.KeyMapping key,
                                      java.util.function.BooleanSupplier forced,
                                      java.util.function.Consumer<Boolean> setForced) {
        if (wanted) {
            key.setDown(true);
            setForced.accept(true);
        } else if (forced.getAsBoolean()) {
            key.setDown(false);
            setForced.accept(false);
        }
    }

    private static void handleAutoJump(Minecraft client) {
        boolean on = Assist.AUTO_JUMP.isEnabled();
        if (on && !autoJumpWasOn) {
            autoJumpOriginal = client.options.autoJump().get();
            client.options.autoJump().set(true);
        } else if (!on && autoJumpWasOn) {
            client.options.autoJump().set(autoJumpOriginal);
        }
        autoJumpWasOn = on;
    }

    private static void handleFullbright(LocalPlayer player) {
        boolean on = Assist.FULLBRIGHT.isEnabled();
        if (on) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            if (current == null || current.getDuration() < 220) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            }
        } else if (nightVisionWasOn) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        nightVisionWasOn = on;
    }

    private static boolean shouldSneakAtEdge(Minecraft client, LocalPlayer player, ClientLevel level) {
        if (!player.onGround() || player.getAbilities().flying) return false;
        if (!client.options.keyUp.isDown() && !client.options.keyDown.isDown()
                && !client.options.keyLeft.isDown() && !client.options.keyRight.isDown()) {
            return false;
        }
        Vec3 velocity = player.getDeltaMovement();
        Vec3 ahead = player.position().add(velocity.x * 3, -0.6, velocity.z * 3);
        return level.getBlockState(BlockPos.containing(ahead)).canBeReplaced();
    }

    private static void autoBridge(Minecraft client, LocalPlayer player, ClientLevel level) {
        if (client.gameMode == null) return;
        if (player.getAbilities().flying || player.onClimbable() || player.isInWater()) return;

        BlockPos below = player.blockPosition().below();
        if (!level.getBlockState(below).canBeReplaced()) return;

        int slot = findBlockSlot(player);
        if (slot == -1) return;

        BlockHitResult hit = findPlacement(level, below);
        if (hit == null) return;

        int previousSlot = player.getInventory().getSelectedSlot();
        player.getInventory().setSelectedSlot(slot);

        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);

        // Go back to the original slot if the player wasn't holding blocks.
        if (previousSlot != slot && !isBlock(player.getInventory().getItem(previousSlot))) {
            player.getInventory().setSelectedSlot(previousSlot);
        }
    }

    /** Finds a solid neighbouring block to click against so a block gets placed at {@code target}. */
    private static BlockHitResult findPlacement(ClientLevel level, BlockPos target) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos neighbor = target.relative(dir);
            BlockState state = level.getBlockState(neighbor);
            if (state.isAir() || state.canBeReplaced()) continue;

            Direction face = dir.getOpposite();
            Vec3 hitPos = Vec3.atCenterOf(neighbor).add(
                    face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            return new BlockHitResult(hitPos, face, neighbor, false);
        }
        return null;
    }

    private static int findBlockSlot(LocalPlayer player) {
        int held = player.getInventory().getSelectedSlot();
        if (isBlock(player.getInventory().getItem(held))) return held;
        for (int i = 0; i < 9; i++) {
            if (isBlock(player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    private static boolean isBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }
}

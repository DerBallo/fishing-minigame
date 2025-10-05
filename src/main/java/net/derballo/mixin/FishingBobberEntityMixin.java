package net.derballo.mixin;

import net.derballo.FishingMinigame;
import net.derballo.FishingMinigameMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.UUID;


@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {
    @Inject(
            method = "use(Lnet/minecraft/item/ItemStack;)I",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/loot/context/LootWorldContext$Builder"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onStartHookCountdownBranch(ItemStack usedItem, CallbackInfoReturnable<Integer> cir, PlayerEntity playerEntity) {
        if(usedItem.get(DataComponentTypes.ITEM_MODEL).equals(Identifier.of("minigamegui:hook")))
        {
            UUID uuid = playerEntity.getUuid();
            if (FishingMinigameMod.games.containsKey(uuid)) {
                FishingMinigameMod.games.get(uuid).GUI.close();
                FishingMinigameMod.games.remove(uuid);
            }

            FishingMinigame minigame = new FishingMinigame((ServerPlayerEntity) playerEntity, usedItem);
            minigame.GUI.open();
            FishingMinigameMod.games.put(uuid, minigame);
            cir.setReturnValue(1);
        }
    }
}
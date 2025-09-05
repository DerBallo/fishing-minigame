package net.derballo;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FishingMinigameMod implements ModInitializer {
    public static final String MOD_ID = "fishing-minigame";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Map<UUID, FishingMinigame> games = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register(CommandManager.literal("game").executes(ctx -> {
                ServerPlayerEntity player = ctx.getSource().getPlayer();
                assert player != null;
                UUID uuid = player.getUuid();
                if (games.containsKey(uuid)) {
                    games.get(uuid).GUI.close();
                    games.remove(uuid);
                }

                FishingMinigame minigame = new FishingMinigame(player);
                minigame.GUI.open();
                games.put(uuid, minigame);

                return 1;
            }));
        });
    }

    public static void wonGame(ServerPlayerEntity playerEntity){
        UUID uuid = playerEntity.getUuid();
        games.get(uuid).GUI.close();
        games.remove(uuid);
        playerEntity.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public static void lostGame(ServerPlayerEntity playerEntity){
        UUID uuid = playerEntity.getUuid();
        games.get(uuid).GUI.close();
        games.remove(uuid);
        playerEntity.playSoundToPlayer(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}
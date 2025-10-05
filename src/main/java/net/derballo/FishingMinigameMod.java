package net.derballo;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import java.util.List;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FishingMinigameMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "fishing-minigame";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Map<UUID, FishingMinigame> games = new ConcurrentHashMap<>();
    public static final Random random = new Random();
    public static final RegistryKey<LootTable> FISHING_MINIGAME = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("fishing-minigame", "gameplay/fishing"));


    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            FishingMinigame.init();
        });
    }

    public static void wonGame(ServerPlayerEntity playerEntity){
        UUID uuid = playerEntity.getUuid();
        var game = games.get(uuid);
        if(playerEntity.fishHook != null)
        {
            playerEntity.getWorld().playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            playerEntity.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);

            LootWorldContext lootWorldContext = (new LootWorldContext.Builder((ServerWorld)playerEntity.getWorld())).add(LootContextParameters.ORIGIN, playerEntity.getPos()).add(LootContextParameters.TOOL, game.fishingRod).add(LootContextParameters.THIS_ENTITY, playerEntity).luck(playerEntity.getLuck()).build(LootContextTypes.FISHING);
            LootTable lootTable = playerEntity.getServer().getReloadableRegistries().getLootTable(FISHING_MINIGAME);
            List<ItemStack> list = lootTable.generateLoot(lootWorldContext);
            for(ItemStack itemStack : list) {
                ItemEntity itemEntity = new ItemEntity(playerEntity.fishHook.getWorld(), playerEntity.fishHook.getX(), playerEntity.fishHook.getY(), playerEntity.fishHook.getZ(), itemStack);
                double d = playerEntity.getX() - playerEntity.fishHook.getX();
                double e = playerEntity.getY() - playerEntity.fishHook.getY();
                double f = playerEntity.getZ() - playerEntity.fishHook.getZ();
                itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                playerEntity.fishHook.getWorld().spawnEntity(itemEntity);
                playerEntity.getWorld().spawnEntity(new ExperienceOrbEntity(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY() + (double) 0.5F, playerEntity.getZ() + (double) 0.5F, random.nextInt(6) + 1));
                playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
            }

            playerEntity.fishHook.discard();

            /*if(!list.isEmpty())
            {
                List<Text> lines = list.getFirst().get(DataComponentTypes.LORE).lines();
                if (!lines.isEmpty())
                {
                    switch (lines.getFirst().getString())
                    {
                        case "Common":
                            playerEntity.getItemCooldownManager().set(game.fishingRod, 40);
                            break;
                        case "Rare":
                            playerEntity.getItemCooldownManager().set(game.fishingRod, 80);
                            break;
                        case "Epic":
                            playerEntity.getItemCooldownManager().set(game.fishingRod, 120);
                            break;
                        case "Legendary":
                            playerEntity.getItemCooldownManager().set(game.fishingRod, 200);
                            break;
                    }
                }
            }*/
        }
        game.GUI.close();
        games.remove(uuid);
        playerEntity.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public static void lostGame(ServerPlayerEntity playerEntity){
        UUID uuid = playerEntity.getUuid();
        var game = games.get(uuid);
        if(playerEntity.fishHook != null)
        {
            playerEntity.fishHook.discard();
            //playerEntity.getItemCooldownManager().set(game.fishingRod, 100);
        }
        game.GUI.close();
        games.remove(uuid);
        playerEntity.playSoundToPlayer(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}
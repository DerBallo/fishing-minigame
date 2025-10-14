package net.derballo;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class FishingMinigame {
    public static final ItemStack backgroundItem = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
    public static final ItemStack waterItem = new ItemStack(Items.BLUE_STAINED_GLASS_PANE);
    public static final ItemStack fishingHookItem = new ItemStack(Items.TRIPWIRE_HOOK);
    public static final ItemStack fishItem = new ItemStack(Items.TROPICAL_FISH);
    public static final ItemStack leftButtonItem = new ItemStack(Items.OAK_BUTTON);
    public static final ItemStack rightButtonItem = new ItemStack(Items.OAK_BUTTON);
    public static final ItemStack confirmButtonItem = new ItemStack(Items.LIME_STAINED_GLASS_PANE);

    public final ServerPlayerEntity playerEntity;
    public final ItemStack fishingRod;
    public SimpleGui GUI;
    public int hookPosition;
    public int fishPosition;

    public static void init() {
        backgroundItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:rock"));
        backgroundItem.set(DataComponentTypes.ITEM_NAME, Text.literal(""));

        waterItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:water"));
        waterItem.set(DataComponentTypes.ITEM_NAME, Text.literal(""));

        fishingHookItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:hook"));
        fishingHookItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Needs to be right above the fish!").withColor(Colors.YELLOW));

        fishItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:mysteryfish"));
        fishItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Who knows which one?").withColor(Colors.YELLOW));

        leftButtonItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:leftarrow"));
        leftButtonItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Move the hook 3 spaces to the left!").withColor(Colors.YELLOW));

        rightButtonItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:rightarrow"));
        rightButtonItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Move the hook 5 spaces to the right!").withColor(Colors.YELLOW));

        confirmButtonItem.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minigamegui:catch"));
        confirmButtonItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Press when the hook is above the fish.").withColor(Colors.YELLOW));
    }

    public FishingMinigame(ServerPlayerEntity player, ItemStack rod){
        playerEntity = player;
        fishingRod = rod;
        hookPosition = FishingMinigameMod.random.nextInt(9);
        fishPosition = FishingMinigameMod.random.nextInt(9);

        GUI = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        GUI.setTitle(Text.literal("Hook the Fish"));

        draw();
    }

    private void draw() {
        for (int i = 0; i < 18; i++)
        {
            GUI.setSlot(i, backgroundItem);
        }

        for (int i = 18; i < 27; i++)
        {
            GUI.setSlot(i, waterItem);
        }

        for (int i = 27; i < 54; i++)
        {
            GUI.setSlot(i, backgroundItem);
        }

        GUI.setSlot(9 + hookPosition, fishingHookItem);
        GUI.setSlot(18 + fishPosition, fishItem);

        GUI.setSlot(38, new GuiElement(leftButtonItem, (slotA, actionA, actorA) -> {
            hookPosition -= 3;
            if (hookPosition < 0){
                FishingMinigameMod.lostGame(playerEntity);
                return;
            }
            playerEntity.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 1.0f, 1.0f);
            draw();
        }));

        GUI.setSlot(40, new GuiElement(confirmButtonItem, (slotB, actionB, actorB) -> {
            if (hookPosition != fishPosition){
                FishingMinigameMod.lostGame(playerEntity);
                return;
            }
            FishingMinigameMod.wonGame(playerEntity);
        }));

        GUI.setSlot(42, new GuiElement(rightButtonItem, (slotC, actionC, actorC) -> {
            hookPosition += 5;
            if (hookPosition > 8){
                FishingMinigameMod.lostGame(playerEntity);
                return;
            }
            playerEntity.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 1.0f, 1.0f);
            draw();
        }));
    }
}

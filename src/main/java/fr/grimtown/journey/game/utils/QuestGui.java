package fr.grimtown.journey.game.utils;

import fr.grimtown.journey.GamePlugin;
import fr.grimtown.journey.game.GameManager;
import fr.grimtown.journey.game.GameUtils;
import fr.grimtown.journey.game.classes.Universe;
import fr.grimtown.journey.quests.classes.Quest;
import fr.grimtown.journey.quests.managers.QuestManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestGui extends FastInv {
    private final GameManager manager = GamePlugin.getManager();
    private final ArrayList<BukkitTask> animations = new ArrayList<>();

    public QuestGui(UUID questsPlayer, Universe universe) {
        super(GamePlugin.getConfigs().getInt("game.quests-gui.size"),
                GamePlugin.getConfigs().getString("game.quests-gui.title"));
        setItems(0, getInventory().getSize()-1, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name(GamePlugin.getConfigs().getString("game.quests-gui.no-quest")).build());
        Bukkit.getScheduler().runTaskAsynchronously(GamePlugin.getPlugin(), ()-> displayQuest(questsPlayer, universe));
    }

    /**
     * Display each quests in slot position
     */
    private void displayQuest(UUID questsPlayer, Universe universe) {
        List<Quest> quests;
        if (universe.equals(GamePlugin.getUniverse())) {
            quests = manager.getLoadedQuests();
        } else {
            quests = QuestManager.getQuests(universe);
        }
        quests.forEach(quest -> {
            if (quest.getSlot() > getInventory().getSize()) return;
            ArrayList<Material> materials = quest.getItem() != null ? quest.getItem() : new ArrayList<>(List.of(Material.PAPER));
            ItemBuilder item = new ItemBuilder(materials.get(0));
            List<String> lore = new ArrayList<>(List.of(quest.getLore().split("%n")));
            if (quest.notBonus()) {
                item.name("§2" + quest.getName());
            } else {
                item.name("§9" + quest.getName());
                item.type(Material.ENDER_CHEST);
                materials.add(Material.ENDER_CHEST);
                lore.add("§6Bonus");
            }
            int progress = GameUtils.getProgression(questsPlayer, quest).getProgress();
            if (GameUtils.hasCompleted(questsPlayer, quest, universe)) {
                item.type(Material.BOOK);
                materials.clear();
            } else {
                if (quest.getCount()<=64 && quest.getCount()>0 && quest.getCount()-progress>0) item.amount(quest.getCount() - progress);
                if (quest.getCount()>0) lore.add("§b" + progress + "§6/§2" + Math.abs(quest.getCount()));
            }
            item.flags(ItemFlag.HIDE_ATTRIBUTES);
            if (materials.size()<=1) {
                setItem(quest.getSlot(), item.lore(lore).build());
            } else {
                AtomicInteger lastDisplayed = new AtomicInteger();
                animations.add(Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getPlugin(), ()-> {
                    lastDisplayed.getAndIncrement();
                    if (lastDisplayed.get()> materials.size()-1) lastDisplayed.set(0);
                    item.type(materials.get(lastDisplayed.get()));
                    setItem(quest.getSlot(), item.lore(lore).build());
                }, 0L, 20L));
            }
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        animations.forEach(BukkitTask::cancel);
    }
}

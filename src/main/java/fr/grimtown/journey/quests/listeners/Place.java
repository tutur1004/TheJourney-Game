package fr.grimtown.journey.quests.listeners;

import fr.grimtown.journey.quests.QuestsUtils;
import fr.grimtown.journey.quests.classes.Quest;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Locale;

public class Place implements Listener {
    private final Quest quest;
    private final Material material;
    public Place(Quest quest) {
        this.quest = quest;
        if (quest.getPayload().equalsIgnoreCase("ANY")) {
            material = null;
        } else {
            material = Material.getMaterial(quest.getPayload().toUpperCase(Locale.ROOT));
            if (material != null) QuestsUtils.questLoadLog(quest.getName(), material.toString());
            else HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event) {
        if (material!=null && !event.getBlock().getType().equals(material)) return;
        if (QuestsUtils.hasCompleted(event.getPlayer().getUniqueId(), quest)) return;
        QuestsUtils.getProgression(event.getPlayer().getUniqueId(), quest).addProgress();
    }
}

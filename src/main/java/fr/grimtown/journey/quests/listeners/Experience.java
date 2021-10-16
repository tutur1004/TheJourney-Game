package fr.grimtown.journey.quests.listeners;

import fr.grimtown.journey.quests.QuestsUtils;
import fr.grimtown.journey.quests.classes.Quest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class Experience implements Listener {
    private final Quest quest;
    public Experience(Quest quest) {
        this.quest = quest;
        QuestsUtils.questLoadLog(quest.getName(), quest.getCount() + "levels");
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (event.getPlayer().getLevel() < quest.getCount()) return;
        if (QuestsUtils.hasCompleted(event.getPlayer().getUniqueId(), quest)) return;
        QuestsUtils.getProgression(event.getPlayer().getUniqueId(), quest).setCompleted();
    }
}

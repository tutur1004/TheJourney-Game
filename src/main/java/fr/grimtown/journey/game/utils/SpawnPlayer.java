package fr.grimtown.journey.game.utils;

import fr.grimtown.journey.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Date;
import java.util.stream.IntStream;

public class SpawnPlayer implements Listener {
    private final Date spawnDate = new Date();
    private final Player player;

    public SpawnPlayer(Player player, boolean keepInventory) {
        this.player = player;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        PlayerInventory inventory = player.getInventory();
        float yaw = player.getLocation().getYaw(), pitch = player.getLocation().getPitch();
        scheduler.runTaskLaterAsynchronously(GamePlugin.getPlugin(), ()-> scheduler.runTask(GamePlugin.getPlugin(), ()-> {
            if (player.isDead()) player.spigot().respawn();
            World world = Bukkit.getWorld("world");
            double max = 100;
            if (world!=null) max = world.getWorldBorder().getSize() * 0.45;
            Location location = new Location(world,
                    (Math.random()>=0.5 ? max/2 + Math.random() * (max - max/2) : (-max/2) + Math.random() * (-max - (-max/2))),
                    200,
                    (Math.random()>=0.5 ? max/2 + Math.random() * (max - max/2) : (-max/2) + Math.random() * (-max - (-max/2))),
                    yaw,
                    pitch);
            player.teleport(location);
            if (!keepInventory) {
                if (Math.random()<=0.1) inventory.setItemInOffHand(new ItemStack(Material.AIR));
                IntStream.range(0, inventory.getSize()).forEach(loop -> {
                    if (Math.random()<=0.1) inventory.setItem(loop, new ItemStack(Material.AIR));
                });
            }
            player.getInventory().setItemInOffHand(inventory.getItemInOffHand());
            player.getInventory().setStorageContents(inventory.getStorageContents());
            player.getInventory().setArmorContents(inventory.getArmorContents());
        }), 1L);
    }

    @EventHandler
    public void onPlayerSpawnFall(EntityDamageEvent event) {
        if (!event.getEntity().getUniqueId().equals(player.getUniqueId())) return;
        if (spawnDate.after(new Date(new Date().getTime()-10000))) {
            event.setCancelled(true);
        } else HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerSpawnVoid(PlayerMoveEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        if (event.getTo().getBlockY() >= 0) return;
        player.teleport(event.getPlayer().getLocation().clone().add(0,10,0));
        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().registerEvents(new SpawnPlayer(event.getPlayer(), true), GamePlugin.getPlugin());
    }
}
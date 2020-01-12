package defineoutside.main;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GlidingTrails extends JavaPlugin implements Listener {
	HashMap<Player, Particle> playersParticleMap = new HashMap<Player, Particle>();
	HashMap<Player, Location> playersPastPositionMap = new HashMap<Player, Location>();

	Particle[] validParticle = new Particle[] { Particle.CRIT_MAGIC, Particle.EXPLOSION_LARGE, Particle.HEART,
			Particle.SWEEP_ATTACK, Particle.SQUID_INK, Particle.CAMPFIRE_COSY_SMOKE, Particle.FALLING_LAVA,
			Particle.FALLING_WATER };

	private Random rand = new Random();

	public static GlidingTrails plugin;

	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(this, this);

		// Create a repeating loop to spawn particles
		new BukkitRunnable() {
			@Override
			public void run() {
				// Keyset is more efficient than going through all players
				for (Player player : playersParticleMap.keySet()) {
					player.getWorld().spawnParticle(playersParticleMap.get(player), playersPastPositionMap.get(player),
							5, 0.5, 0.5, 0.5);
					playersPastPositionMap.put(player, player.getLocation());
				}
			}
		}.runTaskTimerAsynchronously(GlidingTrails.plugin, 0L, 2L);
	}

	// Since this runs when player toggles, the old gliding value is still used, so we have to flip them!
	public void updatePlayerTrailStatus(Player player) {
		if (playersParticleMap.containsKey(player) && player.isGliding()) {
			playersParticleMap.remove(player);
			playersPastPositionMap.remove(player);
		} else if (player.hasPermission("glidingtrails.allowed") && !player.isGliding()) {
			playersParticleMap.put(player, getRandomParticle());
			playersPastPositionMap.put(player, player.getLocation());
		}
	}
	
	public Particle getRandomParticle() {
		return validParticle[rand.nextInt(validParticle.length)];
	}

	@EventHandler
	public void onPlayerGlideEvent(EntityToggleGlideEvent event) {
		updatePlayerTrailStatus((Player) event.getEntity());
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		// Repeat code here :(
		// Quickest way to fix trails not joining when a player logs in
		if (event.getPlayer().isGliding() && event.getPlayer().hasPermission("glidingtrails.allowed")) {
			playersParticleMap.put(event.getPlayer(), getRandomParticle());
			playersPastPositionMap.put(event.getPlayer(), event.getPlayer().getLocation());
		}

	}
	
	// Stop memory leaks
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		if (playersParticleMap.containsKey(event.getPlayer())) {
			playersParticleMap.remove(event.getPlayer());
			playersPastPositionMap.remove(event.getPlayer());
		}
	}
}

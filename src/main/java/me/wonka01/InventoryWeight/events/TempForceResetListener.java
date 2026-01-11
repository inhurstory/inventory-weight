package me.wonka01.InventoryWeight.events;

import me.wonka01.InventoryWeight.playerweight.PlayerWeight;
import me.wonka01.InventoryWeight.playerweight.PlayerWeightMap;
import me.wonka01.InventoryWeight.util.WorldList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class TempForceResetListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        resetTempForce(event.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        resetTempForce(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        resetTempForce(event.getPlayer());
    }

    private void resetTempForce(Player player) {
        UUID playerId = player.getUniqueId();
        boolean cleared = WorldList.getInstance().clearTempForce(playerId);
        if (cleared) {
            PlayerWeight weight = PlayerWeightMap.getPlayerWeightMap().get(playerId);
            if (weight != null) {
                weight.changeSpeed();
            }
        }
    }
}

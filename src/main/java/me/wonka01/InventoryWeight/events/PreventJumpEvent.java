package me.wonka01.InventoryWeight.events;

import me.wonka01.InventoryWeight.util.WorldList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PreventJumpEvent implements Listener {

    private static final Set<UUID> jumpPreventedPlayers = new HashSet<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null || !jumpPreventedPlayers.contains(player.getUniqueId())) {
            return;
        }

        if (!WorldList.getInstance().isInventoryWeightEnabled(player.getWorld().getName())) {
            return;
        }

        double deltaY = event.getTo().getY() - event.getFrom().getY();
        if (deltaY <= 0.2D) {
            return;
        }

        Location from = event.getFrom().clone();
        from.setDirection(event.getTo().getDirection());
        event.setTo(from);
    }

    public static void preventJump(UUID playerId) {
        jumpPreventedPlayers.add(playerId);
    }

    public static void allowJump(UUID playerId) {
        jumpPreventedPlayers.remove(playerId);
    }
}

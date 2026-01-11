package me.wonka01.InventoryWeight.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class WorldList {

    private static WorldList instance;

    private List<String> worlds = new ArrayList<String>();
    private Map<UUID, TempForceState> tempForces = new HashMap<UUID, TempForceState>();

    public enum TempForceState {
        ON, OFF
    }

    public static void initializeWorldList(List<String> worlds) {
        instance = new WorldList(worlds);
    }

    public static WorldList getInstance() {
        if(instance == null) {
            return new WorldList(new ArrayList<String>());
        } else {
            return instance;
        }
    }

    private WorldList(List<String> worlds){
        this.worlds.addAll(worlds);
    }

    public boolean isInventoryWeightEnabled(Player player) {
        if (player == null) {
            return false;
        }
        TempForceState state = tempForces.get(player.getUniqueId());
        if (state == TempForceState.ON) {
            return true;
        }
        if (state == TempForceState.OFF) {
            return false;
        }
        return isInventoryWeightEnabled(player.getWorld().getName());
    }

    public boolean isInventoryWeightEnabled(String world) {
        return (worlds.isEmpty() || worlds.contains(world));
    }

    public void setTempForce(UUID playerId, TempForceState state) {
        tempForces.put(playerId, state);
    }

    public boolean clearTempForce(UUID playerId) {
        return tempForces.remove(playerId) != null;
    }
}

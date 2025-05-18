package me.wiceh.locks.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.inventory.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationUtils {

    public static void locationToStatement(PreparedStatement statement, int startIndex, Location location) throws SQLException {
        Object[] params = {
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        };

        int maxParams = statement.getParameterMetaData().getParameterCount();
        for (int i = 0; i < params.length && startIndex + i <= maxParams; i++) {
            statement.setObject(startIndex + i, params[i]);
        }
    }

    public static Location locationFromResultSet(ResultSet resultSet) throws SQLException {
        return new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getInt("x"), resultSet.getInt("y"),
                resultSet.getInt("z"));
    }

    public static String locationToString(Location location) {
        return String.format("%sx %sy %sz", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static boolean isSimilar(Location loc1, Location loc2) {
        return loc1.getWorld().getName().equals(loc2.getWorld().getName())
                && loc1.getX() == loc2.getX()
                && loc1.getY() == loc2.getY()
                && loc1.getZ() == loc2.getZ();
    }

    public static Location normalize(Block block) {
        BlockState state = block.getState();
        if (state instanceof Chest singleChest) {
            Inventory inv = singleChest.getInventory();
            if (inv.getHolder() instanceof DoubleChest dc) {
                return ((Chest) dc.getLeftSide()).getBlock().getLocation();
            }

            return block.getLocation();
        }

        BlockData data = block.getBlockData();
        if (data instanceof Door && ((Door) data).getHalf() == Door.Half.TOP) {
            return block.getLocation().add(0, -1, 0);
        }

        return block.getLocation();
    }
}

/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package github.BTEPlotSystem.utils;

import dev.dbassett.skullcreator.SkullCreator;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Utils {

    // Head Database API
    public static HeadDatabaseAPI headDatabaseAPI;

    public static ItemStack getItemHead(String headID) {
        return headDatabaseAPI != null ? headDatabaseAPI.getItemHead(headID) : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
    }

    // Get player head by UUID
    public static ItemStack getPlayerHead(UUID playerUUID) {
        return SkullCreator.itemFromUuid(playerUUID) != null ? SkullCreator.itemFromUuid(playerUUID) : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
    }
    // Sounds
    public static Sound TeleportSound = Sound.ENTITY_ENDERMEN_TELEPORT;
    public static Sound ErrorSound = Sound.ENTITY_ITEM_BREAK;
    public static Sound CreatePlotSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound FinishPlotSound = Sound.ENTITY_PLAYER_LEVELUP;
    public static Sound AbandonPlotSound = Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE;
    public static Sound Done = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

    // Spawn Location
    public static Location getSpawnPoint() {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

        return new Location(Bukkit.getWorld(HUB_WORLD_NAME),
                config.getDouble("spawn-point.x"),
                config.getDouble("spawn-point.y"),
                config.getDouble("spawn-point.z"),
                (float) config.getDouble("spawn-point.yaw"),
                (float) config.getDouble("spawn-point.pitch")
        );
    }

    // Player Messages
    private static final String messagePrefix = "§9[§bBuildTheEarth.Asia§9] §r";

    public static String getInfoMessageFormat(String info) {
        return messagePrefix + "§a" + info;
    }

    public static String getErrorMessageFormat(String error) {
        return messagePrefix + "§c" + error;
    }

    // Servers
    public final static String HUB_WORLD_NAME = "world";
    public final static String PLOT_SERVER = "hub"; //It's the hub server.. of course.

    public final static String JAPAN_SERVER = "japan";
    public final static String HKMU_SERVER = "hkmu"; //wont be in use for the forseeable future
    public final static String MIDEAST_SERVER = "middle_east";
    public final static String DUBAI_SERVER = "uae"; //wont be in use
    public final static String ISREALWB_SERVER = "isreal_wb"; //why am i bothering
    public final static String CIS_SERVER = "cis"; //wont be in use for the forseeable future
    public final static String ASEAN_SERVER = "asean";
    public final static String KOREA_SERVER = "korea_unified";
    public final static String SOUTHASIA_SERVER = "south_asia";
    public final static String TAIWAN_SERVER = "taiwan";
    public final static String CHINA_SERVER = "china"; //why am i even bothering to include this

//    public final static String EVENT_SERVER = "ALPS-3";

//    public final static String TEST_SERVER = "ALPS-4";

    // Integer Try Parser
    public static Integer TryParseInt(String someText) {
        try {
            return Integer.parseInt(someText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String getPointsByColor(int points) {
        switch (points) {
            case 0:
                return "§7" + points;
            case 1:
                return "§4" + points;
            case 2:
                return "§6" + points;
            case 3:
                return "§e" + points;
            case 4:
                return "§2" + points;
            default:
                return "§a" + points;
        }
    }

    public static String getFormattedDifficulty(PlotDifficulty plotDifficulty) {
        switch (plotDifficulty) {
            case EASY:
                return "§a§lEasy";
            case MEDIUM:
                return "§6§lMedium";
            case HARD:
                return "§c§lHard";
            default:
                return "";
        }
    }
}

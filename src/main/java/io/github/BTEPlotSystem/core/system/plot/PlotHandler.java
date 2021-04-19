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

package github.BTEPlotSystem.core.system.plot;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.menus.CompanionMenu;
import github.BTEPlotSystem.core.menus.ReviewMenu;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.multiverse.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class PlotHandler {

    public static void teleportPlayer(Plot plot, Player player) {
        player.sendMessage(Utils.getInfoMessageFormat("Teleporting to plot §6#" + plot.getID()));

        String worldName = "P-" + plot.getID();
        if(Bukkit.getWorld(worldName) == null) {
            BTEPlotSystem.getMultiverseCore().getMVWorldManager().loadWorld(worldName);
        }

        player.teleport(getPlotSpawnPoint(Bukkit.getWorld(worldName)));

        player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
        player.setAllowFlight(true);
        player.setFlying(true);

        //TODO: Unsure if should use Companion Menu
        player.getInventory().setItem(8, CompanionMenu.getMenuItem());

        if(player.hasPermission("oceania.review")) {
            player.getInventory().setItem(7, ReviewMenu.getMenuItem());
        } else if(player.getInventory().contains(ReviewMenu.getMenuItem())) {
            player.getInventory().remove(ReviewMenu.getMenuItem());
        }

        sendLinkMessages(plot, player);

        if(plot.getBuilder().getUUID().equals(player.getUniqueId())) {
            try {
                plot.setLastActivity(false);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
    }

    public static void submitPlot(Plot plot) throws Exception {
        plot.setStatus(Status.unreviewed);

        loadPlot(plot);

        plot.removeBuilderPerms(plot.getBuilder().getUUID()).save();

        String worldName = "P-" + plot.getID();
        if(Bukkit.getWorld(worldName) != null) {
            for(Player player : Bukkit.getWorld(worldName).getPlayers()) {
                player.teleport(Utils.getSpawnPoint());
            }
        }
    }

    public static void undoSubmit(Plot plot) throws SQLException {
        plot.setStatus(Status.unfinished);

        plot.addBuilderPerms(plot.getBuilder().getUUID()).save();
    }

    public static void abandonPlot(Plot plot) throws Exception {
        String worldName = "P-" + plot.getID();
        if(Bukkit.getWorld(worldName) != null) {
            for(Player player : Bukkit.getWorld(worldName).getPlayers()) {
                player.teleport(Utils.getSpawnPoint());
            }
        }

        plot.getBuilder().removePlot(plot.getSlot());
        plot.setBuilder(null);
        plot.setLastActivity(true);
        BTEPlotSystem.getMultiverseCore().getMVWorldManager().deleteWorld(worldName, true, true);
        BTEPlotSystem.getMultiverseCore().getMVWorldManager().removeWorldFromConfig(worldName);

        if(plot.isReviewed()) {
            PreparedStatement stmt_reviews = DatabaseConnection.prepareStatement("DELETE FROM reviews WHERE id_review = '" + plot.getReview().getReviewID() + "'");
            stmt_reviews.executeUpdate();

            PreparedStatement stmt_plots = DatabaseConnection.prepareStatement("UPDATE plots SET idreview = DEFAULT(idreview) WHERE idplot = '" + plot.getID() + "'");
            stmt_plots.executeUpdate();
        }

        plot.setScore(-1);
        plot.setStatus(Status.unclaimed);

        FileUtils.deleteDirectory(new File(getWorldGuardConfigPath(plot.getID())));
        FileUtils.deleteDirectory(new File(getMultiverseInventoriesConfigPath(plot.getID())));
    }

    public static void deletePlot(Plot plot) throws Exception {
        abandonPlot(plot);

        Files.deleteIfExists(Paths.get(PlotManager.getOutlinesSchematicPath(),String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));

        String query = "DELETE FROM plots WHERE idplot = '" + plot.getID() + "'";
        PreparedStatement statement = DatabaseConnection.prepareStatement(query);
        statement.execute();
    }

    public static void loadPlot(Plot plot) {
        if(Bukkit.getWorld("P-" + plot.getID()) == null) {
            BTEPlotSystem.getMultiverseCore().getMVWorldManager().loadWorld("P-" + plot.getID());
        }
    }

    public static void unloadPlot(Plot plot) {
        World world = Bukkit.getWorld("P-" + plot.getID());
        if(world.getPlayers().size() - 1 == 0) {
            try {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> Bukkit.getServer().unloadWorld(world, true), 1, 20*3);
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while unloading plot world!", ex);
            }
        }
    }

    public static Location getPlotSpawnPoint(World world) {
        try {
            return new Location(world,
                    (double) (PlotManager.getPlotSize(PlotManager.getPlotByWorld(world)) / 2) + 0.5,
                    30, // TODO: Fit Y value to schematic height to prevent collision
                    (double) (PlotManager.getPlotSize(PlotManager.getPlotByWorld(world)) / 2) + 0.5,
                    -90,
                    90);
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return world.getSpawnLocation();
        }
    }

    public static void sendLinkMessages(Plot plot, Player player){
        TextComponent[] tc = new TextComponent[3];
        tc[0] = new TextComponent();
        tc[1] = new TextComponent();
        tc[2] = new TextComponent();

        tc[0].setText("§7>> Click me to open the §aGoogle Maps §7link....");
        tc[1].setText("§7>> Click me to open the §aGoogle Earth Web §7link....");
        tc[2].setText("§7>> Click me to open the §aOpen Street Map §7link....");

        tc[0].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleMapsLink()));
        tc[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleEarthLink()));
        tc[2].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getOSMMapsLink()));

        tc[0].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Google Maps").create()));
        tc[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Google Earth Web").create()));
        tc[2].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Open Street Map").create()));

        player.sendMessage("§7--------------------");
        player.spigot().sendMessage(tc[0]);
        player.spigot().sendMessage(tc[1]);
        player.spigot().sendMessage(tc[2]);
        player.sendMessage("§7--------------------");
    }

    public static void sendFeedbackMessage(List<Plot> plots, Player player) throws SQLException {
        player.sendMessage("§7--------------------");
        for(Plot plot : plots) {
            player.sendMessage("§aYour plot with the ID §6#" + plot.getID() + " §ahas been reviewed!");
            TextComponent tc = new TextComponent();
            tc.setText("§6Click Here §ato check your feedback.");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/feedback " + plot.getID()));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Feedback").create()));
            player.spigot().sendMessage(tc);

            if(plots.size() != plots.indexOf(plot) + 1) {
                player.sendMessage("");
            }

            plot.getReview().setFeedbackSent(true);
        }
        player.sendMessage("§7--------------------");
        player.playSound(player.getLocation(), Utils.FinishPlotSound, 1, 1);
    }

    public static void sendUnfinishedPlotReminderMessage(List<Plot> plots, Player player) {
        player.sendMessage("§aYou still have §6" + plots.size() + " §aunfinished plots!");
        TextComponent tc = new TextComponent();
        tc.setText("§6Click Here §ato open your plots menu.");
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plots"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Show my plots").create()));
        player.spigot().sendMessage(tc);
    }

    public static String getWorldGuardConfigPath(int plotID) {
        return Bukkit.getPluginManager().getPlugin("WorldGuard").getDataFolder() + "/worlds/P-" + plotID;
    }

    public static String getMultiverseInventoriesConfigPath(int plotID) {
        return Bukkit.getPluginManager().getPlugin("Multiverse-Inventories").getDataFolder() + "/worlds/P-" + plotID;
    }
}

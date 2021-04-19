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

package github.BTEPlotSystem.commands.plot;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Submit implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("oceania.plot")) {
                Player player = (Player) sender;
                World playerWorld = player.getWorld();

                Plot plot = null;
                if(args.length == 0) {
                    if(PlotManager.isPlotWorld(playerWorld)) {
                        try {
                            plot = PlotManager.getPlotByWorld(playerWorld);
                        } catch (SQLException ex) {
                            player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/submit or /submit <ID>"));
                    }
                } else if(args.length == 1 && Utils.TryParseInt(args[0]) != null) {
                    try {
                        plot = new Plot(Integer.parseInt(args[0]));
                    } catch (SQLException ex) {
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/submit or /submit <ID>"));
                    return true;
                }

                try {
                    assert plot != null;
                    if(plot.getStatus() == Status.unfinished) {
                        if(plot.getBuilder().getUUID().equals(player.getUniqueId()) || player.hasPermission("alpsbte.review")) {
                            PlotHandler.submitPlot(plot);
                            FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

                            if(config.getBoolean("plot.annouceall"))
                                Bukkit.broadcastMessage(Utils.getInfoMessageFormat("Plot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + " §ahas been finished!"));
                            player.playSound(player.getLocation(), Utils.FinishPlotSound, 1, 1);
                        } else {
                            player.sendMessage(Utils.getErrorMessageFormat("You are not allowed to submit this plot!"));
                        }
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("You can only submit unfinished plots!"));
                    }
                } catch (Exception ex) {
                    player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            }
        }
        return true;
    }
}

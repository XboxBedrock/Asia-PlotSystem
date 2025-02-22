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

package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.CityProject;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotGenerator;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.*;
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CompanionMenu extends AbstractMenu {

    private final List<CityProject> cityProjects = CityProject.getCityProjects();
    private static final int MAX_BUILDS = 3;
    private final Plot[] slots = new Plot[MAX_BUILDS];

    private PlotDifficulty selectedPlotDifficulty = null;

    public CompanionMenu(Player player) {
        super(6, "Asia Plots", player);

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
        mask.apply(getMenu());

        try {
            // Get player slots of player
            Builder builder = new Builder(getMenuPlayer().getUniqueId());
            for(int i = 0; i < MAX_BUILDS; i++) {
                slots[i] = builder.getPlot(Slot.values()[i]);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Set navigator item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.COMPASS, 1)
                        .setName("§6§lNavigator").setLore(new LoreBuilder()
                                .addLine("Open the navigator menu").build())
                        .build());
//TODO: Move to seperate function setCityProjectItems()

//        // Add "Player Plots Slots" Items
//        for(int i = 0; i < cityProjects.size(); i++) {
//            if(i <= 28) { // Why is it limited to 28 States? Actually, this is enough,
//                          // it would fill up inventory otherwise.
//                ItemStack stateProjectItem = null;
//                switch (cityProjects.get(i).getState()) {
//
//                }
//
//                try {
//                    getMenu().getSlot(9 + i)
//                            .setItem(new ItemBuilder(stateProjectItem)
//                                    .setName("§b§l" + cityProjects.get(i).getName())
//                                    .setLore(new LoreBuilder()
//                                            .addLines(cityProjects.get(i).getDescription(),
//                                                    "",
//                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.unclaimed).size() + " §7Plots Open",
//                                                    "§f---------------------",
//                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.unfinished, Status.unreviewed).size() + " §7Plots In Progress",
//                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.complete).size() + " §7Plots Completed",
//                                                    "",
//                                                    getCityDifficultyForBuilder(cityProjects.get(i).getID(), new Builder(getMenuPlayer().getUniqueId())))
//                                            .build())
//                                    .build());
//                } catch (SQLException ex) {
//                    getMenu().getSlot(9 + i).setItem(errorItem());
//                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
//                }
//            }
        // Set switch plots difficulty item
        try {
            getMenu().getSlot(7).setItem(getSelectedDifficultyItem());
        } catch (SQLException ex) {
            getMenu().getSlot(7).setItem(MenuItems.errorItem());
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        setCityProjectItems();
        // Set city project items
        for (int i = 0; i < MAX_BUILDS; i++) {
        
            try {
                getMenu().getSlot(46 + i)
                        .setItem(new ItemBuilder(Material.MAP, 1 + i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .addLines("§7ID: §f" + slots[i].getID(),
                                                "§7City: §f" + slots[i].getCity().getName(),
                                                "§7Difficulty: §f" + slots[i].getDifficulty().name().charAt(0) + slots[i].getDifficulty().name().substring(1).toLowerCase(),
                                                "",
                                                "§6§lStatus: §7§l" + slots[i].getStatus().name().substring(0, 1).toUpperCase() + slots[i].getStatus().name().substring(1)
                                        ).build())
                                .build());
            } catch (Exception ex) {
                getMenu().getSlot(46 + i)
                        .setItem(new ItemBuilder(Material.EMPTY_MAP, 1 + i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .addLines("§7Click on a city project to create a new plot",
                                                "",
                                                "§6§lStatus: §7§lUnassigned")
                                        .build())
                                .build());
            }
        }

        // Set builder utilities item
        getMenu().getSlot(50).setItem(BuilderUtilitiesMenu.getMenuItem());

        // Set show plots item
        getMenu().getSlot(51).setItem(PlayerPlotsMenu.getMenuItem());

        // Add "Player Settings Menu" Item
//        getMenu().getSlot(52)
//                .setItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
//                        .setName("§b§lSettings")
//                        .setLore(new LoreBuilder()
//                                .addLine("Modify your user settings").build())
//                        .build());
    }

    @Override
    protected void setItemClickEvents() {
        // Add Click Event for Navigator Item
//        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
//            clickPlayer.closeInventory();
//            clickPlayer.performCommand("navigator");
//        });

        // Add click event for switch plots difficulty item
        getMenu().getSlot(7).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                        PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                        PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);
                getMenu().getSlot(7).setItem(getSelectedDifficultyItem());
                setCityProjectItems();

                clickPlayer.playSound(clickPlayer.getLocation(), Utils.Done, 1, 1);
            } catch (SQLException ex) {
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // Add click event for city projects items
        for(int i = 0; i < cityProjects.size(); i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                if(!getMenu().getSlot(9 + itemSlot).getItem(clickPlayer).equals(MenuItems.errorItem())) {
                    try {
                        clickPlayer.closeInventory();
                        Builder builder = new Builder(clickPlayer.getUniqueId());
                        int cityID = cityProjects.get(itemSlot).getID();
                        if (builder.getFreeSlot() != null){
                            PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityID, builder);
                            if (PlotManager.getPlots(cityID, plotDifficultyForCity, Status.unclaimed).size() != 0){
                                clickPlayer.sendMessage(Utils.getInfoMessageFormat("Creating a new plot..."));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);

                                new PlotGenerator(cityID, plotDifficultyForCity, builder);
                            } else {
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat("This city project doesn't have any more plots left. Please select another project."));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                            }
                        } else {
                            clickPlayer.sendMessage(Utils.getErrorMessageFormat("All your slots are occupied! Please finish your current plots before creating a new one."));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        }
                    } catch (SQLException ex) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred! Please try again or contact a staff member."));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                }
            });
        }

        // Add Click Event For Player Plot Slots
        for(int i = 0; i < MAX_BUILDS; i++) {
            if(slots[i] != null) {
                int itemSlot = i;
                getMenu().getSlot(46 + i).setClickHandler((clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    try {
                        new PlotActionsMenu(clickPlayer, slots[itemSlot]);
                    } catch (Exception ex ) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("Something went wrong... please message a Manager or Developer."));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while opening the plot actions menu.", ex);
                    }
                });
            }
        }

        // Add click event for builder utilities item
        getMenu().getSlot(50).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Add click event for show plots item
        getMenu().getSlot(51).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Add Click Event For Player Settings Menu
//        getMenu().getSlot(52).setClickHandler(((clickPlayer, clickInformation) -> clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1)));
    }

   public static ItemStack getMenuItem() {
       return new ItemBuilder(Material.NETHER_STAR, 1)
               .setName("§b§lCompanion §7(Right Click)")
               .setEnchantment(true)
               .build();
   }

    // Set city project items
    private void setCityProjectItems() {
        for(int i = 0; i < cityProjects.size(); i++) {
            if(i <= 28) {
                ItemStack stateProjectItem = MenuItems.errorItem();
                switch (cityProjects.get(i).getState()) {
                    //Auto generated by python script
                    case AF:
                            //Afghanistan
                            stateProjectItem = Utils.getItemHead("21902");
                            break;
                    case AM:
                            //Armenia
                            stateProjectItem = Utils.getItemHead("25233");
                            break;
                    case AZ:
                            //Azerbaijan
                            stateProjectItem = Utils.getItemHead("25231");
                            break;
                    case BH:
                            //Bahrain
                            stateProjectItem = Utils.getItemHead("25229");
                            break;
                    case BD:
                            //Bangladesh
                            stateProjectItem = Utils.getItemHead("25228");
                            break;
                    case BT:
                            //Bhutan
                            stateProjectItem = Utils.getItemHead("25328");
                            break;
                    case BN:
                            //Brunei Darussalam
                            stateProjectItem = Utils.getItemHead("25326");
                            break;
                    case KH:
                            //Cambodia
                            stateProjectItem = Utils.getItemHead("25553");
                            break;
                    case CN:
                            //China
                            stateProjectItem = Utils.getItemHead("23238");
                            break;
                    case CY:
                            //Cyprus
                            stateProjectItem = Utils.getItemHead("25543");
                            break;
                    case GE:
                            //Georgia
                            stateProjectItem = Utils.getItemHead("26154");
                            break;
                    case HK:
                            //Hong Kong
                            stateProjectItem = Utils.getItemHead("27573");
                            break;
                    case IN:
                            //India
                            stateProjectItem = Utils.getItemHead("26189");
                            break;
                    case ID:
                            //Indonesia
                            stateProjectItem = Utils.getItemHead("38065");
                            break;
                    case IR:
                            //Iran (Islamic Republic of)
                            stateProjectItem = Utils.getItemHead("26188");
                            break;
                    case IQ:
                            //Iraq
                            stateProjectItem = Utils.getItemHead("26187");
                            break;
                    case IL:
                            //Israel
                            stateProjectItem = Utils.getItemHead("13994");
                            break;
                    case JP:
                            //Japan
                            stateProjectItem = Utils.getItemHead("23239");
                            break;
                    case JO:
                            //Jordan
                            stateProjectItem = Utils.getItemHead("26185");
                            break;
                    case KZ:
                            //Kazakhstan
                            stateProjectItem = Utils.getItemHead("26184");
                            break;
                    case KP:
                            //Korea (Democratic People's Republic of)
                            stateProjectItem = Utils.getItemHead("888");
                            break;
                    case KR:
                            //Korea, Republic of
                            stateProjectItem = Utils.getItemHead("27583");
                            break;
                    case KW:
                            //Kuwait
                            stateProjectItem = Utils.getItemHead("26181");
                            break;
                    case KG:
                            //Kyrgyzstan
                            stateProjectItem = Utils.getItemHead("26180");
                            break;
                    case LA:
                            //Lao People's Democratic Republic
                            stateProjectItem = Utils.getItemHead("5592");
                            break;
                    case LB:
                            //Lebanon
                            stateProjectItem = Utils.getItemHead("26178");
                            break;
                    case MO:
                            //Macao
                            stateProjectItem = Utils.getItemHead("27572");
                            break;
                    case MY:
                            //Malaysia
                            stateProjectItem = Utils.getItemHead("26172");
                            break;
                    case MV:
                            //Maldives
                            stateProjectItem = Utils.getItemHead("26171");
                            break;
                    case MN:
                            //Mongolia
                            stateProjectItem = Utils.getItemHead("26166");
                            break;
                    case MM:
                            //Myanmar
                            stateProjectItem = Utils.getItemHead("26163");
                            break;
                    case NP:
                            //Nepal
                            stateProjectItem = Utils.getItemHead("26404");
                            break;
                    case OM:
                            //Oman
                            stateProjectItem = Utils.getItemHead("26398");
                            break;
                    case PK:
                            //Pakistan
                            stateProjectItem = Utils.getItemHead("23241");
                            break;
                    case PS:
                            //Palestine, State of
                            stateProjectItem = Utils.getItemHead("26396");
                            break;
                    case PH:
                            //Philippines
                            stateProjectItem = Utils.getItemHead("27579");
                            break;
                    case QA:
                            //Qatar
                            stateProjectItem = Utils.getItemHead("26393");
                            break;
                    case SA:
                            //Saudi Arabia
                            stateProjectItem = Utils.getItemHead("26656");
                            break;
                    case SG:
                            //Singapore
                            stateProjectItem = Utils.getItemHead("891");
                            break;
                    case LK:
                            //Sri Lanka
                            stateProjectItem = Utils.getItemHead("26648");
                            break;
                    case SY:
                            //Syrian Arab Republic
                            stateProjectItem = Utils.getItemHead("26677");
                            break;
                    case TW:
                            //Taiwan
                            stateProjectItem = Utils.getItemHead("11627");
                            break;
                    case TJ:
                            //Tajikistan
                            stateProjectItem = Utils.getItemHead("26676");
                            break;
                    case TH:
                            //Thailand
                            stateProjectItem = Utils.getItemHead("13993");
                            break;
                    case TL:
                            //Timor-Leste
                            stateProjectItem = Utils.getItemHead("25535");
                            break;
                    case TR:
                            //Turkey
                            stateProjectItem = Utils.getItemHead("4405");
                            break;
                    case TM:
                            //Turkmenistan
                            stateProjectItem = Utils.getItemHead("26669");
                            break;
                    case UAE:
                            //United Arab Emirates
                            stateProjectItem = Utils.getItemHead("26667");
                            break;
                    case UZ:
                            //Uzbekistan
                            stateProjectItem = Utils.getItemHead("26666");
                            break;
                    case VN:
                            //Vietnam
                            stateProjectItem = Utils.getItemHead("26662");
                            break;
                    case YE:
                            //Yemen
                            stateProjectItem = Utils.getItemHead("5580");
                            break;
                    default:
                        stateProjectItem = Utils.getItemHead("36076"); // Barrier
                     break;
                }

                try {
                    PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ?
                            selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityProjects.get(i).getID(), new Builder(getMenuPlayer().getUniqueId()));

                    getMenu().getSlot(9 + i)
                            .setItem(new ItemBuilder(stateProjectItem)
                                    .setName("§b§l" + cityProjects.get(i).getName())
                                    .setLore(new LoreBuilder()
                                            .addLines(cityProjects.get(i).getDescription(),
                                                    "",
                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.unclaimed).size() + " §7Plots Open",
                                                    "§f---------------------",
                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.unfinished, Status.unreviewed).size() + " §7Plots In Progress",
                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.complete).size() + " §7Plots Completed",
                                                    "",
                                                    PlotManager.getPlots(cityProjects.get(i).getID(), plotDifficultyForCity, Status.unclaimed).size() != 0 ?
                                                            Utils.getFormattedDifficulty(plotDifficultyForCity) : "§f§lNo Plots Available"
                                                    )
                                            .build())
                                    .build());
                } catch (SQLException ex) {
                    getMenu().getSlot(9 + i).setItem(MenuItems.errorItem());
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            }
        }
    }

    // Get selected plot difficulty item of player
    private ItemStack getSelectedDifficultyItem() throws SQLException {
        ItemStack item = Utils.getItemHead("9248");

        if(selectedPlotDifficulty != null) {
            if(selectedPlotDifficulty == PlotDifficulty.EASY) {
                item = Utils.getItemHead("10220");
            } else if(selectedPlotDifficulty == PlotDifficulty.MEDIUM) {
                item = Utils.getItemHead("9680");
            } else if(selectedPlotDifficulty == PlotDifficulty.HARD){
                item = Utils.getItemHead("9356");
            }
        }

        return new ItemBuilder(item)
                .setName("§b§lPLOT DIFFICULTY")
                .setLore(new LoreBuilder()
                        .addLines("",
                                selectedPlotDifficulty != null ? Utils.getFormattedDifficulty(selectedPlotDifficulty) : "§f§lAutomatic",
                                selectedPlotDifficulty != null ? "§7Score Multiplier: §fx" + PlotManager.getMultiplierByDifficulty(selectedPlotDifficulty) : "",
                                "",
                                "§7Click To Switch...")
                        .build())
                .build();
    }
}

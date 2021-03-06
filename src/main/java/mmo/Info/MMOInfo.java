/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Info;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mmo.Core.CoreAPI.MMOHUDEvent;
import mmo.Core.InfoAPI.MMOInfoEvent;
import mmo.Core.MMO;
import mmo.Core.MMOPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.ContainerType;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericGradient;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class MMOInfo extends MMOPlugin implements Listener {
	static String config_info = "{name}~mmoMinecraft~{compass}{coords}";
	int height = 10, offset = 1;

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void loadConfiguration(FileConfiguration cfg) {
		config_info = cfg.getString("info", config_info);
	}

	@EventHandler
	public void onMMOHUD(MMOHUDEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("mmo.info.display")) {
			if (event.getAnchor() == WidgetAnchor.TOP_LEFT || event.getAnchor() == WidgetAnchor.TOP_CENTER || event.getAnchor() == WidgetAnchor.TOP_RIGHT) {
				event.setOffsetY(event.getOffsetY() + height + offset + 1);
			}
		}
	}

	@EventHandler
	public void onMMOInfo(MMOInfoEvent event) {
		if (event.isToken("name")) {
			SpoutPlayer player = event.getPlayer();
			event.setWidget(plugin, new GenericLabel(MMO.getName(player)).setResize(true).setFixed(true));
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("info")) {
			onSpoutCraftPlayer(SpoutManager.getPlayer((Player) sender));
			return true;
		}
		return false;
	}
	
	public void onSpoutCraftPlayer(SpoutPlayer player) {
		if (!player.hasPermission("mmo.info.display")) {
			return;
		}
		Container left, center, right, current;
		Color back = new Color(0f, 0f, 0f, 0.75f);
		Color bottom = new Color(1f, 1f, 1f, 0.75f);
		Screen screen = player.getMainScreen();

		screen.removeWidgets(this); // In case we're re-creating the bar on config change
		screen.attachWidget(plugin, new GenericGradient() //
				.setBottomColor(back) //
				.setTopColor(back) //
				.setMaxWidth(2048) //
				.setX(0) //
				.setY(0) //
				.setWidth(2048) //
				.setHeight(height + offset) //
				.setAnchor(WidgetAnchor.TOP_LEFT) //
				.setPriority(RenderPriority.Highest));
		screen.attachWidget(plugin, new GenericGradient() //
				.setBottomColor(bottom) //
				.setTopColor(bottom) //
				.setX(0) //
				.setY(height + offset) //
				.setMaxWidth(2048) //
				.setWidth(2048) //
				.setHeight(1) //
				.setAnchor(WidgetAnchor.TOP_LEFT) //
				.setPriority(RenderPriority.Highest));
		screen.attachWidget(plugin, left = (Container) new GenericContainer() //
				.setLayout(ContainerType.HORIZONTAL) //
				.setAlign(WidgetAnchor.TOP_LEFT) //
				.setAnchor(WidgetAnchor.TOP_LEFT) //
				.setWidth(427) //
				.setHeight(height) //
				.setX(0) //
				.setY(offset));
		screen.attachWidget(plugin, center = (Container) new GenericContainer() //
				.setLayout(ContainerType.HORIZONTAL) //
				.setAlign(WidgetAnchor.TOP_CENTER) //
				.setAnchor(WidgetAnchor.TOP_CENTER) //
				.setWidth(427) //
				.setHeight(height) //
				.setX(-213) //
				.setY(offset));
		screen.attachWidget(plugin, right = (Container) new GenericContainer() //
				.setLayout(ContainerType.HORIZONTAL) //
				.setAlign(WidgetAnchor.TOP_RIGHT) //
				.setAnchor(WidgetAnchor.TOP_RIGHT) //
				.setWidth(427) //
				.setHeight(height) //
				.setX(-427) //
				.setY(offset));
		//		screen.attachWidget(plugin, new GenericContainer(left, center, right));
		current = left;
		Matcher match = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}").matcher(config_info);
		while (match.find()) {
			if (match.group(1) != null) {
				if (current == left) {
					current = center;
				} else if (current == center) {
					current = right;
				} else {
					break;
				}
			} else if (match.group(2) != null) {
				String[] args = MMO.smartSplit(match.group(2));
				MMOInfoEventAPI event = new MMOInfoEventAPI(player, args[0], Arrays.copyOfRange(args, 1, args.length));
				pm.callEvent(event);
				if (!event.isCancelled() && event.getWidget() != null) {
					Widget widget = event.getWidget().setMargin(0, 3);
					Plugin widgetPlugin = event.getPlugin();
					if (event.getIcon() != null) {
						Widget icon = new GenericTexture(event.getIcon()).setMargin(0, 0, 0, 3).setHeight(8).setWidth(8).setFixed(true);
						screen.attachWidget(widgetPlugin, icon); // Widget owner
						current.addChild(icon);
					}
					screen.attachWidget(widgetPlugin, widget); // Widget owner
					current.addChild(widget);
				}
			} else {
				String str = match.group().trim();
				if (!str.isEmpty()) {
					current.addChild(new GenericLabel(str).setResize(true).setMargin(0, 3).setFixed(true));
				}
			}
		}
	}
}

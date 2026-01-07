package io.github.itokagimaru.artifact.utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public final class Utils {

    private Utils() {}

    public static TextComponent parseLegacy(String legacy) {
        Map<Character, NamedTextColor> colors = Map.ofEntries(
                Map.entry('0', NamedTextColor.BLACK),       Map.entry('1', NamedTextColor.DARK_BLUE),
                Map.entry('2', NamedTextColor.DARK_GREEN),  Map.entry('3', NamedTextColor.DARK_AQUA),
                Map.entry('4', NamedTextColor.DARK_RED),    Map.entry('5', NamedTextColor.DARK_PURPLE),
                Map.entry('6', NamedTextColor.GOLD),        Map.entry('7', NamedTextColor.GRAY),
                Map.entry('8', NamedTextColor.DARK_GRAY),   Map.entry('9', NamedTextColor.BLUE),
                Map.entry('a', NamedTextColor.GREEN),       Map.entry('b', NamedTextColor.AQUA),
                Map.entry('c', NamedTextColor.RED),         Map.entry('d', NamedTextColor.LIGHT_PURPLE),
                Map.entry('e', NamedTextColor.YELLOW),      Map.entry('f', NamedTextColor.WHITE)
        );

        Map<Character, TextDecoration> decoMap = Map.of(
                'l', TextDecoration.BOLD,
                'm', TextDecoration.STRIKETHROUGH,
                'n', TextDecoration.UNDERLINED,
                'o', TextDecoration.ITALIC
        );

        final NamedTextColor[] colorHolder = { null };
        final Set<TextDecoration> decorations = new HashSet<>();
        final List<Component> parts = new ArrayList<>();
        final StringBuilder buf = new StringBuilder();

        Runnable flush = () -> {
            if (buf.isEmpty()) return;
            Component comp = Component.text(buf.toString());
            if (colorHolder[0] != null) {
                comp = comp.color(colorHolder[0]);
            }
            if (!decorations.isEmpty()) {
                for (TextDecoration d : decorations) {
                    comp = comp.decoration(d, true);
                }
            }
            parts.add(comp);
            buf.setLength(0);
        };

        int i = 0;
        while (i < legacy.length()) {
            if (legacy.charAt(i) == '§' && i + 1 < legacy.length()) {
                char code = Character.toLowerCase(legacy.charAt(i + 1));

                if (colors.containsKey(code)) {
                    flush.run();
                    colorHolder[0] = colors.get(code);
                    decorations.clear();
                } else if (decoMap.containsKey(code)) {
                    flush.run();
                    decorations.add(decoMap.get(code));
                } else if (code == 'r') {
                    flush.run();
                    colorHolder[0] = null;
                    decorations.clear();
                }
                // マッチした場合は2文字進める
                i += 2;
            } else {
                buf.append(legacy.charAt(i));
                i++;
            }
        }
        flush.run();

        TextComponent.Builder builder = Component.text();
        for (Component part : parts) {
            builder.append(part);
        }
        return builder.decoration(TextDecoration.ITALIC, false).build();
    }

    public static String getPlainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static void promptTextInput(
            Player player,
            String message,
            int maxLength,
            Consumer<String> onComplete
    ) {
        player.sendMessage("§e" + message + " \n§7(最大" + maxLength + "文字)");
        player.sendMessage("§7キャンセルする場合は「cancel」と入力");

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(Utils.class);

        Listener listener = new Listener() {
            @EventHandler
            public void onChat(AsyncChatEvent event) {
                if (!event.getPlayer().equals(player)) return;

                event.setCancelled(true);
                HandlerList.unregisterAll(this);

                String input = PlainTextComponentSerializer.plainText()
                        .serialize(event.message())
                        .trim();

                if (input.length() > maxLength) {
                    input = input.substring(0, maxLength);
                }

                String finalInput = input;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (finalInput.equalsIgnoreCase("cancel")) {
                        player.sendMessage("§c入力をキャンセルしました。");
                    } else {
                        onComplete.accept(finalInput);
                    }
                });
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}

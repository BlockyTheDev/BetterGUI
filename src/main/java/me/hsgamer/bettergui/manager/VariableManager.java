package me.hsgamer.bettergui.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.hsgamer.bettergui.hook.PlaceholderAPIHook;
import me.hsgamer.bettergui.object.GlobalVariable;
import me.hsgamer.bettergui.util.BukkitUtils;
import me.hsgamer.bettergui.util.CommonUtils;
import me.hsgamer.bettergui.util.ExpressionUtils;
import me.hsgamer.bettergui.util.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class VariableManager {

  public static final Pattern PATTERN = Pattern.compile("[{]([^{}]+)[}]");
  private static final Map<String, GlobalVariable> variables = new HashMap<>();

  static {
    register("player", (executor, identifier) -> executor.getName());
    register("online",
        (executor, identifier) -> String.valueOf(BukkitUtils.getOnlinePlayers().size()));
    register("max_players", (executor, identifier) -> String.valueOf(Bukkit.getMaxPlayers()));
    register("world", (executor, identifier) -> executor.getWorld().getName());
    register("x", (executor, identifier) -> String.valueOf(executor.getLocation().getX()));
    register("y", (executor, identifier) -> String.valueOf(executor.getLocation().getY()));
    register("z", (executor, identifier) -> String.valueOf(executor.getLocation().getZ()));
    register("bed_", ((executor, identifier) -> {
      if (executor.getBedSpawnLocation() == null) {
        return null;
      } else if (identifier.equalsIgnoreCase("world")) {
        return executor.getBedSpawnLocation().getWorld().getName();
      } else if (identifier.equalsIgnoreCase("x")) {
        return String.valueOf(executor.getBedSpawnLocation().getX());
      } else if (identifier.equalsIgnoreCase("y")) {
        return String.valueOf(executor.getBedSpawnLocation().getY());
      } else if (identifier.equalsIgnoreCase("z")) {
        return String.valueOf(executor.getBedSpawnLocation().getZ());
      } else {
        return null;
      }
    }));
    register("exp", (executor, identifier) -> String.valueOf(executor.getTotalExperience()));
    register("level", (executor, identifier) -> String.valueOf(executor.getLevel()));
    register("exp_to_level", (executor, identifier) -> String.valueOf(executor.getExpToLevel()));
    register("food_level", (executor, identifier) -> String.valueOf(executor.getFoodLevel()));
    register("ip", (executor, identifier) -> executor.getAddress().getAddress().getHostAddress());
    register("biome",
        (executor, identifier) -> String.valueOf(executor.getLocation().getBlock().getBiome()));
    register("ping", ((executor, identifier) -> BukkitUtils.getPing(executor)));
    register("rainbow", (executor, identifier) -> {
      ChatColor[] values = ChatColor.values();
      ChatColor color;
      do {
        color = values[ThreadLocalRandom.current().nextInt(values.length - 1)];
      } while (color.equals(ChatColor.BOLD)
          || color.equals(ChatColor.ITALIC)
          || color.equals(ChatColor.STRIKETHROUGH)
          || color.equals(ChatColor.RESET)
          || color.equals(ChatColor.MAGIC)
          || color.equals(ChatColor.UNDERLINE));
      return CommonUtils.colorize("&" + color.getChar());
    });
    register("random_", (executor, identifier) -> {
      identifier = identifier.trim();
      if (identifier.contains(":")) {
        String[] split = identifier.split(":", 2);
        String s1 = split[0].trim();
        String s2 = split[1].trim();
        if (Validate.isValidInteger(s1) && Validate.isValidInteger(s2)) {
          int i1 = Integer.parseInt(s1);
          int i2 = Integer.parseInt(s2);
          int max = Math.max(i1, i2);
          int min = Math.min(i1, i2);
          return String.valueOf(min + ThreadLocalRandom.current().nextInt(max - min + 1));
        }
      } else if (Validate.isValidInteger(identifier)) {
        return String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(identifier)));
      }
      return null;
    });
    register("condition_", (executor, identifier) -> {
      if (ExpressionUtils.isValidExpression(identifier)) {
        return ExpressionUtils.getResult(identifier).toPlainString();
      }
      return null;
    });
    register("uuid", (executor, identifier) -> executor.getUniqueId().toString());
  }

  private VariableManager() {

  }

  /**
   * Register new variable
   *
   * @param prefix   the prefix
   * @param variable the Variable object
   */
  public static void register(String prefix, GlobalVariable variable) {
    variables.put(prefix, variable);
  }

  /**
   * Check if a string contains variables
   *
   * @param message the string
   * @return true if it has, otherwise false
   */
  public static boolean hasVariables(String message) {
    if (message == null || message.trim().isEmpty()) {
      return false;
    }
    if (isMatch(message, variables.keySet())) {
      return true;
    }
    return PlaceholderAPIHook.hasValidPlugin() && PlaceholderAPIHook.hasPlaceholders(message);
  }

  /**
   * Replace the variables of the string
   *
   * @param message  the string
   * @param executor the player involved in
   * @return the replaced string
   */
  public static String setVariables(String message, Player executor) {
    String old;
    do {
      old = message;
      message = setSingleVariables(message, executor);
    } while (hasVariables(message) && !old.equals(message));
    if (PlaceholderAPIHook.hasValidPlugin()) {
      message = PlaceholderAPIHook.setPlaceholders(message, executor);
    }
    return message;
  }

  private static String setSingleVariables(String message, Player executor) {
    Matcher matcher = PATTERN.matcher(message);
    while (matcher.find()) {
      String identifier = matcher.group(1).trim();
      for (Map.Entry<String, GlobalVariable> variable : variables.entrySet()) {
        if (identifier.startsWith(variable.getKey())) {
          String replace = variable.getValue()
              .getReplacement(executor, identifier.substring(variable.getKey().length()));
          if (replace != null) {
            message = message
                .replaceAll(Pattern.quote(matcher.group()), Matcher.quoteReplacement(replace));
          }
        }
      }
    }
    return message;
  }

  public static boolean isMatch(String string, Collection<String> matchString) {
    Pattern pattern = Pattern.compile("(" + String.join("|", matchString) + ").*");
    Matcher matcher = PATTERN.matcher(string);
    while (matcher.find()) {
      String identifier = matcher.group(1).trim();
      if (pattern.matcher(identifier).find()) {
        return true;
      }
    }
    return false;
  }
}

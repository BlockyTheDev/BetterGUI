package me.hsgamer.bettergui.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.hsgamer.hscore.variable.ExternalStringReplacer;
import me.hsgamer.hscore.variable.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import static me.hsgamer.bettergui.BetterGUI.getInstance;

public class ExtraPlaceholderExpansion extends PlaceholderExpansion {
  @Override
  public @NotNull String getIdentifier() {
    return getInstance().getName().toLowerCase(Locale.ROOT);
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public boolean register() {
    boolean success = super.register();
    if (success) {
      VariableManager.addExternalReplacer(new ExternalStringReplacer() {
        @Override
        public String replace(String original, UUID uuid) {
          return PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), original);
        }

        @Override
        public boolean canBeReplaced(String original) {
          return PlaceholderAPI.containsPlaceholders(original);
        }
      });
    }
    return success;
  }

  @Override
  public @NotNull String getAuthor() {
    return Arrays.toString(getInstance().getDescription().getAuthors().toArray());
  }

  @Override
  public @NotNull String getVersion() {
    return getInstance().getDescription().getVersion();
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String identifier) {
    return VariableManager.setVariables("{" + identifier + "}", player.getUniqueId());
  }
}
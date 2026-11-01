package com.fancyinnovations.fancyholograms.commands.fancyholograms;

import com.fancyinnovations.fancyholograms.api.data.HologramData;
import com.fancyinnovations.fancyholograms.api.hologram.Hologram;
import com.fancyinnovations.fancyholograms.commands.FancyContext;
import de.oliver.fancyanalytics.logger.LogLevel;
import de.oliver.fancylib.translations.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ReloadCMD extends FancyContext {

    public static final ReloadCMD INSTANCE = new ReloadCMD();

    private ReloadCMD() {
    }

    @Command("fancyholograms reload")
    @Description("Reloads the configuration and all holograms from storage")
    @CommandPermission("fancyholograms.commands.fancyholograms.reload")
    public void reload(
            final BukkitCommandActor actor
    ) {
        config.reload();
        logger.setCurrentLevel(LogLevel.valueOf(config.getLogLevel()));

        translator.loadLanguages(plugin.getDataFolder().getAbsolutePath());
        final Language selectedLanguage = translator.getLanguages().stream()
                .filter(language -> language.getLanguageName().equals(config.getLanguage()))
                .findFirst().orElse(translator.getFallbackLanguage());
        translator.setSelectedLanguage(selectedLanguage);

        Collection<Hologram> allHolograms = new ArrayList<>(plugin.getRegistry().getAll());

        plugin.getRegistry().clear();

        for (Hologram hologram : allHolograms) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                hologram.despawnFrom(player);
            }

            for (UUID viewer : hologram.getViewers()) {
                hologram.removeViewer(viewer);
            }
        }

        Collection<HologramData> hologramData = plugin.getStorage().loadAll();
        for (HologramData data : hologramData) {
            Hologram hologram = plugin.getHologramFactory().apply(data);
            plugin.getRegistry().register(hologram);
        }

        translator.translate("commands.fancyholograms.reload.success")
                .withPrefix()
                .replace("count", String.valueOf(hologramData.size()))
                .send(actor.sender());
    }

}

package io.github.itokagimaru.artifact.artifact.artifacts.config;

import io.github.itokagimaru.artifact.ArtifactMain;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

public class UiConfig {
    Plugin plugin;
    YamlConfiguration config;
    private Material artifactMaterial;
    private Material skillMaterial;
    private Material uiMaterial;
    List<Float> cutIconCMD;
    Float statViewerCMD;

    public UiConfig(Plugin plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "ui_config.yml");

        // ファイルが存在しない場合はデフォルトを作成
        if (!configFile.exists()) {
            plugin.saveResource("ui_config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        loadConfig();
    }

    public void loadConfig() {
        artifactMaterial = Material.getMaterial(config.getString("artifact.material"));
        if (artifactMaterial == null) {
            artifactMaterial = Material.BARRIER;
            ArtifactMain.getInstance().errorLog("artifacts material is not found");
        }
        skillMaterial = Material.getMaterial(config.getString("skill.material"));
        if (skillMaterial == null) {
            skillMaterial = Material.BARRIER;
            ArtifactMain.getInstance().errorLog("skill material is not found");
        }
        uiMaterial = Material.getMaterial(config.getString("icons.material"));
        if (artifactMaterial == null) {
            artifactMaterial = Material.BARRIER;
            ArtifactMain.getInstance().errorLog("icons material is not found");
        }
        cutIconCMD = config.getFloatList("icons.cmd.cut");
        if (cutIconCMD.size() != 6) {
            ArtifactMain.getInstance().errorLog("ui_config.icons.cmd.cut list size must be 6 (found: " + cutIconCMD.size() + ")");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        double statViewerCMD = config.getDouble("icons.cmd.stat_viewer");
        this.statViewerCMD = (float) statViewerCMD;
    }

    public Material getArtifactMaterial() {
        return artifactMaterial;
    }

    public Material getSkillMaterial() {
        return skillMaterial;
    }

    public Material getUiMaterial() {
        return uiMaterial;
    }

    public List<Float> getCutIconCMD() {
        return cutIconCMD;
    }

    public Float getStatViewerCMD() {
        return statViewerCMD;
    }
}

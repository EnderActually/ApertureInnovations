package net.mistersecret312.aperture_innovations;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForgeConfig;

import java.io.File;

public class Config
{
	private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec COMMON_CONFIG;

	static
	{
		COMMON_BUILDER.push("portal_gun");
		PortalGunConfig.init(COMMON_BUILDER);
		COMMON_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
	}

	public static void loadConfig(ModConfigSpec config, String path)
	{
		ApertureInnovations.LOGGER.info("Loading Config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(
				WritingMode.REPLACE).build();
		ApertureInnovations.LOGGER.info("Built config: " + path);
		file.load();
		ApertureInnovations.LOGGER.info("Loaded Config: " + path);
		config.correct(file);
	}

}

package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import java.util.ArrayList;
import java.util.List;

public class StatisticsInit
{
	private static final List<Runnable> STATISTIC_SETUP = new ArrayList<>();

	public static final ResourceLocation TIMES_USED_PORTALS =
			registerDefaultStatistic("times_used_portals");

	private static ResourceLocation registerDefaultStatistic(String key)
	{
		ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, key);
		STATISTIC_SETUP.add(() -> Stats.CUSTOM.get(resourceLocation, StatFormatter.DEFAULT));
		return resourceLocation;
	}

	public static void register()
	{
		STATISTIC_SETUP.forEach(Runnable::run);
	}
}

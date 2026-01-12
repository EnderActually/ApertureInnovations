package net.mistersecret312.aperture_innovations.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class PortalGunConfig
{
	public static ModConfigSpec.BooleanValue use_portalable_tag;
	public static ModConfigSpec.IntValue portal_gun_shoot_range;

	public static ModConfigSpec.BooleanValue portal_gun_uses_energy;
	public static ModConfigSpec.IntValue portal_gun_max_energy_stored;
	public static ModConfigSpec.IntValue portal_gun_shoot_consumption;
	public static ModConfigSpec.IntValue portal_gun_passive_consumption;
	public static ModConfigSpec.BooleanValue portal_gun_consume_on_shot;

	public static void init(ModConfigSpec.Builder server)
	{
		use_portalable_tag = server
			.comment("If true, portals can be placed only on blocks in the Portalable block tag")
			.define("portalable_tag", false);

		portal_gun_shoot_range = server
			.comment("How far the Portal Gun can shoot and open a portal")
			.defineInRange("portal_gun_shoot_range", 256, 0, Integer.MAX_VALUE);

		portal_gun_uses_energy = server
			.comment("If true, the Portal Gun will require energy to create and upkeep portals")
			.define("portal_gun_uses_energy", false);

		portal_gun_max_energy_stored = server
			.comment("The maximum amount of energy the Portal Gun can store in itself")
			.defineInRange("portal_gun_max_energy_stored", 1000000, 0, Integer.MAX_VALUE);

		portal_gun_consume_on_shot = server
			.comment("If true, the Portal Gun will consume energy when shooting the portal, not when it actually opens")
			.define("portal_gun_consume_on_shot", false);

		portal_gun_shoot_consumption = server
			.comment("The amount of energy the Portal Gun uses when shooting/opening the portal")
			.defineInRange("portal_gun_shoot_consumption", 2500, 0, Integer.MAX_VALUE);

		portal_gun_passive_consumption = server
			.comment("The amount of energy the Portal Gun uses when two portals are open")
			.defineInRange("portal_gun_passive_consumption", 10, 0, Integer.MAX_VALUE);
	}
}

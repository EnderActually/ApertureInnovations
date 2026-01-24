package net.mistersecret312.aperture_innovations.init;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

public class SoundInit
{
    public static SoundEvent PORTAL_OPEN_PRIMARY = registerSoundEvent("portal_open_primary");
    public static SoundEvent PORTAL_OPEN_SECONDARY = registerSoundEvent("portal_open_secondary");
    public static SoundEvent PORTAL_AMBIENT = registerSoundEvent("portal_ambient");
    public static SoundEvent PORTAL_ENTER = registerSoundEvent("portal_enter");
    public static SoundEvent PORTAL_INVALID_SURFACE = registerSoundEvent("portal_invalid_surface");
    public static SoundEvent PORTAL_FIZZLE = registerSoundEvent("portal_fizzle");

    public static SoundEvent PORTAL_GUN_ACTIVATION = registerSoundEvent("portal_gun_activation");
    public static SoundEvent PORTAL_GUN_FIRE_PRIMARY = registerSoundEvent("portal_gun_fire_primary");
    public static SoundEvent PORTAL_GUN_FIRE_SECONDARY = registerSoundEvent("portal_gun_fire_secondary");
    public static SoundEvent PORTAL_GUN_RESET = registerSoundEvent("portal_gun_reset");

    public static SoundEvent LONG_FALL_BOOTS_LAND = registerSoundEvent("long_fall_boots_land");

	private static SoundEvent registerSoundEvent(String id) {
		ResourceLocation location = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, id);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, location, SoundEvent.createVariableRangeEvent(location));
	}

	public static void initialize() {

	}
}

package net.mistersecret312.aperture_innovations.init;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class SoundInit
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ApertureInnovations.MODID);

    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_OPEN_PRIMARY = registerSoundEvent("portal_open_primary");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_OPEN_SECONDARY = registerSoundEvent("portal_open_secondary");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_AMBIENT = registerSoundEvent("portal_ambient");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_ENTER = registerSoundEvent("portal_enter");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_INVALID_SURFACE = registerSoundEvent("portal_invalid_surface");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_FIZZLE = registerSoundEvent("portal_fizzle");

    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_GUN_ACTIVATION = registerSoundEvent("portal_gun_activation");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_GUN_FIRE_PRIMARY = registerSoundEvent("portal_gun_fire_primary");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_GUN_FIRE_SECONDARY = registerSoundEvent("portal_gun_fire_secondary");
    public static DeferredHolder<SoundEvent, SoundEvent> PORTAL_GUN_RESET = registerSoundEvent("portal_gun_reset");

    public static DeferredHolder<SoundEvent, SoundEvent> LONG_FALL_BOOTS_LAND = registerSoundEvent("long_fall_boots_land");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, sound)));
    }

    public static void register(IEventBus bus)
    {
        SOUNDS.register(bus);
    }
}

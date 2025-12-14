package net.mistersecret312.aperture_innovations.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

public class SoundInit
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ApertureInnovations.MODID);

    public static RegistryObject<SoundEvent> PORTAL_OPEN_PRIMARY = registerSoundEvent("portal_open_primary");
    public static RegistryObject<SoundEvent> PORTAL_OPEN_SECONDARY = registerSoundEvent("portal_open_secondary");
    public static RegistryObject<SoundEvent> PORTAL_AMBIENT = registerSoundEvent("portal_ambient");
    public static RegistryObject<SoundEvent> PORTAL_ENTER = registerSoundEvent("portal_enter");
    public static RegistryObject<SoundEvent> PORTAL_INVALID_SURFACE = registerSoundEvent("portal_invalid_surface");
    public static RegistryObject<SoundEvent> PORTAL_FIZZLE = registerSoundEvent("portal_fizzle");

    public static RegistryObject<SoundEvent> PORTAL_GUN_ACTIVATION = registerSoundEvent("portal_gun_activation");
    public static RegistryObject<SoundEvent> PORTAL_GUN_FIRE_PRIMARY = registerSoundEvent("portal_gun_fire_primary");
    public static RegistryObject<SoundEvent> PORTAL_GUN_FIRE_SECONDARY = registerSoundEvent("portal_gun_fire_secondary");
    public static RegistryObject<SoundEvent> PORTAL_GUN_RESET = registerSoundEvent("portal_gun_reset");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ApertureInnovations.MODID, sound)));
    }

    public static void register(IEventBus bus)
    {
        SOUNDS.register(bus);
    }
}

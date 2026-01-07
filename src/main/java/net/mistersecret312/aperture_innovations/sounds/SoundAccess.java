package net.mistersecret312.aperture_innovations.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariant;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;

import java.util.Optional;
import java.util.UUID;

public class SoundAccess
{
	public static final String EMPTY = new ResourceLocation(ApertureInnovations.MODID, "empty").toString();

	protected static Minecraft minecraft = Minecraft.getInstance();

	public static void playPortalAmbient(UUID uuid, boolean isPrimary, boolean stop)
	{
		ClientPortalLink link = PortalUtilities.getPortalLinks().get(uuid);
		if(minecraft.level.dimension() != PortalUtilities.getPortalDimension(minecraft.level, uuid, isPrimary))
			return;

		PortalSoundWrapper.PortalAmbient ambientSound = ClientPortalUtilities.getAmbientSound(uuid, isPrimary);

		if(link.isOpen() && PortalUtilities.getPortalPos(minecraft.level, uuid, isPrimary) != null)
		{
			if(ambientSound == null)
			{
				ambientSound = new PortalSoundWrapper.PortalAmbient(link, isPrimary);
				ClientPortalUtilities.setAmbientSound(ambientSound, uuid, isPrimary);
			}

			BlockPos portalPos = isPrimary ? link.posPrimary() : link.posSecondary();
			if(!portalPos.equals(ambientSound.sound.portalPos))
			{
				ambientSound.stopSound();
				ambientSound = new PortalSoundWrapper.PortalAmbient(link, isPrimary);
				ClientPortalUtilities.setAmbientSound(ambientSound, uuid, isPrimary);
			}
		}

		if(stop)
			ambientSound.stopSound();
		else if(!ambientSound.isPlaying())
		{
			ambientSound.playSound();
		};
	}

	public static void playOpenPortalSound(UUID linkID, boolean isPrimary)
	{
		if(minecraft.level.dimension() != PortalUtilities.getPortalDimension(minecraft.level, linkID, isPrimary))
			return;
		GenericPortalSound sound = new GenericPortalSound(PortalUtilities.getPortalLinks().get(linkID), isPrimary,
				getPortalOpenSound(linkID, isPrimary), 5, 10, 0.5F);
		ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, isPrimary);
		//playPortalAmbient(linkID, isPrimary, false);
		minecraft.getSoundManager().play(sound);
	}

	public static void playEnterPortalSound(UUID linkID, boolean isPrimary)
	{
		if(minecraft.level.dimension() != PortalUtilities.getPortalDimension(minecraft.level, linkID, isPrimary))
			return;
		GenericPortalSound sound = new GenericPortalSound(PortalUtilities.getPortalLinks().get(linkID), isPrimary,
				getEnterPortalSound(linkID, isPrimary), 5, 10, 0.35F);
		minecraft.getSoundManager().play(sound);
	}

	public static void playFizzlePortalSound(UUID linkID, boolean isPrimary)
	{
		if(minecraft.level.dimension() != PortalUtilities.getPortalDimension(minecraft.level, linkID, isPrimary))
			return;
		GenericPortalSound sound = new GenericPortalSound(PortalUtilities.getPortalLinks().get(linkID), isPrimary,
				getFizzlePortalSound(linkID, isPrimary), 5, 10, 0.5F);
		ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, isPrimary);
		minecraft.getSoundManager().play(sound);
		playPortalAmbient(linkID, isPrimary, true);
	}

	public static void playInvalidSurfaceSound(UUID linkID, BlockPos pos, boolean isPrimary)
	{
		GenericGunSound sound = new GenericGunSound(PortalUtilities.getPortalLinks().get(linkID), pos, isPrimary,
				getInvalidSurfaceSound(linkID, isPrimary), 5, 10, 0.5F);
		minecraft.getSoundManager().play(sound);
	}

	public static void playGunActivateSound(UUID linkID, BlockPos pos, boolean isPrimary)
	{
		GenericGunSound sound = new GenericGunSound(PortalUtilities.getPortalLinks().get(linkID), pos, isPrimary,
				getGunActivateSound(linkID), 5, 10, 0.5F);
		minecraft.getSoundManager().play(sound);
	}

	public static void playShootPortalSound(UUID linkID, BlockPos pos, boolean isPrimary)
	{
		GenericGunSound sound = new GenericGunSound(PortalUtilities.getPortalLinks().get(linkID), pos, isPrimary,
				getShotPortalSound(linkID, isPrimary), 5, 10, 0.5F);
		minecraft.getSoundManager().play(sound);
	}

	public static void playResetPortalSound(UUID linkID, BlockPos pos, boolean isPrimary)
	{
		GenericGunSound sound = new GenericGunSound(PortalUtilities.getPortalLinks().get(linkID), pos, isPrimary,
				getGunResetSound(linkID), 5, 10, 0.5F);
		minecraft.getSoundManager().play(sound);
	}

	public static SoundEvent getPortalAmbient(UUID uuid, boolean isPrimary)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		if(isPrimary)
			return SoundEvent.createVariableRangeEvent(variant.getPrimaryPortal().getAmbientSound());
		else return SoundEvent.createVariableRangeEvent(variant.getSecondaryPortal().getAmbientSound());
	}

	public static SoundEvent getPortalOpenSound(UUID uuid, boolean isPrimary)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		if(isPrimary)
			return SoundEvent.createVariableRangeEvent(variant.getPrimaryPortal().getOpeningSound());
		else return SoundEvent.createVariableRangeEvent(variant.getSecondaryPortal().getOpeningSound());
	}

	public static SoundEvent getEnterPortalSound(UUID uuid, boolean isPrimary)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		if(isPrimary)
			return SoundEvent.createVariableRangeEvent(variant.getPrimaryPortal().getEnterSound());
		else return SoundEvent.createVariableRangeEvent(variant.getSecondaryPortal().getEnterSound());
	}

	public static SoundEvent getFizzlePortalSound(UUID uuid, boolean isPrimary)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		if(isPrimary)
			return SoundEvent.createVariableRangeEvent(variant.getPrimaryPortal().getFizzleSound());
		else return SoundEvent.createVariableRangeEvent(variant.getSecondaryPortal().getFizzleSound());
	}

	public static SoundEvent getInvalidSurfaceSound(UUID uuid, boolean isPrimary)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		if(isPrimary)
			return SoundEvent.createVariableRangeEvent(variant.getPrimaryPortal().getInvalidSurfaceSound());
		else return SoundEvent.createVariableRangeEvent(variant.getSecondaryPortal().getInvalidSurfaceSound());
	}

	public static SoundEvent getGunActivateSound(UUID uuid)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		return SoundEvent.createVariableRangeEvent(variant.getActivationSound());
	}

	public static SoundEvent getShotPortalSound(UUID uuid, boolean isPrimary)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		if(isPrimary)
			return SoundEvent.createVariableRangeEvent(variant.getPrimaryPortal().getShotSound());
		else return SoundEvent.createVariableRangeEvent(variant.getSecondaryPortal().getShotSound());
	}

	public static SoundEvent getGunResetSound(UUID uuid)
	{
		ClientPortalGunVariant variant = PortalUtilities.getPortalLinks().get(uuid).getVariant();
		return SoundEvent.createVariableRangeEvent(variant.getResetSound());
	}

}


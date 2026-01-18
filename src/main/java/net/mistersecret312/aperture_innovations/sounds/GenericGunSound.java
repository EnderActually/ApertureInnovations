package net.mistersecret312.aperture_innovations.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;

public class GenericGunSound extends PortalSound<ClientPortalLink>
{

	public GenericGunSound(ClientPortalLink link, BlockPos pos, boolean isPrimary, SoundEvent soundEvent, int fullDistance,
						   int maxDistance, float maxVolume)
	{
		super(link, isPrimary, soundEvent, SoundSource.PLAYERS, fullDistance, maxDistance);
		this.portalPos = pos;
		this.volume = maxVolume;
	}

	@Override
	public boolean isLooping()
	{
		return false;
	}

	@Override
	public float getMaxVolume()
	{
		return this.volume;
	}
}

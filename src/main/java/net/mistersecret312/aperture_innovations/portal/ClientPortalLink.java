package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariant;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariants;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.sounds.PortalSoundWrapper;

import java.util.UUID;


public class ClientPortalLink
{
	private UUID linkID;

	private BlockPos posPrimary;
	private BlockPos posSecondary;

	private boolean wallPrimary;
	private boolean wallSecondary;

	private boolean ceilingPrimary;
	private boolean ceilingSecondary;

	private ResourceKey<Level> dimensionPrimary;
	private ResourceKey<Level> dimensionSecondary;

	private Direction directionPrimary;
	private Direction directionSecondary;

	private boolean moonshotPrimary;
	private boolean moonshotSecondary;

	private int openingPrimary;
	private int openingSecondary;
	
	private ResourceLocation variantKey;
	
	public PortalSoundWrapper.PortalAmbient primaryAmbient = null;
	public PortalSoundWrapper.PortalAmbient secondaryAmbient = null;

	public ClientPortalLink(UUID linkID, BlockPos posPrimary, BlockPos posSecondary,
					 boolean wallPrimary, boolean wallSecondary,
					 boolean ceilingPrimary, boolean ceilingSecondary,
					 ResourceKey<Level> dimensionPrimary, ResourceKey<Level> dimensionSecondary,
					 Direction directionPrimary, Direction directionSecondary,
					 boolean moonshotPrimary, boolean moonshotSecondary,
					 int openingPrimary, int openingSecondary,
					 ResourceLocation variantKey)
	{
		this.linkID = linkID;
		
		this.posPrimary = posPrimary;
		this.posSecondary = posSecondary;
		
		this.wallPrimary = wallPrimary;
		this.wallSecondary = wallSecondary;
		
		this.ceilingPrimary = ceilingPrimary;
		this.ceilingSecondary = ceilingSecondary;
		
		this.dimensionPrimary = dimensionPrimary;
		this.dimensionSecondary = dimensionSecondary;
		
		this.directionPrimary = directionPrimary;
		this.directionSecondary = directionSecondary;
		
		this.moonshotPrimary = moonshotPrimary;
		this.moonshotSecondary = moonshotSecondary;
		
		this.openingPrimary = openingPrimary;
		this.openingSecondary = openingSecondary;
		
		this.variantKey = variantKey;
	}

	public boolean isOpen()
	{
		return (posPrimary != null || moonshotPrimary) && (posSecondary != null || moonshotSecondary);
	}

	public ClientPortalGunVariant getVariant()
	{
		Registry<PortalGunVariant> registry =
				Minecraft.getInstance().level.registryAccess().registryOrThrow(PortalGunVariant.REGISTRY_KEY);
		PortalGunVariant dataVariant = registry.get(variantKey);

		return ClientPortalGunVariants.getPortalGunVariant(dataVariant.getClientVariant());
	}
	
	public UUID linkID()
	{
		return linkID;
	}

	public BlockPos posPrimary()
	{
		return posPrimary;
	}

	public BlockPos posSecondary()
	{
		return posSecondary;
	}

	public Direction directionPrimary()
	{
		return directionPrimary;
	}

	public Direction directionSecondary()
	{
		return directionSecondary;
	}

	public ResourceKey<Level> dimensionPrimary()
	{
		return dimensionPrimary;
	}

	public ResourceKey<Level> dimensionSecondary()
	{
		return dimensionSecondary;
	}

	public int openingPrimary()
	{
		return openingPrimary;
	}

	public int openingSecondary()
	{
		return openingSecondary;
	}

	public ResourceLocation variantKey()
	{
		return variantKey;
	}

	public boolean ceilingPrimary()
	{
		return ceilingPrimary;
	}

	public boolean ceilingSecondary()
	{
		return ceilingSecondary;
	}

	public boolean moonshotPrimary()
	{
		return moonshotPrimary;
	}

	public boolean moonshotSecondary()
	{
		return moonshotSecondary;
	}

	public boolean wallPrimary()
	{
		return wallPrimary;
	}

	public boolean wallSecondary()
	{
		return wallSecondary;
	}

	public void stopAmbient(boolean isPrimary)
	{
		if(isPrimary)
			primaryAmbient.stopSound();
		else secondaryAmbient.stopSound();
	}

	public void playAmbient(boolean isPrimary)
	{
		if(!this.primaryAmbient.isPlaying() && !this.secondaryAmbient.isPlaying())
		{
			if(isPrimary)
				primaryAmbient.playSound();
			else secondaryAmbient.playSound();
		}
	}
}

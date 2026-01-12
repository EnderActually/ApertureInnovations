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
	public UUID linkID;

	public BlockPos posPrimary;
	public BlockPos posSecondary;

	public boolean wallPrimary;
	public boolean wallSecondary;

	public boolean ceilingPrimary;
	public boolean ceilingSecondary;

	public ResourceKey<Level> dimensionPrimary;
	public ResourceKey<Level> dimensionSecondary;

	public Direction directionPrimary;
	public Direction directionSecondary;

	public boolean moonshotPrimary;
	public boolean moonshotSecondary;
	
	public ResourceLocation variantKey;

	public int primaryPortalColor = -1;
	public int secondaryPortalColor = -1;
	
	public PortalSoundWrapper.PortalAmbient primaryAmbient = null;
	public PortalSoundWrapper.PortalAmbient secondaryAmbient = null;

	public ClientPortalLink()
	{}

	public ClientPortalLink(UUID linkID, BlockPos posPrimary, BlockPos posSecondary,
					 boolean wallPrimary, boolean wallSecondary,
					 boolean ceilingPrimary, boolean ceilingSecondary,
					 ResourceKey<Level> dimensionPrimary, ResourceKey<Level> dimensionSecondary,
					 Direction directionPrimary, Direction directionSecondary,
					 boolean moonshotPrimary, boolean moonshotSecondary,
					 float openingPrimary, float openingSecondary,
					 ResourceLocation variantKey, int primaryPortalColor, int secondaryPortalColor)
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
		
		this.variantKey = variantKey;

		this.primaryPortalColor = primaryPortalColor;
		this.secondaryPortalColor = secondaryPortalColor;
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

	public int primaryPortalColor()
	{
		return primaryPortalColor;
	}

	public int secondaryPortalColor()
	{
		return secondaryPortalColor;
	}

	public void stopAmbient(boolean isPrimary)
	{
		if(primaryAmbient != null)
			primaryAmbient.stopSound();
		if(secondaryAmbient != null)
			secondaryAmbient.stopSound();
	}

	public void playAmbient(boolean isPrimary)
	{
		if(this.primaryAmbient != null)
			if(!this.primaryAmbient.isPlaying())
				primaryAmbient.playSound();

		if(this.secondaryAmbient != null)
			if(!this.secondaryAmbient.isPlaying())
				secondaryAmbient.playSound();

	}
}

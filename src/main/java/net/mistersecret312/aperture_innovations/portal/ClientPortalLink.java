package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;


public record ClientPortalLink(UUID linkID, BlockPos posPrimary, BlockPos posSecondary,
							   boolean wallPrimary, boolean wallSecondary,
							   boolean ceilingPrimary, boolean ceilingSecondary,
							   ResourceKey<Level> dimensionPrimary, ResourceKey<Level> dimensionSecondary,
							   Direction directionPrimary, Direction directionSecondary,
							   boolean moonshotPrimary, boolean moonshotSecondary,
							   int openingPrimary, int openingSecondary)
{
	public boolean isOpen()
	{
		return (posPrimary != null || moonshotPrimary) && (posSecondary != null || moonshotSecondary);
	}
}

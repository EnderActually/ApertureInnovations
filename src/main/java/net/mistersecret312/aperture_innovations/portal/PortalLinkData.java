package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalLinkData extends SavedData
{
	private static final String FILE_NAME = ApertureInnovations.MODID + "-portal_links";

	private static final String PORTAL_LINK = "portal_links";

	public HashMap<UUID, PortalLink> portalLinks = new HashMap<>();

	private MinecraftServer server;

	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();

		tag.put(PORTAL_LINK, serializePortalLinkData());

		return tag;
	}

	private CompoundTag serializePortalLinkData()
	{
		CompoundTag objectsTag = new CompoundTag();

		this.portalLinks.forEach((uuid, pad) ->
			{
				objectsTag.put(uuid.toString(), pad.save());
			});

		return objectsTag;
	}

	private void deserialize(CompoundTag tag)
	{
		deserializePortalLinkData(tag.getCompound(PORTAL_LINK));
	}

	private void deserializePortalLinkData(CompoundTag tag)
	{
		for(String key : tag.getAllKeys())
		{
			this.portalLinks.put(UUID.fromString(key),
					PortalLink.load(tag.getCompound(key)));
		}
	}

	public void addFreshLink(UUID uuid)
	{
		this.portalLinks.put(uuid, new PortalLink(uuid));
		this.setDirty();
	}
	
	public PortalLink getLink(UUID uuid)
	{
		return this.portalLinks.get(uuid);
	}

	@Nullable
	public PortalLink getLink(ItemStack stack)
	{
		if(stack.getItem() instanceof PortalGunItem portalGun)
		{
			return this.portalLinks.get(portalGun.getUUID(stack));
		}
		else return null;
	}

	@Override
	public void setDirty()
	{
		super.setDirty();
	}

	public PortalLinkData(MinecraftServer server)
	{
		this.server = server;
	}

	public static PortalLinkData create(MinecraftServer server)
	{
		return new PortalLinkData(server);
	}

	public static PortalLinkData load(MinecraftServer server, CompoundTag tag)
	{
		PortalLinkData data = create(server);

		data.server = server;
		data.deserialize(tag);

		return data;
	}

	public CompoundTag save(CompoundTag tag)
	{
		tag = serialize();

		return tag;
	}

	@Nonnull
	public static PortalLinkData get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");

		return PortalLinkData.get(level.getServer());
	}

	@Nonnull
	public static PortalLinkData get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();

		return storage.computeIfAbsent((tag) -> load(server, tag), () -> create(server), FILE_NAME);
	}
}

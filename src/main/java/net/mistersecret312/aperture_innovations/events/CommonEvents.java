package net.mistersecret312.aperture_innovations.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ClientBoundPortalLinkSyncPacket;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents
{
	@SubscribeEvent
	public static void levelTick(TickEvent.LevelTickEvent event)
	{
		if(event.side.isServer() && event.phase.equals(TickEvent.Phase.END))
		{
			PortalLinkData data = PortalLinkData.get(event.level);
			NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientBoundPortalLinkSyncPacket(data.portalLinks, new HashMap<>()));
		}
	}
}

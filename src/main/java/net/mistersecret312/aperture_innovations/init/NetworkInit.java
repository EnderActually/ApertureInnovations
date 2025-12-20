package net.mistersecret312.aperture_innovations.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.network.ClientBoundPortalLinkSyncPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;

public class NetworkInit
{
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(
					ApertureInnovations.MODID, "main_network"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public NetworkInit()
	{

	}

	public static void register()
	{
		int index = 0;

		INSTANCE.messageBuilder(ClientBoundPortalLinkSyncPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientBoundPortalLinkSyncPacket::encode)
				.decoder(ClientBoundPortalLinkSyncPacket::decode)
				.consumerMainThread(ClientBoundPortalLinkSyncPacket::handle).add();

		INSTANCE.messageBuilder(ServerboundOpenPortalPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerboundOpenPortalPacket::encode)
				.decoder(ServerboundOpenPortalPacket::new)
				.consumerMainThread(ServerboundOpenPortalPacket::handle).add();

		INSTANCE.messageBuilder(ServerboundResetPortalLinkPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerboundResetPortalLinkPacket::encode)
				.decoder(ServerboundResetPortalLinkPacket::new)
				.consumerMainThread(ServerboundResetPortalLinkPacket::handle).add();

		INSTANCE.messageBuilder(ClientboundTeleportMomentumPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundTeleportMomentumPacket::encode)
				.decoder(ClientboundTeleportMomentumPacket::decode)
				.consumerMainThread(ClientboundTeleportMomentumPacket::handle).add();
	}
}

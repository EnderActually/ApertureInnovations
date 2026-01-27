package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import net.mistersecret312.aperture_innovations.portal.PortalPlacement;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;

public record ServerboundOpenPortalPacket(boolean isPrimary) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ServerboundOpenPortalPacket> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "c2s_open_portal"));

	public static final StreamCodec<ByteBuf, ServerboundOpenPortalPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ServerboundOpenPortalPacket::isPrimary,
			ServerboundOpenPortalPacket::new
	);

	@Override
	public CustomPacketPayload.Type<ServerboundOpenPortalPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerboundOpenPortalPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) ctx.player();
			Level level = player.level();

			boolean isPrimary = packet.isPrimary;

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if (!hasPortalGun) return;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			if(PortalGunConfig.portal_gun_consume_on_shot.get() && PortalGunConfig.portal_gun_uses_energy.get())
				if(!consumeEnergy(gunStack, player))
					return;

			boolean moonshot = portalGun.isLookingAtMoon(player, level);
			if(moonshot)
			{
				if(!PortalGunConfig.portal_gun_consume_on_shot.get() && PortalGunConfig.portal_gun_uses_energy.get())
					if(!consumeEnergy(gunStack, player))
						return;

				UUID linkID = portalGun.getUUID(gunStack, true);

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}
				portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
				portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

				if(isPrimary)
				{
					portalGun.setLastShotPortal(gunStack, 0);
					link.setMoonshot(isPrimary, true, level);
				}
				else
				{
					portalGun.setLastShotPortal(gunStack, 1);
					link.setMoonshot(isPrimary, true, level);
				}

				PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
						new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
				return;
			}

			BlockHitResult result = PortalGunItem.rayTrace(player.level(), player, PortalGunConfig.portal_gun_shoot_range.get());
			if(!result.getType().equals(HitResult.Type.MISS))
			{
				UUID linkID = portalGun.getUUID(gunStack, false);

				if(linkID != null && !level.getBlockState(result.getBlockPos()).is(ApertureInnovations.SHOOT_THROUGH) &&
						   (level.getBlockState(result.getBlockPos()).is(ApertureInnovations.IMPORTALABLE)
				|| !level.getFluidState(result.getBlockPos()).isEmpty() ||
									(PortalGunConfig.use_portalable_tag.get() && !level.getBlockState(result.getBlockPos()).is(ApertureInnovations.PORTALABLE))))
				{
					if(!PortalGunConfig.portal_gun_consume_on_shot.get() && PortalGunConfig.portal_gun_uses_energy.get())
						if(!consumeEnergy(gunStack, player))
							return;

					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

					PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
							new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
					PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
							new ClientboundPortalSoundsPacket.InvalidSurface(linkID, player.blockPosition(), isPrimary));
					return;
				}

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}

				PortalPlacement.Result placement = PortalPlacement.getBestPlacement(level, result, player, linkID, isPrimary);
				if(true)
				{
					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main",
							"shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main",
							"shoot");

					if(!PortalGunConfig.portal_gun_consume_on_shot.get() && PortalGunConfig.portal_gun_uses_energy.get())
						if(!consumeEnergy(gunStack, player)) return;

					if(isPrimary)
					{
						portalGun.setLastShotPortal(gunStack, 0);
						link.createPrimaryPortal(level, result.getLocation(), level.dimension(), placement.facing,
								placement.rotation);

						PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
								new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(),
										isPrimary));
					} else
					{
						portalGun.setLastShotPortal(gunStack, 1);
						link.createSecondaryPortal(level, result.getLocation(), level.dimension(), placement.facing,
								placement.rotation);

						PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
								new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(),
										isPrimary));
					}
				}
				else
				{
					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

					PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
							new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
					PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
							new ClientboundPortalSoundsPacket.InvalidSurface(linkID, player.blockPosition(), isPrimary));
				}
			}

		});
	}

	public static boolean consumeEnergy(ItemStack stack, Player player)
	{
		@Nullable IEnergyStorage capability = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		if(capability != null && capability instanceof ApertureEnergy energy)
		{
			long requiredEnergy = PortalGunConfig.portal_gun_shoot_consumption.get();
			if(energy.getTrueEnergyStored() > requiredEnergy)
			{
				energy.extractLongEnergy(requiredEnergy, false);
				return true;
			}
			else
			{
				player.displayClientMessage(Component.translatable("item.aperture_innovations.portal_gun.not_enough_energy").withStyle(ChatFormatting.DARK_RED), true);
				return false;
			}
		}
		return false;
	}
}

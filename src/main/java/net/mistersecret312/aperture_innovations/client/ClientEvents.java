package net.mistersecret312.aperture_innovations.client;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.mistersecret312.aperture_innovations.client.boots.ShortenedLegsLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static net.mistersecret312.aperture_innovations.ApertureInnovations.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new ShortenedLegsLayer(playerRenderer));
            }
        }
    }
}
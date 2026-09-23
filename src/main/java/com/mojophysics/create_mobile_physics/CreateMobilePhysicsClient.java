package com.mojophysics.create_mobile_physics;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateMobilePhysics.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateMobilePhysics.MODID, value = Dist.CLIENT)
public class CreateMobilePhysicsClient {
    public CreateMobilePhysicsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CreateMobilePhysics.LOGGER.info("Create Mobile Physics client setup - ready for Mojo Launcher");
    }
}

package com.mojophysics.create_mobile_physics;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Create Mobile Physics - Lightweight physics addon for Create on Mojo Launcher / Android.
 * Focus: balloons, simple thrust, low memory footprint for large-ish structures.
 */
@Mod(CreateMobilePhysics.MODID)
public class CreateMobilePhysics {
    public static final String MODID = "create_mobile_physics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // === Blocks ===
    // Hot air balloon block - provides upward lift. Multiple balloons share load.
    public static final DeferredBlock<Block> BALLOON_BLOCK = BLOCKS.register("balloon",
            () -> new BalloonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .strength(0.5f)
                    .noOcclusion()
                    .isViewBlocking((s, l, p) -> false)));

    public static final DeferredItem<BlockItem> BALLOON_ITEM = ITEMS.registerSimpleBlockItem("balloon", BALLOON_BLOCK);

    // Simple thruster - applies force when powered by redstone or later Create kinetic.
    public static final DeferredBlock<Block> THRUSTER_BLOCK = BLOCKS.register("thruster",
            () -> new ThrusterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> THRUSTER_ITEM = ITEMS.registerSimpleBlockItem("thruster", THRUSTER_BLOCK);

    // Creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_mobile_physics"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> BALLOON_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(BALLOON_ITEM.get());
                        output.accept(THRUSTER_ITEM.get());
                    }).build());

    public CreateMobilePhysics(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("Create Mobile Physics loaded - optimized for Mojo Launcher / mobile");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Create Mobile Physics common setup complete");
        LOGGER.info("Max structure blocks: {}", Config.MAX_STRUCTURE_BLOCKS.get());
        LOGGER.info("Balloon lift force: {}", Config.BALLOON_LIFT.get());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(BALLOON_ITEM);
            event.accept(THRUSTER_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Create Mobile Physics ready on server");
    }
}

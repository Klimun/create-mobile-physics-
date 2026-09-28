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

@Mod(CreateMobilePhysics.MODID)
public class CreateMobilePhysics {
    public static final String MODID = "create_mobile_physics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> BALLOON_BLOCK = BLOCKS.register("balloon",
            () -> new BalloonBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion()));
    public static final DeferredItem<BlockItem> BALLOON_ITEM = ITEMS.registerSimpleBlockItem("balloon", BALLOON_BLOCK);

    public static final DeferredBlock<Block> WING_BLOCK = BLOCKS.register("wing",
            () -> new WingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.8f).noOcclusion()));
    public static final DeferredItem<BlockItem> WING_ITEM = ITEMS.registerSimpleBlockItem("wing", WING_BLOCK);

    public static final DeferredBlock<Block> HEATER_BLOCK = BLOCKS.register("heater",
            () -> new HeaterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(2.0f).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> HEATER_ITEM = ITEMS.registerSimpleBlockItem("heater", HEATER_BLOCK);

    public static final DeferredBlock<Block> WING_MOTOR_BLOCK = BLOCKS.register("wing_motor",
            () -> new WingMotorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> WING_MOTOR_ITEM = ITEMS.registerSimpleBlockItem("wing_motor", WING_MOTOR_BLOCK);

    public static final DeferredBlock<Block> THRUSTER_BLOCK = BLOCKS.register("thruster",
            () -> new ThrusterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5f).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> THRUSTER_ITEM = ITEMS.registerSimpleBlockItem("thruster", THRUSTER_BLOCK);

    public static final DeferredBlock<Block> TRACK_BLOCK = BLOCKS.register("track",
            () -> new TrackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> TRACK_ITEM = ITEMS.registerSimpleBlockItem("track", TRACK_BLOCK);

    public static final DeferredBlock<Block> WHEEL_BLOCK = BLOCKS.register("wheel",
            () -> new WheelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0f).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> WHEEL_ITEM = ITEMS.registerSimpleBlockItem("wheel", WHEEL_BLOCK);

    public static final DeferredItem<Item> PHYSICS_GLUE = ITEMS.register("physics_glue",
            () -> new PhysicsGlueItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_mobile_physics"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> BALLOON_ITEM.get().getDefaultInstance())
                    .displayItems((p, out) -> {
                        out.accept(PHYSICS_GLUE.get());
                        out.accept(BALLOON_ITEM.get());
                        out.accept(WING_ITEM.get());
                        out.accept(HEATER_ITEM.get());
                        out.accept(WING_MOTOR_ITEM.get());
                        out.accept(THRUSTER_ITEM.get());
                        out.accept(TRACK_ITEM.get());
                        out.accept(WHEEL_ITEM.get());
                    }).build());

    public CreateMobilePhysics(IEventBus bus, ModContainer container) {
        bus.addListener(this::commonSetup);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
        NeoForge.EVENT_BUS.register(this);
        bus.addListener(this::addCreative);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        LOGGER.info("Create Mobile Physics 0.4 - Fizik tutkali");
    }

    private void commonSetup(FMLCommonSetupEvent e) {
        LOGGER.info("CMP 0.4 glue | maxBlocks={}", Config.MAX_STRUCTURE_BLOCKS.get());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            e.accept(PHYSICS_GLUE);
        }
        if (e.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            e.accept(BALLOON_ITEM);
            e.accept(WING_ITEM);
            e.accept(HEATER_ITEM);
            e.accept(WING_MOTOR_ITEM);
            e.accept(THRUSTER_ITEM);
            e.accept(TRACK_ITEM);
            e.accept(WHEEL_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent e) {
        LOGGER.info("Create Mobile Physics 0.4 hazir");
    }
}

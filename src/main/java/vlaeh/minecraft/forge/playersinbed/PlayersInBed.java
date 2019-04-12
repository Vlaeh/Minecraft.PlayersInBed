package vlaeh.minecraft.forge.playersinbed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import vlaeh.minecraft.forge.playersinbed.server.I18nLanguageHook;

@Mod(PlayersInBed.MODID)
//TODO 1.13 guiFactory = "vlaeh.minecraft.forge.playersinbed.PlayersInBedGUIFactory")
public class PlayersInBed 
{
    public static final String MODID = "playersinbed";
    public static final Logger LOGGER = LogManager.getLogger();
    public static I18nLanguageHook i18n = new I18nLanguageHook().loadLanguage(MODID, "en_us");

    public PlayersInBed() {
        LOGGER.debug("Creating Player In Bed mod");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PlayersInBedConfig.serverSpec);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
        LOGGER.info("Server starting");
        PlayersInBedServerCommand.registerAll(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void serverStarted(final FMLServerStartedEvent event) {
        LOGGER.info("Server started");
        MinecraftForge.EVENT_BUS.register(new PlayersInBedServerSide());
    }

    @SubscribeEvent
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        LOGGER.info("Config changed {}" + event);
    }
    
    @SubscribeEvent
    public void onLoad(final ModConfig.Loading configEvent) {
        final ModConfig config = configEvent.getConfig();
        LOGGER.info("Loading configuration {}", config);
        PlayersInBedConfig.instance.load(config.getConfigData());
    }

    @SubscribeEvent
    public void onFileChange(final ModConfig.ConfigReloading configEvent) {
        final ModConfig config = configEvent.getConfig();
        LOGGER.info("Reloading configuration {}", config);
        PlayersInBedConfig.instance.load(config.getConfigData());
    }

}

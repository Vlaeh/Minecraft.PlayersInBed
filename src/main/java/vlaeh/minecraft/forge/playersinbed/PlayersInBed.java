package vlaeh.minecraft.forge.playersinbed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import vlaeh.minecraft.forge.playersinbed.server.I18nLanguageHook;

@Mod(PlayersInBed.MODID)
//TODO 1.13 guiFactory = "vlaeh.minecraft.forge.playersinbed.PlayersInBedGUIFactory")
public class PlayersInBed 
{
    public static final String MODID = "playersinbed";
    public static final Logger LOGGER = LogManager.getLogger();
    public static I18nLanguageHook i18n = new I18nLanguageHook().loadLanguage(MODID, "en_us");

    private PlayersInBedServerSide serverSide = null;

    public PlayersInBed() {
        LOGGER.debug("Creating Player In Bed mod");
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PlayersInBedConfig.serverSpec);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(PlayersInBedConfig.class);
    }

    @SubscribeEvent
    public void serverStarting(final FMLServerStartingEvent event) {
        LOGGER.info("Server starting");
        PlayersInBedServerCommand.registerAll(event.getServer().getCommandManager().getDispatcher());
    }

    @SubscribeEvent
    public void serverStarted(final FMLServerStartedEvent event) {
        LOGGER.info("Server started");
        serverSide = new PlayersInBedServerSide();
        MinecraftForge.EVENT_BUS.register(serverSide);
    }

    @SubscribeEvent
    public void serverStopped(final FMLServerStoppedEvent event) {
        LOGGER.info("Server stopped");
        MinecraftForge.EVENT_BUS.unregister(serverSide);
    }


}

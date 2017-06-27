package vlaeh.minecraft.forge.playersinbed;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

import vlaeh.minecraft.forge.playersinbed.commands.PlayerInBedCommand;

@Mod(modid = PlayerInBed.MODID, 
     version = PlayerInBed.VERSION, 
     name = PlayerInBed.NAME, 
     acceptableRemoteVersions = "*", 
     acceptedMinecraftVersions = "[1.9,1.13)", 
     guiFactory = "vlaeh.minecraft.forge.playersinbed.PlayerInBedGUIFactory")
public class PlayerInBed 
{
    public static final String MODID = "playersinbed";
    public static final String VERSION = "1.3";
    public static final String NAME = "Player In Bed";
	
    public static Configuration config;
    public static boolean skipEnabled = true;
    public static boolean messageEnabled = true;
    public static boolean statusEnabled = false;
    public static boolean clearWeather = true;
    public static boolean setSpawnDuringDayEnabled = false;
    public static int ratio = 50;
    
    @SidedProxy(serverSide = "vlaeh.minecraft.forge.playersinbed.CommonProxy", clientSide = "vlaeh.minecraft.forge.playersinbed.CommonProxy")
    public static CommonProxy proxy;
    
    @SidedProxy(serverSide = "vlaeh.minecraft.forge.playersinbed.server.I18nServer", clientSide = "vlaeh.minecraft.forge.playersinbed.client.I18nClient")
    public static I18nProxy i18n;
    
    @Mod.Instance
    public static PlayerInBed instance;
    
    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
    	config = new Configuration(event.getSuggestedConfigurationFile());
    	syncConfig();
   	}
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	proxy.postInit(event);
    }
    
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
    	event.registerServerCommand(new PlayerInBedCommand());
    }

	public static void syncConfig() {
	    if (FMLCommonHandler.instance().getSide() == Side.SERVER)
	    	i18n.load(config.getString("serverLang", Configuration.CATEGORY_GENERAL, "en_US", "Server language"));
		skipEnabled = config.getBoolean("1.enabled", Configuration.CATEGORY_GENERAL, skipEnabled, "playerinbed.conf.enabled.tooltip", "playerinbed.conf.enabled");
		ratio = config.getInt("2.ratio", Configuration.CATEGORY_GENERAL, ratio, 0, 100, "playerinbed.conf.ratio.tooltip", "playerinbed.conf.ratio");
		clearWeather = config.getBoolean("3.clearWeather", Configuration.CATEGORY_GENERAL, skipEnabled, "playerinbed.conf.clearweathe.tooltipr", "playerinbed.conf.clearweather");
		messageEnabled = config.getBoolean("4.sendMessageToChat", Configuration.CATEGORY_GENERAL, messageEnabled, "playerinbed.conf.sendmessagetochat.tooltip", "playerinbed.conf.sendmessagetochat");
	    statusEnabled = config.getBoolean("5.sendMessageToStatus", Configuration.CATEGORY_GENERAL, statusEnabled, "playerinbed.conf.sendmessagetostatus.tooltip", "playerinbed.conf.sendmessagetostatus");
		setSpawnDuringDayEnabled = config.getBoolean("6.setSpawnDuringDay", Configuration.CATEGORY_GENERAL, setSpawnDuringDayEnabled, "playerinbed.conf.setspawnduringday.tooltip", "playerinbed.conf.setspawnduringday");
	    if(config.hasChanged())
	      config.save();
	}

}

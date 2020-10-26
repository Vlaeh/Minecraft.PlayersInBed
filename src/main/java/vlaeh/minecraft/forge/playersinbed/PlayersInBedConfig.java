package vlaeh.minecraft.forge.playersinbed;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class PlayersInBedConfig {

    public static final String NOTIFY_TO_STATUS_STR = "notifyToStatus";
    public static final String NOTIFY_TO_CHAT_STR = "notifyToChat";
    public static final String CLEAR_WEATHER_STR = "clearWeather";
    public static final String RATIO_STR = "ratio";
    public static final String SKIP_NIGHT_STR = "skipNight";
    public static final String ENABLE_STR = "enable";
    public static final String LOCALE_STR = "locale";
 
    public static boolean enabled = true;
    public static boolean skipNightEnabled = true;
    public static boolean notifyToChatEnabled = true;
    public static boolean notifyToStatusEnabled = false;
    public static boolean clearWeatherEnabled = true;
    public static int ratio = 30;
    public static String locale = "en_us";

    private final BooleanValue enabled_c;
    private final BooleanValue skipNightEnabled_c;
    private final BooleanValue notifyToChatEnabled_c;
    private final BooleanValue notifyToStatusEnabled_c;
    private final BooleanValue clearWeatherEnabled_c;
    private final IntValue ratio_c;
    private final ConfigValue<String> locale_c;
    private CommentedFileConfig fileConfig = null;

    public static final PlayersInBedConfig instance;
    public static final ForgeConfigSpec serverSpec;

    static {
        final Pair<PlayersInBedConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(PlayersInBedConfig::new);
        serverSpec = specPair.getRight();
        instance = specPair.getLeft();
    }

    PlayersInBedConfig(ForgeConfigSpec.Builder builder) {

        enabled_c = builder
                .comment("Mod Enabled.")
                .translation("playersinbed.conf.enable.tooltip")
                .define(ENABLE_STR, enabled);

        skipNightEnabled_c = builder
            .comment("Night skipping enabled.")
            .translation("playersinbed.conf.skipnight.tooltip")
            .define(SKIP_NIGHT_STR, skipNightEnabled);

        ratio_c = builder
            .comment("Percentage of players being asleep to start skipping night.")
            .translation("playersinbed.conf.ratio.tooltip")
            .defineInRange(RATIO_STR, ratio, 0, 100);

        clearWeatherEnabled_c = builder
            .comment("Clear weather when skipping night.")
            .translation("playersinbed.conf.clearweathe.tooltipr")
            .define(CLEAR_WEATHER_STR, clearWeatherEnabled);

        notifyToChatEnabled_c = builder
            .comment("Send messages to chat.")
            .translation("playersinbed.conf.sendmessagetochat.tooltipr")
            .define(NOTIFY_TO_CHAT_STR, notifyToChatEnabled);

        notifyToStatusEnabled_c = builder
            .comment("Send messages to status.")
            .translation("playersinbed.conf.sendmessagetostatus.tooltipr")
            .define(NOTIFY_TO_STATUS_STR, notifyToStatusEnabled);

        locale_c = builder
                .comment("Server locale to use.")
                .translation("playersinbed.conf.locale.tooltipr")
                .define(LOCALE_STR, locale);
    }
    
    public synchronized void load(final CommentedConfig commentedConfig) {
        PlayersInBed.LOGGER.info("Loading configuration " + commentedConfig);
        PlayersInBed.LOGGER.info("Loading configuration " + commentedConfig.getClass());
        if (commentedConfig instanceof CommentedFileConfig) {
            fileConfig = (CommentedFileConfig) commentedConfig;
            fileConfig.load(); // Note: file is not reloaded automatically
        }
        enabled = enabled_c.get();
        skipNightEnabled = skipNightEnabled_c.get();
        notifyToChatEnabled = notifyToChatEnabled_c.get();
        notifyToStatusEnabled = notifyToStatusEnabled_c.get();
        clearWeatherEnabled = clearWeatherEnabled_c.get();
        ratio = ratio_c.get();
        locale = locale_c.get();
        PlayersInBed.i18n.loadLanguage(PlayersInBed.MODID, locale);
    }

    
    private final <T> void changeValue(ConfigValue<T> c, T v) {
        if (! v.equals(c.get()))
            fileConfig.set(c.getPath(), v);
    }
    
    // Note: "synchronized" is to avoid configuration from being reloaded
    //       and values not yet set from being reset
    public synchronized void save() {
        PlayersInBed.LOGGER.info("Saving configuration");
        PlayersInBed.LOGGER.info("File config: " + fileConfig);
        if (fileConfig == null) 
            return;
        PlayersInBed.LOGGER.debug("Saving configuration");
        changeValue(enabled_c, enabled);
        changeValue(skipNightEnabled_c, skipNightEnabled);
        changeValue(notifyToChatEnabled_c, notifyToChatEnabled);
        changeValue(notifyToStatusEnabled_c, notifyToStatusEnabled);
        changeValue(clearWeatherEnabled_c, clearWeatherEnabled);
        changeValue(ratio_c, ratio);
        changeValue(locale_c, locale);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        final ModConfig config = configEvent.getConfig();
        PlayersInBed.LOGGER.info("Loading configuration {}", config);
        instance.load(config.getConfigData());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        final ModConfig config = configEvent.getConfig();
        PlayersInBed.LOGGER.info("Reloading configuration {}", config);
        instance.load(config.getConfigData());
    }


}
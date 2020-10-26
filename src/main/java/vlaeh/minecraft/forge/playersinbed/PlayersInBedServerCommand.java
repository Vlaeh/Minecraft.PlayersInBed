package vlaeh.minecraft.forge.playersinbed;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class PlayersInBedServerCommand {
    
    public static void registerAll(CommandDispatcher<CommandSource> dispatcher) 
    {
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal("playersInBed")
            .requires(cs -> cs.hasPermissionLevel(0))
            .then(registerEnabled())
            .then(registerSkipNight())
            .then(registerNotifyToChat())
            .then(registerNotifyToStatus())
            .then(registerClearWeather())
            .then(registerRatio())
            .then(registerLocale())
            .executes(ctx -> {
                ctx.getSource().sendFeedback(new StringTextComponent("§ePlayers In Bed§f:\n - §e" + PlayersInBedConfig.ENABLE_STR +  "§f: " + PlayersInBedConfig.enabled + "\n - §e" + PlayersInBedConfig.CLEAR_WEATHER_STR + "§f: " + PlayersInBedConfig.clearWeatherEnabled + "\n - §e" + PlayersInBedConfig.RATIO_STR + "§f: " + PlayersInBedConfig.ratio + " %"), true);
                return 0;
            })
        );
    }
    
    private final static int sendfeecback(final CommandContext<CommandSource> ctx, final String name, final Object value) {
        ctx.getSource().sendFeedback(new StringTextComponent("§e" + name + "§f = " + value), true);
        return 0;
    }
    
    static ArgumentBuilder<CommandSource, ?> registerEnabled()
    {
        return Commands.literal(PlayersInBedConfig.ENABLE_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("status", BoolArgumentType.bool())
                    .executes(ctx -> {
                        PlayersInBedConfig.enabled = BoolArgumentType.getBool(ctx, "status");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.ENABLE_STR, PlayersInBedConfig.enabled);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.ENABLE_STR, PlayersInBedConfig.enabled);
                });
    }

    static ArgumentBuilder<CommandSource, ?> registerSkipNight()
    {
        return Commands.literal(PlayersInBedConfig.SKIP_NIGHT_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("status", BoolArgumentType.bool())
                    .executes(ctx -> {
                        PlayersInBedConfig.skipNightEnabled = BoolArgumentType.getBool(ctx, "status");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.SKIP_NIGHT_STR, PlayersInBedConfig.skipNightEnabled);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.SKIP_NIGHT_STR, PlayersInBedConfig.skipNightEnabled);
                });
    }

    static ArgumentBuilder<CommandSource, ?> registerNotifyToChat()
    {
        return Commands.literal(PlayersInBedConfig.NOTIFY_TO_CHAT_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("status", BoolArgumentType.bool())
                    .executes(ctx -> {
                        PlayersInBedConfig.notifyToChatEnabled = BoolArgumentType.getBool(ctx, "status");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.NOTIFY_TO_CHAT_STR, PlayersInBedConfig.notifyToChatEnabled);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.NOTIFY_TO_CHAT_STR, PlayersInBedConfig.notifyToChatEnabled);
                });
    }

    static ArgumentBuilder<CommandSource, ?> registerNotifyToStatus()
    {
        return Commands.literal(PlayersInBedConfig.NOTIFY_TO_STATUS_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("status", BoolArgumentType.bool())
                    .executes(ctx -> {
                        PlayersInBedConfig.notifyToStatusEnabled = BoolArgumentType.getBool(ctx, "status");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.NOTIFY_TO_STATUS_STR, PlayersInBedConfig.notifyToStatusEnabled);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.NOTIFY_TO_STATUS_STR, PlayersInBedConfig.notifyToStatusEnabled);
                });
    }

    static ArgumentBuilder<CommandSource, ?> registerClearWeather()
    {
        return Commands.literal(PlayersInBedConfig.CLEAR_WEATHER_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("status", BoolArgumentType.bool())
                    .executes(ctx -> {
                        PlayersInBedConfig.clearWeatherEnabled = BoolArgumentType.getBool(ctx, "status");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.CLEAR_WEATHER_STR, PlayersInBedConfig.clearWeatherEnabled);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.CLEAR_WEATHER_STR, PlayersInBedConfig.clearWeatherEnabled);
                });
    }

    static ArgumentBuilder<CommandSource, ?> registerRatio()
    {
        return Commands.literal(PlayersInBedConfig.RATIO_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                    .executes(ctx -> {
                        PlayersInBedConfig.ratio = IntegerArgumentType.getInteger(ctx, "percent");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.RATIO_STR, PlayersInBedConfig.ratio);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.RATIO_STR, PlayersInBedConfig.ratio);
                });
    }

    static ArgumentBuilder<CommandSource, ?> registerLocale()
    {
        return Commands.literal(PlayersInBedConfig.LOCALE_STR)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("locale", StringArgumentType.word())
                    .executes(ctx -> {
                        PlayersInBedConfig.locale = StringArgumentType.getString(ctx, "locale");
                        PlayersInBedConfig.instance.save();
                        return sendfeecback(ctx, PlayersInBedConfig.LOCALE_STR, PlayersInBedConfig.locale);
                    })
                )
                .executes(ctx -> {
                    return sendfeecback(ctx, PlayersInBedConfig.LOCALE_STR, PlayersInBedConfig.locale);
                });
    }

}

package vlaeh.minecraft.forge.playersinbed;

import java.util.HashSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayersInBedServerSide {
    private final HashSet<String> playersInBed = new HashSet<String>();

    private final int getSleepingPercent(final PlayerEntity player, int sleepingCount) {
        final RegistryKey<World> dimension = player.world.getDimensionKey();
        int playersCount = 0;
        int activeCount = 0;
        for (final PlayerEntity p : player.world.getPlayers()) {
            if (p.isSpectator())
                continue;
            if (p.world.getDimensionKey() != dimension)
                continue;
            playersCount++;
            if (! p.isCreative())
                activeCount++;
            if (p.isSleeping())
                sleepingCount++;
        }
        if (sleepingCount <= 0)
            return 0;
        if (playersCount == sleepingCount)
            return 101; // All sleeping: let Minecraft skip the night
        if (activeCount == 0) // All players in creative mode
            return (sleepingCount * 100) / playersCount;
        if (sleepingCount >= activeCount)
            return 100;
        return (sleepingCount * 100) / activeCount;
    }

    @SubscribeEvent
    public void onPlayerWakeUpEvent(final PlayerWakeUpEvent event) {
        if (! PlayersInBedConfig.enabled)
            return;
        final PlayerEntity entityPlayer = event.getPlayer();
        PlayersInBed.LOGGER.debug("onPlayerWakeUpEvent {} {}", entityPlayer, entityPlayer.world.getDayTime());
        if (! (entityPlayer instanceof ServerPlayerEntity))
            return; // Note: also triggered on client side when hosting the game
        final String name = entityPlayer.getScoreboardName();
        if (playersInBed.remove(name)) {
            if ((entityPlayer.world.getDayTime() % 24000L) != 0) {
                final int ratio = getSleepingPercent(entityPlayer, -1);
                if (ratio >= PlayersInBedConfig.ratio)
                    sendMessageToPlayers(entityPlayer.world,
                            new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.leftbedOK"), name, ratio > 100 ? 100 : ratio));
                else
                    sendMessageToPlayers(entityPlayer.world,
                            new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.leftbedKO"), name, ratio > 100 ? 100 : ratio));
            }
        }
    }

    @SubscribeEvent
    public void onSleepingLocationCheckEvent(final SleepingLocationCheckEvent event) {
        if (! PlayersInBedConfig.enabled)
            return;
        final Entity entity = event.getEntity();
        if (! (entity instanceof PlayerEntity))
            return;
        final PlayerEntity entityPlayer = (PlayerEntity)entity;
        final String name = entityPlayer.getScoreboardName();
        final int ratio = getSleepingPercent(entityPlayer, 0);
        PlayersInBed.LOGGER.debug("onSleepingLocationCheckEvent {}, dayTime={}, gameTime={}, ratio={}", entityPlayer, entityPlayer.world.getDayTime(), entityPlayer.world.getGameTime(), ratio);

        if (playersInBed.add(name)) {
            // TODO: add click action & command to kick players out of bed
            if (ratio >= PlayersInBedConfig.ratio)
                sendMessageToPlayers(entityPlayer.world,
                        new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.isinbedOK"), name, ratio > 100 ? 100 : ratio));
            else
                sendMessageToPlayers(entityPlayer.world,
                        new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.isinbedKO"), name, ratio > 100 ? 100 : ratio));
        }

        if (PlayersInBedConfig.skipNightEnabled 
                && (ratio >= PlayersInBedConfig.ratio)
                && (ratio != 101)
                && entityPlayer.isPlayerFullyAsleep()) {
            PlayersInBed.LOGGER.debug("TIME is to be changed {} >= {}", ratio, PlayersInBedConfig.ratio);
            final World world = entityPlayer.getEntityWorld();
            final IWorldInfo wi = world.getWorldInfo();
            if ((wi == null) || (! (wi instanceof ServerWorldInfo))) {
                PlayersInBed.LOGGER.error("Incompatible world type {} for player {}", entityPlayer, wi);
                return;
            }
            final ServerWorldInfo worldInfo = (ServerWorldInfo)wi;
            final long worldTime = worldInfo.getDayTime();
            final long daytime = worldTime % 24000L;
            final long shifttime = 24000L - daytime;
            if ((shifttime < 12000L) // Ensure another mod is not making us skip several nights
                    || (worldInfo.isThundering()) && shifttime < 23000L) { // Skip thunder 
                PlayersInBed.LOGGER.debug("TIME CHANGE: {} / {} : + {}", worldTime, daytime, shifttime);
                sendMessageToPlayers(entityPlayer.world,
                        new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.passingnight")));
                if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                    worldInfo.setDayTime(net.minecraftforge.event.ForgeEventFactory.onSleepFinished((ServerWorld)world, worldTime + shifttime, worldTime));
                }
                if (PlayersInBedConfig.clearWeatherEnabled
                        && world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
                    worldInfo.setRainTime(0);
                    worldInfo.setRaining(false);
                    worldInfo.setThunderTime(0);
                    worldInfo.setThundering(false);
                }
                event.setResult(Result.DENY);
            }
        }
    }
    
    private final static void sendMessageToPlayer(final PlayerEntity player, final ITextComponent text) {
        if (PlayersInBedConfig.notifyToChatEnabled)
            player.sendMessage(text, player.getUniqueID());
        if (PlayersInBedConfig.notifyToStatusEnabled)
            player.sendStatusMessage(text, true);
    }

    private final static void sendMessageToPlayers(final World world, final ITextComponent text) {
        for (PlayerEntity player : world.getPlayers())
            sendMessageToPlayer(player, text);
    }

}

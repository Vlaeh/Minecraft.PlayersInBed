package vlaeh.minecraft.forge.playersinbed;

import java.util.HashSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
            if (p.world.getDimensionKey() != dimension)
                continue;
            playersCount++;
            if (! p.isCreative() && ! p.isSpectator())
                activeCount++;
            if (p.isSleeping())
                sleepingCount++;
        }
        if (sleepingCount <= 0)
            return 0;
        if (playersCount == sleepingCount)
            return 100; // All sleeping: let Minecraft skip the night
        if (activeCount == 0)
            return (sleepingCount * 100) / playersCount;
        if (sleepingCount == activeCount)
            return 99;
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
                            new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.leftbedOK"), name, ratio));
                else
                    sendMessageToPlayers(entityPlayer.world,
                            new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.leftbedKO"), name, ratio));
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
        PlayersInBed.LOGGER.debug("onSleepingLocationCheckEvent {} {} {}", entityPlayer.world.getDayTime(), entityPlayer.world.getGameTime(), entityPlayer.world.isDaytime(), entityPlayer.world.isThundering());

        if (playersInBed.add(name)) {
            // TODO: add click action & command to kick players out of bed
            if (ratio >= PlayersInBedConfig.ratio)
                sendMessageToPlayers(entityPlayer.world,
                        new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.isinbedOK"), name, ratio));
            else
                sendMessageToPlayers(entityPlayer.world,
                        new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.isinbedKO"), name, ratio));
        }

        if (PlayersInBedConfig.skipNightEnabled 
                && (ratio >= PlayersInBedConfig.ratio)
                && (ratio != 100)
                && entityPlayer.isPlayerFullyAsleep()) {
            PlayersInBed.LOGGER.debug("TIME is to be changed {} >= {}", ratio, PlayersInBedConfig.ratio);
            final IWorldInfo w = entityPlayer.getEntityWorld().getWorldInfo();
            if ((w == null) || (! (w instanceof ServerWorldInfo))) {
                PlayersInBed.LOGGER.error("Incompatible world type {} for player {}", entityPlayer, w);
                return;
            }
            final ServerWorldInfo worldInfo = (ServerWorldInfo)w;
            final long worldTime = worldInfo.getDayTime();
            final long daytime = worldTime % 24000L;
            final long shifttime = 24000L - daytime;
            if (shifttime < 12000L) { // Ensure we are not skipping another day.
                PlayersInBed.LOGGER.debug("TIME CHANGE: {} / {} : + {}", worldTime, daytime, shifttime);
                sendMessageToPlayers(entityPlayer.world,
                        new TranslationTextComponent(PlayersInBed.i18n.translateKey("playersinbed.passingnight")));
                worldInfo.setDayTime(worldTime + shifttime);
                if (PlayersInBedConfig.clearWeatherEnabled) {
                    worldInfo.setClearWeatherTime(6000);
                    worldInfo.setRainTime(0);
                    worldInfo.setThunderTime(0);
                    worldInfo.setRaining(false);
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

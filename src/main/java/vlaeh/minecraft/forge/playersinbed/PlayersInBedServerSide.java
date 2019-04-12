package vlaeh.minecraft.forge.playersinbed;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayersInBedServerSide {
    private final HashSet<String> playersInBed = new HashSet<String>();

    private final int getSleepingPercent(final EntityPlayer player, int startCount) {
        final DimensionType dimension = player.dimension;
        int playersCount = 0;
        for (final EntityPlayer p : player.world.playerEntities) {
            if (p.dimension != dimension)
                continue;
            playersCount++;
            if (p.isPlayerSleeping())
                startCount++;
        }
        if (startCount <= 0)
            return 0;
        return (startCount * 100) / playersCount;
    }

    @SubscribeEvent
    public void onPlayerWakeUpEvent(final PlayerWakeUpEvent event) {
        if (! PlayersInBedConfig.enabled)
            return;
        final EntityPlayer entityPlayer = event.getEntityPlayer();
        PlayersInBed.LOGGER.info("onPlayerWakeUpEvent {} {}", entityPlayer, entityPlayer.world.getDayTime());
        if (! (entityPlayer instanceof EntityPlayerMP))
            return; // Note: also triggered on client side when hosting the game
        final String name = entityPlayer.getScoreboardName();
        if (playersInBed.remove(name)) {
            if ((entityPlayer.world.getDayTime() % 24000L) != 0) {
                final int ratio = getSleepingPercent(entityPlayer, -1);
                if (ratio >= PlayersInBedConfig.ratio)
                    sendMessageToPlayers(entityPlayer.world,
                            new TextComponentTranslation(PlayersInBed.i18n.translateKey("playersinbed.leftbedOK"), name, ratio));
                else
                    sendMessageToPlayers(entityPlayer.world,
                            new TextComponentTranslation(PlayersInBed.i18n.translateKey("playersinbed.leftbedKO"), name, ratio));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(final PlayerSleepInBedEvent event) {
        if ( (! PlayersInBedConfig.enabled)
             || (! PlayersInBedConfig.setSpawnDuringDayEnabled) )
            return;
        final EntityPlayer entityPlayer = event.getEntityPlayer();
        final World world = entityPlayer.getEntityWorld();
        PlayersInBed.LOGGER.info("onPlayerSleepInBedEvent {} {}", entityPlayer, entityPlayer.world.getDayTime());
        if ( world.isRemote 
                || ! world.isDaytime()
                || entityPlayer.isSneaking() 
                || ! world.getDimension().canRespawnHere() )
            return;
        final BlockPos pos = event.getPos();
        final BlockPos currentBedLocation = entityPlayer.getBedLocation(entityPlayer.dimension);
        if ((currentBedLocation != null) && (currentBedLocation.getDistance(pos.getX(), pos.getY(), pos.getZ()) <= 4))
            return;
        entityPlayer.setSpawnPoint(pos, false, entityPlayer.dimension);
        entityPlayer.bedLocation = pos;
        final TextComponentTranslation text = new TextComponentTranslation(PlayersInBed.i18n.translateKey("playersinbed.spawnchanged"));
        sendMessageToPlayer(entityPlayer, text);
    }

    @SubscribeEvent
    public void onSleepingLocationCheckEvent(final SleepingLocationCheckEvent event) {
        if (! PlayersInBedConfig.enabled)
            return;
        final EntityPlayer entityPlayer = event.getEntityPlayer();
        final String name = entityPlayer.getScoreboardName();
        final int ratio = getSleepingPercent(entityPlayer, 0);
        PlayersInBed.LOGGER.info("onSleepingLocationCheckEvent {}", entityPlayer);
        PlayersInBed.LOGGER.info("onSleepingLocationCheckEvent {} {} {}", entityPlayer.world.getDayTime(), entityPlayer.world.getGameTime(), entityPlayer.world.isDaytime(), entityPlayer.world.isThundering());

        if (playersInBed.add(name)) {
            // TODO: add click action & command to kick players out of bed
            if (ratio >= PlayersInBedConfig.ratio)
                sendMessageToPlayers(entityPlayer.world,
                        new TextComponentTranslation(PlayersInBed.i18n.translateKey("playersinbed.isinbedOK"), name, ratio));
            else
                sendMessageToPlayers(entityPlayer.world,
                        new TextComponentTranslation(PlayersInBed.i18n.translateKey("playersinbed.isinbedKO"), name, ratio));
        }

        if (PlayersInBedConfig.skipNightEnabled 
                && (ratio >= PlayersInBedConfig.ratio) && (ratio < 100)
                && entityPlayer.isPlayerFullyAsleep()) {
            final long worldTime = entityPlayer.world.getDayTime();
            final long daytime = worldTime % 24000L;
            final long shifttime = 24000L - daytime;
            if (shifttime < 12000L) { // Ensure we are not skipping another day.
                entityPlayer.world.setDayTime(worldTime + shifttime);
                PlayersInBed.LOGGER.debug("TIME CHANGE: {} / {} : + {}", worldTime, daytime, shifttime);
                if (PlayersInBedConfig.clearWeatherEnabled) {
                    final WorldInfo worldInfo = entityPlayer.world.getWorldInfo();
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
    
    private final static void sendMessageToPlayer(final EntityPlayer player, final ITextComponent text) {
        if (PlayersInBedConfig.notifyToChatEnabled)
            player.sendMessage(text);
        if (PlayersInBedConfig.notifyToStatusEnabled)
            player.sendStatusMessage(text, true);
        // [TODO] status not working, use /title ?
    }

    private final static void sendMessageToPlayers(final World world, final ITextComponent text) {
        for (EntityPlayer player : world.playerEntities)
            sendMessageToPlayer(player, text);
    }

}

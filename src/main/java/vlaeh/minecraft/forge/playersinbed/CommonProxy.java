package vlaeh.minecraft.forge.playersinbed;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {

    private final HashSet<String> playersInBed = new HashSet<String>();

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.getModID().equals(PlayerInBed.MODID))
            PlayerInBed.syncConfig();
    }

    private final int getSleepingPercent(final EntityPlayer player, int startCount) {
        final int dimension = player.dimension;
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
        final EntityPlayer entityPlayer = event.getEntityPlayer();
        final String name = entityPlayer.getName();
        if (playersInBed.contains(name)) {
            playersInBed.remove(entityPlayer.getName());
            if ((entityPlayer.world.getWorldTime() % 24000L) != 0) {
                final int ratio = getSleepingPercent(entityPlayer, -1);
                if (ratio >= PlayerInBed.ratio)
                    sendMessageToPlayers(entityPlayer.world,
                            new TextComponentString(PlayerInBed.i18n.format("playerinbed.leftbedOK", name, ratio)));
                else
                    sendMessageToPlayers(entityPlayer.world,
                            new TextComponentString(PlayerInBed.i18n.format("playerinbed.leftbedKO", name, ratio)));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(final PlayerSleepInBedEvent event) {
        if (!PlayerInBed.setSpawnDuringDayEnabled)
            return;
        final EntityPlayer entityPlayer = event.getEntityPlayer();
        final World world = entityPlayer.getEntityWorld();
        if (world.isRemote || !world.isDaytime() || entityPlayer.isSneaking())
            return;
        final BlockPos pos = event.getPos();
        final BlockPos currentBedLocation = entityPlayer.getBedLocation();
        if ((currentBedLocation != null) && (currentBedLocation.getDistance(pos.getX(), pos.getY(), pos.getZ()) <= 4))
            return;
        if (!world.provider.canRespawnHere() || world.provider.getBiomeForCoords(pos) == Biomes.HELL)
            return;
        entityPlayer.setSpawnPoint(pos, false);
        entityPlayer.setSpawnChunk(pos, false, world.provider.getDimension());
        final TextComponentString text = new TextComponentString(PlayerInBed.i18n.format("playerinbed.spawnchanged"));
        if (PlayerInBed.statusEnabled)
            try { // Does not exist in 1.9
                entityPlayer.sendStatusMessage(text, true);
                return;
            } catch (final NoSuchMethodError e) {}
        entityPlayer.sendMessage(text);
    }

    @SubscribeEvent
    public void onSleepingLocationCheckEvent(final SleepingLocationCheckEvent event) {
        final EntityPlayer entityPlayer = event.getEntityPlayer();
        final String name = entityPlayer.getName();
        final int ratio = getSleepingPercent(entityPlayer, 0);
        if (!playersInBed.contains(name)) {
            playersInBed.add(name);
            if (ratio >= PlayerInBed.ratio)
                sendMessageToPlayers(entityPlayer.world,
                        new TextComponentString(PlayerInBed.i18n.format("playerinbed.isinbedOK", name, ratio)));
            else
                sendMessageToPlayers(entityPlayer.world,
                        new TextComponentString(PlayerInBed.i18n.format("playerinbed.isinbedKO", name, ratio)));
        }

        if (PlayerInBed.skipEnabled && (ratio >= PlayerInBed.ratio) && (ratio < 100)
                && entityPlayer.isPlayerFullyAsleep()) {
            final long worldTime = entityPlayer.world.getWorldTime();
            final long daytime = worldTime % 24000L;
            final long shifttime = 24000L - daytime;
            if (shifttime < 20000L) { // Ensure we are not skipping a full day.
                entityPlayer.world.setWorldTime(worldTime + shifttime);
                if (PlayerInBed.clearWeather)
                    entityPlayer.world.provider.resetRainAndThunder();
            }
        }
    }

    public final void sendMessageToPlayers(final World world, final ITextComponent text) {
        for (EntityPlayer player : world.playerEntities) {
            if (PlayerInBed.messageEnabled)
                player.sendMessage(text);
            if (PlayerInBed.statusEnabled)
                try { // Does not exist in 1.9
                    player.sendStatusMessage(text, true);
                } catch (final NoSuchMethodError e) {}
        }
    }

}

package vlaeh.minecraft.forge.playersinbed;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class PlayerInBedGUIConfig extends GuiConfig {

    public PlayerInBedGUIConfig(GuiScreen parent) {
        super(parent,
                new ConfigElement(PlayerInBed.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                PlayerInBed.MODID, false, false, GuiConfig.getAbridgedConfigPath(PlayerInBed.config.toString()));
    }

}

package vlaeh.minecraft.forge.playersinbed.client;

import net.minecraft.client.resources.I18n;

import vlaeh.minecraft.forge.playersinbed.I18nProxy;

public class I18nClient implements I18nProxy {

	@Override
	public String format(final String key, Object... parameters) {
		return I18n.format(key, parameters);
	}

	@Override
	public void load(final String lang) {
	}

}

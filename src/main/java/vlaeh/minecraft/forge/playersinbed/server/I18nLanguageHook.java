package vlaeh.minecraft.forge.playersinbed.server;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.JsonUtils;

// [TODO] find correct alternative:
// -- locale on server side
// -- send mod's lang files to client side (as datapack)
public class I18nLanguageHook
{
    private static final Gson GSON = new Gson();
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<String, String> modTable = Maps.newHashMap();
    private String last_modid = null;
    private String last_langName = null;

    private void loadLocaleData(final InputStream inputstream) {
        try
        {
            JsonElement jsonelement = GSON.fromJson(new InputStreamReader(inputstream, StandardCharsets.UTF_8), JsonElement.class);
            JsonObject jsonobject = JsonUtils.getJsonObject(jsonelement, "strings");

            jsonobject.entrySet().forEach(entry -> {
                String s = PATTERN.matcher(JsonUtils.getString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
                modTable.put(entry.getKey(), s);
            });
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }
    }
    
    public I18nLanguageHook loadLanguage(String modid, String langName) {
        if (modid.equals(last_modid) && langName.equals(last_langName))
            return this;
        final String langFile = String.format("/assets/%s/lang/%s.json", modid, langName);
        final InputStream inputstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(langFile);
        try {
            loadLocaleData(inputstream);
            last_modid = modid;
            last_langName = langName;
        } catch (Throwable t) {
            LOGGER.warn("Skipped language file for {}: {}", modid, langFile, t);
        }
        return this;
    }

    public String translateKey(String translateKey) {
        String s = this.modTable.get(translateKey);
        return s == null ? translateKey : s;
     }

}
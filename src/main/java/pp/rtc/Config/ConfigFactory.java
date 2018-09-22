package pp.rtc.Config;

import pp.rtc.Interfaces.IRSSParser;
import pp.rtc.Interfaces.ITorrentClient;
import pp.rtc.RSSParsers.LostFilmRSSParser;
import pp.rtc.TorrentClients.TransmissionClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;
/**
 * Factory produces the objects of the torrentApi and rssParser, according config file.
 */
public class ConfigFactory {
    /**
     * Class is initialised by the config.
     */
    private final String configPath;

    /**
     * Return the RSS and Torrect client objects.
     * @param configPathOnFs is absolute path of the config file.
     * Config file should be created on FS
     */
    public ConfigFactory(final String configPathOnFs) {
        //initialise the class by config file
        this.configPath = configPathOnFs;
    }
    /**
     *
     * @param key is parameter from the config file.
     * @return value of this parameter
     */
    private String getConfigValue(final String key) {
        Properties prop = new Properties();
        InputStream input = null;
        String value = "";
        try {
            input = new FileInputStream(this.configPath);
            // load a properties file
            prop.load(input);
            value = prop.getProperty(key);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //exit in case if parameter was not found
        if (value == "") {
            System.exit(1);
        }
        return value;
    }
    /**
     *
     * @return the Object which wraps the API of the torrent client.
     */
    public ITorrentClient getTorrentApi() {
        String torrentClient =  this.getConfigValue("torrent_client");
        ITorrentClient torrentApi = null;
        //Use the switch case functional and use default client instead null
        if (torrentClient.equals("transmission")) {
            String urlOfWebInterface = this.getConfigValue("web_interface");
            torrentApi = new TransmissionClient(urlOfWebInterface);
        }
        return torrentApi;
    }
    /**
     *
     * @return the Object for parsing the RSS response.
     */
    public IRSSParser getRSSparser() {
        String rssUrl =  this.getConfigValue("rss_url");
        String rssParserStr =  this.getConfigValue("rss_parser");
        //TODO:Use the switch case functional!
        IRSSParser rssParser = null;
        if (rssParserStr.equals("LostFilm")) {
            String userId = this.getConfigValue("userId");
            String uses = this.getConfigValue("uses");
            String serialNamesStr = this.getConfigValue("serials");
            String[] serialNames = serialNamesStr.split(",");
            String quality = this.getConfigValue("quality");
            rssParser = new LostFilmRSSParser(
                rssUrl, userId, uses, serialNames, quality);
        }
        return rssParser;
    }
}

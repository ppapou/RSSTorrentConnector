package pp.rtc;

import pp.rtc.Config.ConfigFactory;
import pp.rtc.Interfaces.IRSSParser;
import pp.rtc.Interfaces.ITorrentClient;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Main class of the RSS torrent connector.
 */
public class RssTorrentConnector {
    /**
     * the log4j is defined for the program.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            RssTorrentConnector.class);
    /**
     *
     * @param args cmd arguments like:
     */
    public static void main(final String[]args) {
        Options options = new Options();

        Option configPathObj = new Option(
                "c", "config", true, "config file path");
        configPathObj.setRequired(true);
        options.addOption(configPathObj);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) {
            LOGGER.error("Error due to command parsing: " + pe.getMessage());
            formatter.printHelp("RssTorrentConnector", options);
            System.exit(1);
            return;
        }

        String configPath = cmd.getOptionValue("config");

        LOGGER.info(
                "+++RssTorrent "
                + "connector is initialising by config: " + configPath + "+++");
        //Initialise configuration with config file
        ConfigFactory configuration = new ConfigFactory(configPath);
        //Get instanses
        IRSSParser rssParser =  configuration.getRSSparser();
        ITorrentClient torrentApi = configuration.getTorrentApi();
        //Get XML structure from RSS
        String xmlstring = rssParser.readRSSFeed();
        LOGGER.debug("XML RSS content: " + xmlstring);
        List<String> fileLinks = rssParser.downloadTorrentFiles(xmlstring);
        //@TODO: file link could be got from HTTP response or from FS.
        torrentApi.addTorrents(fileLinks);
        rssParser.deleteTorrentFiles(fileLinks);
    }
}

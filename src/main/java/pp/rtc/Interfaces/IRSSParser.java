package pp.rtc.Interfaces;

import java.util.List;

/**
 * Basic interface for RSS parser.
 */
public interface IRSSParser {
    /**
     * Get the xml file from the Server.
     * @return the xml file from the Server
     */
    String readRSSFeed();
    /**
     * Parse the xml file and download the torrent files.
     * @param xmlstring Xml in String format
     * @return list of torrents files Path on FS
     */
    List<String> downloadTorrentFiles(String xmlstring);

    /**
     *  Clear the FS of the imported torrents.
     * @param torrentFiles - torrent file
     */
    void deleteTorrentFiles(List<String> torrentFiles);
}

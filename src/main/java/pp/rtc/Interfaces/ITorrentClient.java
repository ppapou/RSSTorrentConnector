package pp.rtc.Interfaces;

import java.util.List;
/**
 * Interface for API oth torrent client.
 */
public interface ITorrentClient {
    /**
     * Add the torrent file in the download queue.
     * @param torrentFiles path on fs
     */
    void addTorrents(List<String> torrentFiles);
}

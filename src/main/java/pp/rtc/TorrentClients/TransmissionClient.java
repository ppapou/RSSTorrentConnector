package pp.rtc.TorrentClients;

import pp.rtc.Interfaces.ITorrentClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import nl.stil4m.transmission.api.*;
import nl.stil4m.transmission.api.domain.*;
import nl.stil4m.transmission.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementing the Torrent client interface.
 */
public class TransmissionClient implements ITorrentClient {
    /**
     * log4g is defined.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
    /**
     * the variable of the transmission web interface.
     */
    private final String urlOfWebInterface;
    /**
     *
     * @param urlOfWebInterface - address of the transmission webServer.
     */
    public TransmissionClient(final String urlOfWebInterface) {
        this.urlOfWebInterface = urlOfWebInterface;
    }
    /**
     * Initialise the transmission client.
     * @return the transmission client instance
     * @throws - RpcException
     */
    private TransmissionRpcClient getTorrentClient() throws RpcException {
        //TODO move log4ConfPath on top level of ConfigFactory
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RpcConfiguration rpcConfiguration = new RpcConfiguration();
        rpcConfiguration.setHost(URI.create(this.urlOfWebInterface));
        RpcClient client = new RpcClient(rpcConfiguration, objectMapper);
        TransmissionRpcClient torrentClient = new TransmissionRpcClient(client);
     return torrentClient;
    }
    /**
     * Print list of active torrents.
     */
    public void showTorrentsList() {
        List<TorrentInfo> torrentsList = null;
        try {
            TransmissionRpcClient torrentClient = this.getTorrentClient();
            TorrentInfoCollection result = torrentClient.getAllTorrentsInfo();
            torrentsList = result.getTorrents();
        } catch (RpcException e) {
            LOGGER.error("Torrent client connection error: " + e.getMessage());
        }
        for (TorrentInfo item : torrentsList) {
            LOGGER.info("Torrent -> " + item.getName());
        }
    }
    /**
     * Add torrent into the Download queue.
     * @param torrentFiles path on fs
     */
    public void addTorrents(final List<String> torrentFiles) {
        for (String file : torrentFiles) {
            try {
                LOGGER.info("Adding the torrent file:" + file);
                TransmissionRpcClient torrentClient = this.getTorrentClient();
                AddTorrentInfo addTorrentInfo = new AddTorrentInfo();
                addTorrentInfo.setFilename(file);
                AddedTorrentInfo result = torrentClient.addTorrent(addTorrentInfo);
                LOGGER.debug("File " + file + " is loaded properly!");
            } catch (RpcException e) {
                LOGGER.error("Error via adding torrent: " + e.getMessage());
            }
        }
    }
}

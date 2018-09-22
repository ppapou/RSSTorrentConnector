package pp.rtc.RSSParsers;

import pp.rtc.Interfaces.IRSSParser;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Parser for LostFilm RSS.
 */
public class LostFilmRSSParser implements IRSSParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            LostFilmRSSParser.class);
    /**
     * Address of the Torrent server.
     */
    private final String urlAddress;
    /**
     * Id of the LostFilm account.
     */
    private final String userId;
    /**
     * Secure use parameter.
     */
    private final String uses;
    /**
    * List of the serial name.
    */
    private final String[] serialNames;
    /**
     * Quality of thevide stream.
     */
    private final String quality;
    /**
     * CLass constructor
     * @param urlAddress - address of the LostFilm RSSserver
     * @param userId
     * @param uses
     * @param serialNames - List with serial's titles
     * @param quality - Quality: SD, HD
     */
    public LostFilmRSSParser(
            final String urlAddress, final String userId,
            final String uses, final String[] serialNames, final String quality) {
       this.urlAddress = urlAddress;
       this.userId = userId;
       this.uses = uses;
       this.serialNames = serialNames;
       this.quality = quality;
    }
    /**
     * Returns the xml structure as string.
     * @return xmlString
     */
    public String readRSSFeed() {
        LOGGER.info("Start downloading the RSS");
        String xmlstring = "";
        try {
            URL rssUrl = new URL (this.urlAddress);
            URLConnection rssConnection = rssUrl.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(rssConnection.getInputStream()));
            String line;
            while((line=in.readLine()) != null) {
                xmlstring += line + "\n" ;
            }
            in.close();
            return xmlstring;
        } catch (MalformedURLException ue) {
            LOGGER.error("Failed to get rss" + ue.getMessage());
        } catch (IOException ioe) {
            LOGGER.error("Reading XML error:" + ioe.getMessage());
        }
        return xmlstring;
    }
    /**
     *
     * @param xmlString - XML structure from the RSS server in str format
     * @return: HashMap
     *{ "title": "Epsode Ttile",
     *   "category": "Episode category",
     *    "pubDate": "Date of Public",
     *    "link": "Link of Episode"
     * }
     */
    private ArrayList<String> getJSONList(String xmlString){
        ArrayList<String> episodesArray = new ArrayList<String>();
        try {
            if (xmlString.equals("")) {
              LOGGER.info("RSS is empty...Exit");
              System.exit(1);
            }
            //create the object factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            //parse the xml string
            Document doc = db.parse(is);
            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                //Initialise Gson object to put information of episodes
                Gson gson = new Gson();
                //HASH map. The epsisode map will be converted to GSON object
                Map episodeMap = new HashMap();
                //Initialise ARRAY list for JSON objects
                Element element = (Element) items.item(i);
                //Get lists objects of the each item
                NodeList titles = element.getElementsByTagName("title");
                NodeList categories = element.getElementsByTagName("category");
                NodeList pubdates = element.getElementsByTagName("pubDate");
                NodeList links = element.getElementsByTagName("link");
                //Get object of each tag:
                Element title = (Element) titles.item(0);
                Element category = (Element) categories.item(0);
                Element pubDate = (Element) pubdates.item(0);
                Element link = (Element) links.item(0);
                //Fill the HASH object
                episodeMap.put("Title", title.getTextContent());
                episodeMap.put("Category", category.getTextContent());
                episodeMap.put("PubDate", pubDate.getTextContent());
                episodeMap.put("Link", link.getTextContent());
                //Convert HASHMAP to the JSON
                String gsonEpisode = gson.toJson(episodeMap);
                //Put the JSON to the Array of episodes
                episodesArray.add(gsonEpisode);
            }
        } catch(Exception e) {
            LOGGER.error(
                    "Error due to parsing xml response: " + e.getMessage());
        }
    return episodesArray;
    }
    /**
     *
     * @param episodesArray Requred episodes array with quality.
     * @return List of the links of serial episodes
     */
    private List<String> getEpisodeLinks(final List<String> episodesArray) {
        String episodeLink;
        List<String> filtredEpisodes = new ArrayList<String>();
        for (String serialName: this.serialNames) {
            //TODO:add ignore case in serial regexp
            String serialRegExp = ".*" + serialName + ".*";
            String qualityRegExp = ".*" + this.quality + ".*";
            for (String episode: episodesArray) {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> decodedEpisode = new HashMap<String, String>();
                decodedEpisode = gson.fromJson(episode, type);
                String title = decodedEpisode.get("Title");
                String category = decodedEpisode.get("Category");
                String link = decodedEpisode.get("Link");
                if ((title.matches(serialRegExp)) && (
                        category.matches(qualityRegExp))) {
                    LOGGER.debug("Episode will be downloaded:" + title);
                    episodeLink = link;
                    filtredEpisodes.add(episodeLink);
                }
            }
        }
    return filtredEpisodes;
    }
    /**
     * Get bytes ot from the RSS server.
     * @param torrentURL: url of the torrent file of LostFilm
     * @return the bytes of torrent file
     */
    private HttpResponse getTorrentFileData(final String torrentURL) {
        //TODO: Could be the log configuration is moved to the common package?
        HttpResponse response = null;
        try {
        HttpGet request = new HttpGet(torrentURL);
        //Initialize cookie
        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie uid = new BasicClientCookie("uid", this.userId);
        BasicClientCookie usess = new BasicClientCookie("usess", this.uses);
        usess.setDomain("tracktor.in");
        usess.setPath("/");
        uid.setDomain("tracktor.in");
        uid.setPath("/");
        cookieStore.addCookie(uid);
        cookieStore.addCookie(usess);
        //Initialise HTTP client and send request with cookie
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        //TODO:The custom exception could be created for ivalid cred
        response = client.execute(request);
        } catch (IOException ioe) {
            LOGGER.error(
                    "Error of reading of the RSS content:" + ioe.getMessage());
        }
        return response;
    }
    /**
     * Download toreent file ans Save on the local FS.
     * @param xmlstring Xml in String format
     * @return list of torrents files paths on local FS
     */
    public List<String> downloadTorrentFiles(final String xmlstring) {
        String fileDir = System.getProperty("user.dir");
        ArrayList<String> filesList = new ArrayList<String>();
        //Get array with hash which includes json structure
        ArrayList<String> episodesArray = this.getJSONList(xmlstring);
        //Get torrent links list
        List<String> torrentLinks = this.getEpisodeLinks(episodesArray);
        LOGGER.debug("Downloading URLS: " + torrentLinks);
        if (torrentLinks.size() == 0) {
            LOGGER.info(
                    "There are no episodes. " +
                    "Ensure that links are presented on tracker");
        } else {
            for (int i = 0; i < torrentLinks.size(); i++) {
                //Download the torrent files
                HttpResponse response = getTorrentFileData(torrentLinks.get(i));
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    outputStream = new FileOutputStream(
                            new File(fileDir + "/" + "file" + i + ".torrent"));
                    inputStream = response.getEntity().getContent();

                    int read;
                    int byteSize = 32768;
                    byte[] buffer = new byte[byteSize];
                    while ((read = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, read);
                    }
                    filesList.add(fileDir + "/" + "file" + i + ".torrent");
                } catch (IOException ioe) {
                    LOGGER.error("File writting error: " + ioe.getMessage());
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            LOGGER.error(
                                    "Input stream is not closed properly: "
                                            + e.getMessage());
                        }
                    }
                    if (outputStream != null) {
                        try {
                            // outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            LOGGER.error(
                                    "Output stream is not closed properly: "
                                            + e.getMessage());
                        }
                    }
                }
            }
        }
    return filesList;
    }
    /**
     * Delete torrent files from FS.
     * @param torrentFiles - torrent files paths on FS
     */
   public void deleteTorrentFiles(final List<String> torrentFiles) {
       for (String file : torrentFiles) {
           try {
               Path fileToDeletePath = Paths.get(file);
               Files.delete(fileToDeletePath);
           } catch (NoSuchFileException nsfe) {
               LOGGER.error(
                       "File is not deleted properly: " + nsfe.getMessage());
           } catch (IOException ioe) {
               LOGGER.error("User has no permissions: " + ioe.getMessage());
           }
       }
   }
}

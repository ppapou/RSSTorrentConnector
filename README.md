# RSS Torrent Connector
A java console utility, which retrieves required serial from the xml of the RSS channel.
Currently the connector supports the transmission API and parses the RSS of lostFilm server.

# Project Status
The connector is still under development and required the additional modifications.

# USAGE
1. Prepare the properties config:
```
# Example of config file.
# Properties for LostFilm RSS
rss_parser=LostFilm
rss_url=http://retre.org/rssdd.xml
userId=
uses=
serials=The Strain
quality=MP4
# Torrent client
torrent_client=transmission
web_interface = http://localhost:9091/transmission/rpc
```
2. Build the jar with maven:
```
mvn package
```
3. Create the log file:
```
touch /var/log/rss_torrent_connector.log
```
4. Run the utility:
```
java -jar rtc-0.0.1-jar-with-dependencies.jar -c Config.properties

```

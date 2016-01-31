# GoogleAnalyticsApp
JavaFX 8 based app for retrieving Google Analytics Data (ga:users and ga:avgTimeOnPage) on monthly base.

# Screenshots
![missing image](addofferscreen.png?raw=true "Add new website screen")
![missing image](mainscreen.png?raw=true "GoogleAnalyticsApp Main screen")

# Why GoogleAnalyticsApp?
We at doubleSlash have some websites which are monitored and reported monthly by our marketing department. They use Google Analytics to get usage information and put this information in an aggregated form into powerpoint.<br>
If I want to have some live data or special reports in the past I have to go to the marketing and discourage them from work.<br>
With this tool I can monitor our website everytime I want and whatever I want.

# Latest stable build: !(bin/gawebsite.jar)

# Prerequisites to compile
- JDK 1.8 Update40 or higher
- Maven 3

'mvn install' will generate a runnable jar-file 'gawebsite.jar' into ./bin directory

Replace src/main/resources/client_secrets.json with your own API-Key. (see https://console.developers.google.com/apis/credentials and register your own app)









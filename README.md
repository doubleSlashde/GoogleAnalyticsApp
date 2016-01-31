# GoogleAnalyticsApp
JavaFX based App for retrieving Google Analytics Data (ga:users and ga:avgTimeOnPage) on monthly base.

Replace src/main/resources/client_secrets.json with your own API-Key. (see https://console.developers.google.com/apis/credentials and register your own app)

# Prerequisites to compile
- JDK 1.8 Update40 or higher
- Maven 3

'mvn install' will generate a runnable jar-file 'gaoffersite.jar'

# Why GoogleAnalyticsApp?
We at doubleSlash have some websites which are monitored and reported monthly by our marketing department. They use Google Analytics to get usage information and put this information in an aggregated form into powerpoint.<br>
If I want to have some live data or special reports in the past I have to go to the marketing and discourage them from work.<br>
With this tool I can monitor our website everytime I want and whatever I want.

# Screenshots
![picture not found](screenshot.png?raw=true "GoogleAnalyticsApp Main screen")






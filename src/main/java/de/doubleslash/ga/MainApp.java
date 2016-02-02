package de.doubleslash.ga;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {

   private static final String APPLICATION_NAME = "GABrowser";
   private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
         ".store/ga_app");
   private FileDataStoreFactory dataStoreFactory;
   private HttpTransport httpTransport;
   private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

   Analytics analytics;

   /**
    * Authorizes the installed application to access user's protected data. <br>
    * To get your own client_secrets.json use the Google API Developer console
    * (https://console.developers.google.com/apis/credentials) the registered client_secrets.json within this project is
    * limited up to 50.000 req/day.
    * 
    * @return
    * @throws IOException
    * @throws Exception
    */
   private Credential authorize() throws IOException {
      // load client secrets
      final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
            new InputStreamReader(MainApp.class.getResourceAsStream("/client_secrets.json")));

      // @formatter:off
       final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                                                    httpTransport, 
                                                    JSON_FACTORY, 
                                                    clientSecrets,
                                                    Collections.singleton(AnalyticsScopes.ANALYTICS_READONLY))
                                               .setDataStoreFactory(dataStoreFactory)
                                               .build();
       // @formatter:on

      return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

   }

   /**
    * Performs all necessary setup steps for running requests against the API.
    * 
    * @return An initialized Analytics service object.
    * @throws IOException
    * @throws Exception
    *            if an issue occurs with OAuth2Native authorize.
    */
   private Analytics initializeAnalytics() throws IOException {
      // Authorization.
      final Credential credential = authorize();

      // Set up and return Google Analytics API client.
      return new Analytics.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
            .build();
   }

   @Override
   public void start(final Stage primaryStage) {

      try {
         httpTransport = GoogleNetHttpTransport.newTrustedTransport();
         dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
         analytics = initializeAnalytics();
      } catch (GeneralSecurityException | IOException e) {
         showException("There was a service error: " + e.toString() + " : " + e.getMessage(), e);
      }

      OfferSite.loadOfferSite();

      try {
         final FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("GABrowser.fxml"));

         final AnchorPane page = (AnchorPane) loader.load();
         final Scene scene = new Scene(page);

         final Stage stage = new Stage();
         stage.setTitle("GoogleAnalytics Browser");
         stage.initModality(Modality.WINDOW_MODAL);
         stage.toFront();
         stage.setResizable(false);
         stage.initOwner(primaryStage);
         stage.setScene(scene);

         // Link with controller
         final GABrowserController controller = loader.getController();

         controller.setStage(stage);
         controller.setAnalytics(analytics);

         controller.start();

      } catch (final Exception e) {
         showException("Dialog konnte nicht initialisiert werden.", e);
      }

   }

   public static void main(final String[] args) {
      launch(args);
   }

   /**
    * @param message
    */
   public static void showInfo(final String message) {
      final Alert alert = new Alert(AlertType.INFORMATION);
      alert.setContentText(message);
      alert.setHeaderText("Hinweis");
      alert.setWidth(480.0);
      alert.setHeight(240.0);
      alert.getDialogPane().setPrefSize(480, 240);
      alert.showAndWait();
   }

   public static void showException(final String message, final Exception e) {
      final Alert alert = new Alert(AlertType.ERROR);
      alert.setContentText(message);
      alert.setHeaderText("Systemfehler");
      alert.setWidth(800.0);
      alert.setHeight(600.0);
      // Create expandable Exception.
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      final String exceptionText = sw.toString();

      final Label label = new Label("The exception stacktrace was:");

      final TextArea textArea = new TextArea(exceptionText);
      textArea.setEditable(false);
      textArea.setWrapText(true);

      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);
      GridPane.setVgrow(textArea, Priority.ALWAYS);
      GridPane.setHgrow(textArea, Priority.ALWAYS);

      final GridPane expContent = new GridPane();
      expContent.setMaxWidth(Double.MAX_VALUE);
      expContent.add(label, 0, 0);
      expContent.add(textArea, 0, 1);

      // Set expandable Exception into the dialog pane.
      // alert.getDialogPane().setPrefSize(800, 540);
      alert.getDialogPane().setExpandableContent(expContent);

      alert.showAndWait();
   }

}

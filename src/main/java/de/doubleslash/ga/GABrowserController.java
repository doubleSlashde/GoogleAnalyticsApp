package de.doubleslash.ga;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.GaData.ColumnHeaders;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 * @author kkrafft
 *
 */
public class GABrowserController {

   Stage                                  stage;
   Analytics                              analytics;

   private List<TableEntry>               dataList;
   private ObservableList<TableEntry>     observableDataList;

   @FXML BarChart<String, Number>         userChart;
   @FXML ListView<OfferSite>              offerListView;
   @FXML TableView<TableEntry>            dataTable;
   @FXML TableColumn<TableEntry, String>  colMonth;
   @FXML TableColumn<TableEntry, Integer> colUsers;
   @FXML TableColumn<TableEntry, String>  colAvgSessionDuration;
   @FXML CheckBox                         chkOrganic;    

   @FXML Button                           removeBtn;
   @FXML Button                           addBtn;

   /**
    * 
    */
   private void initOfferListView() {

      offerListView.getItems().setAll(OfferSite.loadOfferSite());
      // Rendering der Zelle auf custom object 'OfferSite' anpassen
      offerListView.setCellFactory(lv -> new ListCell<OfferSite>() {
         @Override
         protected void updateItem(OfferSite site, boolean bln) {
            super.updateItem(site, bln);
            if (site != null) {
               // Schreibe einfach nur den Namen des Offers in die Zelle
               setText(site.offerName);
            }
            else {
               setText("");
            }
         }
      });

      offerListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OfferSite>() {

         @Override
         public void changed(ObservableValue<? extends OfferSite> observable, OfferSite oldValue, OfferSite newValue) {
            reloadData(newValue);
         }
      });
   }

   /**
    * 
    * @param data
    */
   private void showData(GaData data) {

      observableDataList.clear();

      if (data.getRows() != null && !data.getRows().isEmpty()) {

         // Print actual data.
         for (List<String> row : data.getRows()) {

            String month = getMonthForInt(new Integer(row.get(1))) + " " + row.get(0);
            Integer users = new Integer(row.get(2));
            int sec = (new Double(row.get(3))).intValue();
            String avgSessionDuration = sec / 60 + ":" + String.format("%02d", (sec % 60));

            TableEntry entry = new TableEntry(month, users, avgSessionDuration);
            observableDataList.add(entry);
         }
      }

      dataTable.refresh();
   }

   /**
    * Show GA data in a somple BarChart. <br>
    * This method assumes that the first two columns contain year and month
    * information.
    * 
    * @param data
    */
   private void showChart(GaData data) {

      userChart.getData().clear();

      if (data.getRows() == null || data.getRows().isEmpty()) {
         userChart.setTitle("Keine Daten.");
      }
      else {

         XYChart.Series<String, Number> seriesUser = new XYChart.Series<>();

         // Print actual data.
         for (List<String> row : data.getRows()) {
            String month = getMonthForInt(new Integer(row.get(1))) + " " + row.get(0);
            Integer users = new Integer(row.get(2));

            XYChart.Data<String, Number> datapoint = new XYChart.Data<>(month, users);
            Tooltip tooltip = new Tooltip();
            tooltip.setText(datapoint.getYValue().toString());
            Tooltip.install(datapoint.getNode(), tooltip);

            seriesUser.getData().add(datapoint);

            // adjust
            NumberAxis y = (NumberAxis) userChart.getYAxis();
            if (users > y.getUpperBound()) {
               y.setUpperBound(users + 100.0);
            }

         }
         seriesUser.setName("Besucher");
         userChart.getData().add(seriesUser);

      }

   }

   /**
    * @see to find and adjust correct parameter use Query-Explorer at
    *      https://ga-dev-tools.appspot.com/query-explorer/
    * @param id
    * @return
    */
/*   private GaData getGAData(String id, String path) {

      try {
         Get get = analytics.data().ga().get("ga:" + id, "2015-07-01", "today", "ga:users,ga:avgTimeOnPage").setDimensions("ga:year,ga:month");
         String filter = null;
         if (path != null && path.length() > 0) {
            filter = "ga:pagePath=~(" + path + ")";
            get = get.setFilters( filter );
         }
         return get.execute();
      }
      catch (IOException e) {
         MainApp.showException("Google Analytics Daten konnte nicht gelesen werden.", e);
         return null;
      }
   }
*/   
   /**
    * @see to find and adjust correct parameter use Query-Explorer at
    *      https://ga-dev-tools.appspot.com/query-explorer/
    * @param id
    * @return
    */
   private GaData getGAData(String id, String path) {

      try {
         Get get = analytics.data().ga().get("ga:" + id, "2015-07-01", "today", "ga:users,ga:avgTimeOnPage").setDimensions("ga:year,ga:month");
         String filter = "";
         if (path != null && path.length() > 0) {
            filter = "ga:pagePath=~(" + path + ")";
         }
         if (chkOrganic.isSelected()) {
            if (filter.length() > 1) filter += ";";
            filter += "ga:medium==organic";
         }
         get.setFilters(filter);
         return get.execute();
      }
      catch (IOException e) {
         MainApp.showException("Google Analytics Daten konnte nicht gelesen werden.", e);
         return null;
      }
   }

   /**
    * Returns the top 25 organic search keywords and traffic source by visits.
    * The Core Reporting API is used to retrieve this data.
    *
    * @param analytics
    *           the analytics service object used to access the API.
    * @param profileId
    *           the profile ID from which to retrieve data.
    * @return the response from the API.
    * @throws IOException
    *            tf an API error occured.
    */
   @SuppressWarnings("unused")
   private static GaData executeDataQuery(Analytics analytics, String profileId) throws IOException {

      /*
       * GaData data = analytics.data().ga().get("ga:" + profileId, // Table Id.
       * ga: + profile id. "2012-01-01", // Start date. "2012-01-14", // End
       * date. "ga:visits") // Metrics. .setDimensions("ga:source,ga:keyword")
       * .setSort("-ga:visits,ga:source") .setFilters("ga:medium==organic")
       * .setMaxResults(25) .execute();
       */
      GaData data = analytics.data().ga().get("ga:" + profileId, "2015-12-01", "2016-01-10", "ga:users").execute();

      return data;
   }

   /**
    * Prints the output from the Core Reporting API. The profile name is printed
    * along with each column name and all the data in the rows.
    *
    * @param results
    *           data returned from the Core Reporting API.
    */
   @SuppressWarnings("unused")
   private static void printGaData(GaData results) {
      System.out.println("printing results for profile: " + results.getProfileInfo().getProfileName());

      if (results.getRows() == null || results.getRows().isEmpty()) {
         System.out.println("No results Found.");
      }
      else {

         // Print column headers.
         for (ColumnHeaders header : results.getColumnHeaders()) {
            System.out.printf("%30s", header.getName());
         }
         System.out.println();

         // Print actual data.
         for (List<String> row : results.getRows()) {
            for (String column : row) {
               System.out.printf("%30s", column);
            }
            System.out.println();
         }

         System.out.println();
      }
   }

   private String getMonthForInt(int num) {
      String month = "wrong";
      DateFormatSymbols dfs = new DateFormatSymbols();
      String[] months = dfs.getShortMonths();
      if (num >= 1 && num <= 12) {
         month = months[num - 1];
      }
      return month;
   }

   /**
    * @param newValue
    */
   private void reloadData(OfferSite newValue) {
      if (newValue != null) {
         userChart.setTitle(newValue.offerName);
         GaData data = getGAData(newValue.viewId, newValue.path);
         showData(data);
         showChart(data);
      }
   }

   public void start() {
   
      initOfferListView();
   
      // init TableView
      colMonth.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("month"));
      colUsers.setCellValueFactory(new PropertyValueFactory<TableEntry, Integer>("users"));
      colAvgSessionDuration.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avgSessionDuration"));   
      dataList = new ArrayList<TableEntry>();
      observableDataList = FXCollections.observableArrayList(dataList);
      dataTable.setItems(observableDataList);
   
      // BarChart
      userChart.getXAxis().setLabel("Monate");
      userChart.getYAxis().setLabel("Besucher");
      userChart.getYAxis().setAutoRanging(false);
   
      stage.centerOnScreen();
      stage.toFront();
      stage.setResizable(true);
      stage.showAndWait();
   
   }

   public void setStage(Stage stage) {
      this.stage = stage;
   }

   public void setAnalytics(Analytics analytics) {
      this.analytics = analytics;
   
   }

   /**
    * 
    */
   @FXML
   public void addOffer() {
      try {
         FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("ProfileSelector.fxml"));
   
         AnchorPane page = (AnchorPane) loader.load();
         Scene scene = new Scene(page);
   
         Stage dialogeStage = new Stage();
         dialogeStage.setTitle("Auswertung für ein Website-Angebot erstellen");
         dialogeStage.initModality(Modality.WINDOW_MODAL);
         dialogeStage.toFront();
         dialogeStage.setResizable(false);
         dialogeStage.initOwner(stage);
         dialogeStage.setScene(scene);
   
         // Link with controller
         ProfileSelectorController controller = loader.getController();
   
         controller.setStage(dialogeStage);
         controller.setAnalytics(analytics);
   
         controller.start();
   
         if (controller.isSave()) {
            OfferSite offer = new OfferSite(controller.getOffername(), controller.getViewid(), controller.getPath());
            if (offer.path == null || offer.path.length() == 0) {
               offer.path = "/";
            }
            offerListView.getItems().add(offer);
            OfferSite.saveFile(offerListView.getItems());
            offerListView.refresh();
         }
   
      }
      catch (Exception e) {
         MainApp.showException("Dialog konnte nicht initialisiert werden.", e);
      }
   
   }

   /**
    * 
    */
   @FXML
   public void removeOffer() {
      final int selIndex = offerListView.getSelectionModel().getSelectedIndex();
      if (selIndex != -1) {
         final int newIndex = (selIndex == offerListView.getItems().size() - 1) ? selIndex - 1 : selIndex;
         offerListView.getItems().remove(selIndex);
         offerListView.getSelectionModel().select(newIndex);
      }
      OfferSite.saveFile(offerListView.getItems());
   }

   @FXML
   public void reloadData() {
      OfferSite offer = offerListView.getSelectionModel().getSelectedItem();
      reloadData( offer );
   }

}

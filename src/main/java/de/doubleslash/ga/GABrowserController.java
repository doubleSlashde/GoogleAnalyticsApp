package de.doubleslash.ga;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.model.GaData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author kkrafft
 */
public class GABrowserController {

    private static final double YAXIS_MARGIN = 100.0;
    private Stage stage;
    private Analytics analytics;

    private ObservableList<TableEntry> observableDataList;

    @FXML
    BarChart<String, Number> userChart;
    @FXML
    ListView<OfferSite> offerListView;
    @FXML
    TableView<TableEntry> dataTable;
    @FXML
    TableColumn<TableEntry, String> colMonth;
    @FXML
    TableColumn<TableEntry, Integer> colUsers;
    @FXML
    TableColumn<TableEntry, String> colAvgSessionDuration;
    @FXML
    CheckBox chkOrganic;
    @FXML
    DatePicker startDate;

    @FXML
    Button removeBtn;
    @FXML
    Button addBtn;

    /**
     *
     */
    private void initOfferListView() {

        offerListView.getItems().setAll(OfferSite.loadOfferSite());
        // Rendering der Zelle auf custom object 'OfferSite' anpassen
        offerListView.setCellFactory(lv -> new ListCell<OfferSite>() {
            @Override
            protected void updateItem(final OfferSite site, final boolean bln) {
                super.updateItem(site, bln);
                if (site != null) {
                    // Schreibe einfach nur den Namen des Offers in die Zelle
                    setText(site.offerName);
                } else {
                    setText("");
                }
            }
        });

        offerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> reloadData(newValue));
    }


    private void showData(final GaData data) {

        observableDataList.clear();

        if (data.getRows() != null && !data.getRows().isEmpty()) {

            // Print actual data.
            for (final List<String> row : data.getRows()) {

                final String month = getMonthForInt(Integer.valueOf(row.get(1))) + " " + row.get(0);
                final Integer users = Integer.valueOf(row.get(2));
                final int sec = (new Double(row.get(3))).intValue();
                final String avgSessionDuration = sec / 60 + ":" + String.format("%02d", (sec % 60));

                final TableEntry entry = new TableEntry(month, users, avgSessionDuration);
                observableDataList.add(entry);
            }
        }

        dataTable.refresh();
    }

    /**
     * Show GA data in a somple BarChart. <br>
     * This method assumes that the first two columns contain year and month information.
     */
    private void showChart(final GaData data) {

        userChart.getData().clear();

        if (data.getRows() == null || data.getRows().isEmpty()) {
            userChart.setTitle("Keine Daten.");
        } else {

            final XYChart.Series<String, Number> seriesUser = new XYChart.Series<>();

            // Print actual data.
            for (final List<String> row : data.getRows()) {
                final String month = getMonthForInt(Integer.valueOf(row.get(1))) + " " + row.get(0);
                final Integer users = Integer.valueOf(row.get(2));

                final XYChart.Data<String, Number> datapoint = new XYChart.Data<>(month, users);
                final Tooltip tooltip = new Tooltip();
                tooltip.setText(datapoint.getYValue().toString());
                Tooltip.install(datapoint.getNode(), tooltip);

                seriesUser.getData().add(datapoint);

                // adjust
                final NumberAxis y = (NumberAxis) userChart.getYAxis();
                if (users > y.getUpperBound()) {
                    y.setUpperBound(users + YAXIS_MARGIN);
                }

            }
            seriesUser.setName("Besucher");
            userChart.getData().add(seriesUser);

        }

    }

    /**
     * https://ga-dev-tools.appspot.com/query-explorer/
     */
    private GaData getGAData(final String id, final String path) {

        String format = startDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try {
            final Get get = analytics.data().ga().get("ga:" + id, format, "today", "ga:users,ga:avgTimeOnPage")
                    .setDimensions("ga:year,ga:month");
            String filter = "";
            if (path != null && path.length() > 0) {
                filter = "ga:pagePath=~(" + path + ")";
            }
            if (chkOrganic.isSelected()) {
                if (filter.length() > 1) {
                    filter += ";";
                }
                filter += "ga:medium==organic";
            }
            get.setFilters(filter);
            return get.execute();
        } catch (final IOException e) {
            MainApp.showException("Google Analytics Daten konnte nicht gelesen werden.", e);
            return null;
        }
    }

    /**
     */
    private String getMonthForInt(final int num) {
        String month = "wrong";
        final DateFormatSymbols dfs = new DateFormatSymbols();
        final String[] months = dfs.getShortMonths();
        if (num >= 1 && num <= 12) {
            month = months[num - 1];
        }
        return month;
    }


    private void reloadData(final OfferSite newValue) {
        if (newValue != null) {
            userChart.setTitle(newValue.offerName);
            final GaData data = getGAData(newValue.viewId, newValue.path);
            showData(data);
            showChart(data);
        }
    }

    void start() {

        initOfferListView();

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.MONTH,-11);
        LocalDate startDateLocal = LocalDate.of( cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        startDate.setValue(startDateLocal);
        startDate.setOnAction(event -> reloadData());

        // init TableView
        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colUsers.setCellValueFactory(new PropertyValueFactory<>("users"));
        colAvgSessionDuration.setCellValueFactory(new PropertyValueFactory<>("avgSessionDuration"));
        observableDataList = FXCollections.observableArrayList(new ArrayList<TableEntry>());
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

    void setStage(final Stage stage) {
        this.stage = stage;
    }

    void setAnalytics(final Analytics analytics) {
        this.analytics = analytics;

    }

    /**
     *
     */
    @FXML
    public void addOffer() {
        try {
            final FXMLLoader loader = new FXMLLoader(MainApp.class.getClassLoader().getResource("ProfileSelector.fxml"));

            final AnchorPane page = loader.load();
            final Scene scene = new Scene(page);

            final Stage dialogeStage = new Stage();
            dialogeStage.setTitle("Auswertung für ein Website-Angebot erstellen");
            dialogeStage.initModality(Modality.WINDOW_MODAL);
            dialogeStage.toFront();
            dialogeStage.setResizable(false);
            dialogeStage.initOwner(stage);
            dialogeStage.setScene(scene);

            // Link with controller
            final ProfileSelectorController controller = loader.getController();

            controller.setStage(dialogeStage);
            controller.setAnalytics(analytics);

            controller.start();

            if (controller.isSave()) {
                final OfferSite offer = new OfferSite(controller.getOffername(), controller.getViewid(),
                        controller.getPath());
                if (offer.path == null || offer.path.length() == 0) {
                    offer.path = "/";
                }
                offerListView.getItems().add(offer);
                OfferSite.saveFile(offerListView.getItems());
                offerListView.refresh();
            }

        } catch (final Exception e) {
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
        final OfferSite offer = offerListView.getSelectionModel().getSelectedItem();
        reloadData(offer);
    }

}

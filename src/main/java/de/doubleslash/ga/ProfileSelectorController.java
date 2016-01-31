package de.doubleslash.ga;

import java.io.IOException;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.Account;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.Profile;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.analytics.model.Webproperty;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ProfileSelectorController {

   Stage stage;
   Analytics analytics; 
   
   @FXML ComboBox<Account> cmbAccount;
   @FXML ComboBox<Webproperty> cmbProperty;
   @FXML ComboBox<Profile> cmbView;
   @FXML Label lblViewid;
   @FXML TextField txtPath;
   @FXML TextField txtOffername;
   private boolean save;

   public void setStage(Stage stage) {
      this.stage = stage;   
    }
   
   public void setAnalytics(Analytics analytics) {
      this.analytics = analytics;  
   }

    public void start() {
       
    // Query for the list of all accounts associated with the service account.
       try {
         Accounts accounts = analytics.management().accounts().list().execute();
         initializeComboBoxes(accounts); 
         
      }
      catch (IOException e) {
        MainApp.showException("Fehler beim Auslesen des Google Kontos.", e);
      }

       
       stage.centerOnScreen();
       stage.toFront();
       stage.setResizable( false );
       stage.showAndWait();
    }

   /**
    * @param accounts
    */
   private void initializeComboBoxes(Accounts accounts) {

      ObservableList<Account> accountList = FXCollections.observableList(accounts.getItems());
      cmbAccount.setItems(accountList);
      cmbAccount.setCellFactory(p -> new ListCell<Account>() {
         @Override
         protected void updateItem(Account account, boolean bln) {
            super.updateItem(account, bln);
            if (account != null) {
               // Schreibe einfach nur den Namen des Offers in die Zelle
               setText(account.getName());
            }
         }
      });

      cmbProperty.setCellFactory(p -> new ListCell<Webproperty>() {
         @Override
         protected void updateItem(Webproperty property, boolean empty) {
            super.updateItem(property, empty);
            if (property != null) {
               // Schreibe einfach nur den Namen des Offers in die Zelle
               setText(property.getName());
            }
         }
      });

      cmbView.setCellFactory(p -> new ListCell<Profile>() {
         @Override
         protected void updateItem(Profile profile, boolean empty) {
            super.updateItem(profile, empty);
            if (profile != null) {
               // Schreibe einfach nur den Namen des Offers in die Zelle
               setText(profile.getName());
            }
         }
      });

      cmbAccount.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Account>() {

         @Override
         public void changed(ObservableValue<? extends Account> observable, Account oldValue, Account newValue) {
            String id = newValue.getId();
            try {
               Webproperties properties = analytics.management().webproperties().list(id).execute();
               ObservableList<Webproperty> propertyList = FXCollections.observableList(properties.getItems());
               cmbProperty.setItems(propertyList);
               cmbView.setItems(null);
            }
            catch (IOException e) {
               MainApp.showException("Fehler beim Auslesen der GoogleAnalytics Web-Properties.", e);
            }

         }
      });

      cmbProperty.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Webproperty>() {

         @Override
         public void changed(ObservableValue<? extends Webproperty> observable, Webproperty oldValue, Webproperty newValue) {
            if (newValue != null) {
               String wbid = newValue.getId();
               String accId = newValue.getAccountId();
               try {
                  Profiles profiles = analytics.management().profiles().list(accId, wbid).execute();
                  ObservableList<Profile> profileList = FXCollections.observableList(profiles.getItems());
                  cmbView.setItems(profileList);
               }
               catch (IOException e) {
                  MainApp.showException("Kann GoogleAnalytics Profile/Views nicht lesen.", e);
               }
            }
         }
      });

      cmbView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Profile>() {

         @Override
         public void changed(ObservableValue<? extends Profile> observable, Profile oldValue, Profile newValue) {
            String viewid = "<viewid>";

            if (newValue != null) {
               viewid = newValue.getId();
            }

            lblViewid.setText(viewid);
         }
      });

      // selected value showed in combo box
      cmbAccount.setConverter(new StringConverter<Account>() {

         @Override
         public String toString(Account object) {
            return object.getName();
         }

         @Override
         public Account fromString(String string) {
            // TODO Auto-generated method stub
            return null;
         }
      });
      // selected value showed in combo box
      cmbProperty.setConverter(new StringConverter<Webproperty>() {

         @Override
         public String toString(Webproperty object) {
            return object.getName();
         }

         @Override
         public Webproperty fromString(String string) {
            // TODO Auto-generated method stub
            return null;
         }
      });
      // selected value showed in combo box
      cmbView.setConverter(new StringConverter<Profile>() {

         @Override
         public String toString(Profile object) {
            return object.getName();
         }

         @Override
         public Profile fromString(String string) {
            // TODO Auto-generated method stub
            return null;
         }
      });

   }
   
   @FXML private void cancel() {
      setSave(false);
      stage.close();
   }

   @FXML private void save() {
      setSave(true);
      stage.close();
   }

   public boolean isSave() {
      return save;
   }

   public void setSave(boolean save) {
      this.save = save;
   }
   
   public String getOffername() {
      return txtOffername.getText();
   }
   public String getViewid() {
      return lblViewid.getText();
   }
   public String getPath() {
      return txtPath.getText();
   }
   
   
}

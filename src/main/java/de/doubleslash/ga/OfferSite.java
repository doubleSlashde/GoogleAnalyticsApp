package de.doubleslash.ga;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class OfferSite {
   
   String offerName;
   String viewId;
   String path;
   
   static String offerFile = System.getProperty("user.home") + "/.gasites";   
   
   // viewIds können über den Query-Explorer ermittelt werden: https://ga-dev-tools.appspot.com/query-explorer/
   /*
   static List<OfferSite> sites = new ArrayList<>(  Arrays.asList( 
                                     new OfferSite("doubleSlash Allgemein", "3477069" , ""), 
                                     new OfferSite("doubleSlash Leistungen", "3477069" , "/leistungen/"),
                                     new OfferSite("Anforderungsanalyse", "3477069" , "/leistungen/softwareentwicklung-a-z/anforderungsanalyse/"), 
                                     new OfferSite("Softwaredesign", "3477069" , "/leistungen/softwareentwicklung-a-z/software-design/"), 
                                     new OfferSite("Programmierung", "3477069" , "/leistungen/softwareentwicklung-a-z/programmierung/"), 
                                     new OfferSite(".NET Programmierung", "3477069" , "/leistungen/softwareentwicklung-a-z/programmierung/dot-net-softwareentwickler/"), 
                                     new OfferSite("Java Programmierung", "3477069" , "/leistungen/softwareentwicklung-a-z/programmierung/java-programmierung/"), 
                                     new OfferSite("Software Modernisierung", "3477069" , "/leistungen/business-it-module/softwaremodernisierung-und-datenmanagement/"), 
                                     new OfferSite("Betrieb & Wartung", "3477069" , "/leistungen/softwareentwicklung-a-z/betrieb-und-wartung/"), 
                                     new OfferSite("Qualitätssicherung", "3477069" , "/leistungen/softwareentwicklung-a-z/qualitaetssicherung/"), 
                                     new OfferSite("Projektmanagement", "3477069" , "/leistungen/softwareentwicklung-a-z/projektmanagement/"), 
                                     new OfferSite("BigData Workshop", "3477069" , "/leistungen/workshops-und-trainings/workshop-big-data/"), 
                                     new OfferSite("HTML5 Workshop", "3477069" , "/leistungen/workshops-und-trainings/workshop-html5/"), 
                                     new OfferSite("ScrumCooking Workshop", "3477069" , "/leistungen/workshops-und-trainings/workshop-scrum-cooking/"), 
                                     new OfferSite("Game of Things Workshop", "3477069" , "/leistungen/workshops-und-trainings/workshop-game-of-things/"), 
                                     new OfferSite("Konfigurator", "3477069" , "/leistungen/business-it-module/online-konfigurator/"), 
                                     new OfferSite("IoT", "3477069" , "/leistungen/business-it-module/iot-services-und-connected-products/"), 
                                     new OfferSite("IoT Check", "3477069" , "/leistungen/business-it-module/iot-services-und-connected-products/iot-check/"), 
                                     new OfferSite("E-Mobility & ConnectedCar", "3477069" , "/leistungen/business-it-module/iot-services-und-connected-products/e-mobility-und-connected-cars/"), 
                                     new OfferSite("Marketing Planner", "41633730" , ""), 
                                     new OfferSite("Marketing Technology", "3477069" , "/leistungen/business-it-module/marketing-technology/"), 
                                     new OfferSite("www.calvadrive.de", "105042460" , ""), 
                                     new OfferSite("my.calvadrive.de", "109882856" , "") 
                             
                                  ));
   */
   
   
   public OfferSite(String offerName, String viewId, String path) {
      super();
      this.offerName = offerName;
      this.viewId = viewId;
      this.path = path;
   }
   
   @Override
   public String toString() {
      return offerName + "," + viewId + "," + path;
   }

   /**
    * 
    */
   public static void saveFile(List<OfferSite> sites) {
      try {
         File f = new File(offerFile);
         FileOutputStream fos = new FileOutputStream(f);
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

         for (OfferSite offerSite : sites) {
            bw.write(offerSite.toString());
            bw.newLine();
         }
         
         bw.close();
      }
      catch (IOException e) {
         MainApp.showException("Could not write file '" + offerFile + "'. Program will not be terminated.", e);
      }
   }
   

   /**
    * 
    */
   public static List<OfferSite> loadOfferSite() {
      File file = new File(offerFile);
      String line;
      List<String> lines = new ArrayList<>();

      try {
         BufferedReader br = new BufferedReader( new FileReader( file ) );
         while ( ( line = br.readLine() ) != null ) lines.add(line);
         br.close();
      } catch (IOException e) {
         MainApp.showInfo("Sicherungsdatei '"+offerFile+"' kann nicht gelesen werden. Wenn du eine Auswertung anlegst, dann wird automatisch eine Sicherungsdatei erzeugt.");
      }
      
      List<OfferSite> sites = new ArrayList<>(); 
      for (String s : lines) {
         try {
            
            String[] parts = s.split(",");
            
            OfferSite site = new OfferSite( parts[0] ,parts[1] ,parts[2] );
            sites.add( site );
            
         } catch (Exception e) {
            MainApp.showException("Corrupt line in dictionary ->"+s, e);
         } 
      }
      
      return sites;
      
   }

}

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

   public OfferSite(final String offerName, final String viewId, final String path) {
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
   public static void saveFile(final List<OfferSite> sites) {
      try {
         final File f = new File(offerFile);
         final FileOutputStream fos = new FileOutputStream(f);
         final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

         for (final OfferSite offerSite : sites) {
            bw.write(offerSite.toString());
            bw.newLine();
         }

         bw.close();
      } catch (final IOException e) {
         MainApp.showException("Could not write file '" + offerFile + "'. Program will not be terminated.", e);
      }
   }

   /**
    * 
    */
   public static List<OfferSite> loadOfferSite() {
      final File file = new File(offerFile);
      String line;
      final List<String> lines = new ArrayList<>();

      try {
         final BufferedReader br = new BufferedReader(new FileReader(file));
         while ((line = br.readLine()) != null) {
            lines.add(line);
         }
         br.close();
      } catch (final IOException e) {
         MainApp.showInfo("Sicherungsdatei '" + offerFile + "' kann nicht gelesen werden. "
               + "Wenn du eine Auswertung anlegst, dann wird automatisch eine Sicherungsdatei erzeugt.");
      }

      final List<OfferSite> sites = new ArrayList<>();
      for (final String s : lines) {
         try {

            final String[] parts = s.split(",");

            final OfferSite site = new OfferSite(parts[0], parts[1], parts[2]);
            sites.add(site);

         } catch (final Exception e) {
            MainApp.showException("Corrupt line in dictionary ->" + s, e);
         }
      }

      return sites;

   }

}

package de.doubleslash.ga;

public class TableEntry {
   
   String month;
   Integer users;
   String avgSessionDuration;

   public TableEntry(String month, Integer users, String avgSessionDuration) {
      super();
      this.month = month;
      this.users = users;
      this.avgSessionDuration = avgSessionDuration;
   }

   public String getMonth() {
      return month;
   }
   public Integer getUsers() {
      return users;
   }
   public String getAvgSessionDuration() {
      return avgSessionDuration;
   }

}

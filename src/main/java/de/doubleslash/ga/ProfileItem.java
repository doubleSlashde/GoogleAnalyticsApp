package de.doubleslash.ga;

public class ProfileItem {

   String id;
   String name;

   public ProfileItem(String name, String id) {
      super();
      this.name = name;
      this.id = id;
   }

   public String getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return name;
   }
}

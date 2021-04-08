
      //  NAME:         Peter McKendry
      //  STUDENT ID:   S1915350


package org.me.gcu.equakestartercode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Earthquake {
    private String title;
    private String description;
    private String location;
    private String link;
    private String pubDate;
    private String category;
    private String magColour;
    private float magnitude;
    private float latitude;
    private float longitude;
    private int depth;
    private String depthColour;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        if(description.contains(";") && description.contains(":")){
            parseDescription(description);
        }
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }

    public String getMagColour() {
        return magColour;
    }

    public void setMagColour(String magColour) {
        this.magColour = magColour;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getDepthColour() {
        return depthColour;
    }

    public void setDepthColour(String depthColour) {
        this.depthColour = depthColour;
    }

    //parses information from the description

    private void parseDescription(String description){

        /**
         * Creates an array of strings which represent the pairs of data delimited in the Title by ";"
         *
         */
        String[] pairs = description.split(";");

        //Create a hashmap to add key:value pairs of variables to once parsed
        Map<String, String> map = new HashMap<String, String>();

        //For each string in the array of pairs split further into key:value pairs and store in map
        for(String s : pairs) {
            if (!s.contains("Origin")) {
                String[] vals = s.split(":");
                for (int i = 0; i < vals.length; i += 2) {
                    map.put(vals[i], vals[i + 1]);
                }
            }
        }


        for (String s : map.keySet()) {
            if(s.contains("Magnitude")){
                this.magnitude = Float.parseFloat(map.get(s));
                //System.out.println(Float.parseFloat(map.get(s)));
                if(magnitude < 0.9){
                    this.magColour = "green";
                }
                if(magnitude > 0.8 && magnitude < 1.5){
                    this.magColour = "orange";
                }
                if(magnitude > 1.4){
                    this.magColour = "red";
                }
            }
            if(s.contains("Location")){
                this.location = map.get(s);
            }
            if(s.contains("Depth")){

                if(map.get(s) != null){

                    String str = map.get(s) ;
                    //Regex to return just the numbers from the string (removes km)
                    str = str.replaceAll("\\D+","");

                    this.depth = Integer.parseInt(str);

                    //Sets the colour identifier of the depth of this earthquake based on the value
                    if(depth < 5){
                        this.depthColour = "green";
                    }
                    if(depth > 4 && depth < 10){
                        this.depthColour = "orange";
                    }
                    if(depth > 9){
                        this.depthColour = "red";
                    }

                }

            }

        }
    }


    @Override
    public String toString(){
        return this.getTitle() + "\n" +
                this.getDescription() + "\n" +
                this.getLocation() + "\n" +
                this.getLink() + "\n" +
                this.getPubDate() + "\n" +
                this.getCategory() + "\n" +
                this.getLatitude() + "\n" +
                this.getLongitude() + "\n" +
                this.getMagnitude() + "\n" +
                this.getDepth() + "\n" +
                this.getMagColour() + "\n";
    }

 //Ascending magnitude comparator

    static Comparator<Earthquake> magAscComparator = new Comparator<Earthquake>(){

        public int compare(Earthquake e1, Earthquake e2){

            return Float.compare(e1.getMagnitude(),e2.getMagnitude());

        }
    };

//Descending magnitude comparator

    static Comparator<Earthquake> magDescComparator = new Comparator<Earthquake>(){

        public int compare(Earthquake e1, Earthquake e2){

            return Float.compare(e2.getMagnitude(), e1.getMagnitude());

        }
    };

//Comparator for depth ascending

    static Comparator<Earthquake> depthAscComparator = new Comparator<Earthquake>(){

        public int compare(Earthquake e1, Earthquake e2){

            return Float.compare(e1.getDepth(), e2.getDepth());

        }
    };

//Comparator for depth descending

    static Comparator<Earthquake> depthDescComparator = new Comparator<Earthquake>(){

        public int compare(Earthquake e1, Earthquake e2){

            return Float.compare(e2.getDepth(), e1.getDepth());

        }
    };

}

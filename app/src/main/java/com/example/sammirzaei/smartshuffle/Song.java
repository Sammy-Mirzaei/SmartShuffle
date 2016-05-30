package com.example.sammirzaei.smartshuffle;

import java.io.Serializable;

/**
 * Created by sammirzaei on 11/1/2015.
 */
public class Song implements Serializable{

    private static final long serialVersionUID = 8640733814697578824L;

    private String name;
    private String artist;
    private String fullPath;
    private double duration;
    private long songID;
    private long albumID;
    private int playCount = 0;
    private double importance;

    protected static final int COMPLETION = 0;
    protected static final int SKIPPED = 1;
    protected static final int RESTARTED = 2;
    protected static final int USER_REQUESTED = 3;
    protected static final double DEFAULT_IMPORTANCE = 0.999;
    protected static final double MAX_IMPORTANCE = 0.999;


    public Song(String fullPath){
        //default importance is 0.999
        this(fullPath, DEFAULT_IMPORTANCE);
    }

    public Song(String fullPath, double importance){
        this(fullPath, fullPath, importance);
    }

    public Song(String name, String fullPath, double importance){
        this(name, "artist", fullPath, importance);
    }

    public Song(String name, String artist, String fullPath, double importance){
        this.fullPath = fullPath;
        this.artist = artist;
        this.name = name;
        this.importance = importance;


    }

    /////////// setters ///////////
    public void setName(String name){
        this.name = name;
    }

    public void setFullPath(String fullPath){
        this.fullPath = fullPath;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }

    public void setImportance(double importance){
        this.importance = importance;
    }

    public void setDuration(double duration){
        this.duration = duration;
    }

    public void setSongID(long songID){ this.songID = songID; }

    public void setAlbumID(long albumID){this.albumID = albumID;}

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public void incrementPlayCount(){
        this.playCount++;
    }


    /////////// getters ///////////
    public String getName(){
        return name;
    }

    public String getArtist(){
        return artist;
    }

    public String getFullPath(){
        return fullPath;
    }

    public double getImportance(){
        return importance;
    }

    public double getDuration(){
        return duration;
    }

    public long getSongID() { return songID; }

    public long getAlbumId() { return albumID; }

    public int getPlayCount() {
        return playCount;
    }


    public void updateImportance(int reason, double value){

        switch (reason){

            case COMPLETION:

                double diff = MAX_IMPORTANCE - this.importance;
                if(diff>0){
                    this.importance += (diff/2);
                }else{
                    this.importance = MAX_IMPORTANCE;
                }
                break;

            case SKIPPED:

                if(this.importance > 0){

                    double prct = (value/this.duration);

                    if(prct > this.importance){
                        this.importance = prct;
                    }else{
                        double diffSkipped = this.importance - prct;
                        this.importance -= (diffSkipped/2);
                    }
                }
                break;

            case RESTARTED:

                double diffR = MAX_IMPORTANCE - this.importance;
                if(diffR>0){
                    this.importance += (diffR/3);
                }
                break;

            case USER_REQUESTED:

                if(value>=0 && value<=MAX_IMPORTANCE){
                    this.setImportance(value);
                }
                break;
        }
    }



    @Override
    public boolean equals(Object obj){
        if(obj instanceof Song){
            Song song = (Song) obj;
            if((song.getName().trim().contentEquals(this.getName().trim())) && (this.artist.trim().contentEquals(song.getArtist().trim()))){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){

        return new StringBuffer(this.name)
                .append("\n")
                .append("  Artist: ")
                .append(this.artist)
                .append("  Importance: ")
                .append(String.valueOf(this.importance)).toString();

    }

}

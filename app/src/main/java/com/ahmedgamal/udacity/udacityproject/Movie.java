package com.ahmedgamal.udacity.udacityproject;

/**
 * The movie entity that holds movie details
 */
public class Movie {
    private String mTitle;
    private String mImageUrl;

    public Movie(String title, String imageUrl) {
        this.mTitle = title;
        this.mImageUrl = imageUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getTitle() {
        return mTitle;
    }
}

package com.nexa.awesome.utils.auth;

public class TokenCache {
    private static String googleToken;
    private static String facebookToken;
    private static String twitterToken;

    public static void setGoogleToken(String token) { 
        googleToken = token; 
    }
    public static String getGoogleToken() { 
        return googleToken; 
    }

    public static void setFacebookToken(String token) { 
        facebookToken = token; 
    }
    public static String getFacebookToken() { 
        return facebookToken; 
    }

    public static void setTwitterToken(String token) { 
        twitterToken = token; 
    }
    public static String getTwitterToken() { 
        return twitterToken; 
    }
}
package io.github.hugoangeles0810.library;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class DummyLibraryClass {

    public static String getDescription() {
        return DummyLibraryClass.class.getPackage().getName() + " v" + BuildConfig.VERSION_NAME + "\n" +
                "With dependencies: \n" +
                "OkHttp v3.10.0 " + OkHttpClient.class.getPackage().getName() + "\n" +
                "Gson v2.8.5 " + Gson.class.getPackage().getName() + "\n" +
                "Picasso v2.71828 " + Picasso.class.getPackage().getName();
    }
}

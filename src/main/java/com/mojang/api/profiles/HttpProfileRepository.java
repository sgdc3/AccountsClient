package com.mojang.api.profiles;

import com.google.gson.Gson;
import com.mojang.api.http.BasicHttpClient;
import com.mojang.api.http.HttpBody;
import com.mojang.api.http.HttpClient;
import com.mojang.api.http.HttpHeader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HttpProfileRepository implements ProfileRepository {

    // You're not allowed to request more than 100 profiles per go.
    private static final int PROFILES_PER_REQUEST = 100;

    private static Gson gson = new Gson();
    private final String agent;
    private HttpClient client;

    public HttpProfileRepository(String agent) {
        this(agent, BasicHttpClient.getInstance());
    }

    public HttpProfileRepository(String agent, HttpClient client) {
        this.agent = agent;
        this.client = client;
    }

    @Override
    public Profile[] findProfilesByNames(String... names) {
        List<Profile> profiles = new ArrayList<Profile>();
        try {

            List<HttpHeader> headers = new ArrayList<HttpHeader>();
            headers.add(new HttpHeader("Content-Type", "application/json"));

            int namesCount = names.length;
            int start = 0;
            int i = 0;
            do {
                int end = PROFILES_PER_REQUEST * (i + 1);
                if (end > namesCount) {
                    end = namesCount;
                }
                String[] namesBatch = Arrays.copyOfRange(names, start, end);
                HttpBody body = getHttpBody(namesBatch);
                Profile[] result = post(getProfilesUrl(), body, headers);
                profiles.addAll(Arrays.asList(result));

                start = end;
                i++;
            } while (start < namesCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return profiles.toArray(new Profile[profiles.size()]);
    }

    @Override
    public Profile findProfileById(UUID uuid) {
        try {
            List<HttpHeader> headers = new ArrayList<HttpHeader>();
            headers.add(new HttpHeader("Content-Type", "application/json"));

            return get(getSessionUrl(uuid), headers);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private URL getProfilesUrl() throws MalformedURLException {
        // To lookup Minecraft profiles, agent should be "minecraft"
        return new URL("https://api.mojang.com/profiles/" + agent);
    }

    private URL getSessionUrl(UUID uuid) throws MalformedURLException {
        String profileId = uuid.toString().replace("-", "");
        return new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + profileId);
    }

    private Profile[] post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException {
        String response = client.post(url, body, headers);
        return (!response.isEmpty() ? gson.fromJson(response, Profile[].class) : null);
    }

    private Profile get(URL url, List<HttpHeader> headers) throws IOException {
        String response = client.get(url, headers);
        return (!response.isEmpty() ? gson.fromJson(response, Profile.class) : null);
    }

    private static HttpBody getHttpBody(String... namesBatch) {
        return new HttpBody(gson.toJson(namesBatch));
    }

}

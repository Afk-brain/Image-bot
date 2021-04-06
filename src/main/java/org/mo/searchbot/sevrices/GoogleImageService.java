package org.mo.searchbot.sevrices;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GoogleImageService implements ImageService {

    private static final String USER_AGENT = "Mozilla/5.0";

    private HttpClient client = HttpClient.newHttpClient();

    @Override
    public List<String> getImages(String query) {
        String googleQuery = "https://www.google.com/search?q=" + query.replaceAll(" ", "+") + "&tbm=isch&nfpr=1";
        String html = "";
        try {
            html = getHTML(googleQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parseLinks(html);
    }

    private String getHTML(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader("User-Agent", USER_AGENT)
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(url + " request status: " + response.statusCode());
        return response.body();
    }

    private List<String> parseLinks(String HTML) {
        Document doc = Jsoup.parse(HTML);
        List<String> links = new ArrayList<String>();
        List<Element> elements = doc.getElementsByTag("img");
        for(int i = 1;i < elements.size();i++) {
           links.add(elements.get(i).attr("src"));
        }
        return links;
    }

}

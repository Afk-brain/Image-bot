package org.mo.searchbot.sevrices;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GoogleImageService implements ImageService {

    private static final String DEFAULT_IMAGE = "https://i.ibb.co/qFs4Tw8/1.png";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103";
    private static final Pattern pattern = Pattern.compile(
            "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
                    "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
                    "|mil|biz|info|mobi|name|aero|jobs|museum" +
                    "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
                    "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
                    "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                    "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
                    "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                    "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
                    "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");
    private HttpClient client = HttpClient.newHttpClient();

    @Override
    @Nullable
    public List<String> getImages(String query) {
        String googleQuery = "https://www.google.com/search?q=" + query.replaceAll(" ", "+") + "&tbm=isch&nfpr=1";
        List<String> links = new ArrayList<>();
        try {
            String html = getHTML(googleQuery);
            links = parseLinks(html);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(links.isEmpty()) {
            links.add(DEFAULT_IMAGE);
        }
        return links;
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

    private List<String> parseLinks(String html) {
        List<Element> scripts = Jsoup.parse(html).getElementsByTag("script");
        return extractUrls(scripts.get(scripts.size() - 5).toString())//fifth script from the end contains all image links
                .stream().filter((link) -> isImage(link)).collect(Collectors.toList());//filter images
    }

    private boolean isImage(String link) {
        return link.endsWith(".jpg") || link.endsWith(".gif") || link.endsWith(".png") || link.endsWith(".jpeg");
    }

    public static List<String> extractUrls(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

}

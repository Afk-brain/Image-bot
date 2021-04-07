package org.mo.searchbot.sevrices;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GoogleImageService implements ImageService {

    private final String defaultImage;
    private final String userAgent;
    private final Pattern pattern;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Logger log = LoggerFactory.getLogger(GoogleImageService.class);

    public GoogleImageService() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/gis.prefs"));
        defaultImage = properties.getProperty("defaultImage");
        userAgent = properties.getProperty("userAgent");
        pattern = Pattern.compile(
                "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
                        "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
                        "|mil|biz|info|mobi|name|aero|jobs|museum" +
                        "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
                        "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
                        "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
                        "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
                        "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");;
    }

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
            links.add(defaultImage);
        }
        return links;
    }

    private String getHTML(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader("User-Agent", userAgent)
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("{} request status: {}", url, response.statusCode());
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

    public List<String> extractUrls(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

}

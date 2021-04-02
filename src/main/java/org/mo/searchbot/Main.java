package org.mo.searchbot;

import org.mo.searchbot.sevrices.GoogleImageService;
import org.mo.searchbot.sevrices.ImageService;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        ImageService imageService = new GoogleImageService();
        while(true) {
            System.out.print("Enter search query: ");
            for(String link : imageService.getImages(in.nextLine())) {
                System.out.println(link);
            }
        }
    }

}

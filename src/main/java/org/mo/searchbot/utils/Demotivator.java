package org.mo.searchbot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Demotivator {

    private static Logger log = LoggerFactory.getLogger(Demotivator.class);

    private BufferedImage src;
    private int innerBlack;
    private int innerWhite;
    private int blackTop;
    private int blackSide;
    private int textHeight;

    public Demotivator(BufferedImage image) {
        log.info("Defining image parameters");
        src = image;
        innerBlack = 5;
        innerWhite = 5;
        blackTop = src.getHeight() / 10;
        blackSide = src.getWidth() / 10;
        textHeight = src.getHeight() / 8;
    }

    public BufferedImage demotivate(String text) {
        log.info("Drawing!!!");
        BufferedImage newImage = new BufferedImage(src.getWidth() + 2 * (innerBlack + innerWhite + blackSide),
                src.getHeight() + 2 * (innerBlack + innerWhite)  + 3 * blackTop + textHeight, src.getType());
        Graphics graphics = newImage.createGraphics();
        drawInnerBorders(graphics);
        graphics.drawImage(src, blackSide + innerBlack + innerWhite, blackTop + innerBlack + innerWhite, null);
        drawText(graphics, text);
        graphics.dispose();
        return newImage;
    }

    private void drawInnerBorders(Graphics graphics) {
        graphics.setColor(Color.WHITE);
        int borders = 2 * (innerBlack + innerWhite);
        graphics.fillRect(blackSide, blackTop, src.getWidth() + borders, src.getHeight() + borders);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(blackSide + innerWhite, blackTop + innerWhite, src.getWidth() + 2 * innerBlack, src.getHeight() + 2 * innerBlack);
    }

    private void drawText(Graphics graphics, String text) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("TimesRoman", Font.PLAIN, textHeight));
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int borders = 2 * (innerBlack + innerWhite + blackTop);
        graphics.drawString(text, (src.getWidth() - textWidth) / 2 + blackSide, src.getHeight() + borders + blackTop);
    }


}

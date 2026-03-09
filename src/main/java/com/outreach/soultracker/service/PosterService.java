package com.outreach.soultracker.service;

import com.outreach.soultracker.entity.AppUser;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PosterService {

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;

    public byte[] generateAchievementPoster(AppUser user, String rankName, int milestone) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Enable Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Background
        drawBackground(g2d, milestone);

        // 2. Theme Specific Elements
        drawThemeElements(g2d, milestone, rankName);

        // 3. User Avatar
        drawUserAvatar(g2d, user);

        // 4. Branding & Text
        drawBrandingAndText(g2d, user, rankName);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private void drawBackground(Graphics2D g2d, int milestone) {
        // Base dark background
        g2d.setColor(new Color(15, 15, 20));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Subtle gradient in the center
        Color centerColor;
        if (milestone >= 100)
            centerColor = new Color(75, 20, 150, 100); // Royal Purple Aura
        else if (milestone >= 50)
            centerColor = new Color(50, 120, 180, 100); // Platinum Blue
        else
            centerColor = new Color(50, 50, 60, 80);

        RadialGradientPaint rgp = new RadialGradientPaint(
                new Point(WIDTH / 2, HEIGHT / 2),
                HEIGHT / 2,
                new float[] { 0.0f, 1.0f },
                new Color[] { centerColor, new Color(15, 15, 20, 0) });
        g2d.setPaint(rgp);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawThemeElements(Graphics2D g2d, int milestone, String rankName) {
        if (milestone <= 20) {
            // Medal Theme: Thick metallic border
            g2d.setStroke(new BasicStroke(40));
            if (milestone >= 20)
                g2d.setColor(new Color(205, 127, 50)); // Bronze
            else if (milestone >= 10)
                g2d.setColor(new Color(212, 175, 55)); // Gold
            else
                g2d.setColor(new Color(192, 192, 192)); // Silver

            g2d.drawRect(20, 20, WIDTH - 40, HEIGHT - 40);
        } else if (milestone <= 40) {
            // Tactical Theme: Industrial sharp lines
            g2d.setColor(new Color(100, 100, 110, 100));
            g2d.setStroke(new BasicStroke(1));
            for (int i = 0; i < WIDTH; i += 80)
                g2d.drawLine(i, 0, i, HEIGHT);
            for (int i = 0; i < HEIGHT; i += 80)
                g2d.drawLine(0, i, WIDTH, i);

            g2d.setStroke(new BasicStroke(20));
            g2d.drawRect(60, 60, WIDTH - 120, HEIGHT - 120);
        } else if (milestone == 50) {
            // Crystal Theme: Light refraction
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i < 360; i += 5) {
                double rad = Math.toRadians(i);
                int x1 = (int) (WIDTH / 2 + Math.cos(rad) * 200);
                int y1 = (int) (HEIGHT / 2 - 100 + Math.sin(rad) * 200);
                int x2 = (int) (WIDTH / 2 + Math.cos(rad) * 900);
                int y2 = (int) (HEIGHT / 2 - 100 + Math.sin(rad) * 900);
                g2d.setColor(new Color(180, 240, 255, 40));
                g2d.drawLine(x1, y1, x2, y2);
            }
        } else if (milestone >= 100) {
            // Majestic Theme: Royal Purple Aura already in background
            // Add a thick gold inner glow or border
            g2d.setStroke(new BasicStroke(60));
            g2d.setColor(new Color(120, 40, 200, 150));
            g2d.drawRect(30, 30, WIDTH - 60, HEIGHT - 60);

            // Draw Crown above avatar (placeholder if no SVG)
            drawCrown(g2d, WIDTH / 2, HEIGHT / 2 - 350);
        } else if (rankName.equals("Master Harvester")) {
            // Harvester Theme: Botanical monochrome silhouette
            drawBotanicalBackground(g2d);
            g2d.setStroke(new BasicStroke(40));
            g2d.setColor(new Color(48, 209, 88, 100));
            g2d.drawRect(20, 20, WIDTH - 40, HEIGHT - 40);
        }
    }

    private void drawBotanicalBackground(Graphics2D g2d) {
        g2d.setColor(new Color(30, 50, 30, 60));
        for (int i = 0; i < 10; i++) {
            int rx = (int) (Math.random() * WIDTH);
            int ry = (int) (Math.random() * HEIGHT);
            g2d.fillOval(rx, ry, 300, 500); // Simple silhouettes
        }
    }

    private void drawCrown(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(255, 223, 0)); // Bright Gold
        int[] px = { x - 100, x - 100, x - 50, x, x + 50, x + 100, x + 100 };
        int[] py = { y, y - 80, y - 40, y - 100, y - 40, y - 80, y };
        g2d.fillPolygon(px, py, 7);
        g2d.fillOval(x - 15, y - 125, 30, 30); // Center gem
    }

    private void drawUserAvatar(Graphics2D g2d, AppUser user) {
        int avatarSize = 450;
        int x = (WIDTH - avatarSize) / 2;
        int y = (HEIGHT - avatarSize) / 2 - 100;

        // circular border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(15));
        g2d.drawOval(x - 10, y - 10, avatarSize + 20, avatarSize + 20);

        try {
            BufferedImage avatar = null;
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                File file = new File("." + user.getAvatarUrl());
                if (file.exists()) {
                    avatar = ImageIO.read(file);
                }
            }

            if (avatar == null) {
                g2d.setColor(new Color(40, 40, 60));
                g2d.fillOval(x, y, avatarSize, avatarSize);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 180));
                String initial = user.getFullName().substring(0, 1).toUpperCase();
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(initial, (WIDTH - fm.stringWidth(initial)) / 2, y + 300);
            } else {
                Shape oldClip = g2d.getClip();
                g2d.setClip(new Ellipse2D.Double(x, y, avatarSize, avatarSize));
                g2d.drawImage(avatar, x, y, avatarSize, avatarSize, null);
                g2d.setClip(oldClip);
            }
        } catch (Exception e) {
            g2d.setColor(new Color(40, 40, 60));
            g2d.fillOval(x, y, avatarSize, avatarSize);
        }
    }

    private void drawBrandingAndText(Graphics2D g2d, AppUser user, String rankName) {
        g2d.setColor(Color.WHITE);

        // Logo
        g2d.setFont(new Font("SansSerif", Font.BOLD, 45));
        g2d.drawString("BILINGUAL SOUL TRACKER", 80, 120);

        // Rank Title
        g2d.setFont(new Font("SansSerif", Font.BOLD, 95));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(rankName.toUpperCase(), (WIDTH - fm.stringWidth(rankName.toUpperCase())) / 2, HEIGHT / 2 + 350);

        // Name
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 65));
        fm = g2d.getFontMetrics();
        g2d.drawString(user.getFullName(), (WIDTH - fm.stringWidth(user.getFullName())) / 2, HEIGHT / 2 + 480);

        // Footer
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 32));
        g2d.setColor(new Color(160, 160, 170));
        String footer = "POWERED BY CCC JB BILINGUAL";
        fm = g2d.getFontMetrics();
        g2d.drawString(footer, (WIDTH - fm.stringWidth(footer)) / 2, HEIGHT - 80);
    }
}

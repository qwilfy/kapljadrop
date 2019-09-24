package com.Tsaika;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Entry {
    public int x, y;
    public int textOffsetX, textOffsetY;
    public int width, height;

    public String text;

    public String font;
    public int fontSize;
    public int fontStyle;

    public boolean IsActive;

    public Entry()
    {
        this.font = "TimesRoman";
        this.fontSize = 12;
        this.fontStyle = Font.PLAIN | Font.ITALIC;

        this.width = 100;
        this.height = 25;
        this.text = "";

        this.textOffsetX = 10;
        this.textOffsetY = this.height /2;

        this.IsActive = false;
    }

    public void update(Graphics g)
    {
        if (!IsActive)
            return;

        g.setColor(new Color(255, 0, 0));
        g.drawRect(x, y, width, height);
        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font(font, fontStyle, fontSize));
        g.drawString(text, x + textOffsetX, y + textOffsetY);
    }

    public void keyPress(KeyEvent e)
    {
        if (!IsActive)
            return;
        //text += e.getKeyChar();
        try
        {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                text = text.substring(0, text.length() - 1);
            else
                text += e.getKeyChar();
        }
        catch (Exception ex)
        {

        }
    }

}

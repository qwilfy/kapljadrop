package com.Tsaika;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;

public class GameWindow extends JFrame {


    private static  GameWindow game_window;
    private static long last_frame_time;
    private static Image background;
    private static Image gameOver;
    private static Image drop;
    private static Image drop2;
    private static Image restart;
    private static float drop_left= 300;
    private static float drop_top = -100;
    private static float drop_v=10;
    private static float drop_left2= 100;
    private static float drop_top2 = -100;
    private static float drop_v2 =10;
    private static int score=0;
    private static boolean end;
    private static float drop_width=100;
    private static float drop_height=150;
    private static float drop_width1=100;
    private static float drop_height1=150;
    private static boolean pause = false;
    private static float drop_speed_saved;
    private static float drop_speed_saved2;

    private static double mousecordX = 0;
    private static double mousecordY = 0;

    public static int direction = -1;


    private static Entry nameEntry;
    private static Datebase db;

    private static boolean isRecorded = false;
    public static boolean drawRecords = false;
    private static ArrayList<String> recordsLast = new ArrayList<String>();


    public static void main(String[] args) throws IOException {

        db = new Datebase("jdbc:mysql://localhost/kaplja?useLegacyDatetimeCode=false&serverTimezone=Europe/Helsinki", "root", "");
        db.init();

        background = ImageIO.read(GameWindow.class.getResourceAsStream("background.jpg"));
        gameOver = ImageIO.read(GameWindow.class.getResourceAsStream("gameOver.png"));
        drop = ImageIO.read(GameWindow.class.getResourceAsStream("drop.png")).getScaledInstance((int) drop_width, (int) drop_height, Image.SCALE_DEFAULT);
        drop2 = ImageIO.read(GameWindow.class.getResourceAsStream("drop2.png")).getScaledInstance((int) drop_width1, (int) drop_height1, Image.SCALE_DEFAULT);
        restart = ImageIO.read(GameWindow.class.getResourceAsStream("restart.png")).getScaledInstance(100,100, Image.SCALE_DEFAULT);

        game_window = new GameWindow();//Создание нового окна
        game_window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//Закрытие программы
        game_window.setLocation(200, 100); //расположение мыши
        game_window.setSize(906, 478); //размер окна
        last_frame_time = System.nanoTime();
        GameField game_field = new GameField();
        onDirection();
        game_field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    if (pause) {
                        pause = false;
                        drop_v = drop_speed_saved;
                        drop_v2 = drop_speed_saved2;
                        try {
                            Robot r = new Robot();
                            r.mouseMove((int) mousecordX, (int) mousecordY);
                        } catch (AWTException ee) {

                        }
                    }
                    else {
                        drop_speed_saved= drop_v;
                        drop_speed_saved2= drop_v2;
                        drop_v=0;
                        drop_v2=0;

                        mousecordX = MouseInfo.getPointerInfo().getLocation().getX();
                        mousecordY = MouseInfo.getPointerInfo().getLocation().getY();

                        pause= true;
                    }
                }
                if(e.getButton() == MouseEvent.BUTTON1){

                    int x = e.getX();
                    int y = e.getY();


                    float drop_right = drop_left + drop.getWidth(null);
                    float drop_bottom = drop_left + drop.getHeight(null);
                    boolean is_drop = x >= drop_left && x <= drop_right && y >= drop_top && y <= drop_bottom;
                    float drop_right2 = drop_left2 + drop.getWidth(null);
                    float drop_bottom2 = drop_left2 + drop.getHeight(null);
                    boolean is_drop2 = x >= drop_left2 && x <= drop_right && y >= drop_top2 && y <= drop_bottom;

                    if(is_drop){
                        if(drop_height > 25 && drop_width > 50){
                            drop_width = drop_width -1;
                            drop_height = drop_height -2;
                            try{
                                dropResize();
                            }
                            catch(IOException ioe){

                            }

                        }


                        drop_top=-100;
                        drop_left= (int) (Math.random() * (game_field.getWidth()-drop.getWidth(null)));
                        drop_v = drop_v+40;
                        //drop_top2=-100;
                        //drop_left2= (int) (Math.random() * (game_field.getWidth()-drop2.getWidth(null)));
                        score++;
                        onDirection();
                        game_window.setTitle("Score: "+ score);

                    }

                    if(is_drop2){
                        if(drop_height > 25 && drop_width > 50){
                            drop_width1 = drop_width1 -1;
                            drop_height1 = drop_height1 -2;
                            try{
                                dropResize();
                            }
                            catch(IOException ioe){

                            }

                        }

                        drop_top2 = -100;
                        drop_v2 = drop_v2+40;
                        drop_left2= (int) (Math.random() * (game_field.getWidth()-drop2.getWidth(null)));
                        onDirection();
                    }

                    if(end){
                        boolean isRestart = x>=175 && x<=175 + restart.getWidth(null) && y >=300 && y<=300 + restart.getHeight(null);


                        if(isRestart){
                            end=false;
                            score=0;
                            game_window.setTitle("Score: "+ score);
                            drop_top=-100;
                            drop_left= (int) (Math.random() * (game_field.getWidth()-drop.getWidth(null)));
                            drop_v = 200;
                            drop_top2=-100;
                            drop_left2= (int) (Math.random() * (game_field.getWidth()-drop2.getWidth(null)));
                            drop_v2 = 200;
                            drop_width=100;
                            drop_height=150;
                            isRecorded = false;
                            drawRecords = false;
                        }
                    }
                }

            }
        });
        nameEntry = new Entry();
        game_window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                nameEntry.keyPress(e);
                if (nameEntry.IsActive && !isRecorded){
                    if(e.getKeyCode()==KeyEvent.VK_ENTER){
                        db.addRecord(nameEntry.text, score);
                        recordsLast = db.getRecords();
                        isRecorded = true;
                        drawRecords = true;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        game_window.add(game_field);
        game_window.setResizable(false);
        game_window.setVisible(true);



    }

    private static void dropResize() throws IOException
    {
        drop = ImageIO.read(GameWindow.class.getResourceAsStream("drop.png")).getScaledInstance((int) drop_width, (int) drop_height, Image.SCALE_DEFAULT);
        drop2 = ImageIO.read(GameWindow.class.getResourceAsStream("drop.png")).getScaledInstance((int) drop_width, (int) drop_height, Image.SCALE_DEFAULT);
    }

    private static int onDirection(){
        int rand = (int)(Math.random()*2+1);
        if(rand == 2) direction = 1;
        else direction = -1;
        System.out.println(direction);

        return direction;
    }

    private static void onRepaint(Graphics g){
        long current_time = System.nanoTime();
        float delta_time = (current_time - last_frame_time)*0.000000001f;
        last_frame_time = current_time;
        drop_top = drop_top +drop_v * delta_time;
        drop_top2 = drop_top2 +drop_v2 * delta_time;
        g.drawImage(background, 0, 0, null);
        g.drawImage(drop, (int)drop_left, (int)drop_top,null);

        drop_left = drop_left+(direction*drop_v)*delta_time;
        drop_left2 = drop_left2+(direction*drop_v2)*delta_time;

        if(score >= 5){
            g.drawImage(drop2, (int)drop_left2,(int)drop_top2, null);
        }

        if (drop_top > game_window.getHeight()){

            g.drawImage(gameOver, 280, 120, null);
            g.drawImage(restart, 175, 300, null);
            end=true;
        }

        if(drop_left <= 0.0 ||drop_left +drop_width > game_window.getWidth()){
            if(direction == -1) direction = 1;
            else direction = -1;
        }

        if(drop_left2 <= 0.0 ||drop_left2 +drop_width > game_window.getWidth()){
            if(direction == -1) direction = 1;
            else direction = -1;
        }

        if (drawRecords)
        {
            g.setColor(new Color(255,255,255));
            for (int i = 0; i < recordsLast.size(); i++)
            {
                g.drawString(recordsLast.get(i), 200, 25 + 25 * i);
                g.setColor(new Color(255,255,255));
            }
        }

        nameEntry.IsActive = end;
        nameEntry.update(g);
    }



    private static class GameField extends JPanel{
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            onRepaint(g);
            repaint();
        }
    }
}

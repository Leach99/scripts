package bot;

import org.powerbot.script.PaintListener;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.rt4.Constants;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

@Script.Manifest(name = "safespotter", description = "kills things that cant kill you", properties = "client=4; topic=1;")

public class safespotter extends PollingScript<ClientContext> implements PaintListener {

    String attackThis = "Lesser demon";
    String choice;
    int x, startXp, startLvl;
    boolean start = true;
    Random rand = new Random();
    boolean ab = false;
    int myStyle;

    @Override
    public void poll() {
        if(start) {
            System.out.print("Start");
            String[] options = {"Magic", "Range"};
            choice = "" + (String) JOptionPane.showInputDialog(null, "Using Magic or Ranged?", "Safespotter", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice.equals("Magic")) {
                System.out.print("USING MAGIC");
                myStyle = Constants.SKILLS_MAGIC;
                startLvl = ctx.skills.realLevel(myStyle);
                startXp = ctx.skills.experience(myStyle);
                start = false;
            } else if (choice.equals("Range")) {
                System.out.print("USING RANGED");
                myStyle = Constants.SKILLS_RANGE;
                startLvl = ctx.skills.realLevel(myStyle);
                startXp = ctx.skills.experience(myStyle);
                start = false;
            } else {
                ctx.controller.stop();
            }
        }
        x++;
        if (x > 150){
            x = 0;
            ab = true;
        }
        switch (state()) {

            case ATTACK:
                System.out.print("ATTACK\n");
                attack();
                break;

            case WAIT:
                System.out.print("WAIT\n");
                idle();
                break;

            case ANTIBAN:
                System.out.print("ANTIBAN\n");
                antiban();
                break;
        }
    }

    private void idle() {
        try {Thread.sleep(2200);}
        catch (InterruptedException ex) {Thread.currentThread().interrupt();}
    }

    private void attack(){
        try {Thread.sleep(rand.nextInt(1000));}
        catch (InterruptedException ex) {Thread.currentThread().interrupt();}
        ctx.npcs.select().name(attackThis).nearest().poll().interact("Attack");

        try {Thread.sleep(6000);}
        catch (InterruptedException ex) {Thread.currentThread().interrupt();}
    }

    private void antiban(){
        int z = rand.nextInt(20)+1;//1-20
        if (z == 2){//move cam right
            int a = 0;
            while (a < rand.nextInt(5)+5){
                a++;
                ctx.input.send("{VK_RIGHT}");
            }
        }
        else if (z == 3){//move cam left
            int b = 0;
            while (b < rand.nextInt(5)+5){
                b++;
                ctx.input.send("{VK_LEFT}");
            }
        }
        else if (z == 4){//move cam down
            int c = 0;
            while (c < rand.nextInt(3)+2){
                c++;
                ctx.input.send("{VK_DOWN}");
            }
        }
        else if (z == 5){//move cam up
            int d = 0;
            while (d < rand.nextInt(3)+2){
                d++;
                ctx.input.send("{VK_UP}");
            }
        }
        ab = false;
    }

    private State state() {
        if (ab){
            return State.ANTIBAN;
        }
        if (ctx.npcs.select().name(attackThis).nearest().poll().inViewport() && (!ctx.npcs.select().name(attackThis).nearest().poll().healthBarVisible() || ctx.chat.chatting())){
            return State.ATTACK;
        }
        return State.WAIT;
    }

    private enum State{
        ATTACK, WAIT, ANTIBAN
    }

    @Override
    public void repaint(Graphics graphics) {
        long milli = this.getTotalRuntime();
        long secs = (milli/1000) % 60;
        long mins = (milli/(1000*60) % 60);
        long hours = (milli/ (1000*60*60)) % 24;
        int xpGain = ctx.skills.experience(myStyle) - startXp;
        int xpHour = (int)(xpGain * (3600000D / milli));
        int xpLeft = ctx.skills.experienceAt(ctx.skills.realLevel(myStyle)+1)-ctx.skills.experience(myStyle);
        int ttl = (int)(((xpLeft * 3600000D) / xpHour)/1000/60)+1;//time to level
        Graphics2D draw = (Graphics2D)graphics;

        draw.setColor(new Color(0, 0, 255, 150));
        draw.fillRect(280, 345, 220, 125);

        draw.setColor(new Color(255, 255, 255));
        draw.drawRect(280, 345, 220, 125);

        draw.drawString("Safespotter:", 290, 360);
        draw.drawString("Runtime: "+String.format("%02d:%02d:%02d", hours, mins, secs), 290, 375);
        draw.drawString("Xp/hour: " + xpHour, 290, 390);
        draw.drawString("XP gained: "+xpGain, 290, 405);
        draw.drawString("Level: " + ctx.skills.realLevel(myStyle)+" (+"+(ctx.skills.realLevel(myStyle) - startLvl)+")"+"  TTL: "+ttl+"min ("+xpLeft/1000+"k)", 290, 420);
        draw.drawString("Version 0.1", 290, 465);
        draw.drawString("Combat: " + choice, 365, 465);
    }

}


package bot;

import org.powerbot.script.PaintListener;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.rt4.Game;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

@Script.Manifest(name = "powerminer", description = "mines and drops", properties = "client=4; topic=99;")

public class powerminer extends PollingScript<ClientContext> implements PaintListener{

    String choice;
    int iron1 = 7455;
    int iron2 = 7488;
    int pickID;
    boolean pickaxe = false;
    int ab, myXp, notMine;
    boolean oreAvailable;
    boolean dropping = false;
    boolean start = true;
    Random rand = new Random();
    Tile myTile;
    Tile tileLeft, tileRight, tileUp, tileDown;
    int tileLeftID, tileRightID, tileUpID, tileDownID;
    int y, startXp, startLvl, oreMined;
    int x = 0;
    boolean powermine;
    boolean banking = false;
    String currentState;


    @Override
    public void poll() {
        ab++;
        if (ctx.chat.chatting()){//if level up chat comes up, dismiss it
            ctx.input.send(" ");
        }

        if (!ctx.objects.select(1).id(iron1).isEmpty() || !ctx.objects.select(1).id(iron2).isEmpty()){//checks if ores next to player
            oreAvailable = true;//ore available
        }
        else {
            oreAvailable = false;//no ore available
        }

        if(ab > 80 && !dropping){//every 10* the script goes to poll()
            ab = 0;
            y = rand.nextInt(10);//generates random num 1-10, if its one has chance to do an anti-ban action
        }

        switch (state()) {

            case MINE:
                System.out.print("MINE\n");
                mine();
                break;

            case DROP:
                System.out.print("DROP\n");
                drop();
                break;

            case IDLE:
                System.out.print("IDLE\n");
                idle();
                break;

            case MOVED:
                System.out.print("CHECK\n");
                moved();
                break;

            case ANTIBAN:
                System.out.print("ANTIBAN\n");
                antiban();
                break;

            case BANK:
                System.out.print("BANK\n");
                bank();
                break;
        }
    }


    private void antiban() {
        int z = rand.nextInt(10)+1;//1-10
        if (z == 1){//checks mining xp
            ctx.widgets.component(548, 49).interact("Skills");
            try {Thread.sleep(100);}
            catch (InterruptedException ex) {Thread.currentThread().interrupt();}
            ctx.input.move(ctx.widgets.component(320, 17).component(2).screenPoint());
            try {Thread.sleep(rand.nextInt(1000)+300);}
            catch (InterruptedException ex) {Thread.currentThread().interrupt();}
            ctx.input.click(ctx.widgets.component(548, 51).screenPoint(), true);
        }
        else if (z == 2){//move cam right
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
    }

    private void moved() {//moves back to start tile, and gets location of all tiles next to player at start
        if(start){
            currentState = "START";
            System.out.print("Start");
            startLvl = ctx.skills.realLevel(14);
            startXp = ctx.skills.experience(14);
            String[] options = {"Bank", "Powermine"};
            choice = ""+(String) JOptionPane.showInputDialog(null, "Bank or Powermine?", "Miner", JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
            if (choice.equals("Bank")){
                System.out.print("WILL BANK");
                powermine = false;
            }
            else if (choice.equals("Powermine")){
                System.out.print("WILL POWERMINE");
                powermine = true;
            }
            else {
                ctx.controller.stop();
            }
            if (ctx.inventory.itemAt(0).name().contains("pickaxe")){
                pickID = ctx.inventory.itemAt(0).id();
                pickaxe = true;
            }
            myTile = ctx.players.local().tile();
            tileLeft = new Tile(myTile.x() - 1, myTile.y());
            tileRight = new Tile(myTile.x() + 1, myTile.y());
            tileUp = new Tile(myTile.x(), myTile.y() + 1);
            tileDown = new Tile(myTile.x(), myTile.y() - 1);
            if (!ctx.objects.select().id(iron1).at(tileLeft).isEmpty()){//if iron1 at left tile set the ID
                tileLeftID = iron1;
            }
            else if (!ctx.objects.select().id(iron2).at(tileLeft).isEmpty()) {//if iron2 at left set ID
                tileLeftID = iron2;
            }
            else{ tileLeftID = 1; }//if nothing at left set ID as 1

            if (!ctx.objects.select().id(iron1).at(tileRight).isEmpty()){
                tileRightID = iron1;
            }
            else if (!ctx.objects.select().id(iron2).at(tileRight).isEmpty()){
                tileRightID = iron2;
            }
            else{ tileRightID = 1; }

            if (!ctx.objects.select().id(iron1).at(tileUp).isEmpty()){
                tileUpID = iron1;
            }
            else if (!ctx.objects.select().id(iron2).at(tileUp).isEmpty()) {
                tileUpID = iron2;
            }
            else{ tileUpID = 1; }

            if (!ctx.objects.select().id(iron1).at(tileDown).isEmpty()){
                tileDownID = iron1;
            }
            else if (!ctx.objects.select().id(iron2).at(tileDown).isEmpty()){
                tileDownID = iron2;
            }
            else{ tileDownID = 1; }
            start = false;
        }
        else if (!ctx.players.local().inMotion()) {//if not moving go back to start tile
            currentState = "RETURNING";
            System.out.print("MOVED");
            ctx.movement.step(myTile);// add this back in when fix it
        }
    }

    private void drop() {
        if(!ctx.game.tab(Game.Tab.INVENTORY)){
            ctx.input.click(ctx.widgets.component(548, 51).screenPoint(), true);
        }
        if(ctx.inventory.isFull()) {
            dropping = true;
        }
        if (ctx.inventory.itemAt(x).name().contains("pickaxe")){//if its not a pickaxe, drop it
            x++;
        }
        else {
            ctx.inventory.drop(ctx.inventory.itemAt(x), true);
            try {
                Thread.sleep(rand.nextInt(60) + 20);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            x++;
        }
        if(x > 27){
            x = 0;
            dropping = false;
        }
    }

    private void mine() {
        if (pickaxe && ctx.inventory.isEmpty()){
            banking = true;
        }
        notMine = 0;
        myXp = ctx.skills.experience(14);//set xp before mined rock
        if (!ctx.objects.select().id(tileLeftID).at(tileLeft).isEmpty()) {//if ore at left, mine it
            ctx.objects.select().id(tileLeftID).at(tileLeft).poll().interact("Mine");
            if (rand.nextInt(5) == 1){
                ctx.input.move(ctx.players.local().centerPoint().x+rand.nextInt(80)-40, ctx.players.local().centerPoint().y+rand.nextInt(80)-40);
            }
            else if (rand.nextInt(3) == 2){
                ctx.input.move(ctx.input.getLocation().x+rand.nextInt(40)-20, ctx.input.getLocation().y+rand.nextInt(40)-20);
            }
            while (ctx.skills.experience(14) == myXp && !ctx.objects.select().id(tileLeftID).at(tileLeft).isEmpty()) {
                try {
                    Thread.sleep(rand.nextInt(20) + 10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                notMine++;
                if (notMine > 25 && ctx.players.local().animation() == -1){
                    break;
                }
                if (ctx.players.local().inMotion() || !myTile.equals(ctx.players.local().tile())) {
                    break;
                }
            }
        } else if (!ctx.objects.select().id(tileUpID).at(tileUp).isEmpty()) {//if ore at up, mine it
            ctx.objects.select().id(tileUpID).at(tileUp).poll().interact("Mine");
            if (rand.nextInt(5) == 1){
                ctx.input.move(ctx.players.local().centerPoint().x+rand.nextInt(80)-40, ctx.players.local().centerPoint().y+rand.nextInt(80)-40);
            }
            else if (rand.nextInt(7) == 2){
                ctx.input.move(ctx.input.getLocation().x+rand.nextInt(40)-20, ctx.input.getLocation().y+rand.nextInt(40)-20);
            }
            while (ctx.skills.experience(14) == myXp && !ctx.objects.select().id(tileUpID).at(tileUp).isEmpty()) {
                try {
                    Thread.sleep(rand.nextInt(20) + 10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                notMine++;
                if (notMine > 25 && ctx.players.local().animation() == -1){
                    break;
                }
                if (ctx.players.local().inMotion() || !myTile.equals(ctx.players.local().tile())) {
                    break;
                }
            }
        } else if (!ctx.objects.select().id(tileRightID).at(tileRight).isEmpty()) {//if ore at left, mine it
            ctx.objects.select().id(tileRightID).at(tileRight).poll().interact("Mine");
            if (rand.nextInt(5) == 1){
                ctx.input.move(ctx.players.local().centerPoint().x+rand.nextInt(80)-40, ctx.players.local().centerPoint().y+rand.nextInt(80)-40);
            }
            else if (rand.nextInt(7) == 2){
                ctx.input.move(ctx.input.getLocation().x+rand.nextInt(40)-20, ctx.input.getLocation().y+rand.nextInt(40)-20);
            }
            while (ctx.skills.experience(14) == myXp && !ctx.objects.select().id(tileRightID).at(tileRight).isEmpty()) {
                try {
                    Thread.sleep(rand.nextInt(20) + 10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                notMine++;
                if (notMine > 25 && ctx.players.local().animation() == -1){
                    break;
                }
                if (ctx.players.local().inMotion() || !myTile.equals(ctx.players.local().tile())) {
                    break;
                }
            }
        } else if (!ctx.objects.select().id(tileDownID).at(tileDown).isEmpty()) {//if ore at down, mine it
            ctx.objects.select().id(tileDownID).at(tileDown).poll().interact("Mine");
            if (rand.nextInt(5) == 1){
                ctx.input.move(ctx.players.local().centerPoint().x+rand.nextInt(80)-40, ctx.players.local().centerPoint().y+rand.nextInt(80)-40);
            }
            else if (rand.nextInt(7) == 2){
                ctx.input.move(ctx.input.getLocation().x+rand.nextInt(40)-20, ctx.input.getLocation().y+rand.nextInt(40)-20);
            }
            while (ctx.skills.experience(14) == myXp && !ctx.objects.select().id(tileDownID).at(tileDown).isEmpty()) {
                try {
                    Thread.sleep(rand.nextInt(20) + 10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                notMine++;
                if (notMine > 25 && ctx.players.local().animation() == -1){
                    break;
                }
                if (ctx.players.local().inMotion() || !myTile.equals(ctx.players.local().tile())) {
                    break;
                }
            }
        }
        if (ctx.skills.experience(14) != myXp){
            oreMined++;
        }
    }

    private void bank(){
        banking = true;
        if (ctx.depositBox.opened()){
            ctx.depositBox.close();
        }
        if(ctx.bank.inViewport() && !ctx.bank.opened()){
            ctx.bank.open();
        }
        else if(!ctx.bank.inViewport() && !ctx.bank.opened()){
            ctx.camera.turnTo(ctx.bank.nearest());
            ctx.movement.step(ctx.bank.nearest());
        }
        if(ctx.bank.opened()){
            if (ctx.inventory.isFull()){
                ctx.bank.depositInventory();
            }
            try {
                Thread.sleep(rand.nextInt(50) + 150);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ctx.bank.withdraw(pickID, 1);
            try {
                Thread.sleep(rand.nextInt(70) + 350);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (ctx.inventory.select().isEmpty() && pickaxe){
                System.out.print("RETRY PICK");
                ctx.bank.withdraw(pickID, 1);
            }
            else if ((!ctx.inventory.select().isEmpty() && pickaxe) || !pickaxe){
                System.out.print("BANK FINISHED");
                ctx.bank.close();
                banking = false;
            }
        }
    }

    private void idle(){}

    private State state() {
        if (y == 1){
            y = 0;
            currentState = "ANTI-BAN";
            return State.ANTIBAN;
        }
        if ((!ctx.players.local().tile().equals(myTile) || start) && !banking){//if not at starting tile, move back
            return State.MOVED;
        }
        if ((ctx.inventory.isFull() || dropping) && powermine){//if inv full, drop
            currentState = "DROPPING";
            return State.DROP;
        }
        if ((ctx.inventory.isFull() && !powermine) || banking){//if inv full, bank
            currentState = "BANKING";
            return State.BANK;
        }
        if (oreAvailable){//if ore near, mine
            currentState = "MINING";
            return State.MINE;
        }
        return State.IDLE;
    }

    private enum State{
        MINE, DROP, IDLE, MOVED, ANTIBAN, BANK
    }

    @Override
    public void repaint(Graphics graphics) {
        long milli = this.getTotalRuntime();
        long secs = (milli/1000) % 60;
        long mins = (milli/(1000*60) % 60);
        long hours = (milli/ (1000*60*60)) % 24;
        int xpGain = ctx.skills.experience(14) - startXp;
        int xpHour = (int)(xpGain * (3600000D / milli));
        int oreHour = (int)(oreMined * (3600000D / milli));
        int xpLeft = ctx.skills.experienceAt(ctx.skills.realLevel(14)+1)-ctx.skills.experience(14);
        int ttl = (int)(((xpLeft * 3600000D) / xpHour)/1000/60)+1;//time to level
        Graphics2D draw = (Graphics2D)graphics;

        draw.setColor(new Color(0, 0, 255, 150));
        draw.fillRect(280, 345, 220, 125);

        draw.setColor(new Color(255, 255, 255));
        draw.drawRect(280, 345, 220, 125);

        draw.drawString("Miner:", 290, 360);
        draw.drawString("Runtime: "+String.format("%02d:%02d:%02d", hours, mins, secs), 290, 375);
        draw.drawString("Xp/hour: " + xpHour, 290, 390);
        draw.drawString("XP gained: "+xpGain, 290, 405);
        draw.drawString("Level: " + ctx.skills.realLevel(14)+" (+"+(ctx.skills.realLevel(14) - startLvl)+")"+"  TTL: "+ttl+"min ("+xpLeft/1000+"k)", 290, 420);
        draw.drawString("Ore mined: " + oreMined + " (" + oreHour + "/hr)", 290, 435);
        draw.drawString("Status: " + currentState, 290, 450);
        draw.drawString("Version 0.1", 290, 465);
        draw.drawString("Option: " + choice, 365, 465);
    }

}

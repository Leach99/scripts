package bot;

import org.powerbot.script.Tile;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Npc;
import org.powerbot.script.rt4.TilePath;

import javax.swing.*;
import java.util.Random;


@Script.Manifest(name = "runner", description = "runs runes to nature altar", properties = "client=4; topic=0;")

public class runner extends PollingScript<ClientContext>{

    int x;
    int y;
    int idleCounter = 0;
    final static int NAT_RUINS = 32492;
    boolean trading = false;
    String crafter;
    Random rand = new Random();
    boolean start = true;



    private final Tile[] path = new Tile[] {
            new Tile(2769, 3125, 0),
            new Tile(2773, 3126, 0),
            new Tile(2778, 3127, 0),
            new Tile(2783, 3126, 0),
            new Tile(2788, 3125, 0),
            new Tile(2793, 3124, 0),
            new Tile(2798, 3122, 0),
            new Tile(2803, 3120, 0),
            new Tile(2807, 3118, 0),
            new Tile(2812, 3117, 0),
            new Tile(2816, 3115, 0),
            new Tile(2820, 3112, 0),
            new Tile(2823, 3109, 0),
            new Tile(2826, 3106, 0),
            new Tile(2828, 3103, 0),
            new Tile(2830, 3099, 0),
            new Tile(2832, 3095, 0),
            new Tile(2835, 3092, 0),
            new Tile(2838, 3089, 0),
            new Tile(2841, 3085, 0),
            new Tile(2844, 3081, 0),
            new Tile(2846, 3077, 0),
            new Tile(2849, 3073, 0),
            new Tile(2850, 3069, 0),
            new Tile(2851, 3065, 0),
            new Tile(2852, 3060, 0),
            new Tile(2854, 3056, 0),
            new Tile(2856, 3051, 0),
            new Tile(2857, 3046, 0),
            new Tile(2858, 3042, 0),
            new Tile(2860, 3038, 0),
            new Tile(2863, 3034, 0),
            new Tile(2864, 3031, 0),
            new Tile(2864, 3027, 0),
            new Tile(2863, 3023, 0),
            new Tile(2867, 3020, 0)
    };
    TilePath pathToAltar = ctx.movement.newTilePath(path);

    private final Tile[] newPath = new Tile[] {
            new Tile(2863, 3023, 0),
            new Tile(2864, 3027, 0),
            new Tile(2864, 3031, 0),
            new Tile(2863, 3034, 0),
            new Tile(2860, 3038, 0),
            new Tile(2858, 3042, 0),
            new Tile(2857, 3046, 0),
            new Tile(2856, 3051, 0),
            new Tile(2854, 3056, 0),
            new Tile(2852, 3060, 0),
            new Tile(2851, 3065, 0),
            new Tile(2850, 3069, 0),
            new Tile(2849, 3073, 0),
            new Tile(2846, 3077, 0),
            new Tile(2844, 3081, 0),
            new Tile(2841, 3085, 0),
            new Tile(2838, 3089, 0),
            new Tile(2835, 3092, 0),
            new Tile(2832, 3095, 0),
            new Tile(2830, 3099, 0),
            new Tile(2828, 3103, 0),
            new Tile(2826, 3106, 0),
            new Tile(2823, 3109, 0),
            new Tile(2820, 3112, 0),
            new Tile(2816, 3115, 0),
            new Tile(2812, 3117, 0),
            new Tile(2807, 3118, 0),
            new Tile(2803, 3120, 0),
            new Tile(2798, 3122, 0),
            new Tile(2793, 3124, 0),
            new Tile(2788, 3125, 0),
            new Tile(2783, 3126, 0),
            new Tile(2778, 3127, 0),
            new Tile(2773, 3126, 0),
            new Tile(2769, 3125, 0)
    };
    TilePath pathToShop = ctx.movement.newTilePath(newPath) ;

    @Override
    public void poll() {

        switch (state()) {


            case RUN:
                System.out.print("RUN");
                idleCounter=0;
                run();
                break;

            case TRADE:
                System.out.print("TRADE");
                idleCounter=0;
                trade();
                break;

            case BUY:
                System.out.print("BUY");
                idleCounter=0;
                buy();
                break;
        }
    }

    private void run() {
        x = rand.nextInt(200);
        y = rand.nextInt(3) + 3;
        System.out.print("\npathToShop: "+pathToShop.start().distanceTo(ctx.players.local()));
        System.out.print("\npathToAltar: "+pathToAltar.start().distanceTo(ctx.players.local()));
        if (ctx.objects.select().id(34756).poll().inViewport()){//portal out of altar
            ctx.objects.select().id(34756).poll().interact("Use");
            try { Thread.sleep(1500); } catch (InterruptedException ex) {Thread.currentThread().interrupt();}
        }
        else if (ctx.players.local().tile().distanceTo(ctx.objects.select().id(34756).poll()) < 10){
            ctx.camera.turnTo(ctx.objects.select().id(34756).poll());
        }
        if (ctx.inventory.isFull()){
            while (pathToAltar.end().distanceTo(ctx.players.local()) > y && !ctx.objects.nearest().id(NAT_RUINS).poll().inViewport()) {
                if (!ctx.players.local().inMotion() || ctx.movement.destination().distanceTo(ctx.players.local()) <= 1){//if stop moving or gets stuck(clicking same tile)randomises next tile
                    System.out.print("NOT IN MOTION");
                    pathToAltar.randomize(1, 1).traverse();
                    idleCounter++;
                }
                if (ctx.movement.destination().equals(Tile.NIL) || ctx.movement.destination().distanceTo(ctx.players.local()) <= y) {
                    pathToAltar.randomize(1, 1).traverse();
                    idleCounter++;
                }
                if (idleCounter > 25){
                    break;
                }
                if ((ctx.movement.energyLevel() > 50 || ctx.players.local().healthBarVisible()) && !ctx.movement.running() && ctx.movement.energyLevel() > 0){
                    ctx.movement.running(true);
                }
                idleCounter++;
            }
            if (pathToAltar.end().distanceTo(ctx.players.local()) < 7){
                if (ctx.objects.select().id(NAT_RUINS).poll().inViewport()){
                    System.out.print("entering ruins\n");
                    ctx.objects.select().id(NAT_RUINS).poll().interact("Enter");
                }
                else {
                    ctx.camera.turnTo(ctx.objects.select().id(NAT_RUINS).poll());
                    ctx.movement.step(ctx.objects.select().id(NAT_RUINS).poll());
                }
            }
        }
        else {
            while (pathToShop.end().distanceTo(ctx.players.local()) > y) {
                if (!ctx.players.local().inMotion() || ctx.movement.destination().distanceTo(ctx.players.local()) <= 1){//if stop moving or gets stuck(clicking same tile)randomises next tile
                    System.out.print("NOT IN MOTION");
                    pathToShop.randomize(1, 1).traverse();
                }
                if (ctx.movement.destination().equals(Tile.NIL) || ctx.movement.destination().distanceTo(ctx.players.local()) <= y) {
                    pathToShop.randomize(1, 1).traverse();
                }
                if (idleCounter > 10){
                    break;
                }
                idleCounter++;
            }
        }
    }

    private void trade(){
        int z = rand.nextInt(200)+100;
        if(!trading){
            if (!ctx.players.select().name(crafter).poll().inViewport()){
                ctx.camera.turnTo(ctx.players.select().name(crafter).poll());
            }
            ctx.players.select().name(crafter).poll().interact("Trade with");
        }
        trading = true;
        if(ctx.widgets.component(334, 13).visible()){//second screen visible
            System.out.print("ACCEPTING 2ND");
            ctx.widgets.component(334, 13).interact("Accept");//accept second screen
            try {Thread.sleep(z+20);}//goes to sleep for x time
            catch (InterruptedException ex){Thread.currentThread().interrupt();}
            if (!ctx.inventory.isFull()){
                trading = false;
            }
        }
        if (ctx.widgets.component(335, 2).visible()) {//if in trade
            if (ctx.widgets.component(335, 9).text().contains("has 25 free")){
                ctx.inventory.itemAt(2).interact("Offer");
            }
            ctx.inventory.itemAt(5).interact("Offer-All");//offer all ess
            try { Thread.sleep(z/2); } catch (InterruptedException ex) {Thread.currentThread().interrupt();}
            ctx.widgets.component(335, 11).interact("Accept");
            while (trading) {
                if (!ctx.widgets.component(335, 2).visible() || !ctx.widgets.component(334, 13).visible()) {
                    break;
                }
                System.out.print(" trading ");
                System.out.print(" ess in trade ");
                while (ctx.inventory.isFull() && ctx.widgets.component(335, 2).visible()) {//while bot hasnt put pure ess in trade
                    System.out.print(" putting ess in ");
                    ctx.inventory.itemAt(1).interact("Offer-All");//offer some un noted back
                }
                ctx.widgets.component(335, 11).interact("Accept");//accepts the trade, first screen
                System.out.print(" accepted 1st ");
                while (!ctx.widgets.component(334, 13).visible()) {//while second screen not visible, wait
                    System.out.print(" waiting for 2nd ");
                    try {
                        Thread.sleep(z/4);
                    }//goes to sleep for x time
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }//wakes up
                }
                System.out.print("ACCEPTING 2ND");
                ctx.widgets.component(334, 13).interact("Accept");//accept second screen
                while (ctx.widgets.component(334, 13).visible()) {//while trade still open wait for other
                    System.out.print("WAITING FOR OTHER");
                    try {
                        Thread.sleep(z + 20);
                    }//goes to sleep for x time
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (!ctx.inventory.isFull()){
                    System.out.print(" TRADE DONE ");
                    trading = false;
                }
            }
        }
        else{
            //wait to be traded, then accept
            System.out.print(" waiting ");
            ctx.input.move(ctx.widgets.component(162, 58).component(0).centerPoint());
            if(ctx.menu.items()[0].contains("Accept trade") && ctx.widgets.component(162, 58).component(0).text().contains(crafter)){
                while(!ctx.widgets.component(335, 2).visible() && ctx.menu.items()[0].contains("Accept trade")){
                    System.out.print(" clicking ");
                    ctx.input.click(true);
                    try {Thread.sleep(z+800);}//goes to sleep for x time
                    catch (InterruptedException ex){Thread.currentThread().interrupt();}//wakes up
                }
            }
        }
    }

    private void buy(){//sells noted ess, buys back un-noted, buys food if needed
        int z = rand.nextInt(300)+100;
        Npc shop = ctx.npcs.select().name("Jiminua").nearest().poll();
        if (!shop.inViewport()){
            ctx.camera.turnTo(shop);
            ctx.movement.step(shop);
        }
        shop.interact("Trade");
        try {Thread.sleep(z*3);}//goes to sleep for x time
        catch (InterruptedException ex){Thread.currentThread().interrupt();}//wakes up
        if(ctx.widgets.component(300, 0).visible()){//if shop open
            if(!ctx.inventory.isFull()){
                System.out.print("Selling");
                ctx.widgets.component(301, 0).component(0).interact("Sell 10");//this is component for inv while in shop
                try {Thread.sleep(z/4);}//goes to sleep for x time
                catch (InterruptedException ex){Thread.currentThread().interrupt();}//wakes up
                ctx.widgets.component(301, 0).component(0).interact("Sell 10");//this is component for inv while in shop
                try {Thread.sleep(z/4);}//goes to sleep for x time
                catch (InterruptedException ex){Thread.currentThread().interrupt();}//wakes up
                ctx.widgets.component(301, 0).component(0).interact("Sell 10");//this is component for inv while in shop
            }
            System.out.print("Buying");
            ctx.widgets.component(300, 16).component(28).interact("Buy 50");//buys noted ess
            ctx.menu.close();
            try {Thread.sleep(z+300);}//goes to sleep for x time
            catch (InterruptedException ex){Thread.currentThread().interrupt();}
        }
    }

    private State state(){
        if (start){
            crafter = "" + JOptionPane.showInputDialog(null, "RSN of Runecrafter:", "Runner", JTextField.CENTER);
            start = false;
        }
        if (trading || (ctx.players.local().tile().distanceTo(ctx.objects.select().id(34756).poll()) < 15 && ctx.inventory.isFull())){//if can see portal
            return State.TRADE;
        }
        if (!ctx.inventory.isFull() && pathToAltar.start().distanceTo(ctx.players.local().tile()) < 15){
            return State.BUY;
        }
        return State.RUN;
    }

    private enum State{
        RUN, TRADE, BUY
    }

}

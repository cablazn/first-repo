package akiraion.isChillBot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;


public class BotisChill extends ListenerAdapter {
    JDA theBot;
    User botUser;
    MessageHandler mh;
    
    public static BotisChill getInstance() throws LoginException, InterruptedException {
        boolean foundResource = false;
        String atoken = "";
        
        try {    
            atoken = getTokenwithStream();
            foundResource = true;
        } catch (IOException e) {
            printErrorMessage(e);        
        } catch (NullPointerException e) {
            
        }
        
        if(!foundResource) {
            System.out.println("Couldn't find resource.");
            try {
                atoken = getTokenwithFile();
            } catch (IOException e) {
                printErrorMessage(e);
            } catch (NullPointerException e) {
                
            }    
        }
        
        BotisChill bic = null;
        if (bic == null) {
            bic = new BotisChill(atoken);
        }
        return bic;
    }

    
    private static String getTokenwithStream() throws IOException {
        BufferedReader br;
        InputStream iputStream = BotisChill.class.getResourceAsStream("/kiwi");
        if(iputStream!=null) {
            br = new BufferedReader(new InputStreamReader(iputStream));
            return br.readLine();
        }
        return null;
    }
    
    private static String getTokenwithFile() throws IOException {
        String returnString = null;
        FileInputStream fstream = new FileInputStream("kiwi");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        if(br!=null) {
            returnString = br.readLine();
        }
        br.close();
        return returnString;
    }
    
    @SuppressWarnings({ "unused", "deprecation" })
    public BotisChill(String atoken) throws LoginException, InterruptedException {
        System.out.println("Token length: " + atoken.length());
        theBot = new JDABuilder(AccountType.BOT)
                .setToken(atoken)
                .buildBlocking();
        theBot.addEventListener(this);
        botUser = theBot.getSelfUser();
        mh = new MessageHandler(theBot);
        
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            mh.handleM();
        }, 1000, 250, TimeUnit.MILLISECONDS);
        ScheduledFuture<?> afuture = executor.scheduleAtFixedRate(() -> {
            mh.popOutQ();
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        //mh.addQ(e);
        mh.addtoINQ(e);
    }

    @Override
    public void onUserUpdateGame(UserUpdateGameEvent e) {
        Member mem = e.getMember();
        if(mem.getOnlineStatus() != null && mem.getOnlineStatus() == OnlineStatus.ONLINE) {
            Game agame = mem.getGame();
            if(agame!= null && agame.getType() != null) {
                String uname = e.getUser().getName();
                if(agame.getType() == GameType.STREAMING) {
                    mh.sendtoQ(theBot.getTextChannels().get(0),uname + " has started streaming at " + agame.getUrl());
                } else if(agame.getType() == GameType.DEFAULT) {
                    //mh.sendtoQ(theBot.getTextChannels().get(0),uname + " has switched to " + agame.getName());
                    mh.sendtoQ(theBot.getTextChannelsByName("example-text-only-channel", true).get(0),uname + " has switched to " + agame.getName());
                }
            }
        }
    }
    
    protected static void printErrorMessage(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        System.out.println(sStackTrace);
    }
    
}

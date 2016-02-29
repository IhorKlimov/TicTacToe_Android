/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package com.iklimov.tictactoe.backend;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import static com.iklimov.tictactoe.backend.OfyService.ofy;

/**
 * A registration endpoint class we are exposing for a device's GCM registration id on the backend
 * <p/>
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * <p/>
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(
        name = "game",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.tictactoe.iklimov.com",
                ownerName = "backend.tictactoe.iklimov.com",
                packagePath = ""
        )
)
public class GameEndpoint {

    private static final Logger log = Logger.getLogger(GameEndpoint.class.getName());
    private static final String API_KEY = System.getProperty("gcm.api.key");
    private static final String ACTION = "action";
    private static final String NAME = "name";
    private static final String REG_ID = "regId";
    private static final String YOU_FIRST = "you first";
    public static final String ROW = "Row";
    public static final String COL = "Col";
    public static final String SIDE = "side";
    public static final String TURN = "turn";
    public static final String IS_ORGANIZER = "Is Organizer";

    private static final String INVITE_TO_PLAY = "invite to play";
    private static final String NEW_USER = "new user";
    private static final String START_GAME = "start game";
    private static final String IS_OFFLINE = "Is Offline";


    /**
     * Register a device to the backend
     *
     * @param regId The Google Cloud Messaging registration Id to add
     */
    @ApiMethod(name = "register")
    public void registerDevice(
            @Named("regId") String regId, @Named("name") String name)
            throws IOException {
        if (findRecord(regId) != null) {
            log.info("Device " + regId + " already registered, skipping register");
            return;
        }
        Player record = new Player();
        record.setRegId(regId);
        record.setName(name);
        ofy().save().entity(record).now();
        signUpForGame(regId, name);
    }

    /**
     * Unregister a device from the backend
     *
     * @param regId The Google Cloud Messaging registration Id to remove
     */
    @ApiMethod(name = "unregister")
    public void unregisterDevice(@Named("regId") String regId) {
        Player record = findRecord(regId);
        if (record == null) {
            log.info("Device " + regId + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();
    }

    @ApiMethod(name = "signUpForGame")
    public void signUpForGame(@Named("regId") String regId, @Named("name") String name)
            throws IOException {
        Collection<Player> players = listOnlinePlayers(regId);
        for (Player p : players) {
            if (p.getRegId().equals(regId)) {
                return;
            }
        }
            OnlinePlayer onlinePlayer = new OnlinePlayer();
            onlinePlayer.setRegId(regId);
            ofy().save().entity(onlinePlayer).now();
            notifyOtherUsers(regId, name);
    }

    private void notifyOtherUsers(String regId, String name) throws IOException {
        List<OnlinePlayer> onlinePlayers = ofy().load().type(OnlinePlayer.class).list();
//        Player you = findRecord(regId);
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder()
                .addData(ACTION, NEW_USER)
                .addData(NAME, name)
                .addData(REG_ID, regId)
                .build();

        for (OnlinePlayer op : onlinePlayers) {
            if (!op.getRegId().equals(regId)) {
                sender.send(msg, op.getRegId(), 5);
            }
        }
    }

    @ApiMethod(name = "unSignUpFromGame")
    public void unSignUpFromGame(@Named("regId") String regId) {
        OnlinePlayer onlinePlayer = ofy().load()
                .type(OnlinePlayer.class)
                .filter("regId", regId)
                .list()
                .get(0);
        ofy().delete().entity(onlinePlayer).now();
    }

    @ApiMethod(name = "inviteToPlay")
    public void inviteToPlay(
            @Named("yourId") String yourId,
            @Named("opponentsId") String opponentsId) throws IOException {
        Player you = findRecord(yourId);

        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder()
                .addData(ACTION, INVITE_TO_PLAY)
                .addData(NAME, you.getName())
                .addData(REG_ID, you.getRegId())
                .build();
        sender.send(msg, opponentsId, 5);
    }

    @ApiMethod(name = "responseToPlay")
    public void responseToPlay(
            @Named("yourId") String yourId,
            @Named("opponentsId") String opponentsId,
            @Named("accepted") boolean accepted,
            @Named("isOrganizer") Boolean isOrganizer) throws IOException {

        Sender sender = null;
        Message toYou = null;
        Message toOpponent = null;

        if (accepted) {
            Player you = findRecord(yourId);
            Player opponent = findRecord(opponentsId);

            boolean youFirst = System.currentTimeMillis() % 2 == 0;
            String yourSide;
            String opponentsSide;
            if (System.currentTimeMillis() % 2 == 0) {
                yourSide = "X";
                opponentsSide = "O";
            } else {
                yourSide = "O";
                opponentsSide = "X";
            }

            sender = new Sender(API_KEY);
            toYou = new Message.Builder()
                    .addData(ACTION, START_GAME)
                    .addData(NAME, opponent.getName())
                    .addData(REG_ID, opponent.getRegId())
                    .addData(YOU_FIRST, youFirst + "")
                    .addData(SIDE, yourSide)
                    .addData(IS_ORGANIZER, isOrganizer + "")
                    .build();

            toOpponent = new Message.Builder()
                    .addData(ACTION, START_GAME)
                    .addData(NAME, you.getName())
                    .addData(REG_ID, you.getRegId())
                    .addData(YOU_FIRST, !youFirst + "")
                    .addData(SIDE, opponentsSide)
                    .addData(IS_ORGANIZER, !isOrganizer + "")
                    .build();
        }

        sender.send(toYou, yourId, 5);
        sender.send(toOpponent, opponentsId, 5);

    }

    @ApiMethod(name = "sendOffline")
    public void sendOffline(@Named("opponentsId") String opponentsId) throws IOException {
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder()
                .addData(ACTION, IS_OFFLINE)
                .build();
        sender.send(msg, opponentsId, 5);
    }

    @ApiMethod(name = "sendTurn")
    public void sendMove(
            @Named("opponentsId") String opponentsId,
            @Named("row") String row,
            @Named("col") String col) throws IOException {
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder()
                .addData(ACTION, TURN)
                .addData(ROW, row)
                .addData(COL, col)
                .build();

        sender.send(msg, opponentsId, 5);
    }

    /**
     * Return a collection of registered devices
     *
     * @param count The number of devices to list
     * @return a list of Google Cloud Messaging registration Ids
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<Player> listDevices(@Named("count") int count) {
        List<Player> records = ofy().load().type(Player.class).limit(count).list();
        return CollectionResponse.<Player>builder().setItems(records).build();
    }

    private Player findRecord(String regId) {
        return ofy().load().type(Player.class).filter("regId", regId).first().now();
    }

    @ApiMethod(name = "listOnlinePlayers", path = "listOnlinePlayers")
    public Collection<Player> listOnlinePlayers(@Named("regId") String regId) {
        List<OnlinePlayer> list = ofy().load().type(OnlinePlayer.class).filter("regId !=", regId).list();
        ArrayList<Player> players = new ArrayList<>();
        for (OnlinePlayer p : list) {
            players.add(findRecord(p.getRegId()));
        }
        return players;
    }

}

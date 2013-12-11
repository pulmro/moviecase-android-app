package org.bigiarini.android.moviedb.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by emanuele on 28/10/13.
 */
public class ServerModel implements Parcelable {

    private static final byte PRESENT = 1;
    private static final byte NOT_PRESENT = 0;

    private static final String ADDRESS_KEY = "org.bigiarini.android.moviedb.key.address_key";
    private static final String PORT_KEY = "org.bigiarini.android.moviedb.key.port_key";
    private static final String VERSION_KEY = "org.bigiarini.android.moviedb.key.version_key";

    private static final boolean ERROR_SERVER = true;



    private String mAddress;

    private int mPort;

    private String mVersion;

    private ServerState mState;

    public static class ServerState {
        private static final String GOOD_SERVER = "Server configuration is good.";
        private int mState;
        private String mMessage;
        private String mVersion;

        private ServerState(final int state) {
            this.mState = state;
        }

        public ServerState withMessage(final String message) {
            this.mMessage = message;
            return this;
        }

        public ServerState withVersion(final String version) {
            this.mVersion = version;
            return this;
        }

        public boolean isGood() {
            return mState==1;
        }

        public String errorMessage() {
            if (!isGood()) {
                return mMessage;
            }
            else
                return GOOD_SERVER;
        }

        public String getVersion() {
            return mVersion;
        }
    }

    public static ServerState createState(final int state) {
        return new ServerState(state);
    }



    private ServerModel(final String address) {
        this.mAddress = address;
    }

    public static final Creator<ServerModel> CREATOR = new Creator<ServerModel>() {

        public ServerModel createFromParcel(Parcel in) {
            return new ServerModel(in);
        }

        public ServerModel[] newArray(int size) {
            return new ServerModel[size];
        }
    };


    public ServerModel(Parcel in) {
        this.mAddress = in.readString();
        if (in.readByte() == PRESENT) {
            this.mPort = in.readInt();
        }
        if (in.readByte() == PRESENT) {
            this.mVersion = in.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAddress);
        if (mPort!=0) {
            dest.writeByte(PRESENT);
            dest.writeInt(mPort);
        }
        else {
            dest.writeByte(PRESENT);
            dest.writeInt(8000);
        }
        if (!TextUtils.isEmpty(mVersion)) {
            dest.writeByte(PRESENT);
            dest.writeString(mVersion);
        }
    }


    public static ServerModel create(final String address) {
        final ServerModel serverModel = new ServerModel(address);
        return serverModel;
    }

    public ServerModel withPort(final int port) {
        if (port==0) {
            throw new IllegalArgumentException("Port cannot be 0!");
        }
        this.mPort = port;
        return this;
    }

    public ServerModel withVersion(final String version) {
        if (TextUtils.isEmpty(version)) {
            this.mVersion = "Unknown";
        }
        this.mVersion = version;
        return this;
    }

    public void save(final Context ctx) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADDRESS_KEY, mAddress);
        editor.putInt(PORT_KEY, mPort);
        editor.putString(VERSION_KEY, mVersion);
        editor.commit();
    }

    public static ServerModel load(final Context ctx) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String address = prefs.getString(ADDRESS_KEY, "");
        ServerModel server = null;
        if (!TextUtils.isEmpty(address)) {
            server = new ServerModel(address);
            server.mPort = prefs.getInt(PORT_KEY, 8000);
            server.mVersion = prefs.getString(VERSION_KEY, "Unknown");
            return server;
        }
        return server;
    }


    public String getAddress() {
        return mAddress;
    }

    public int getPort() {
        return mPort;
    }

    public void setState(final ServerState state) {
        this.mState = state;
    }

    public ServerState state() {
        return mState;
    }

}

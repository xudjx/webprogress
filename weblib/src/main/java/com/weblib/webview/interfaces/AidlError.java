package com.weblib.webview.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xud on 2017/8/22.
 */

public class AidlError implements Parcelable {

    public int code;
    public String message;
    public String extra;

    public AidlError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.code);
        dest.writeString(this.message);
        dest.writeString(this.extra);
    }

    public AidlError() {
    }

    protected AidlError(Parcel in) {
        this.code = in.readInt();
        this.message = in.readString();
        this.extra = in.readString();
    }

    public static final Creator<AidlError> CREATOR = new Creator<AidlError>() {
        @Override
        public AidlError createFromParcel(Parcel source) {
            return new AidlError(source);
        }

        @Override
        public AidlError[] newArray(int size) {
            return new AidlError[size];
        }
    };
}

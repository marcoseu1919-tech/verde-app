package com.verde.player;

import android.Manifest;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.PluginMethod;

@CapacitorPlugin(
    name = "MusicLibrary",
    permissions = {
        @Permission(alias = "audio", strings = { Manifest.permission.READ_MEDIA_AUDIO }),
        @Permission(alias = "storage", strings = { Manifest.permission.READ_EXTERNAL_STORAGE })
    }
)
public class MusicLibraryPlugin extends Plugin {

    private String alias() {
        return Build.VERSION.SDK_INT >= 33 ? "audio" : "storage";
    }

    @PluginMethod
    public void getTracks(PluginCall call) {
        if (getPermissionState(alias()) != PermissionState.GRANTED) {
            requestPermissionForAlias(alias(), call, "permDone");
            return;
        }
        load(call);
    }

    @PermissionCallback
    private void permDone(PluginCall call) {
        if (getPermissionState(alias()) == PermissionState.GRANTED) {
            load(call);
        } else {
            call.reject("denied");
        }
    }

    private void load(PluginCall call) {
        JSArray arr = new JSArray();
        String[] proj = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        };
        String where = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DURATION + " > 30000";
        try (Cursor c = getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, where, null,
                MediaStore.Audio.Media.TITLE + " ASC")) {
            if (c != null) {
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    JSObject o = new JSObject();
                    o.put("id", id);
                    o.put("title", c.getString(1));
                    o.put("artist", c.getString(2));
                    o.put("duration", c.getLong(3) / 1000.0);
                    o.put("uri", ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString());
                    arr.put(o);
                }
            }
        } catch (Exception e) {
            call.reject(String.valueOf(e.getMessage()));
            return;
        }
        JSObject r = new JSObject();
        r.put("tracks", arr);
        call.resolve(r);
    }
}

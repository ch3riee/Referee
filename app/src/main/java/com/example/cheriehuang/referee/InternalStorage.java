package com.example.cheriehuang.referee;

import android.content.Context;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.ArrayList;
/**
 * Created by cheriehuang on 5/20/17.
 */

public final class InternalStorage{

    protected InternalStorage() {}

    public static void writeObject(Context context, String key, ArrayList<HashMap<String, String>> object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static ArrayList<HashMap<String, String>> readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<HashMap<String, String>> object = (ArrayList<HashMap<String, String>>) ois.readObject();
        return object;
    }
}

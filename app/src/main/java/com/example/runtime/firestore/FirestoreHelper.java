package com.example.runtime.firestore;

import android.util.Log;

import com.example.runtime.firestore.models.Run;
import com.example.runtime.firestore.models.RunSegment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * to deserialize firebase needs the java object to have an empty constructor, and the set methods
 * need to get the correct firebase type.
 * <p>
 * This class aims to be a helper for firestore init, due to asynchronous limitation the methods
 * are implemented inside the class that needs them.
 */
public class FirestoreHelper {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseFirestore getDb() {
        return db;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("d MMM yyyy, 'at' HH:mm", Locale.ENGLISH);

        return dateTime.format(formatter);
    }

    public static String formatDateTimeWithSecondos(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("d MMM yyyy, 'at' HH:mm:ss", Locale.ENGLISH);

        return dateTime.format(formatter);
    }

    //firestore uses own timestamp to handle data while our application use LocalDateTime
    public static Timestamp getFirebaseTimestampFromLocalDateTime(LocalDateTime date) {
        Instant instant = date.atZone(ZoneId.systemDefault()).toInstant();
        return new Timestamp(instant.toEpochMilli() / 1000, (int) ((instant.toEpochMilli() % 1000) * 1000000));
    }

    public static LocalDateTime getLocalDateTimeFromFirebaseTimestamp(Timestamp date) {
        Instant instant = date.toDate().toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

}

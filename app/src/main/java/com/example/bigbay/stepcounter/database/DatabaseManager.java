package com.example.bigbay.stepcounter.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager instance;

    private static StepDatabaseHelper dbHelper;

    private SQLiteDatabase db;

    public static synchronized void initializeInstance(StepDatabaseHelper helper) {

        if (instance == null) {

            instance = new DatabaseManager();

            dbHelper = helper;

        }

    }

    public static synchronized DatabaseManager getInstance(StepDatabaseHelper helper) {

        if (instance == null) {

            initializeInstance(helper);

        }

        return instance;

    }

    public synchronized SQLiteDatabase getWritableDatabase() {

        if (mOpenCounter.incrementAndGet() == 1) {

// Opening new database

            db = dbHelper.getWritableDatabase();

        }

        return db;

    }

    public synchronized SQLiteDatabase getReadableDatabase() {

        if (mOpenCounter.incrementAndGet() == 1) {

// Opening new database

            db = dbHelper.getReadableDatabase();

        }

        return db;

    }

    public synchronized void closeDatabase() {

        if (mOpenCounter.decrementAndGet() == 0) {

// Closing database

            db.close();

        }

    }
}
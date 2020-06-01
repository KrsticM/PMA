package com.example.pma.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DBContentProvider extends ContentProvider {
    private static final String TAG = "DBContentProvider";

    private RouteSQLiteHelper database;

    private static final String AUTHORITY = "com.example.pma";

    private static final String ROUTE_PATH = "route";
    public static final Uri CONTENT_URI_ROUTE = Uri.parse("content://" + AUTHORITY + "/" + ROUTE_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ALL_ROUTES = 1;
    private static final int ONE_ROUTE = 2;
    private static final int ROUTE_STOPS = 3;
    private static final int ONE_STOP = 4;

    static {
        sURIMatcher.addURI(AUTHORITY, ROUTE_PATH, ALL_ROUTES);
        sURIMatcher.addURI(AUTHORITY, ROUTE_PATH + "/#", ONE_ROUTE);
        sURIMatcher.addURI(AUTHORITY, ROUTE_PATH + "/#/stop", ROUTE_STOPS);
        sURIMatcher.addURI(AUTHORITY, ROUTE_PATH + "/#/stop/#", ONE_STOP);

    }

    @Override
    public boolean onCreate() {
        database = new RouteSQLiteHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ONE_ROUTE:
                // Adding the ID to the original query
                queryBuilder.appendWhere(RouteSQLiteHelper.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                //$FALL-THROUGH$
            case ALL_ROUTES:
                // Set the table
                queryBuilder.setTables(RouteSQLiteHelper.TABLE_ROUTE);
                break;
            case ONE_STOP:
                // TODO: Napraviti query
                break;
            case ROUTE_STOPS:
                int length = uri.getPathSegments().size();
                String route_id = uri.getPathSegments().get(length-2); // Uzimamo pretposlednji path segment jer je to route id

                queryBuilder.appendWhere(RouteSQLiteHelper.COLUMN_ROUTE_ID + "=" + route_id);
                queryBuilder.setTables(RouteSQLiteHelper.TABLE_BUSSTOP);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri retVal = null;
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case ALL_ROUTES:
                id = sqlDB.insert(RouteSQLiteHelper.TABLE_ROUTE, null, values);
                retVal = Uri.parse(ROUTE_PATH + "/" + id);
                break;
            case ROUTE_STOPS:
                id = sqlDB.insert(RouteSQLiteHelper.TABLE_BUSSTOP, null, values);
                retVal = Uri.parse(ROUTE_PATH + "/stop/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        int rowsDeleted = 0;
        switch (uriType) {
            case ALL_ROUTES:
                rowsDeleted = sqlDB.delete(RouteSQLiteHelper.TABLE_ROUTE,
                        selection,
                        selectionArgs);
                break;
            case ONE_ROUTE:
                String idRoute = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(RouteSQLiteHelper.TABLE_ROUTE,
                            RouteSQLiteHelper.COLUMN_ID + "=" + idRoute,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(RouteSQLiteHelper.TABLE_ROUTE,
                            RouteSQLiteHelper.COLUMN_ID + "=" + idRoute
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        int rowsUpdated = 0;
        switch (uriType) {
            case ALL_ROUTES:
                rowsUpdated = sqlDB.update(RouteSQLiteHelper.TABLE_ROUTE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ONE_ROUTE:
                String idRoute = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(RouteSQLiteHelper.TABLE_ROUTE,
                            values,
                            RouteSQLiteHelper.COLUMN_ID + "=" + idRoute,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(RouteSQLiteHelper.TABLE_ROUTE,
                            values,
                            RouteSQLiteHelper.COLUMN_ID + "=" + idRoute
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}

package com.simplicityapp.base.persistence.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.simplicityapp.R;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.modules.notifications.model.ContentInfo;
import com.simplicityapp.modules.places.model.Category;
import com.simplicityapp.modules.places.model.Images;
import com.simplicityapp.modules.places.model.Place;

import static com.simplicityapp.base.config.Constant.LOG_TAG;
import static com.simplicityapp.base.persistence.db.DatabaseConstants.*;

public class DatabaseHandler extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private Context context;

    private int cat_id[]; // category id
    private String cat_name[]; // category name
    private TypedArray cat_icon; // category name

    public DatabaseHandler(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
        this.context = context;
        this.db = getWritableDatabase();

        // get data from res/values/category.xml
        cat_id = context.getResources().getIntArray(R.array.id_category);
        cat_name = context.getResources().getStringArray(R.array.category_name);
        cat_icon = context.getResources().obtainTypedArray(R.array.category_icon);

        // if length not equal refresh table category
        if(getCategorySize() != cat_id.length) {
            defineCategory(this.db);  // define table category
        }

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase d) {
        createTablePlace(d);
        createTableImages(d);
        createTableCategory(d);
        createTableRelational(d);
        createTableFavorites(d);
        createTableContentInfo(d);
    }

    private void createTablePlace(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DatabaseConstants.TABLE_PLACE + " ("
                + DatabaseConstants.KEY_PLACE_ID + " INTEGER PRIMARY KEY, "
                + DatabaseConstants.KEY_NAME + " TEXT, "
                + DatabaseConstants.KEY_IMAGE + " TEXT, "
                + DatabaseConstants.KEY_ADDRESS + " TEXT, "
                + DatabaseConstants.KEY_PHONE + " TEXT, "
                + DatabaseConstants.KEY_WEBSITE + " TEXT, "
                + DatabaseConstants.KEY_DESCRIPTION + " TEXT, "
                + DatabaseConstants.KEY_LNG + " REAL, "
                + DatabaseConstants.KEY_LAT + " REAL, "
                + DatabaseConstants.KEY_DISTANCE + " REAL, "
                + DatabaseConstants.KEY_LAST_UPDATE + " NUMERIC "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableImages(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DatabaseConstants.TABLE_IMAGES + " ("
                + DatabaseConstants.KEY_IMG_PLACE_ID + " INTEGER, "
                + DatabaseConstants.KEY_IMG_NAME + " TEXT, "
                + " FOREIGN KEY(" + DatabaseConstants.KEY_IMG_PLACE_ID + ") REFERENCES " + DatabaseConstants.TABLE_PLACE + "(" + DatabaseConstants.KEY_PLACE_ID + ")"
                + " )";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableCategory(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DatabaseConstants.TABLE_CATEGORY + "("
                + DatabaseConstants.KEY_CAT_ID + " INTEGER PRIMARY KEY, "
                + DatabaseConstants.KEY_CAT_NAME + " TEXT, "
                + DatabaseConstants.KEY_CAT_ICON + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableFavorites(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DatabaseConstants.TABLE_FAVORITES + "("
                + DatabaseConstants.KEY_PLACE_ID + " INTEGER PRIMARY KEY "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void defineCategory(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + DatabaseConstants.TABLE_CATEGORY); // refresh table content
        db.execSQL("VACUUM");
        for (int i = 0; i < cat_id.length; i++) {
            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.KEY_CAT_ID, cat_id[i]);
            values.put(DatabaseConstants.KEY_CAT_NAME, cat_name[i]);
            values.put(DatabaseConstants.KEY_CAT_ICON, cat_icon.getResourceId(i, 0));
            db.insert(DatabaseConstants.TABLE_CATEGORY, null, values); // Inserting Row
        }
    }

    // Table Relational place_category
    private void createTableRelational(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DatabaseConstants.TABLE_PLACE_CATEGORY + "("
                + DatabaseConstants.KEY_RELATION_PLACE_ID + " INTEGER, "      // id from table place
                + DatabaseConstants.KEY_RELATION_CAT_ID + " INTEGER "        // id from table category
                + ")";
        db.execSQL(CREATE_TABLE);
    }


    private void createTableContentInfo(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DatabaseConstants.TABLE_NEWS_INFO+ " ("
                + DatabaseConstants.KEY_NEWS_ID+ " INTEGER PRIMARY KEY, "
                + DatabaseConstants.KEY_NEWS_TITLE+ " TEXT, "
                + DatabaseConstants.KEY_NEWS_BRIEF_CONTENT+ " TEXT, "
                + DatabaseConstants.KEY_NEWS_FULL_CONTENT+ " TEXT, "
                + DatabaseConstants.KEY_NEWS_IMAGE+ " TEXT, "
                + DatabaseConstants.KEY_NEWS_LAST_UPDATE+ " NUMERIC "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "DB, onUpgrade "+oldVersion+" to "+newVersion);
        if(oldVersion < newVersion) {
            // Drop older table if existed
            truncateDB(db);
        }
    }

    public void truncateDB(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_PLACE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_PLACE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_NEWS_INFO);

        // Create tables again
        onCreate(db);
    }

    // refresh table place and place_category
    public void refreshTablePlace(){
        db.execSQL("DELETE FROM " + DatabaseConstants.TABLE_PLACE_CATEGORY);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + DatabaseConstants.TABLE_IMAGES);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + DatabaseConstants.TABLE_PLACE);
        db.execSQL("VACUUM");
    }

    // refresh table place and place_category
    public void refreshTableContentInfo(){
        db.execSQL("DELETE FROM " + DatabaseConstants.TABLE_NEWS_INFO);
        db.execSQL("VACUUM");
    }


    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Insert List place
    public void insertListPlace(List<Place> modelList) {
        modelList = Tools.Companion.itemsWithDistance(context, modelList);
        for (Place p : modelList) {
            ContentValues values = getPlaceValue(p);
            // Inserting or Update Row
            db.insertWithOnConflict(DatabaseConstants.TABLE_PLACE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            // Insert relational place with category
            insertListPlaceCategory(p.getPlace_id(), p.getCategories());
            // Insert Images places
            insertListImages(p.getImages());
        }
    }

    // Insert List place
    public void insertListContentInfo(List<ContentInfo> modelList) {
        for (ContentInfo n : modelList) {
            ContentValues values = getContentInfoValue(n);
            // Inserting or Update Row
            db.insertWithOnConflict(DatabaseConstants.TABLE_NEWS_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Update one place
    public Place updatePlace(Place place) {
        List<Place> objcs = new ArrayList<>();
        objcs.add(place);
        insertListPlace(objcs);
        if(isPlaceExist(place.getPlace_id())){
            return getPlace(place.getPlace_id());
        }
        return null;
    }

    private ContentValues getPlaceValue(Place model){
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.KEY_PLACE_ID, model.getPlace_id());
        values.put(DatabaseConstants.KEY_NAME, model.getName());
        values.put(DatabaseConstants.KEY_IMAGE, model.getImage());
        values.put(DatabaseConstants.KEY_ADDRESS, model.getAddress());
        values.put(DatabaseConstants.KEY_PHONE, model.getPhone());
        values.put(DatabaseConstants.KEY_WEBSITE, model.getWebsite());
        values.put(DatabaseConstants.KEY_DESCRIPTION, model.getDescription());
        values.put(DatabaseConstants.KEY_LNG, model.getLng());
        values.put(DatabaseConstants.KEY_LAT, model.getLat());
        values.put(DatabaseConstants.KEY_DISTANCE, model.getDistance());
        values.put(DatabaseConstants.KEY_LAST_UPDATE, model.getLast_update());
        return values;
    }

    private ContentValues getContentInfoValue(ContentInfo model){
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.KEY_NEWS_ID, model.getId());
        values.put(DatabaseConstants.KEY_NEWS_TITLE, model.getTitle());
        values.put(DatabaseConstants.KEY_NEWS_BRIEF_CONTENT, model.getBrief_content());
        values.put(DatabaseConstants.KEY_NEWS_FULL_CONTENT, model.getFull_content());
        values.put(DatabaseConstants.KEY_NEWS_IMAGE, model.getImage());
        values.put(DatabaseConstants.KEY_LAST_UPDATE, model.getLast_update());
        return values;
    }

    // Adding new location by Category
    public List<Place> searchAllPlace(String keyword) {
        List<Place> locList = new ArrayList<>();
        Cursor cur;
        if (keyword.equals("")) {
            cur = db.rawQuery("SELECT p.* FROM "+ DatabaseConstants.TABLE_PLACE+" p ORDER BY " + DatabaseConstants.KEY_LAST_UPDATE + " DESC", null);
        } else {
            keyword = keyword.toLowerCase();
            cur = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_PLACE + " WHERE LOWER(" + DatabaseConstants.KEY_NAME + ") LIKE ? OR LOWER("+ DatabaseConstants.KEY_ADDRESS + ") LIKE ? OR LOWER("+ DatabaseConstants.KEY_DESCRIPTION + ") LIKE ? ",
                    new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"});
        }
        locList = getListPlaceByCursor(cur);
        return locList;
    }

    public List<Place> getAllPlace() {
        return getAllPlaceByCategory(-1);
    }

    public List<Place> getPlacesByPage(int c_id, int limit, int offset) {
        List<Place> locList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT DISTINCT p.* FROM "+ DatabaseConstants.TABLE_PLACE+" p ");
        if(c_id == -2) {
            sb.append(", "+ DatabaseConstants.TABLE_FAVORITES+" f ");
            sb.append(" WHERE p." + DatabaseConstants.KEY_PLACE_ID+ " = f." + DatabaseConstants.KEY_PLACE_ID+" ");
        } else if(c_id != -1){
            sb.append(", "+ DatabaseConstants.TABLE_PLACE_CATEGORY+" pc ");
            sb.append(" WHERE pc." + DatabaseConstants.KEY_RELATION_PLACE_ID+ " = p." + DatabaseConstants.KEY_PLACE_ID+ " AND pc." + DatabaseConstants.KEY_RELATION_CAT_ID+ "=" +c_id+ " ");
        }
        sb.append(" ORDER BY p."+ DatabaseConstants.KEY_DISTANCE+" ASC, p."+ DatabaseConstants.KEY_LAST_UPDATE+" DESC ");
        sb.append(" LIMIT "+limit+" OFFSET "+ offset+" ");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            locList = getListPlaceByCursor(cursor);
        }
        return locList;
    }

    public List<Place> getAllPlaceByCategory(int c_id) {
        List<Place> locList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT DISTINCT p.* FROM "+ DatabaseConstants.TABLE_PLACE+" p ");
        if(c_id == -2) {
            sb.append(", "+ DatabaseConstants.TABLE_FAVORITES+" f ");
            sb.append(" WHERE p." + DatabaseConstants.KEY_PLACE_ID+ " = f." + DatabaseConstants.KEY_PLACE_ID+" ");
        } else if(c_id != -1){
            sb.append(", "+ DatabaseConstants.TABLE_PLACE_CATEGORY+" pc ");
            sb.append(" WHERE pc." + DatabaseConstants.KEY_RELATION_PLACE_ID+ " = p." + DatabaseConstants.KEY_PLACE_ID+ " AND pc." + DatabaseConstants.KEY_RELATION_CAT_ID+ "=" +c_id+ " ");
        }
        sb.append(" ORDER BY p."+ DatabaseConstants.KEY_LAST_UPDATE+" DESC ");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            locList = getListPlaceByCursor(cursor);
        }
        return locList;
    }

    public Place getPlace(int place_id) {
        Place p = new Place();
        String query = "SELECT * FROM " + DatabaseConstants.TABLE_PLACE + " p WHERE p." + DatabaseConstants.KEY_PLACE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{place_id+""});
        p.setPlace_id(place_id);
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            p = getPlaceByCursor(cursor);
        }
        return p;
    }

    private List<Place> getListPlaceByCursor(Cursor cur) {
        List<Place> locList = new ArrayList<>();
        // looping through all rows and adding to list
        if (cur.moveToFirst()) {
            do {
                // Adding place to list
                locList.add(getPlaceByCursor(cur));
            } while (cur.moveToNext());
        }
        return locList;
    }

    private List<ContentInfo> getListContentInfoByCursor(Cursor cur) {
        List<ContentInfo> list = new ArrayList<>();
        // looping through all rows and adding to list
        if (cur.moveToFirst()) {
            do {
                // Adding place to list
                list.add(getContentInfoByCursor(cur));
            } while (cur.moveToNext());
        }
        return list;
    }

    private Place getPlaceByCursor(Cursor cur){
        Place p       = new Place();
        p.setPlace_id(cur.getInt(cur.getColumnIndex(DatabaseConstants.KEY_PLACE_ID)));
        p.setName(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_NAME)));
        p.setImage(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_IMAGE)));
        p.setAddress(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_ADDRESS)));
        p.setPhone(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_PHONE)));
        p.setWebsite(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_WEBSITE)));
        p.setDescription(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_DESCRIPTION)));
        p.setLng(cur.getDouble(cur.getColumnIndex(DatabaseConstants.KEY_LNG)));
        p.setLat(cur.getDouble(cur.getColumnIndex(DatabaseConstants.KEY_LAT)));
        p.setDistance(cur.getFloat(cur.getColumnIndex(DatabaseConstants.KEY_DISTANCE)));
        p.setLast_update(cur.getLong(cur.getColumnIndex(DatabaseConstants.KEY_LAST_UPDATE)));
        return p;
    }

    private ContentInfo getContentInfoByCursor(Cursor cur){
        ContentInfo n      = new ContentInfo();
        n.setId(cur.getInt(cur.getColumnIndex(DatabaseConstants.KEY_NEWS_ID)));
        n.setTitle(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_NEWS_TITLE)));
        n.setBrief_content(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_NEWS_BRIEF_CONTENT)));
        n.setFull_content(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_NEWS_FULL_CONTENT)));
        n.setImage(cur.getString(cur.getColumnIndex(DatabaseConstants.KEY_NEWS_IMAGE)));
        n.setLast_update(cur.getLong(cur.getColumnIndex(DatabaseConstants.KEY_NEWS_LAST_UPDATE)));
        return n;
    }

    // Get LIst Images By Place Id
    public List<Images> getListImageByPlaceId(int place_id) {
        List<Images> imageList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseConstants.TABLE_IMAGES + " WHERE " + DatabaseConstants.KEY_IMG_PLACE_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{place_id + ""});
        if (cursor.moveToFirst()) {
            do {
                Images img = new Images();
                img.setPlace_id(cursor.getInt(0));
                img.setName(cursor.getString(1));
                imageList.add(img);
            } while (cursor.moveToNext());
        }
        return imageList;
    }

    public Category getCategory(int c_id){
        Category category = new Category();
        try {
            Cursor cur = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_CATEGORY + " WHERE " + DatabaseConstants.KEY_CAT_ID + " = ?", new String[]{c_id + ""});
            cur.moveToFirst();
            category.setCat_id(cur.getInt(0));
            category.setName(cur.getString(1));
            category.setIcon(cur.getInt(2));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Db Error: " + e.toString());
            return null;
        }
        return category;
    }


    // get list News Info
    public List<ContentInfo> getContentInfoByPage(int limit, int offset) {

        Log.d(LOG_TAG,"DB, Size : " + getContentInfoSize());
        Log.d(LOG_TAG, "DB, Limit : " + limit + " Offset : " + offset);
        List<ContentInfo> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT DISTINCT n.* FROM "+ DatabaseConstants.TABLE_NEWS_INFO+" n ");
        sb.append(" ORDER BY n."+ DatabaseConstants.KEY_NEWS_ID+" DESC ");
        sb.append(" LIMIT "+limit+" OFFSET "+ offset+" ");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            list = getListContentInfoByCursor(cursor);
        }
        return list;
    }

    // Insert new imagesList
    public void insertListImages(List<Images> images) {
        for (int i = 0; i < images.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.KEY_IMG_PLACE_ID, images.get(i).getPlace_id());
            values.put(DatabaseConstants.KEY_IMG_NAME, images.get(i).getName());
            // Inserting or Update Row
            db.insertWithOnConflict(DatabaseConstants.TABLE_IMAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Inserting new Table PLACE_CATEGORY relational
    public void insertListPlaceCategory(int place_id, List<Category> categories) {
        for (Category c : categories) {
            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.KEY_RELATION_PLACE_ID, place_id);
            values.put(DatabaseConstants.KEY_RELATION_CAT_ID, c.getCat_id());
            // Inserting or Update Row
            db.insertWithOnConflict(DatabaseConstants.TABLE_PLACE_CATEGORY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Adding new Connector
    public void addFavorites(int id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.KEY_PLACE_ID, id);
        // Inserting Row
        db.insert(DatabaseConstants.TABLE_FAVORITES, null, values);
    }

    // all Favorites
    public List<Place> getAllFavorites() {
        List<Place> locList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT p.* FROM " + DatabaseConstants.TABLE_PLACE + " p, " + DatabaseConstants.TABLE_FAVORITES + " f" +" WHERE p." + DatabaseConstants.KEY_PLACE_ID + " = f." + DatabaseConstants.KEY_PLACE_ID, null);
        locList = getListPlaceByCursor(cursor);
        return locList;
    }

    public void deleteFavorites(int id) {
        if (isFavoritesExist(id)) {
            db.delete(DatabaseConstants.TABLE_FAVORITES, DatabaseConstants.KEY_PLACE_ID + " = ?", new String[]{id+""});
        }
    }

    public boolean isFavoritesExist(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_FAVORITES + " WHERE " + DatabaseConstants.KEY_PLACE_ID + " = ?", new String[]{id+""});
        int count = cursor.getCount();
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPlaceExist(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_PLACE + " WHERE " + DatabaseConstants.KEY_PLACE_ID + " = ?", new String[]{id + ""});
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getPlacesSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, DatabaseConstants.TABLE_PLACE);
        return count;
    }

    public int getContentInfoSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, DatabaseConstants.TABLE_NEWS_INFO);
        return count;
    }

    public int getPlacesSize(int c_id) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(DISTINCT p."+ DatabaseConstants.KEY_PLACE_ID+") FROM "+ DatabaseConstants.TABLE_PLACE+" p ");
        if(c_id == -2) {
            sb.append(", "+ DatabaseConstants.TABLE_FAVORITES+" f ");
            sb.append(" WHERE p." + DatabaseConstants.KEY_PLACE_ID+ " = f." + DatabaseConstants.KEY_PLACE_ID+" ");
        } else if(c_id != -1){
            sb.append(", "+ DatabaseConstants.TABLE_PLACE_CATEGORY+" pc ");
            sb.append(" WHERE pc." + DatabaseConstants.KEY_RELATION_PLACE_ID+ " = p." + DatabaseConstants.KEY_PLACE_ID+ " AND pc." + DatabaseConstants.KEY_RELATION_CAT_ID+ "=" +c_id+ " ");
        }
        Cursor cursor = db.rawQuery(sb.toString(), null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        cursor.close();
        return size;
    }

    public int getCategorySize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, DatabaseConstants.TABLE_CATEGORY);
        return count;
    }

    public int getFavoritesSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, DatabaseConstants.TABLE_FAVORITES);
        return count;
    }

    public int getImagesSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, DatabaseConstants.TABLE_IMAGES);
        return count;
    }

    public int getPlaceCategorySize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, DatabaseConstants.TABLE_PLACE_CATEGORY);
        return count;
    }

    // to export database file
    // for debugging only
    private void exportDatabase(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/"+ DatabaseConstants.DATABASE_NAME;
                String backupDBPath = "backup_"+ DatabaseConstants.DATABASE_NAME+".db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

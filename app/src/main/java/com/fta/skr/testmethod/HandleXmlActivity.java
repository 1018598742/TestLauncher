package com.fta.skr.testmethod;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HandleXmlActivity extends AppCompatActivity {

    private static final String TAG = "My_Test";
    private Context mContext;

    private Resources mSourceRes;

    private PackageManager mPackageManager;

    private static final String TAG_INCLUDE = "include";
    private static final String TAG_WORKSPACE = "workspace";
    private static final String TAG_APP_ICON = "appicon";
    private static final String TAG_AUTO_INSTALL = "autoinstall";
    private static final String TAG_FOLDER = "folder";
    private static final String TAG_APPWIDGET = "appwidget";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String TAG_EXTRA = "extra";

    private static final String ATTR_CONTAINER = "container";
    private static final String ATTR_RANK = "rank";

    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_CLASS_NAME = "className";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_SCREEN = "screen";

    // x and y can be specified as negative integers, in which case -1 represents the
    // last row / column, -2 represents the second last, and so on.
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";

    private static final String ATTR_SPAN_X = "spanX";
    private static final String ATTR_SPAN_Y = "spanY";
    private static final String ATTR_ICON = "icon";
    private static final String ATTR_URL = "url";

    // Attrs for "Include"
    private static final String ATTR_WORKSPACE = "workspace";

    // Style attrs -- "Extra"
    private static final String ATTR_KEY = "key";
    private static final String ATTR_VALUE = "value";


    protected static final String TAG_RESOLVE = "resolve";
    private static final String TAG_FAVORITES = "favorites";
    protected static final String TAG_FAVORITE = "favorite";
    private static final String TAG_PARTNER_FOLDER = "partner-folder";

    protected static final String ATTR_URI = "uri";
    private static final String ATTR_FOLDER_ITEMS = "folderItems";

    // TODO: Remove support for this broadcast, instead use widget options to send bind time options
    private static final String ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE =
            "com.android.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handlexml);
        mContext = this;

        mSourceRes = mContext.getResources();

        mPackageManager = mContext.getPackageManager();
    }

    public void handleXmlAC(View view) {
        parseLayout();
        Log.i(TAG, "HandleXmlActivity-handleXmlAC: parseLayout end");
    }

    private void parseLayout() {
        try {
            int count = parseLayout(R.xml.default_workspace_4x4, new ArrayList<Long>());
            Log.i(TAG, "HandleXmlActivity-parseLayout: count is " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected int parseLayout(int layoutId, ArrayList<Long> screenIds)
            throws XmlPullParserException, IOException {
        if (layoutId == R.xml.dw_phone_hotseat) {
            Log.i(TAG, "HandleXmlActivity-parseLayout: is hotseat");
        } else if (layoutId == R.xml.default_workspace_4x4) {
            Log.i(TAG, "HandleXmlActivity-parseLayout: is default_workspace_4x4");
        } else {
            Log.i(TAG, "HandleXmlActivity-parseLayout: is other");
        }
        XmlResourceParser parser = mSourceRes.getXml(layoutId);
        //判断与设定的初始化节点是否相同(初始化位置)
        beginDocument(parser, "favorites");
        final int depth = parser.getDepth();
        Log.i(TAG, "HandleXmlActivity-parseLayout: depth=" + depth);
        int type;
        ArrayMap<String, TagParser> tagParserMap = getLayoutElementsMap();
        int count = 0;

        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.i(TAG, "HandleXmlActivity-parseLayout: parse name is=" + name);
            count += parseAndAddNode(parser, tagParserMap, screenIds);
        }
        return count;
    }

    private final long[] mTemp = new long[2];

    /**
     * Parses the current node and returns the number of elements added.
     */
    protected int parseAndAddNode(XmlResourceParser parser, ArrayMap<String, TagParser> tagParserMap, ArrayList<Long> screenIds) throws XmlPullParserException, IOException {

        if (TAG_INCLUDE.equals(parser.getName())) {
            Log.i(TAG, "HandleXmlActivity-parseAndAddNode: is include");
            final int resId = getAttributeResourceValue(parser, ATTR_WORKSPACE, 0);
            if (resId != 0) {
                // recursively load some more favorites, why not?
                return parseLayout(resId, screenIds);
            } else {
                return 0;
            }
        }

//        mValues.clear();
        parseContainerAndScreen(parser, mTemp);
        final long container = mTemp[0];
        final long screenId = mTemp[1];

//        mValues.put(Favorites.CONTAINER, container);
//        mValues.put(Favorites.SCREEN, screenId);
//
//        mValues.put(Favorites.CELLX,
//                convertToDistanceFromEnd(getAttributeValue(parser, ATTR_X), mColumnCount));
//        mValues.put(Favorites.CELLY,
//                convertToDistanceFromEnd(getAttributeValue(parser, ATTR_Y), mRowCount));

        TagParser tagParser = tagParserMap.get(parser.getName());
        if (tagParser == null) {
//            if (LOGD) Log.d(TAG, "Ignoring unknown element tag: " + parser.getName());
            return 0;
        }
        long newElementId = tagParser.parseAndAdd(parser);
        if (newElementId >= 0) {
            // Keep track of the set of screens which need to be added to the db.
            if (!screenIds.contains(screenId) &&
                    container == Favorites.CONTAINER_DESKTOP) {
                screenIds.add(screenId);
            }
            return 1;
        }
        return 0;
    }

    private static final String HOTSEAT_CONTAINER_NAME =
            Favorites.containerToString(Favorites.CONTAINER_HOTSEAT);

    protected void parseContainerAndScreen(XmlResourceParser parser, long[] out) {
        String name = parser.getName();
        Log.i(TAG, "HandleXmlActivity-parseContainerAndScreen: name=" + name);
        out[0] = -100;
        String strContainer = getAttributeValue(parser, ATTR_CONTAINER);
        if (strContainer != null) {
            out[0] = Long.valueOf(strContainer);
        }
        out[1] = Long.parseLong(getAttributeValue(parser, ATTR_SCREEN));
    }

    protected static void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) ;
        Log.i(TAG, "HandleXmlActivity-beginDocument: type=" + type);

        if (type != XmlPullParser.START_TAG) {//2
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public void knownXmlAC(View view) throws IOException, XmlPullParserException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try (XmlResourceParser parser = mSourceRes.getXml(R.xml.dw_phone_hotseat)) {
                int depth = parser.getDepth();
                Log.i(TAG, "HandleXmlActivity-knownXmlAC: depth is " + depth);
                int type;
                /**
                 * START_DOCUMENT 0
                 * END_DOCUMENT 1
                 * START_TAG 2
                 * END_TAG 3
                 * TEXT 4
                 * CDSECT 5
                 * ENTITY_REF 6
                 */
                while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                    //当前节点深度
                    //favorites 深度为1
                    //resolve 深度为2
                    //favorite 深度为3
                    int depth1 = parser.getDepth();
                    String name = parser.getName();
                    Log.i(TAG, "HandleXmlActivity-knownXmlAC: type is " + type);
                    Log.i(TAG, "HandleXmlActivity-knownXmlAC: depth1 is " + depth1);
                    Log.i(TAG, "HandleXmlActivity-knownXmlAC: name is " + name);
                }
            } catch (IOException | XmlPullParserException e) {
                throw new RuntimeException();
            }
        }


    }

    protected interface TagParser {
        /**
         * Parses the tag and adds to the db
         *
         * @return the id of the row added or -1;
         */
        long parseAndAdd(XmlResourceParser parser)
                throws XmlPullParserException, IOException;
    }

    protected ArrayMap<String, TagParser> getLayoutElementsMap() {
        ArrayMap<String, TagParser> parsers = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            parsers = new ArrayMap<>();
            parsers.put(TAG_FAVORITE, new AppShortcutWithUriParser());
            parsers.put(TAG_APPWIDGET, new AppWidgetParser());
            parsers.put(TAG_SHORTCUT, new UriShortcutParser(mSourceRes));
            parsers.put(TAG_RESOLVE, new ResolveParser());
            parsers.put(TAG_FOLDER, new MyFolderParser());
            parsers.put(TAG_PARTNER_FOLDER, new PartnerFolderParser());
        }
        return parsers;
    }

    protected static String getAttributeValue(XmlResourceParser parser, String attribute) {
        String value = parser.getAttributeValue(
                "http://schemas.android.com/apk/res-auto/com.android.launcher3", attribute);
        if (value == null) {
            value = parser.getAttributeValue(null, attribute);
        }
        return value;
    }


    protected long addShortcut(String title, Intent intent, int type) {
        //保存到数据库中
        Log.i(TAG, "HandleXmlActivity-addShortcut: title=" + title);
        Log.i(TAG, "HandleXmlActivity-addShortcut: intent=" + intent.toUri(0));
        return 0;
    }


    /**
     * App shortcuts: required attributes packageName and className
     */
    protected class AppShortcutParser implements TagParser {

        @Override
        public long parseAndAdd(XmlResourceParser parser) {
            final String packageName = getAttributeValue(parser, ATTR_PACKAGE_NAME);
            final String className = getAttributeValue(parser, ATTR_CLASS_NAME);

            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
                ActivityInfo info;
                try {
                    ComponentName cn;
                    try {
                        cn = new ComponentName(packageName, className);
                        info = mPackageManager.getActivityInfo(cn, 0);
                    } catch (PackageManager.NameNotFoundException nnfe) {
                        String[] packages = mPackageManager.currentToCanonicalPackageNames(
                                new String[]{packageName});
                        cn = new ComponentName(packages[0], className);
                        info = mPackageManager.getActivityInfo(cn, 0);
                    }
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setComponent(cn)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    return addShortcut(info.loadLabel(mPackageManager).toString(),
                            intent, Favorites.ITEM_TYPE_APPLICATION);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Favorite not found: " + packageName + "/" + className);
                }
                return -1;
            } else {
                return invalidPackageOrClass(parser);
            }
        }

        /**
         * Helper method to allow extending the parser capabilities
         */
        protected long invalidPackageOrClass(XmlResourceParser parser) {
            Log.w(TAG, "Skipping invalid <favorite> with no component");
            return -1;
        }
    }

    /**
     * AutoInstall: required attributes packageName and className
     */
    protected class AutoInstallParser implements TagParser {

        @Override
        public long parseAndAdd(XmlResourceParser parser) {
            final String packageName = getAttributeValue(parser, ATTR_PACKAGE_NAME);
            final String className = getAttributeValue(parser, ATTR_CLASS_NAME);
            if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
//                if (LOGD) Log.d(TAG, "Skipping invalid <favorite> with no component");
                return -1;
            }

//            mValues.put(Favorites.RESTORED, ShortcutInfo.FLAG_AUTOINSTALL_ICON);
            final Intent intent = new Intent(Intent.ACTION_MAIN, null)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setComponent(new ComponentName(packageName, className))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            return addShortcut(mContext.getString(R.string.package_state_unknown), intent,
                    Favorites.ITEM_TYPE_APPLICATION);
        }
    }

    /**
     * Parses a web shortcut. Required attributes url, icon, title
     */
    protected class ShortcutParser implements TagParser {

        private final Resources mIconRes;

        public ShortcutParser(Resources iconRes) {
            mIconRes = iconRes;
        }

        @Override
        public long parseAndAdd(XmlResourceParser parser) {
            final int titleResId = getAttributeResourceValue(parser, ATTR_TITLE, 0);
            final int iconId = getAttributeResourceValue(parser, ATTR_ICON, 0);

            if (titleResId == 0 || iconId == 0) {
//                if (LOGD) Log.d(TAG, "Ignoring shortcut");
                return -1;
            }

            final Intent intent = parseIntent(parser);
            if (intent == null) {
                return -1;
            }

            Drawable icon = mIconRes.getDrawable(iconId);
            if (icon == null) {
//                if (LOGD) Log.d(TAG, "Ignoring shortcut, can't load icon");
                return -1;
            }

            // Auto installs should always support the current platform version.
//            LauncherIcons li = LauncherIcons.obtain(mContext);
//            mValues.put(LauncherSettings.Favorites.ICON, Utilities.flattenBitmap(
//                    li.createBadgedIconBitmap(icon, Process.myUserHandle(), VERSION.SDK_INT).icon));
//            li.recycle();
//
//            mValues.put(Favorites.ICON_PACKAGE, mIconRes.getResourcePackageName(iconId));
//            mValues.put(Favorites.ICON_RESOURCE, mIconRes.getResourceName(iconId));

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            return addShortcut(mSourceRes.getString(titleResId),
                    intent, Favorites.ITEM_TYPE_SHORTCUT);
        }

        protected Intent parseIntent(XmlResourceParser parser) {
            final String url = getAttributeValue(parser, ATTR_URL);
            if (TextUtils.isEmpty(url) || !Patterns.WEB_URL.matcher(url).matches()) {
//                if (LOGD) Log.d(TAG, "Ignoring shortcut, invalid url: " + url);
                return null;
            }
            return new Intent(Intent.ACTION_VIEW, null).setData(Uri.parse(url));
        }
    }

    /**
     * AppWidget parser: Required attributes packageName, className, spanX and spanY.
     * Options child nodes: <extra key=... value=... />
     * It adds a pending widget which allows the widget to come later. If there are extras, those
     * are passed to widget options during bind.
     * The config activity for the widget (if present) is not shown, so any optional configurations
     * should be passed as extras and the widget should support reading these widget options.
     */
    protected class PendingWidgetParser implements TagParser {

        @Override
        public long parseAndAdd(XmlResourceParser parser)
                throws XmlPullParserException, IOException {
            final String packageName = getAttributeValue(parser, ATTR_PACKAGE_NAME);
            final String className = getAttributeValue(parser, ATTR_CLASS_NAME);
            if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
//                if (LOGD) Log.d(TAG, "Skipping invalid <appwidget> with no component");
                return -1;
            }

            //ContentValues
//            mValues.put(Favorites.SPANX, getAttributeValue(parser, ATTR_SPAN_X));
//            mValues.put(Favorites.SPANY, getAttributeValue(parser, ATTR_SPAN_Y));
//            mValues.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);

            // Read the extras
            Bundle extras = new Bundle();
            int widgetDepth = parser.getDepth();
            int type;
            while ((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > widgetDepth) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                if (TAG_EXTRA.equals(parser.getName())) {
                    String key = getAttributeValue(parser, ATTR_KEY);
                    String value = getAttributeValue(parser, ATTR_VALUE);
                    if (key != null && value != null) {
                        extras.putString(key, value);
                    } else {
                        throw new RuntimeException("Widget extras must have a key and value");
                    }
                } else {
                    throw new RuntimeException("Widgets can contain only extras");
                }
            }

            return verifyAndInsert(new ComponentName(packageName, className), extras);
        }

        protected long verifyAndInsert(ComponentName cn, Bundle extras) {
//            mValues.put(Favorites.APPWIDGET_PROVIDER, cn.flattenToString());
//            mValues.put(Favorites.RESTORED,
//                    LauncherAppWidgetInfo.FLAG_ID_NOT_VALID |
//                            LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY |
//                            LauncherAppWidgetInfo.FLAG_DIRECT_CONFIG);
//            mValues.put(Favorites._ID, mCallback.generateNewItemId());
//            if (!extras.isEmpty()) {
//                mValues.put(Favorites.INTENT, new Intent().putExtras(extras).toUri(0));
//            }
//
//            long insertedId = mCallback.insertAndCheck(mDb, mValues);
//            if (insertedId < 0) {
//                return -1;
//            } else {
//                return insertedId;
//            }
            return -1;
        }

    }

    protected static int getAttributeResourceValue(XmlResourceParser parser, String attribute,
                                                   int defaultValue) {
        int value = parser.getAttributeResourceValue(
                "http://schemas.android.com/apk/res-auto/com.android.launcher3", attribute,
                defaultValue);
        if (value == defaultValue) {
            value = parser.getAttributeResourceValue(null, attribute, defaultValue);
        }
        return value;
    }

    protected ArrayMap<String, TagParser> getFolderElementsMap() {
        ArrayMap<String, TagParser> parsers = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            parsers = new ArrayMap<>();
            parsers.put(TAG_APP_ICON, new AppShortcutParser());
            parsers.put(TAG_AUTO_INSTALL, new AutoInstallParser());
            parsers.put(TAG_SHORTCUT, new ShortcutParser(mSourceRes));
        }

        return parsers;

    }

    ArrayMap<String, TagParser> getFolderElementsMap(Resources res) {
        ArrayMap<String, TagParser> parsers = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            parsers = new ArrayMap<>();
            parsers.put(TAG_FAVORITE, new AppShortcutWithUriParser());
            parsers.put(TAG_SHORTCUT, new UriShortcutParser(res));
        }
        return parsers;
    }

    protected class FolderParser implements TagParser {
        private final ArrayMap<String, TagParser> mFolderElements;

        public FolderParser() {
            this(getFolderElementsMap());
        }

        public FolderParser(ArrayMap<String, TagParser> elements) {
            mFolderElements = elements;
        }

        @Override
        public long parseAndAdd(XmlResourceParser parser)
                throws XmlPullParserException, IOException {
            final String title;
            final int titleResId = getAttributeResourceValue(parser, ATTR_TITLE, 0);
            if (titleResId != 0) {
                title = mSourceRes.getString(titleResId);
            } else {
                title = mContext.getResources().getString(R.string.folder_name);
            }

            //ContentValues
//            mValues.put(Favorites.TITLE, title);
//            mValues.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_FOLDER);
//            mValues.put(Favorites.SPANX, 1);
//            mValues.put(Favorites.SPANY, 1);
//            mValues.put(Favorites._ID, mCallback.generateNewItemId());
//            long folderId = mCallback.insertAndCheck(mDb, mValues);
//            if (folderId < 0) {
//                if (LOGD) Log.e(TAG, "Unable to add folder");
//                return -1;
//            }

//            final ContentValues myValues = new ContentValues(mValues);
            ArrayList<Long> folderItems = new ArrayList<>();

            int type;
            int folderDepth = parser.getDepth();
            int rank = 0;
            while ((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > folderDepth) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                //ContentValues
//                mValues.clear();
//                mValues.put(Favorites.CONTAINER, folderId);
//                mValues.put(Favorites.RANK, rank);

                TagParser tagParser = mFolderElements.get(parser.getName());
                if (tagParser != null) {
                    final long id = tagParser.parseAndAdd(parser);
                    if (id >= 0) {
                        folderItems.add(id);
                        rank++;
                    }
                } else {
                    throw new RuntimeException("Invalid folder item " + parser.getName());
                }
            }

//            long addedId = folderId;

            // We can only have folders with >= 2 items, so we need to remove the
            // folder and clean up if less than 2 items were included, or some
            // failed to add, and less than 2 were actually added
//            if (folderItems.size() < 2) {
            // Delete the folder
//                Uri uri = Favorites.getContentUri(folderId);
//                SqlArguments args = new SqlArguments(uri, null, null);
            //数据库删除
//                mDb.delete(args.table, args.where, args.args);
//                addedId = -1;

            // If we have a single item, promote it to where the folder
            // would have been.
//                if (folderItems.size() == 1) {
//                    final ContentValues childValues = new ContentValues();
//                    copyInteger(myValues, childValues, Favorites.CONTAINER);
//                    copyInteger(myValues, childValues, Favorites.SCREEN);
//                    copyInteger(myValues, childValues, Favorites.CELLX);
//                    copyInteger(myValues, childValues, Favorites.CELLY);
//
//                    addedId = folderItems.get(0);
//                    //保存到数据库
////                    mDb.update(Favorites.TABLE_NAME, childValues,
////                            Favorites._ID + "=" + addedId, null);
//                }
//            }
//            return addedId;
            return -1;
        }
    }


    static void copyInteger(ContentValues from, ContentValues to, String key) {
        to.put(key, from.getAsInteger(key));
    }

    /**
     * Favorites.
     */
    public static final class Favorites implements BaseLauncherColumns {

        public static final String TABLE_NAME = "favorites";

        /**
         * The content:// style URL for this table
         */
//        public static final Uri CONTENT_URI = Uri.parse("content://" +
//                LauncherProvider.AUTHORITY + "/" + TABLE_NAME);

        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id The row id.
         *
         * @return The unique content URL for the specified row.
         */
//        public static Uri getContentUri(long id) {
//            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
//                    "/" + TABLE_NAME + "/" + id);
//        }

        /**
         * The container holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String CONTAINER = "container";

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        public static final int CONTAINER_DESKTOP = -100;
        public static final int CONTAINER_HOTSEAT = -101;

        static final String containerToString(int container) {
            switch (container) {
                case CONTAINER_DESKTOP:
                    return "desktop";
                case CONTAINER_HOTSEAT:
                    return "hotseat";
                default:
                    return String.valueOf(container);
            }
        }

        static final String itemTypeToString(int type) {
            switch (type) {
                case ITEM_TYPE_APPLICATION:
                    return "APP";
                case ITEM_TYPE_SHORTCUT:
                    return "SHORTCUT";
                case ITEM_TYPE_FOLDER:
                    return "FOLDER";
                case ITEM_TYPE_APPWIDGET:
                    return "WIDGET";
                case ITEM_TYPE_CUSTOM_APPWIDGET:
                    return "CUSTOMWIDGET";
                case ITEM_TYPE_DEEP_SHORTCUT:
                    return "DEEPSHORTCUT";
                default:
                    return String.valueOf(type);
            }
        }

        /**
         * The screen holding the favorite (if container is CONTAINER_DESKTOP)
         * <P>Type: INTEGER</P>
         */
        public static final String SCREEN = "screen";

        /**
         * The X coordinate of the cell holding the favorite
         * (if container is CONTAINER_HOTSEAT or CONTAINER_HOTSEAT)
         * 持有收藏夹的单元格的X坐标
         * <P>Type: INTEGER</P>
         */
        public static final String CELLX = "cellX";

        /**
         * The Y coordinate of the cell holding the favorite
         * (if container is CONTAINER_DESKTOP)
         * <P>Type: INTEGER</P>
         */
        public static final String CELLY = "cellY";

        /**
         * The X span of the cell holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String SPANX = "spanX";

        /**
         * The Y span of the cell holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String SPANY = "spanY";

        /**
         * The profile id of the item in the cell.
         * 单元格中项目的配置文件ID
         * <p>
         * Type: INTEGER
         * </P>
         */
        public static final String PROFILE_ID = "profileId";

        /**
         * The favorite is a user created folder
         */
        public static final int ITEM_TYPE_FOLDER = 2;

        /**
         * The favorite is a widget
         */
        public static final int ITEM_TYPE_APPWIDGET = 4;

        /**
         * The favorite is a custom widget provided by the launcher
         */
        public static final int ITEM_TYPE_CUSTOM_APPWIDGET = 5;

        /**
         * The gesture is an application created deep shortcut
         */
        public static final int ITEM_TYPE_DEEP_SHORTCUT = 6;

        /**
         * The appWidgetId of the widget
         * <p>
         * <P>Type: INTEGER</P>
         */
        public static final String APPWIDGET_ID = "appWidgetId";

        /**
         * The ComponentName of the widget provider
         * <p>
         * <P>Type: STRING</P>
         */
        public static final String APPWIDGET_PROVIDER = "appWidgetProvider";

        /**
         * Boolean indicating that his item was restored and not yet successfully bound.
         * <P>Type: INTEGER</P>
         */
        public static final String RESTORED = "restored";

        /**
         * Indicates the position of the item inside an auto-arranged view like folder or hotseat.
         * <p>Type: INTEGER</p>
         */
        public static final String RANK = "rank";

        /**
         * Stores general flag based options for
         * <p>Type: INTEGER</p>
         */
        public static final String OPTIONS = "options";

        public static void addTableToDb(SQLiteDatabase db, long myProfileId, boolean optional) {
            String ifNotExists = optional ? " IF NOT EXISTS " : "";
            db.execSQL("CREATE TABLE " + ifNotExists + TABLE_NAME + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "itemType INTEGER," +
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "appWidgetProvider TEXT," +
                    "modified INTEGER NOT NULL DEFAULT 0," +
                    "restored INTEGER NOT NULL DEFAULT 0," +
                    "profileId INTEGER DEFAULT " + myProfileId + "," +
                    "rank INTEGER NOT NULL DEFAULT 0," +
                    "options INTEGER NOT NULL DEFAULT 0" +
                    ");");
        }
    }

    static public interface BaseLauncherColumns extends ChangeLogColumns {
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to {@link android.content.Intent#parseUri(String, int)} to create
         * an Intent that can be launched.
         * <P>Type: TEXT</P>
         */
        public static final String INTENT = "intent";

        /**
         * The type of the gesture
         * 手势类型
         * <p>
         * <P>Type: INTEGER</P>
         */
        public static final String ITEM_TYPE = "itemType";

        /**
         * The gesture is an application
         */
        public static final int ITEM_TYPE_APPLICATION = 0;

        /**
         * The gesture is an application created shortcut
         */
        public static final int ITEM_TYPE_SHORTCUT = 1;

        /**
         * The icon package name in Intent.ShortcutIconResource
         * <P>Type: TEXT</P>
         */
        public static final String ICON_PACKAGE = "iconPackage";

        /**
         * The icon resource name in Intent.ShortcutIconResource
         * <P>Type: TEXT</P>
         */
        public static final String ICON_RESOURCE = "iconResource";

        /**
         * The custom icon bitmap.
         * <P>Type: BLOB</P>
         */
        public static final String ICON = "icon";
    }

    static interface ChangeLogColumns extends BaseColumns {
        /**
         * The time of the last update to this row.
         * <P>Type: INTEGER</P>
         */
        public static final String MODIFIED = "modified";
    }


    /**
     * AppShortcutParser which also supports adding URI based intents
     */
    public class AppShortcutWithUriParser extends AppShortcutParser {

        @Override
        protected long invalidPackageOrClass(XmlResourceParser parser) {
            final String uri = getAttributeValue(parser, ATTR_URI);
            if (TextUtils.isEmpty(uri)) {
                Log.e(TAG, "Skipping invalid <favorite> with no component or uri");
                return -1;
            }

            final Intent metaIntent;
            try {
                metaIntent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                Log.e(TAG, "Unable to add meta-favorite: " + uri, e);
                return -1;
            }

            ResolveInfo resolved = mPackageManager.resolveActivity(metaIntent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            final List<ResolveInfo> appList = mPackageManager.queryIntentActivities(
                    metaIntent, PackageManager.MATCH_DEFAULT_ONLY);

            // Verify that the result is an app and not just the resolver dialog asking which
            // app to use.
            if (wouldLaunchResolverActivity(resolved, appList)) {
                // If only one of the results is a system app then choose that as the default.
                final ResolveInfo systemApp = getSingleSystemActivity(appList);
                if (systemApp == null) {
                    // There is no logical choice for this meta-favorite, so rather than making
                    // a bad choice just add nothing.
                    Log.w(TAG, "No preference or single system activity found for "
                            + metaIntent.toString());
                    return -1;
                }
                resolved = systemApp;
            }
            final ActivityInfo info = resolved.activityInfo;
            String packageName = info.packageName;
            Log.i(TAG, "AppShortcutWithUriParser-invalidPackageOrClass: packageName="+packageName);
            final Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                return -1;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            String appName = info.loadLabel(mPackageManager).toString();
            Log.i(TAG, "AppShortcutWithUriParser-invalidPackageOrClass: uri is " + uri + " =appName=" + appName);
            return addShortcut(appName, intent,
                    Favorites.ITEM_TYPE_APPLICATION);
        }

        private ResolveInfo getSingleSystemActivity(List<ResolveInfo> appList) {
            ResolveInfo systemResolve = null;
            final int N = appList.size();
            for (int i = 0; i < N; ++i) {
                try {
                    ApplicationInfo info = mPackageManager.getApplicationInfo(
                            appList.get(i).activityInfo.packageName, 0);
                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        if (systemResolve != null) {
                            return null;
                        } else {
                            systemResolve = appList.get(i);
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Unable to get info about resolve results", e);
                    return null;
                }
            }
            return systemResolve;
        }

        private boolean wouldLaunchResolverActivity(ResolveInfo resolved,
                                                    List<ResolveInfo> appList) {
            // If the list contains the above resolved activity, then it can't be
            // ResolverActivity itself.
            for (int i = 0; i < appList.size(); ++i) {
                ResolveInfo tmp = appList.get(i);
                if (tmp.activityInfo.name.equals(resolved.activityInfo.name)
                        && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                    return false;
                }
            }
            return true;
        }
    }


    /**
     * Shortcut parser which allows any uri and not just web urls.
     */
    public class UriShortcutParser extends ShortcutParser {

        public UriShortcutParser(Resources iconRes) {
            super(iconRes);
        }

        @Override
        protected Intent parseIntent(XmlResourceParser parser) {
            String uri = null;
            try {
                uri = getAttributeValue(parser, ATTR_URI);
                return Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                Log.w(TAG, "Shortcut has malformed uri: " + uri);
                return null; // Oh well
            }
        }
    }

    /**
     * Contains a list of <favorite> nodes, and accepts the first successfully parsed node.
     */
    public class ResolveParser implements TagParser {

        private final AppShortcutWithUriParser mChildParser = new AppShortcutWithUriParser();

        @Override
        public long parseAndAdd(XmlResourceParser parser) throws XmlPullParserException,
                IOException {
            final int groupDepth = parser.getDepth();
            String name = parser.getName();
            Log.i(TAG, "ResolveParser-parseAndAdd: name is " + name);
            int type;
            long addedId = -1;
            while ((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > groupDepth) {
                if (type != XmlPullParser.START_TAG || addedId > -1) {
                    continue;
                }
                final String fallback_item_name = parser.getName();
                Log.i(TAG, "ResolveParser-parseAndAdd: fallback_item_name=" + fallback_item_name);
                if (TAG_FAVORITE.equals(fallback_item_name)) {
                    addedId = mChildParser.parseAndAdd(parser);
                } else {
                    Log.e(TAG, "Fallback groups can contain only favorites, found "
                            + fallback_item_name);
                }
            }
            return addedId;
        }
    }

    /**
     * A parser which adds a folder whose contents come from partner apk.
     */
    class PartnerFolderParser implements TagParser {

        @Override
        public long parseAndAdd(XmlResourceParser parser) throws XmlPullParserException,
                IOException {
            // Folder contents come from an external XML resource
            final Partner partner = Partner.get(mPackageManager);
            if (partner != null) {
                final Resources partnerRes = partner.getResources();
                final int resId = partnerRes.getIdentifier(Partner.RES_FOLDER,
                        "xml", partner.getPackageName());
                if (resId != 0) {
                    final XmlResourceParser partnerParser = partnerRes.getXml(resId);
                    beginDocument(partnerParser, TAG_FOLDER);

                    FolderParser folderParser = new FolderParser(getFolderElementsMap(partnerRes));
                    return folderParser.parseAndAdd(partnerParser);
                }
            }
            return -1;
        }
    }

    /**
     * An extension of FolderParser which allows adding items from a different xml.
     */
    class MyFolderParser extends FolderParser {

        @Override
        public long parseAndAdd(XmlResourceParser parser) throws XmlPullParserException,
                IOException {
            final int resId = getAttributeResourceValue(parser, ATTR_FOLDER_ITEMS, 0);
            if (resId != 0) {
                parser = mSourceRes.getXml(resId);
                beginDocument(parser, TAG_FOLDER);
            }
            return super.parseAndAdd(parser);
        }
    }


    /**
     * AppWidget parser which enforces that the app is already installed when the layout is parsed.
     */
    protected class AppWidgetParser extends PendingWidgetParser {

        @Override
        protected long verifyAndInsert(ComponentName cn, Bundle extras) {
            try {
                mPackageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                String[] packages = mPackageManager.currentToCanonicalPackageNames(
                        new String[]{cn.getPackageName()});
                cn = new ComponentName(packages[0], cn.getClassName());
                try {
                    mPackageManager.getReceiverInfo(cn, 0);
                } catch (Exception e1) {
                    Log.d(TAG, "Can't find widget provider: " + cn.getClassName());
                    return -1;
                }
            }

            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            long insertedId = -1;
            try {
//                int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
//
//                if (!appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn)) {
//                    Log.e(TAG, "Unable to bind app widget id " + cn);
//                    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
//                    return -1;
//                }
//
//                mValues.put(Favorites.APPWIDGET_ID, appWidgetId);
//                mValues.put(Favorites.APPWIDGET_PROVIDER, cn.flattenToString());
//                mValues.put(Favorites._ID, mCallback.generateNewItemId());
//                insertedId = mCallback.insertAndCheck(mDb, mValues);
//                if (insertedId < 0) {
//                    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
//                    return insertedId;
//                }
//
//                // Send a broadcast to configure the widget
//                if (!extras.isEmpty()) {
//                    Intent intent = new Intent(ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE);
//                    intent.setComponent(cn);
//                    intent.putExtras(extras);
//                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//                    mContext.sendBroadcast(intent);
//                }
            } catch (RuntimeException ex) {
                Log.e(TAG, "Problem allocating appWidgetId", ex);
            }
            return insertedId;
        }
    }

}

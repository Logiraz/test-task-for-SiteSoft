package ru.nikitadmitriev13.test1;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    final String LOG_TAG = "myLogs";
    int numofrecords[] = new int [2];
    String[] dbnames = new String[2];
    String[] dbtnames = new String[2];
    String current = "";
    int currpage = 0;

    ListView mainList;
    ListView secondList;
    TextView descriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainList = (ListView) findViewById(R.id.mainList);
        secondList = (ListView) findViewById(R.id.secondList);
        descriptionText = (TextView) findViewById(R.id.descriptionText);
        descriptionText.setMovementMethod(new ScrollingMovementMethod());

        dbnames[0] = getString(R.string.enterprisedbname);
        dbnames[1] = getString(R.string.newsdbname);
        dbtnames[0] = getString(R.string.enterprisedbtablename);
        dbtnames[1] = getString(R.string.newsdbtablename);
        final String[][] enterpriseArray = new String[2][];
        final String[][] titleArray = new String[2][];
        final String[][] descriptionArray = new String[2][];

        for (int i = 0; i < 2; i++){
            DatabaseHelper dbh = new DatabaseHelper(this, dbnames[i]);
            SQLiteDatabase sqdb = dbh.getWritableDatabase();
            Cursor c = sqdb.query(dbtnames[i], null, null, null, null, null, null);
            numofrecords[i] = c.getCount();
            enterpriseArray[i] = new String[numofrecords[i]];
            titleArray[i] = new String[numofrecords[i]];
            descriptionArray[i] = new String[numofrecords[i]];
            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex(getString(R.string.tableraw0));
                int entColIndex = c.getColumnIndex(getString(R.string.tableraw1));
                int titleColIndex = c.getColumnIndex(getString(R.string.tableraw2));
                int descColIndex = c.getColumnIndex(getString(R.string.tableraw3));
                do {
                    enterpriseArray[i][c.getInt(idColIndex)-1] = c.getString(entColIndex);
                    titleArray[i][c.getInt(idColIndex)-1] = c.getString(titleColIndex);
                    descriptionArray[i][c.getInt(idColIndex)-1] = c.getString(descColIndex);
                } while (c.moveToNext());
            } else
                Log.d(LOG_TAG, getString(R.string.cleartable));
            c.close();
            dbh.close();
            sqdb.close();
        }

        ArrayList<String> mainListarray =  new ArrayList<String>(Arrays.asList(enterpriseArray[0]));
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(mainListarray);
        mainListarray.clear();
        mainListarray.addAll(hs);
        Collections.sort(mainListarray);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mainListarray);
        mainList.setAdapter(adapter);

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView textView = (TextView) view;
                String chooseent = textView.getText().toString();
                current = chooseent;
                ArrayList<String> secondListarray = new ArrayList<String>();
                for (int records = 0; records < numofrecords[currpage]; records++){
                     if (enterpriseArray[currpage][records].equals(chooseent)) {
                         secondListarray.add(titleArray[currpage][records]);
                     }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, secondListarray);
                secondList.setAdapter(adapter);
                mainList.setVisibility(View.GONE);
                secondList.setVisibility(View.VISIBLE);
            }
        });

        secondList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView textView = (TextView) view;
                String choosetitle = textView.getText().toString();
                for (int records = 0; records < numofrecords[currpage]; records++){
                     if ((enterpriseArray[currpage][records].equals(current)) && (titleArray[currpage][records].equals(choosetitle))) {
                         descriptionText.setText(descriptionArray[currpage][records]);
                     }
                }
                secondList.setVisibility(View.GONE);
                descriptionText.setVisibility(View.VISIBLE);
            }
        });

        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout)findViewById(R.id.drawer_layout));
        mTitle = getTitle();
    }

    @Override
    public void onBackPressed() {
        if ((mainList.getVisibility() == View.VISIBLE) || (currpage == 2)) {
            finish();
            return;
        }
        if (secondList.getVisibility() == View.VISIBLE) {
            secondList.setVisibility(View.GONE);
            mainList.setVisibility(View.VISIBLE);
        }
        if (descriptionText.getVisibility() == View.VISIBLE) {
            descriptionText.setVisibility(View.GONE);
            secondList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
    }

    public void onSectionAttached(int number) {
        currpage = number - 1;
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                mainList.setVisibility(View.VISIBLE);
                secondList.setVisibility(View.GONE);
                descriptionText.setVisibility(View.GONE);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                mainList.setVisibility(View.VISIBLE);
                secondList.setVisibility(View.GONE);
                descriptionText.setVisibility(View.GONE);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                mainList.setVisibility(View.GONE);
                secondList.setVisibility(View.GONE);
                descriptionText.setVisibility(View.VISIBLE);
                descriptionText.setText(R.string.contacts);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.action_settings) || super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class DatabaseHelper extends SQLiteOpenHelper {

        private final Context fContext;
        public String TABLE_NAME = getString(R.string.enterprisedbtablename);
        private InputStream is;

        public DatabaseHelper (Context context, String a) {
            super(context, a, null, 1);
            fContext = context;
            if (a.equals(getString(R.string.enterprisedbname))){
                is = fContext.getResources().openRawResource(R.raw.enterprises);
            }
            if (a.equals(getString(R.string.newsdbname))){
                is = fContext.getResources().openRawResource(R.raw.news);
                TABLE_NAME = getString(R.string.newsdbtablename);
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME
                    + " (id integer primary key autoincrement," +
                    getString(R.string.tableraw1) + " text, " +
                    getString(R.string.tableraw2) + " text, " +
                    getString(R.string.tableraw3) + " text);");
            ContentValues values = new ContentValues();

            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(is);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName(getString(R.string.tableraw1));
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String enterprisename = eElement.getAttribute("name");
                        int numofrecords = Integer.parseInt(eElement.getAttribute("quantity"));
                        for (int records = 0; records < numofrecords; records++){
                            values.put(getString(R.string.tableraw1), enterprisename);
                            values.put(getString(R.string.tableraw2), eElement.getElementsByTagName("record").item(records).getAttributes().item(0).getTextContent());
                            values.put(getString(R.string.tableraw3), eElement.getElementsByTagName("record").item(records).getAttributes().item(1).getTextContent());
                            db.insert(TABLE_NAME, null, values);
                            Log.d(LOG_TAG, values.toString());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}
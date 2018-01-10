package com.easyweather.bryant.easyweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CityFinderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_finder);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AutoCompleteTextView edt = (AutoCompleteTextView) this.findViewById(R.id.edtCity);
        CityAdapter adpt = new CityAdapter(this, null);
        edt.setAdapter(adpt);

        edt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityResult result = (CityResult) parent.getItemAtPosition(position);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CityFinderActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("woeid", result.getWoeid());
                editor.putString("cityName", result.getCityName());
                editor.putString("region", result.getRegion());
                editor.putString("country", result.getCountry());
                editor.commit();
                NavUtils.navigateUpFromSameTask(CityFinderActivity.this);
            }
        });
    }


    private class CityAdapter extends ArrayAdapter<CityResult> implements Filterable {

        private Context ctx;
        private List<CityResult> cityList = new ArrayList<CityResult>();

        public CityAdapter(Context ctx, List<CityResult> cityList) {
            super(ctx, R.layout.city_results, cityList);
            this.cityList = cityList;
            this.ctx = ctx;
        }


        @Override
        public CityResult getItem(int position) {
            if (cityList != null)
                return cityList.get(position);

            return null;
        }

        @Override
        public int getCount() {
            if (cityList != null)
                return cityList.size();

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = convertView;

            if (result == null) {
                LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                result = inf.inflate(R.layout.city_results, parent, false);

            }

            TextView tv = (TextView) result.findViewById(R.id.cityNameTextView);
            tv.setText(cityList.get(position).getCityName() + ", " + cityList.get(position).getCountry());


            return result;
        }

        @Override
        public long getItemId(int position) {
            if (cityList != null)
                return cityList.get(position).hashCode();

            return 0;
        }

        @Override
        public Filter getFilter() {
            Filter cityFilter = new Filter() {


                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new Filter.FilterResults();
                    if (constraint == null || constraint.length() < 2)
                        return results;

                    List<CityResult> cityResultList = YahooClient.getCityList(constraint.toString());
                    results.values = cityResultList;
                    results.count = cityResultList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    cityList = (List) results.values;
                    notifyDataSetChanged();
                }
            };

            return cityFilter;
        }
    }

}

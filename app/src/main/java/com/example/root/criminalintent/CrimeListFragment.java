package com.example.root.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by root on 30.01.17.
 */

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private LinearLayout mLinearLayout;
    private ImageButton mEmptyImageButton;
    private int mCrimePossition;
    private boolean mSubtitleVisable;
    private Callbacks mCallbacks;

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private static final String SAVED_SUBTITLE_STATE = "subtitle";
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list,container,false);
        mCrimeRecyclerView = (RecyclerView)view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLinearLayout = (LinearLayout)view.findViewById(R.id.empty_linear_layout);
        mEmptyImageButton = (ImageButton)view.findViewById(R.id.empty_add_btn);
        setHasOptionsMenu(true);
        updateUI();
        if(savedInstanceState != null){
            mSubtitleVisable = savedInstanceState.getBoolean(SAVED_SUBTITLE_STATE);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_STATE,mSubtitleVisable);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;
        private Crime mCrime;


        public CrimeHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView)itemView.findViewById(R.id.list_item_crime_title_textview);
            mDateTextView = (TextView)itemView.findViewById(R.id.list_item_crime_date_textview);
            mSolvedCheckBox = (CheckBox)itemView.findViewById(R.id.crime_list_solved_checkbox);
            itemView.setOnClickListener(this);
        }
        public void bindCrime(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDateListFormatted());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
            mSolvedCheckBox.setEnabled(false);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onCrimeSelected(mCrime);
        }
    }
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime,parent,false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else{
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();

        }
        updateSubtitle();
        if(crimes.size() == 0){
            mLinearLayout.setVisibility(View.VISIBLE);
            mEmptyImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewCrime();

                }
            });
        }
        else
            mLinearLayout.setVisibility(View.GONE);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);
        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if(mSubtitleVisable){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }else
        subtitleItem.setTitle(R.string.show_subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_item_new_crime:
                addNewCrime();
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisable = !mSubtitleVisable;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural,crimeCount,crimeCount);
        if(!mSubtitleVisable){
            subtitle = null;
        }
        AppCompatActivity appCompatActivity = (AppCompatActivity)getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);

    }
    private void addNewCrime(){
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
       updateUI();
        mCallbacks.onCrimeSelected(crime);

    }

}

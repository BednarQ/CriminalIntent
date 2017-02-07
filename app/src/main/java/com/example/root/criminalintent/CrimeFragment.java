package com.example.root.criminalintent;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.root.criminalintent.database.ImageViewFragment;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by root on 29.01.17.
 */

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private Button mConfirmBtn;
    private Callbacks mCallbacks;


    private static int mSuspectId;

    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_ZOOM = "DialogZoom";

    private static final int REQUEST_TIME =1;
    private static final int REQUEST_DATE =0;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PERMISSION_CODE =3;
    private static final int REQUEST_PHOTO = 4;

    public static CrimeFragment newInstance(UUID crimeID){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID,crimeID);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (com.example.root.criminalintent.CrimeFragment.Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeID = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeID);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime,container,false);
        setHasOptionsMenu(true);

        mConfirmBtn = (Button)v.findViewById(R.id.confirm_crime);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCrime.getTitle().length() == 0 || mCrime.getTitle() == null){
                    Toast.makeText(getActivity(),R.string.empty_title,Toast.LENGTH_SHORT).show();
                }else {
                    getActivity().finish();
                    Toast.makeText(getActivity(), R.string.crime_added_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mDateButton = (Button)v.findViewById(R.id.date_btn);
        mDateButton.setText(mCrime.getDateFormatted());
        mDateButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
                dialog.show(manager,DIALOG_DATE);
            }
        });
        mTimeButton = (Button)v.findViewById(R.id.time_btn);
        mTimeButton.setText(mCrime.getTimeFormatted());
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });
        mTitleField.setText(mCrime.getTitle());
        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved_checkbox);
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });
        mSolvedCheckBox.setChecked(mCrime.isSolved());

        mReportButton=(Button)v.findViewById(R.id.send_crime_btn);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder i = ShareCompat.IntentBuilder.from(getActivity());
                i.setType("text/plain");
                i.setText(getCrimeReport());
                i.setSubject(getString(R.string.crime_report_subject));
                i.setChooserTitle(getString(R.string.send_report));
                i.startChooser();
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button)v.findViewById(R.id.choose_person_btn);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact,REQUEST_CONTACT);
            }
        });
        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY)==null){
            mSuspectButton.setEnabled(false);
        }

        mCallSuspectButton = (Button)v.findViewById(R.id.call_suspect_btn);
        mCallSuspectButton.setVisibility(View.INVISIBLE);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selectClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                String[] queryFields = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                String[] contactID = {Integer.toString(mSuspectId)};

                Cursor cursor = getActivity().getContentResolver().query(contactUri, queryFields, selectClause, contactID, null);
                try {
                    if (cursor.getCount() == 0) {
                        return;
                    }
                    cursor.moveToFirst();
                    String number = cursor.getString(0);
                    Uri phoneNumber = Uri.parse("tel:"+number);
                    Intent intent = new Intent(Intent.ACTION_DIAL,phoneNumber);
                    startActivity(intent);
                }finally {
                    cursor.close();
                }
            }
        });

        mPhotoButton = (ImageButton)v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) !=null;
        mPhotoButton.setEnabled(canTakePhoto);
        if(canTakePhoto){
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageView)v.findViewById(R.id.crime_photo);
        updatePhotoView();
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                ImageViewFragment dialog = new ImageViewFragment().newInstance(mPhotoFile);
                dialog.show(manager,DIALOG_ZOOM);
            }
        });
        return v;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(mCrime.getDateFormatted());
            updateCrime();
        }
        else if(requestCode == REQUEST_TIME){
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            mTimeButton.setText(mCrime.getTimeFormatted());
            updateCrime();
        }
        else if(requestCode == REQUEST_CONTACT){
            askForPermissions();
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone._ID
            };
            Cursor c = getActivity().getContentResolver().query(contactUri,queryFields,null,null,null);
            try{
                if(c.getCount() == 0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mSuspectId = c.getInt(1);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            }finally {
                c.close();
            }
        }
        else if(requestCode == REQUEST_PHOTO){
            updateCrime();
            updatePhotoView();
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_remove_crime:
                UUID id = mCrime.getId();
                if(getActivity().findViewById(R.id.detail_fragment_container) == null) {
                    CrimeLab.get(getActivity()).removeCrime(id);
                    Toast.makeText(getActivity(), R.string.crime_removed_toast, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }else{
                    CrimeLab.get(getActivity()).removeCrime(id);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .remove(this)
                            .commit();
                    updateCrime();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateString = mCrime.getDateFormatted();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect+mSuspectId);

        return report;
    }
    private boolean askForPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQUEST_PERMISSION_CODE);

            }else{
                mCallSuspectButton.setVisibility(View.VISIBLE);
            }

        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCallSuspectButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(),R.string.permissin_denied, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    private void updatePhotoView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setEnabled(false);
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
}



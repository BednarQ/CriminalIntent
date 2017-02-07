package com.example.root.criminalintent.database;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.root.criminalintent.PictureUtils;
import com.example.root.criminalintent.R;
import com.example.root.criminalintent.TimePickerFragment;

import java.io.File;
import java.util.Date;
import java.util.zip.Inflater;

/**
 * Created by root on 07.02.17.
 */

public class ImageViewFragment extends DialogFragment {
    public static final String ARG_PHOTO = "photo";
    public static final String EXTRA_PHOTO = "com.example.root.criminalintent";

    private ImageView mImageView;

    public static ImageViewFragment newInstance(File photoFile){
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO, photoFile);

        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_zoom_photo,null);
        File photoFile = (File) getArguments().getSerializable(ARG_PHOTO);
        mImageView = (ImageView)v.findViewById(R.id.dialog_zoom_photo);
        Bitmap image = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
        mImageView.setImageBitmap(image);

        return new AlertDialog.Builder(getActivity()).setView(mImageView).show();

    }
}

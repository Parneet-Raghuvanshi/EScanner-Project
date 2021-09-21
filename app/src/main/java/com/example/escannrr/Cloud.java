package com.example.escannrr;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Cloud extends Fragment {

    public Cloud() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cloud, container, false);

        Intent intent = new Intent(getContext(),Writexls.class);
        getActivity().startActivity(intent);

        return view;
    }
}
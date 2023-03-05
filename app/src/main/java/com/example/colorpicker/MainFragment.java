package com.example.colorpicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.colorpicker.databinding.FragmentMainBinding;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainFragment extends Fragment {
        FragmentMainBinding bnd;
        Button goToImage;
        Bundle bundle;
        public View onCreateView(
                @NonNull LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState
                ) {
                bnd = FragmentMainBinding.inflate(inflater, container, false);
                goToImage = bnd.goToImageBtn;
                bnd.openExplorer.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        launchSomeActivity.launch(intent);
                });
                goToImage.setOnClickListener(v -> {
                        Navigation.findNavController(v).navigate(R.id.action_mainFragment_to_photoFragment, bundle);
                });
        return bnd.getRoot();
        }

        ActivityResultLauncher<Intent> launchSomeActivity
                = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                        if (result.getResultCode()
                                == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null
                                        && data.getData() != null) {
                                        final Uri uri = data.getData();
                                        try {
                                                final InputStream imStream =
                                                        requireContext().getContentResolver().openInputStream(uri);
                                                final Bitmap bitmap = BitmapFactory.decodeStream(imStream);
                                                bnd.imageView.setImageBitmap(bitmap);
                                                bundle = new Bundle();
                                                String stringUri = uri.toString();
                                                bundle.putString(Cons.PATH, stringUri);
                                                goToImage.setVisibility(View.VISIBLE);
                                        } catch (FileNotFoundException e) {
                                                throw new RuntimeException(e);
                                        }
                                };
                        }
                });
};
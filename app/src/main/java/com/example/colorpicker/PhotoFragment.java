package com.example.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.colorpicker.databinding.FragmentPhotoBinding;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PhotoFragment extends Fragment {
        String path;
        FragmentPhotoBinding bnd = null;
        Bitmap bitmap;
        boolean clicked;
        String complementary;
        String triadic;
        @SuppressLint("ClickableViewAccessibility")
        public View onCreateView(
                @NonNull LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState
                ) {
                requireActivity().getOnBackPressedDispatcher()
                        .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                                @Override
                                public void handleOnBackPressed() {
                                        Navigation.findNavController(bnd.getRoot()).popBackStack(); // clearing cache
                                }
                        });
                Bundle bundle =  getArguments();
                if (bundle != null) {
                        path = bundle.getString(Cons.PATH);
                }
                bnd = FragmentPhotoBinding.inflate(inflater, container, false);
                clicked = false;
                final InputStream imageStream;

                try {
                        Uri uri = Uri.parse(path);
                        imageStream = requireContext().getContentResolver().openInputStream(uri);
                        bitmap = BitmapFactory.decodeStream(imageStream);
                        bnd.image.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                }
                ImageView image = bnd.image;

                image.setOnTouchListener((v, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                                clicked = true;
                                int selectedPixel = gettingColor(event, v, bitmap);
                                String selectedHex = gettingHexColorString(selectedPixel);

                                int complementaryPixel = getInvertPixel(selectedPixel);
                                String complementaryHex = gettingHexColorString(complementaryPixel);
                                bnd.complFirst.setText(selectedHex);
                                bnd.complSecond.setText(complementaryHex);
                                bnd.complFirst.setBackgroundColor(Color.parseColor(selectedHex));
                                bnd.complSecond.setBackgroundColor(Color.parseColor(complementaryHex));
                                complementary = "Complementary:\n" + selectedHex + "\n" + complementaryHex;

                                int[] triadicPixels = getTriadic(selectedPixel);
                                String firstHex = gettingHexColorString(triadicPixels[0]);
                                String secondHex = gettingHexColorString(triadicPixels[1]);
                                bnd.triangleOne.setText(selectedHex);
                                bnd.triangleTwo.setText(firstHex);
                                bnd.triangleThree.setText(secondHex);
                                bnd.triangleOne.setBackgroundColor(Color.parseColor(selectedHex));
                                bnd.triangleTwo.setBackgroundColor(Color.parseColor(firstHex));
                                bnd.triangleThree.setBackgroundColor(Color.parseColor(secondHex));
                                triadic = "Triadic Harmony:\n" + selectedHex + "\n" + firstHex + "\n" + secondHex;
                        }
                        bnd.complementaryCard.setOnClickListener(view -> {
                                if (clicked) {
                                        shareIntent(requireContext(), complementary);
                                }
                        });
                        bnd.triadicCard.setOnClickListener(view -> {
                                if (clicked) {
                                        shareIntent(requireContext(), triadic);
                                }
                        });
                        return true;
                });
        return bnd.getRoot();
        }
        static void shareIntent(Context ctx, String text) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(intent, null);
                ctx.startActivity(shareIntent);
        }
        static int[] getTriadic(int pixel) {
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                int first = Color.rgb(blue, red, green);
                int second = Color.rgb(green, blue, red);

                return new int[] {first, second};
        }
        static int getInvertPixel(int pixel) {
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                int invertRed = (255 - red);
                int invertGreen = (255 - green);
                int invertBlue = (255 - blue);
                return Color.rgb(invertRed, invertGreen, invertBlue);
        }
        static String gettingHexColorString(int pixelToString) {
                String format = "#%06X";
                String hexColor;
                hexColor = String.format(format, (pixelToString));
                return hexColor;
        }
        static int gettingColor(MotionEvent event, View v, Bitmap bitmap) {
                float touchX = event.getX();
                float touchY = (int) event.getY();
                int vHei = v.getHeight();
                int vWid = v.getWidth();
                int bmHei = bitmap.getHeight();
                int bmWid = bitmap.getWidth();
                int imageHei = (int) (touchX * bmHei / vHei);
                int imageWid = (int) (touchY * bmWid / vWid);
                return bitmap.getPixel(
                        imageHei,
                        imageWid
                );
        }
}
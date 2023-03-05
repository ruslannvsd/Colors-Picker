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
import android.widget.Toast;

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
        String tetradic;
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

                                // complementary
                                int complementaryPixel = getInvertPixel(selectedPixel);
                                String complementaryHex = gettingHexColorString(complementaryPixel);
                                bnd.complFirst.setText(selectedHex);
                                bnd.complSecond.setText(complementaryHex);
                                bnd.complFirst.setTextColor(Color.parseColor(complementaryHex));
                                bnd.complSecond.setTextColor(Color.parseColor(selectedHex));
                                bnd.complFirst.setBackgroundColor(Color.parseColor(selectedHex));
                                bnd.complSecond.setBackgroundColor(Color.parseColor(complementaryHex));
                                complementary = "Complementary:\n" + selectedHex + "\n" + complementaryHex;

                                // triadic
                                int[] triadicPixels = getTriadic(selectedPixel);
                                String secondTriadicHex = gettingHexColorString(triadicPixels[0]);
                                String thirdTriadicHex = gettingHexColorString(triadicPixels[1]);
                                bnd.triangleOne.setText(selectedHex);
                                bnd.triangleTwo.setText(secondTriadicHex);
                                bnd.triangleThree.setText(thirdTriadicHex);

                                bnd.triangleOne.setTextColor(getInvertPixel(selectedPixel));
                                bnd.triangleTwo.setTextColor(getInvertPixel(triadicPixels[0]));
                                bnd.triangleThree.setTextColor(getInvertPixel(triadicPixels[1]));
                                bnd.triangleOne.setBackgroundColor(selectedPixel);
                                bnd.triangleTwo.setBackgroundColor(triadicPixels[0]);
                                bnd.triangleThree.setBackgroundColor(triadicPixels[1]);
                                triadic = "Triadic Harmony:\n" + selectedHex + "\n" + secondTriadicHex + "\n" + thirdTriadicHex;

                                // tetradic
                                int[] tetradicPixels = getTetradic(selectedPixel);
                                String secondTetradicHex = gettingHexColorString(tetradicPixels[0]);
                                String thirdTetradicHex = gettingHexColorString(tetradicPixels[1]);
                                String fourthTetradicHex = gettingHexColorString(tetradicPixels[2]);

                                bnd.tetradicOne.setText(selectedHex);
                                bnd.tetradicTwo.setText(secondTetradicHex);
                                bnd.tetradicThree.setText(thirdTetradicHex);
                                bnd.tetradicFour.setText(fourthTetradicHex);

                                bnd.tetradicOne.setTextColor(getInvertPixel(selectedPixel));
                                bnd.tetradicTwo.setTextColor(getInvertPixel(tetradicPixels[0]));
                                bnd.tetradicThree.setTextColor(getInvertPixel(tetradicPixels[1]));
                                bnd.tetradicFour.setTextColor(getInvertPixel(tetradicPixels[2]));
                                bnd.tetradicOne.setBackgroundColor(selectedPixel);
                                bnd.tetradicTwo.setBackgroundColor(tetradicPixels[0]);
                                bnd.tetradicThree.setBackgroundColor(tetradicPixels[1]);
                                bnd.tetradicFour.setBackgroundColor(tetradicPixels[2]);

                                tetradic = "Tetradic Harmony:\n"
                                        + selectedHex + "\n"
                                        + secondTetradicHex + "\n"
                                        + thirdTetradicHex + "\n"
                                        + fourthTetradicHex;
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
                        bnd.tetradicCard.setOnClickListener(view -> {
                                if (clicked) {
                                        shareIntent(requireContext(), tetradic);
                                }
                        });
                        return true;
                });
        return bnd.getRoot();
        }
        static void shareIntent(Context ctx, String text) {
                Toast.makeText(ctx, "Share the colors", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(intent, null);
                ctx.startActivity(shareIntent);
        }
        static int[] getRGB(int pixel) {
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                return new int[] {red, green, blue};
        }
        static int[] getTriadic(int pixel) {
                int[] rgb = getRGB(pixel);
                int first = Color.rgb(rgb[2], rgb[0], rgb[1]);
                int second = Color.rgb(rgb[1], rgb[2], rgb[0]);

                return new int[] {first, second};
        }
        static float getHueHSV(float initialHue) {
                if (initialHue > 360) return initialHue - 360; else return initialHue;
        }
        static int[] getTetradic(int pixel) {
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                float[] firstHsv = new float[] {getHueHSV(hsv[0] + 90), hsv[1], hsv[2]};
                float[] secondHsv = new float[] {getHueHSV(hsv[0] + 180), hsv[1], hsv[2]};
                float[] thirdHsv = new float[] {getHueHSV(hsv[0] + 270), hsv[1], hsv[2]};
                int firstIntColor = Color.HSVToColor(firstHsv);
                int secondIntColor = Color.HSVToColor(secondHsv);
                int thirdIntColor = Color.HSVToColor(thirdHsv);
                return new int[] {firstIntColor, secondIntColor, thirdIntColor};
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
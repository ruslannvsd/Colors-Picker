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
import java.util.ArrayList;
import java.util.List;

public class PhotoFragment extends Fragment {
        String path;
        FragmentPhotoBinding bnd;
        Bitmap bitmap;
        boolean clicked;
        String complementary;
        String triadic;
        String tetradic;
        String analogous;
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
                                int touchedPix = getTouchColor(event, v, bitmap);
                                Colors colors = getPixelColor(touchedPix);

                                // complementary
                                complementary = complementaryBind(colors, bnd);
                                // triadic
                                triadic = triadicBind(colors, bnd);
                                // tetradic
                                tetradic = tetradicBind(colors, bnd);
                                // analogous
                                analogous = analogousBind(colors, bnd);
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
                        bnd.analogousCard.setOnClickListener(view -> {
                                if (clicked) {
                                        shareIntent(requireContext(), analogous);
                                }
                        });
                        return true;
                });
        return bnd.getRoot();
        }

        static Colors getPixelColor(int pixel) {
                ColorInfo pixelColor  = new ColorInfo(pixel, getHexColorString(pixel), getIntensity(pixel));
                ColorInfo complementary = getComplementaryPixel(pixel);
                List<ColorInfo> triadic = getTriadic(pixel);
                List<ColorInfo> tetradic = getTetradic(pixel);
                List<ColorInfo> analogous = getAnalogous(pixel);
                Triadic triadicObj = new Triadic(triadic.get(0), triadic.get(1));
                Tetradic tetradicObj = new Tetradic(tetradic.get(0), tetradic.get(1), tetradic.get(2));
                Analogous analogousObj = new Analogous(analogous.get(0), analogous.get(1));
                return new Colors(pixelColor , complementary, triadicObj, tetradicObj, analogousObj);
        }

        // supporting methods
        static int getIntensity(int color) {
                int black = Cons.BLACK;
                int white = Cons.WHITE;
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                if (((red * 0.299) + (green * 0.587) + (blue * 0.114)) > 186)
                        return black;
                else return white;
        }
        static int getTouchColor(MotionEvent event, View v, Bitmap bitmap) {
                float touchX = event.getX();
                float touchY = event.getY();
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
        static float getHueHSV(float initialHue) {
                if (initialHue >= 360) return initialHue - 360;
                else if (initialHue < 0) return initialHue + 360;
                else return initialHue;
        }
        static String getHexColorString(int pixelToString) {
                String format = "#%06X";
                String hexColor;
                hexColor = String.format(format, (pixelToString));
                return hexColor;
        }

        // methods for getting harmonies after touching pixel
        static ColorInfo getComplementaryPixel(int pixel) {
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                int color = Color.HSVToColor(new float[] {getHueHSV(hsv[0] + 180), hsv[1], hsv[2]});
                String hex = getHexColorString(color);
                int intensity = getIntensity(color);
                return new ColorInfo(color, hex, intensity);
        }
        static List<ColorInfo> getTriadic(int pixel) {
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                float[] firstHsv = new float[] {getHueHSV(hsv[0] + 120), hsv[1], hsv[2]};
                int firstIntColor = Color.HSVToColor(firstHsv);
                int firstIntensity = getIntensity(firstIntColor);

                float[] secondHsv = new float[] {getHueHSV(hsv[0] + 240), hsv[1], hsv[2]};
                int secondIntColor = Color.HSVToColor(secondHsv);
                int secondIntensity = getIntensity(secondIntColor);

                ColorInfo firstColor = new ColorInfo(firstIntColor, getHexColorString(firstIntColor), firstIntensity);
                ColorInfo secondColor = new ColorInfo(secondIntColor, getHexColorString(secondIntColor), secondIntensity);
                ArrayList<ColorInfo> list;
                list = new ArrayList<>();
                list.add(firstColor);
                list.add(secondColor);
                return list;
        }
        static List<ColorInfo> getTetradic(int pixel) {
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                float[] firstHsv = new float[] {getHueHSV(hsv[0] + 90), hsv[1], hsv[2]};
                int firstIntColor = Color.HSVToColor(firstHsv);
                int firstIntensity = getIntensity(firstIntColor);

                float[] secondHsv = new float[] {getHueHSV(hsv[0] + 180), hsv[1], hsv[2]};
                int secondIntColor = Color.HSVToColor(secondHsv);
                int secondIntensity = getIntensity(secondIntColor);

                float[] thirdHsv = new float[] {getHueHSV(hsv[0] + 270), hsv[1], hsv[2]};
                int thirdIntColor = Color.HSVToColor(thirdHsv);
                int thirdIntensity = getIntensity(thirdIntColor);

                ArrayList<ColorInfo> list;
                list = new ArrayList<>();
                list.add(new ColorInfo(firstIntColor, getHexColorString(firstIntColor), firstIntensity));
                list.add(new ColorInfo(secondIntColor, getHexColorString(secondIntColor), secondIntensity));
                list.add(new ColorInfo(thirdIntColor, getHexColorString(thirdIntColor), thirdIntensity));
                return list;
        }
        static List<ColorInfo> getAnalogous(int pixel) {
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                float[] oneSideHSV = new float[] {getHueHSV(hsv[0] - 30), hsv[1], hsv[2]};
                int oneSideColor = Color.HSVToColor(oneSideHSV);
                int oneSideIntensity = getIntensity(oneSideColor);

                float[] otherSideHSV = new float[] {getHueHSV(hsv[0] + 30), hsv[1], hsv[2]};
                int otherSideColor = Color.HSVToColor(otherSideHSV);
                int otherSideIntensity = getIntensity(otherSideColor);
                ArrayList<ColorInfo> list;
                list = new ArrayList<>();
                list.add(new ColorInfo(oneSideColor, getHexColorString(oneSideColor), oneSideIntensity));
                list.add(new ColorInfo(otherSideColor, getHexColorString(otherSideColor), otherSideIntensity));
                return list;
        }

        // setting texts and colors to panels aka binding
        // complementary
        static String complementaryBind(Colors colors, FragmentPhotoBinding bnd) {
                bnd.complFirst.setText(colors.pixelColor.colorString);
                bnd.complFirst.setBackgroundColor(colors.pixelColor.colorInt);
                bnd.complFirst.setTextColor(colors.pixelColor.textColor);

                bnd.complSecond.setText(colors.complementary.colorString);
                bnd.complSecond.setBackgroundColor(colors.complementary.colorInt);
                bnd.complSecond.setTextColor(colors.complementary.textColor);

                return "Complementary:\n" +
                        colors.pixelColor.colorString + "\n" +
                        colors.complementary.colorString;
        }
        // triadic
        static String triadicBind(Colors colors, FragmentPhotoBinding bnd) {
                bnd.triangleOne.setText(colors.pixelColor.colorString);
                bnd.triangleOne.setBackgroundColor(colors.pixelColor.colorInt);
                bnd.triangleOne.setTextColor(colors.pixelColor.textColor);

                bnd.triangleTwo.setText(colors.triadic.second.colorString);
                bnd.triangleTwo.setBackgroundColor(colors.triadic.second.colorInt);
                bnd.triangleTwo.setTextColor(colors.triadic.second.textColor);

                bnd.triangleThree.setText(colors.triadic.third.colorString);
                bnd.triangleThree.setBackgroundColor(colors.triadic.third.colorInt);
                bnd.triangleThree.setTextColor(colors.triadic.third.textColor);
                return "Triadic Harmony:\n" +
                        colors.pixelColor.colorString+ "\n" +
                        colors.triadic.second.colorString + "\n" +
                        colors.triadic.third.colorString;
        }
        //tetradic
        static String tetradicBind(Colors colors, FragmentPhotoBinding bnd) {
                bnd.tetradicOne.setText(colors.pixelColor.colorString);
                bnd.tetradicOne.setBackgroundColor(colors.pixelColor.colorInt);
                bnd.tetradicOne.setTextColor(colors.pixelColor.textColor);

                bnd.tetradicTwo.setText(colors.tetradic.second.colorString);
                bnd.tetradicTwo.setBackgroundColor(colors.tetradic.second.colorInt);
                bnd.tetradicTwo.setTextColor(colors.tetradic.second.textColor);

                bnd.tetradicThree.setText(colors.tetradic.third.colorString);
                bnd.tetradicThree.setBackgroundColor(colors.tetradic.third.colorInt);
                bnd.tetradicThree.setTextColor(colors.tetradic.third.textColor);

                bnd.tetradicFour.setText(colors.tetradic.fourth.colorString);
                bnd.tetradicFour.setBackgroundColor(colors.tetradic.fourth.colorInt);
                bnd.tetradicFour.setTextColor(colors.tetradic.fourth.textColor);

                return "Tetradic Harmony:\n"
                        + colors.pixelColor.colorString + "\n"
                        + colors.tetradic.second.colorString + "\n"
                        + colors.tetradic.third.colorString + "\n"
                        + colors.tetradic.fourth.colorString;
        }
        // analogous
        static String analogousBind(Colors colors, FragmentPhotoBinding bnd) {
                bnd.analogousOne.setText(colors.analogous.first.colorString);
                bnd.analogousOne.setBackgroundColor(colors.analogous.first.colorInt);
                bnd.analogousOne.setTextColor(colors.analogous.first.textColor);

                bnd.analogousTwoInitial.setText(colors.pixelColor.colorString);
                bnd.analogousTwoInitial.setBackgroundColor(colors.pixelColor.colorInt);
                bnd.analogousTwoInitial.setTextColor(colors.pixelColor.textColor);

                bnd.analogousThree.setText(colors.analogous.second.colorString);
                bnd.analogousThree.setBackgroundColor(colors.analogous.second.colorInt);
                bnd.analogousThree.setTextColor(colors.analogous.second.textColor);

                return "Analogous Harmony:\n" +
                        colors.analogous.first.colorString + "\n" +
                        colors.pixelColor.colorString + "\n" +
                        colors.analogous.second.colorString;
        }
}
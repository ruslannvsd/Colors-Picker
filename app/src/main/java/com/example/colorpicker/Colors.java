package com.example.colorpicker;


public class Colors {
    ColorInfo pixelColor;
    ColorInfo complementary;
    Triadic triadic;
    Tetradic tetradic;
    Analogous analogous;
    Colors(ColorInfo pixel, ColorInfo complementary, Triadic triadic, Tetradic tetradic, Analogous analogous) {
        pixelColor = pixel;
        this.complementary = complementary;
        this.triadic = triadic;
        this.tetradic = tetradic;
        this.analogous = analogous;
    }
}

class Triadic {
    ColorInfo second;
    ColorInfo third;
    public Triadic(ColorInfo second, ColorInfo third) {
        this.second = second;
        this.third = third;
    }
}

class Tetradic {
    ColorInfo second;
    ColorInfo third;
    ColorInfo fourth;
    public Tetradic(ColorInfo second,ColorInfo third, ColorInfo fourth) {
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }
}

class Analogous {
    ColorInfo first;
    ColorInfo second;
    public Analogous(ColorInfo first, ColorInfo second) {
        this.first = first;
        this.second = second;
    }
}

class ColorInfo {
    int colorInt;
    String colorString;
    int textColor;
    public ColorInfo(int colorInt, String colorString, int textColor) {
        this.colorInt = colorInt;
        this.colorString = colorString;
        this.textColor = textColor;
    }
}
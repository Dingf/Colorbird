package com.teamhex.colorbird;

public class ColorConverter 
{
    public static int ColorRGB(int r, int g, int b)
    {
        return ((0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }
    
    public static int ColorHSV(double h, double s, double v)
    {
        int flag = (Double.isNaN(h) ? 1 : 0);
        
        int v1 = (Double.isNaN(h) ? 0 : (int)(h * 91.0222222));
        int v2 = (int)(s * 255);
        int v3 = (int)(v * 255);
        
        return (((flag & 0x1) << 31) | (v1 & 0xFFFF) << 16) | ((v2 & 0xFF) << 8) | (v3 & 0xFF);
    }
    
    public static double getHue(int hsvColor)
    {
        int flag = (hsvColor >> 31);
        if (flag == 1)
        {
            return Double.NaN;
        }
        else
        {
            int n = (hsvColor  >> 16) & 0x7FFF;
            return (n * 0.01098632812);
        }
    }
    
    public static double getSaturation(int hsvColor)
    {
        return ((double)((hsvColor >> 8) & 0xFF))/255.0;
    }
    
    public static double getValue(int hsvColor)
    {
        return ((double)(hsvColor & 0xFF))/255.0;
    }
    
    public static int RGBtoHSV(int rgbColor)
    {
        double r = (double)((rgbColor >> 16) & 0xFF)/255.0;
        double g = (double)((rgbColor >> 8) & 0xFF)/255.0; 
        double b = (double)(rgbColor & 0xFF)/255.0;
        
        double max = (r > g) ? ((r > b) ? r : b) : ((g > b) ? g : b);
        double min = (r < g) ? ((r < b) ? r : b) : ((g < b) ? g : b);
        
        double v = max;
        double d = max - min;
        
        if (max == 0)
        {
            return ColorHSV(Double.NaN,0.0,v);
        }
        else
        {
            double h = Double.NaN;
            double s = (max == 0) ? 0 : d/v;
            if (r == max)
            {
                h = 60 * (g - b)/d;
            }
            else if (g == max)
            {
                h = 120 + 60 * (b - r)/d;
            }
            else if (b == max)
            {
                h = 240 + 60 * (r - g)/d;
            }
            return ColorHSV(h,s,v);
        }
    }
    
    public static int HSVtoRGB(int hsvColor)
    {
        double h = getHue(hsvColor)/60.0;
        double s = getSaturation(hsvColor);
        double v = getValue(hsvColor);
        
        if (s == 0.0)
        {
            return ColorRGB((int)(v*255.0), (int)(v*255.0), (int)(v*255.0));
        }
        
        double c = s * v;
        double m = v - c;
        double x = c * (1 - Math.abs((h % 2) - 1));

        switch ((int)h)
        {
            case 0:
                return ColorRGB((int)((c+m)*255.0),(int)((x+m)*255.0),(int)(m*255.0));
            case 1:
                return ColorRGB((int)((x+m)*255.0),(int)((c+m)*255.0),(int)(m*255.0));
            case 2:
                return ColorRGB((int)(m*255.0),(int)((c+m)*255.0),(int)((x+m)*255.0));
            case 3:
                return ColorRGB((int)(m*255.0),(int)((x+m)*255.0),(int)((c+m)*255.0));
            case 4:
                return ColorRGB((int)((x+m)*255.0),(int)(m*255.0),(int)((c+m)*255.0));
            case 5:
                return ColorRGB((int)((c+m)*255.0),(int)(m*255.0),(int)((x+m)*255.0));
            default:
                return ColorRGB((int)(m*255.0),(int)(m*255.0),(int)(m*255.0));
        }
    }
    
    //References:
    //http://msdn.microsoft.com/en-us/library/aa917087.aspx
    //http://en.wikipedia.org/wiki/YUV
        
    //http://www.41post.com/3470/programming/android-retrieving-the-camera-preview-as-a-pixel-array
        
    public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) 
    {  
        final int frameSize = width * height;  

        for (int j = 0, yp = 0; j < height; j++) 
        {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  

            for (int i = 0; i < width; i++, yp++) 
            {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;  
                if (y < 0)  
                    y = 0;  
                if ((i & 1) == 0) {  
                    v = (0xff & yuv420sp[uvp++]) - 128;  
                    u = (0xff & yuv420sp[uvp++]) - 128;  
                }  

                int y1192 = 1192 * y;  
                int r = (y1192 + 1634 * v);  
                int g = (y1192 - 833 * v - 400 * u);  
                int b = (y1192 + 2066 * u);  

                if (r < 0)                  r = 0;               else if (r > 262143)  
                    r = 262143;  
                if (g < 0)                  g = 0;               else if (g > 262143)  
                    g = 262143;  
                if (b < 0)                  b = 0;               else if (b > 262143)  
                    b = 262143;  

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);  
            }  
        }  
    } 
}

package com.teamhex.colorbird;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

import com.teamhex.colorbird.palette.ColorPaletteGenerator;

public class DrawImageActivity extends ActionBarActivity implements DrawingView.OnSelectionListener 
{
	private Bitmap mBitmap;
	private DrawingView mDrawingView;
	private int[] mPalette = null;
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
        	getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        }
	    
	    setContentView(R.layout.activity_draw);

	    ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
	    actionBar.setDisplayHomeAsUpEnabled(true);

	    //Setup the drawing view for making selections
	    mDrawingView = (DrawingView) findViewById(R.id.drawing_view);
	    mDrawingView.setOnSelectionListener(this);
	    
	    Intent i = getIntent();
	    if (i.hasExtra("URI"))
	    {
	    	int width = i.getIntExtra("width", 0);
	    	int height = i.getIntExtra("height", 0);
	    	
	    	if ((width != 0) && (height != 0))
	    	{
		    	Uri uri = Uri.parse(i.getStringExtra("URI"));
		        loadBitmap(uri.getPath(), width, height);
	    	}
	    	else
	    	{
	    		//Error: width and/or height undefined
	    	}
	    }
	}
	
	private void loadBitmap(String filename, int width, int height)
	{
		File file = new File(filename);
        InputStream ios = null;
        
        byte[] data = new byte[(int)file.length()];
        try 
        {
            ios = new FileInputStream(file);
            if ( ios.read(data) == -1 ) 
            {
                //Error: EOF reached before "data" could be filled
            }        
        } 
        catch (Exception e) {}
	    finally 
	    { 
            try 
            {
                if (ios != null)
                {
                    ios.close();
                }
            } 
            catch ( IOException e) {}
        }
        
        int[] pixels = new int[width * height];
        ColorConverter.decodeYUV420SP(pixels, data, width, height);
        
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            int[] newPixels = new int[width * height];
            for (int y = 0; y < height; y++) 
            {
                for (int x = 0; x < width; x++)
                {
                    newPixels[x * height + height - y - 1] = pixels[x + y * width];
                }
            }
            
            mBitmap = Bitmap.createBitmap(newPixels, height, width, Bitmap.Config.ARGB_8888);
        }
        else
        {
        	mBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        }
        mDrawingView.setBitmap(mBitmap);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_draw, menu);
		return true;
	}
	
	private void createPalette()
	{
		Log.i("TeamHex", "Using the ColorPaletteGenerator.colorAlgorithm to get the [] colors.");
		AppSettings settings = AppSettings.getSettings();
		
		int pixels[] = mDrawingView.getSelectedPixels();
	    int[] colors = ColorPaletteGenerator.colorAlgorithm(pixels, settings.paletteSize);
	    
	    mPalette = colors;
	    /*if(colors != null)
	    {
	    	// Store the output from colors[] into a new PaletteRecord
		    palette = new PaletteRecord();
		    palette.setName("Untitled_Palette");
		    
		    for (int i = 0; i < settings.paletteSize; i++)
		    	palette.addColor(colors[i]);
		    
		    palette.setX11Names();
	    }
	    	
	    Log.i("TeamHex", "Finixhed adding the colors to a new palette.");
	   */
		//mDrawingView.setColors(mPalette);
	    
	}
	
	private class ProcessTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... arg0) {
			createPalette();
			return null;

		}
		
		@Override
	    protected void onPostExecute(Object obj) 
		{
			mDrawingView.setColors(mPalette);
	    }
	}

	private ProcessTask mProcessTask;
	@Override
	public void onSelection()
	{
		if (mProcessTask != null)
		{
			mProcessTask.cancel(true);
		}
		mProcessTask = new ProcessTask();
		mProcessTask.execute();
	}
	
} //END ACTIVITY



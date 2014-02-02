package com.teamhex.colorbird;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

public class MainActivity extends ActionBarActivity implements PreviewCallback
{
	private Camera mCamera = null;
	private CameraView mPreview = null;
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
        	getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        }
		
		setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
		
		mCamera = getCameraInstance();
		
        // Create preview and set as content of activity
		if (mPreview == null)
		{
			mPreview = new CameraView(this, mCamera);
		}
		else
		{
			mPreview.setCamera(mCamera);
		}
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
        preview.addView(mPreview);
        
        mPreview.setOnClickListener
        (
            new View.OnClickListener() 
            {
                @Override
                public void onClick(View v) 
                {
                	mCamera.setOneShotPreviewCallback(MainActivity.this);
                }
            }
        );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		
		Camera c = getCameraInstance();
		if (c != null)
		{
			mCamera = c;
			mPreview.setCamera(mCamera);
		}
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) 
	{
		camera.stopPreview();
		
		Parameters parameters = camera.getParameters();
		
    	int width = parameters.getPreviewSize().width;
    	int height = parameters.getPreviewSize().height;
        
        File cacheDir = getCacheDir();
        try 
        {
			File temp = File.createTempFile("temp_bitmap", ".tmp", cacheDir);
			
			FileOutputStream outputStream = new FileOutputStream(temp);
			
			outputStream.write(data);
			outputStream.close();
			
			Uri uri = Uri.fromFile(temp);
			
			Intent i = new Intent(MainActivity.this, DrawImageActivity.class);
			i.putExtra("URI", uri.toString());
			i.putExtra("width",  width);
			i.putExtra("height", height);
			
	    	startActivity(i);
		} 
        catch (IOException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Camera getCameraInstance()
	{
	    Camera c = null;
	    try 
	    {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e)
	    {
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

}

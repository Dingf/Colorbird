package com.teamhex.colorbird;

public class AppSettings 
{
    public float scaleFactor = 0.5f;
    
    public int paletteSize = 8;
    
	private static AppSettings instance = null;
	
	protected AppSettings()
	{
		//Load settings from a file
	}
	
	public static AppSettings getSettings()
	{
		if (instance == null)
			instance = new AppSettings();
		
		return instance;
	}
}

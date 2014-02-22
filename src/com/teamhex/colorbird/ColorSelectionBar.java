package com.teamhex.colorbird;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorSelectionBar extends View
{
	public enum PaletteState
	{
		PALETTE_STATE_EMPTY,
		PALETTE_STATE_UNCONFIRMED,
		PALETTE_STATE_CONFIRMED,
		PALETTE_STATE_MOVING,
	}
	
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint[] mBorderPaint = null;
    private Paint[] mShadowPaint = null;
    private Paint[] mPalettePaint = null;

    private int[] mPalette;
    private PaletteState[] mPaletteState;
    private float mPaletteUnit;
    
    private int mWidth, mHeight;
    
    private boolean mTouchMoved = false;
    private boolean mTouchState = false;
    
    private AppSettings mSettings = AppSettings.getSettings();
    
    public ColorSelectionBar(Context context, AttributeSet attrs)
    { 
        super(context, attrs);
        init();
    }
    
    public ColorSelectionBar(Context context) 
    {
        super(context);
        init();
    }
    
    private void init()
    {
    	mPalette = new int[mSettings.paletteSize];
    	mPaletteState = new PaletteState[mSettings.paletteSize];
    	for (int i = 0; i < mPaletteState.length; i++)
    	{
    		mPaletteState[i] = PaletteState.PALETTE_STATE_EMPTY;
    	}
    	
    	mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setAlpha(128);
        mBorderPaint = new Paint[2];

        mBorderPaint[0] = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint[0].setStyle(Paint.Style.STROKE);
        mBorderPaint[0].setColor(Color.WHITE);
        mBorderPaint[0].setStrokeWidth(3);
        mBorderPaint[0].setAlpha(128);
        mBorderPaint[0].setStrokeJoin(Paint.Join.ROUND);
        mBorderPaint[0].setStrokeCap(Paint.Cap.ROUND);

        mBorderPaint[1] = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint[1].setStyle(Paint.Style.STROKE);
        mBorderPaint[1].setColor(Color.rgb(0, 255, 0));
        mBorderPaint[1].setStrokeWidth(5);
        mBorderPaint[1].setAlpha(192);
        mBorderPaint[1].setStrokeJoin(Paint.Join.ROUND);
        mBorderPaint[1].setStrokeCap(Paint.Cap.ROUND);

        mShadowPaint = new Paint[mPalette.length];
        mPalettePaint = new Paint[mPalette.length];
        for (int i = 0; i < mPalette.length; i++)
        {
            mShadowPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPalettePaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPalettePaint[i].setStyle(Paint.Style.FILL);
            mPalettePaint[i].setColor(Color.BLACK);
            mPalettePaint[i].setAlpha(64);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) 
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    	mPaletteUnit = w/(mPalette.length + 3 + (2 * mPalette.length));
    	for (int i = 0; i < mPalette.length; i++)
    	{
            RadialGradient gradient = 
            	new RadialGradient(2.0f * mPaletteUnit + ((float)((i * 3) + 1) * mPaletteUnit),
            					   mHeight - (1.5f * mPaletteUnit),
            					   mPaletteUnit, 
            					   new int[]{Color.argb(0,0,0,0),Color.argb(0,0,0,0),Color.argb(32, 0, 0, 0)},
            					   new float[]{0.0f,0.5f,1.0f},
            					   Shader.TileMode.CLAMP);
            
            mShadowPaint[i].setShader(gradient);
            		
    	}
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
        float touchX = Math.max(0, Math.min(mWidth - 1, event.getX()));
        float touchY = Math.max(0, Math.min(mHeight - 1, event.getY()));
        
    	switch(event.getAction()) 
        {
            case MotionEvent.ACTION_DOWN:
            	if (touchY > mHeight - (3.0f * mPaletteUnit))
            	{
            		mTouchState = true;
            	}
            	break;
            
            case MotionEvent.ACTION_MOVE:
            	mTouchMoved = true;
            	//Do stuff here
            	break;

            case MotionEvent.ACTION_UP:
            	if (mTouchMoved == false)
            	{
            		int nearest = (int)((touchX - mPaletteUnit * 1.5f)/(mPaletteUnit * 3.0f));
    				if ((nearest >= 0) && (nearest < mPalette.length))
    				{
                		float dx = touchX - (2.0f * mPaletteUnit + ((float)((nearest * 3) + 1) * mPaletteUnit));
        				float dy = touchY - (mHeight - (1.5f * mPaletteUnit));
        				
        				PaletteState state = mPaletteState[nearest];
        				
        				if (dx * dx + dy * dy <= mPaletteUnit * mPaletteUnit)
        				{
            				if (state == PaletteState.PALETTE_STATE_UNCONFIRMED)
            				{
            					mPaletteState[nearest] = PaletteState.PALETTE_STATE_CONFIRMED;
            					invalidate();
            				}
            				else if (state == PaletteState.PALETTE_STATE_CONFIRMED)
            				{
            					mPaletteState[nearest] = PaletteState.PALETTE_STATE_UNCONFIRMED;
            					invalidate();
            				}
        				}
    				}
            	}
            	else
            	{
            		mTouchMoved = false;
            	}
            	mTouchState = false;
            	break;
             
            default:
                return false;
        }
    	return mTouchState;
    }
    
    public void setColors(int[] colors)
    {
        if (colors != null)
        {
        	int j = 0;
        	for (int i = 0; i < mPalette.length; i++)
        	{
        		if ((mPaletteState[i] == PaletteState.PALETTE_STATE_EMPTY) || (mPaletteState[i] == PaletteState.PALETTE_STATE_UNCONFIRMED))
        		{
        			if (j < colors.length)
        			{
        				mPalette[i] = colors[j];
            			mPaletteState[i] = PaletteState.PALETTE_STATE_UNCONFIRMED;
                        mPalettePaint[i].setColor(colors[i]);
                        mPalettePaint[i].setAlpha(255);
        				j++;
        			}
        			else
        			{
        				mPalette[i] = 0;
                        mPalettePaint[i].setColor(Color.BLACK);
                        mPalettePaint[i].setAlpha(64);
            			mPaletteState[i] = PaletteState.PALETTE_STATE_EMPTY;
        			}
        		}
        	}
            invalidate();
        }
        else
        {
            //TODO: Alert the user that there weren't enough colors?
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        
        canvas.drawRect(0, height - (3.0f * mPaletteUnit), width, height, mBackgroundPaint);
        if (mPalettePaint != null)
        {
            for (int i = 0; i < mPalette.length; i++)
            {
                canvas.drawCircle(2.0f * mPaletteUnit + ((float)((i * 3) + 1) * mPaletteUnit),
                                  height - (1.5f * mPaletteUnit),
                                  mPaletteUnit,
                                  mPalettePaint[i]);
                
                canvas.drawCircle(2.0f * mPaletteUnit + ((float)((i * 3) + 1) * mPaletteUnit),
                        		  height - (1.5f * mPaletteUnit),
                                  mPaletteUnit,
                                  mShadowPaint[i]);
                
                if ((mPaletteState[i] == PaletteState.PALETTE_STATE_EMPTY)|| (mPaletteState[i] == PaletteState.PALETTE_STATE_UNCONFIRMED))
                {
                	canvas.drawCircle(2.0f * mPaletteUnit + ((float)((i * 3) + 1) * mPaletteUnit),
                                  	  height - (1.5f * mPaletteUnit),
                                  	  mPaletteUnit,
                                  	  mBorderPaint[0]);
                }
                else if (mPaletteState[i] == PaletteState.PALETTE_STATE_CONFIRMED)
                {
                	canvas.drawCircle(2.0f * mPaletteUnit + ((float)((i * 3) + 1) * mPaletteUnit),
                        	  		  height - (1.5f * mPaletteUnit),
                        	  		  mPaletteUnit,
                        	  		  mBorderPaint[1]);
                }
            }
        }
    }
}

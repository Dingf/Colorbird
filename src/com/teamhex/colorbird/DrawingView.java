package com.teamhex.colorbird;
/* DrawingView
 * 
 * Contains logic for drawing a selection area onto the screen.
 * 
 * There are three forms of selections:
 * 	1. Lasso
 *  2. Rectangle
 *  3. Entire Image 
 * 
 * 1. Lasso
 * 	The lasso creates a list of points, stored in the Path drawPath
 * 	
 */

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View
{
	// The methods of selection users are allowed
	public enum SelectionType 
	{
		SELECTION_TYPE_NONE,
		SELECTION_TYPE_LASSO,
		SELECTION_TYPE_EYEDROPPER,
	}
	
	public enum TouchState
	{
		TOUCH_STATE_FREE,
		TOUCH_STATE_LIFT,
		TOUCH_STATE_UP,
	}
	
	//Instance variables
	private SelectionType mSelectionType = SelectionType.SELECTION_TYPE_LASSO;
	private TouchState mTouchState = TouchState.TOUCH_STATE_FREE;
	
	private Path mDrawPath = new Path();
    private Canvas mCanvas = null;
    private Bitmap mBitmap = null;
    private Bitmap mScaledBitmap = null;
    private Rect mCanvasRect;
    private Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
	private ArrayList<Point> mPointList = new ArrayList<Point>();
	private int[] mSelectionPixels;
    
	private AppSettings mSettings = AppSettings.getSettings();
	private OnSelectionListener mOnSelectionListener;
    
    
    //int[] pixels; // The currently selected pixels

    
    
    //Rect myRect; // A rectangle to draw (if needed by onDraw)
    
	
	// The selection listener is a basic pipe to onSelection()
	public interface OnSelectionListener 
	{
    	void onSelection();
    }
	
    public void setOnSelectionListener(OnSelectionListener listener) {
    	mOnSelectionListener = listener;
    }
    // Constructor
    public DrawingView(Context context, AttributeSet attrs)
    { 
    	super(context, attrs); 
    	Init();
    }
    
    public DrawingView(Context context) 
    {
        super(context);
        Init();
    }
    
    // Init() just sets the visual style of the selector
    private void Init()
    {
    	mPathPaint.setStyle(Paint.Style.STROKE);
    	mPathPaint.setColor(Color.WHITE);
    	mPathPaint.setStrokeWidth(1);
      	mPathPaint.setStrokeJoin(Paint.Join.ROUND);
      	mPathPaint.setStrokeCap(Paint.Cap.ROUND);
    }
   
    public void setBitmap(Bitmap bitmap)
    {
    	if (mBitmap == null)
    	{
	    	mBitmap = bitmap;
	    	
	    	int width = mBitmap.getWidth();
	    	int height = mBitmap.getHeight();
	    	
	        mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(width * mSettings.scaleFactor), (int)(height * mSettings.scaleFactor), false);
    	}
    }
    
    // Sets the selection type being used
    public void setSelectionType(SelectionType type) 
    {
    	mSelectionType = type;
    }
    
    // Get the pixels selected
    public int[] getSelectedPixels() {
    	return mSelectionPixels;
    }
    
	// Size assigned to view
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);
		Bitmap canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(canvasBitmap);
		mCanvasRect = new Rect(0, 0, w, h);
		//mPaletteUnit = w/(mSettings.paletteSize + 1 + (2 * mSettings.paletteSize));
	}
	
	// Draw the view - will be called after touch event
	@Override
	protected void onDraw(Canvas canvas)
	{
        //If the bitmap is null, what's to draw anyways?
		if(mBitmap == null)
			return;
		
		canvas.drawBitmap(mBitmap, null, mCanvasRect, null);
		canvas.drawPath(mDrawPath, mPathPaint);

		//Wait for one frame before getting the pixels (this allows us to connect the lasso
		//immediately, instead of after the analysis)
		if (mTouchState == TouchState.TOUCH_STATE_LIFT)
		{
			mTouchState = TouchState.TOUCH_STATE_UP;
			invalidate();
		}
		else if (mTouchState == TouchState.TOUCH_STATE_UP)
		{
			if(mSelectionType == SelectionType.SELECTION_TYPE_LASSO) 
			{
				lassoScanlineAlgorithm();
			}

			mTouchState = TouchState.TOUCH_STATE_FREE;
			mOnSelectionListener.onSelection();
		}
		
		/*else if(selectionTYPE == SelectionType.CROP)
		{
            //Traverse points
            for(int i=0; i<pointsList.size(); i++) 
            {
                Point current = pointsList.get(i);

                // Draw points
                canvas.drawCircle(current.x, current.y, 10, paint);   
            }
            
            Point p1 = null;
            Point p2 = null;
            // Draw a square if there are 2 corners
            if(corners == 0) 
            {
            		p1 = new Point(pointsList.get(0).x, pointsList.get(1).y);
            		p2 = new Point(pointsList.get(1).x, pointsList.get(0).y);
            		if(pointsList.contains(p1) == false)
            		{
            		 pointsList.add(p1);
            		 Log.i("TeamHex", "Added point one.");
            		}
            		if(pointsList.contains(p2) == false)
            		{
            		 pointsList.add(p2);
            		 Log.i("TeamHex", "Added point two.");
            		}
            		
            		// Draw the square
                    Point o1 = pointsList.get(0);
                    Point o2 = pointsList.get(1);
                    canvas.drawLine(o1.x, o1.y, p1.x, p1.y, paint);
                    canvas.drawLine(o1.x, o1.y, p2.x, p2.y, paint);
                    canvas.drawLine(o2.x, o2.y, p1.x, p1.y, paint);
                    canvas.drawLine(o2.x, o2.y, p2.x, p2.y, paint);
                    square = true;
                    
                    //Find top left point
                    int topLeftX;
                    int topLeftY;
                    
                    if(o1.x > o2.x)
                    	topLeftX = o2.x;
                    else
                    	topLeftX = o1.x;
                    if(o1.y > o2.y)
                    	topLeftY = o2.y;
                    else
                    	topLeftY = o1.y;
                    
                    int width = Math.abs(o2.x - o1.x);
                    int height = Math.abs(o2.y - o1.y);
                    
                    Log.i("TeamHex", "Bitmap width: " + b.getWidth());
                    Log.i("TeamHex", "Top left X: " + (int)((double)topLeftX / canvas.getWidth() * b.getWidth()));
                    Log.i("TeamHex", "Top right Y: " + (int)((double)topLeftY / canvas.getHeight() * b.getHeight()));
                    Log.i("TeamHex", "Width: " + ((int) ((double)width / canvas.getWidth() * b.getWidth())));
                    Log.i("TeamHex", "Height: " + ((int) ((double)height / canvas.getHeight() * b.getHeight())));
                    Log.i("TeamHex", "Width only: " + width);
                    Log.i("TeamHex", "Height only: " + height);
                    
                    int bTopLeftX = (int)((double)topLeftX / canvas.getWidth()  * b.getWidth()),
                    	bTopLeftY = (int)((double)topLeftY / canvas.getHeight() * b.getHeight()),
                    	bWidth    = (int)((double)width    / canvas.getWidth()  * b.getWidth()),
                    	bHeight   = (int)((double)height   / canvas.getHeight() * b.getHeight());
                    
                    
                    pixels = new int[width * height];
                    b.getPixels(pixels, 0, bWidth, bTopLeftX, bTopLeftY, bWidth, bHeight);
                    
	                //Allow the user to make another select
	          		//touchLift = false;
	          		touchState = 0;
	          		
	          		//Process the selection
	          		onSelectionListener.onSelection();
	          		
                    //i1.putExtra("com.teamhex.cooler.polygonPixels", pixels);
                    
              }
            }*/
	}
	
	// Clears the selected area's points and path
	public void resetSelection()
	{
		mPointList.clear();
		mDrawPath.reset();
	}
	
	// The complete listing of how to react to touch events
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if(mSelectionType == SelectionType.SELECTION_TYPE_LASSO)
		{
			// If this is a touchLift, simply return true to bundle pixels & analyze
			/*if(mTouchState == TouchState.TOUCH_STATE_LIFT) 
			{
				return true;
			}*/
			
			// Get the X,Y location of the event, and bounds-check it to be within the box
			float touchX = Math.max(0, Math.min(mCanvas.getWidth() - 1, event.getX()));
			float touchY = Math.max(0, Math.min(mCanvas.getHeight() - 1, event.getY()));
		 
			 // Depending on what type of action this touch event is...
			switch(event.getAction()) {
				
				// The finger is just now touching the screen; kill everything and start anew
				case MotionEvent.ACTION_DOWN:
					
					// Clear the old selection
					resetSelection(); 
					 
					mPointList.add(new Point((int)(touchX * mSettings.scaleFactor), (int)(touchY * mSettings.scaleFactor)));
					mDrawPath.moveTo(touchX, touchY);
				break;
				
				// Typical movement, get a new point and put it there
				case MotionEvent.ACTION_MOVE:
					 mPointList.add(new Point((int)(touchX * mSettings.scaleFactor), (int)(touchY * mSettings.scaleFactor)));
					 mDrawPath.lineTo(touchX, touchY);
					 //drawPath.cubicTo((float)(points.get(points.size()-1).x)/2, points.get(points.size()-1).y, touchX, touchY);
					 /*float x1 = mPointList.get(mPointList.size()-1).x,
						   y1 = mPointList.get(mPointList.size()-1).y,
						   x3 = touchX,
						   y3 = touchY,
					 	   x2 = (x1 + x3) / 2,
					 	   y2 = (y1 + y3) / 2;
						
					 //drawPath.moveTo(x1, y1);
					 mDrawPath.cubicTo(x1, y1, x2, y2, x3, y3);*/
				break;
				
				// The finger is no longer touching the screen, still add a point but also wrap it up
				case MotionEvent.ACTION_UP:
					 mPointList.add(new Point((int)(touchX * mSettings.scaleFactor), (int)(touchY * mSettings.scaleFactor)));
					 mDrawPath.lineTo(touchX, touchY);
					 /*mDrawPath.quadTo(mPointList.get(mPointList.size()-1).x, mPointList.get(mPointList.size()-1).y, touchX, touchY);
				
					 x3 = mPointList.get(0).x;
					 y3 = mPointList.get(0).y;
					 x1 = touchX;
					 y1 = touchY;
					 x2 = (x1 + x3) / 2;
					 y2 = (y1 + y3) / 2;
						
					 //drawPath.moveTo(x1, y1);
					 mDrawPath.cubicTo(x1, y1, x2, y2, x3, y3);*/
					 
					 mDrawPath.moveTo(touchX, touchY);
					 mDrawPath.lineTo(mPointList.get(0).x/mSettings.scaleFactor, mPointList.get(0).y/mSettings.scaleFactor);
					 
					 //touchLift = true;
					 mTouchState = TouchState.TOUCH_STATE_LIFT;
					 //mCanvas.drawPath(mDrawPath, mPaint);
					 //if(!touchLift) {
						 //drawPath.reset();
					 //}
				 break;
				 
				 // If it was none of the above, just do nothing
				 default:
					 return false;
			 }
		}
		//redraw
		invalidate();
		return true;

	}
	
	private void lassoScanlineAlgorithm()
	{
		 // When the user stops touching, determine if it's valid
		
		Log.i("TeamHex", "About to run the ray casting algorithm.");
	  
		// Get total image pixels
		// Get bounding box around points
		// ArrayList<Point> boundingBox = new ArrayList<Point>();

		int num_points = mPointList.size();
		
		if (num_points > 0)
		{
			int left = mPointList.get(0).x;
			int right = left;
			int top = mPointList.get(0).y;
			int bottom = top;
			
			int height;
			
			// Get top left and bottom right bounding box coordinates
			for(int a = 0; a < num_points; a++) 
			{
				// To do: should the capping be done here? 
				int ax = mPointList.get(a).x;
				int ay = mPointList.get(a).y;
				if(ax > right)     
					right = ax;
				if(ax < left) 
					left = ax;
				if(ay > bottom)    
					bottom = ay;
				if(ay < top)  
					top = ay;
			}
			
			// Redundant bounds checking
			left   = Math.max(left, 0);
			top    = Math.min(top, mCanvas.getHeight() - 1);
			right  = Math.min(right, mCanvas.getWidth() - 1);
			bottom = Math.max(bottom, 0);
	
			height = bottom - top;
		  
			/*Log.i("TeamHex", "The [top, right, bottom, left] coordinates are: [" + 
				Integer.toString(top) + "," +
				Integer.toString(right) + "," + 
				Integer.toString(bottom) + "," +
				Integer.toString(left) + "]");*/
		  
		    // Map each line segment in the lasso contour to the horizontal rows that it
		    // passes through. This saves us from performing collision checks on lines that
		    // wouldn't have intersected anyways.
		    @SuppressWarnings("unchecked")
		    ArrayList<Integer>[] lineMap = (ArrayList<Integer>[]) new ArrayList[height+1];
		    for (int i = 0; i <= height; i++) 
		    {
			    lineMap[i] = new ArrayList<Integer>();
		    }
		  
		    int y1, y2;
		    for (int i = 0; i < num_points; i++) {
			  if (i != (num_points - 1)) {
				  y1 = mPointList.get(i).y;
				  y2 = mPointList.get(i+1).y;
			  }
			  else {
				  y1 = mPointList.get(num_points - 1).y;
				  y2 = mPointList.get(0).y;
			  }
			  
			  if (y2 <= y1) {
				  int temp = y1;
				  y1 = y2;
				  y2 = temp;
			  }
			  
			  for(int j = y1; j <= y2; j++) {
				  lineMap[j-top].add(i);
			  }
		  }
		  
		  // Perform intersection checks with the lasso contour and each row inside of the
		  // bounding box. Each row will be split up into intervals. Because the lasso forms
		  // a line loop by its nature, every even-numbered interval will be within the area
		  // bounded by the lasso.

		  ArrayList<Point> polygonPixels = new ArrayList<Point>();
		  for(int i = 0; i <= height; i++) {
			  ArrayList<Double> intersections = new ArrayList<Double>();
			  ArrayList<Integer> lines = lineMap[i];
			  
			  for (int j = 0; j < lines.size(); j++) {
				  int lineID = lines.get(j);
				  Point p1, p2;
				  
				  if (lineID != (num_points - 1))
				  {
					  p1 = mPointList.get(lineID);
					  p2 = mPointList.get(lineID+1);
				  }
				  else
				  {
					  p1 = mPointList.get(num_points - 1);
					  p2 = mPointList.get(0);
				  }
				  
				  if (p1.x == p2.x)
				  {
					  intersections.add((double)p1.x);
				  }
				  else if (p1.y == p2.y)
				  {
					  intersections.add((double)p1.x);
					  intersections.add((double)p2.x);
				  }
				  else
				  {
					  double slope = (double)(p2.y - p1.y)/(double)(p2.x - p1.x);
					  double yInt = (double)p2.y - ((double)p2.x * slope);
					  
					  intersections.add(((double)(i + top) - yInt)/slope);
				  }
			  }
			  Collections.sort(intersections);

			  for (int j = 0; j < intersections.size() - 1; j += 2)
			  {
				  int y = i + top;
				  int x1 = (int)Math.floor(intersections.get(j));
				  int x2 = (int)Math.ceil(intersections.get(j+1));
				  
				  for (int k = x1; k <= x2; k++)
				  {
					  polygonPixels.add(new Point(k, y));
				  }
			  }
		  }
		  
		  //Send polygonPixels to MainActivity
		  //Conversion factor?
		  
		  mSelectionPixels = new int[polygonPixels.size()];
		  for(int a = 0; a < polygonPixels.size(); a++)
		  {
			int x = (int)((double)polygonPixels.get(a).x / mCanvas.getWidth() * mBitmap.getWidth());
			int y = (int)((double)polygonPixels.get(a).y / mCanvas.getHeight() * mBitmap.getHeight());
			mSelectionPixels[a] = mScaledBitmap.getPixel(x, y);  
		  }
		}
	}
	
 }

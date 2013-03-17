package org.nism.tellthetime;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;
import android.view.MotionEvent;


/**
 * This widget displays an analogic clock with two hands for hours and
 * minutes.
 */
@RemoteView
public class AnalogClockFace extends View {
    public  int     mQuantum = 1;

    private enum State { idle, adjusting_hours, adjusting_minutes };
	
    private Drawable mHourHand;
    private Drawable mHourHandSelected;
    private Drawable mMinuteHand;
    private Drawable mMinuteHandSelected;
    private Drawable mDial;
//thungy
    private int mDialWidth;
    private int mDialHeight;

    private boolean mAttached;

    private final   Handler mHandler = new Handler();
    private State   mState = State.idle;
    private float   mMinutes;
    private float   mHour=1.0f;
    private boolean mChanged;

    private float mHourHandLength;

    public AnalogClockFace(Context context) {
        this(context, null);
    }

    public AnalogClockFace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClockFace(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        Resources r = context.getResources();

        mDial = r.getDrawable(org.nism.tellthetime.R.drawable.clock_dial);
        mHourHand = r.getDrawable(org.nism.tellthetime.R.drawable.clock_hand_hour);
        mHourHandSelected = r.getDrawable(org.nism.tellthetime.R.drawable.clock_hand_hour_glowing);
        mMinuteHand = r.getDrawable(org.nism.tellthetime.R.drawable.clock_hand_minute);
        mMinuteHandSelected = r.getDrawable(org.nism.tellthetime.R.drawable.clock_hand_minute_glowing);

        //mCalendar = new Time();

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
        mHourHandLength = r.getFraction(org.nism.tellthetime.R.fraction.clock_hour_hand_length,1,1);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();


        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        //mCalendar = new Time();

        // Make sure we update to the current time
        onTimeChanged();
    }

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mDialWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mDialHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getRight() - getLeft();        
        int availableHeight = getBottom() - getTop();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                                   (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mState == State.adjusting_hours ?
        							mHourHandSelected : mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mState == State.adjusting_minutes ? 
        		                       mMinuteHandSelected : mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();

        if (scaled) {
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent mev) {
        int w = getRight() - getLeft();        
        int h = getBottom() - getTop();
        int x = w / 2;
        int y = h / 2;
        int radius = (w > h ? h : w)/2;  // Circular clock fits min of height and width
        
        // Find event position relative to centre of clock face
        float evX = mev.getX() - x;
        float evY = mev.getY() - y;
        // Find distance from centre (0..1) and angle (clockwise from 12 in radians)
        float r   = (float)Math.sqrt(evX*evX + evY*evY)/radius;
        float θ   = (float)Math.atan2(-evX, evY) + (float)Math.PI;
        
    	int mev_type = mev.getActionMasked();
    	if (mev_type == MotionEvent.ACTION_DOWN) {
    		// Hour hand or minute hand? Radius tells us.
    		if (r > mHourHandLength) {
    			mState = State.adjusting_minutes;
    			mChanged = true;
    		} else {
    			mState = State.adjusting_hours;
    			mChanged = true;
    		}
    	} else if (mev_type == MotionEvent.ACTION_UP) {
    		mState = State.idle;
    		mChanged = true;
    	}
    	
    	if (mState == State.adjusting_hours) {
    		// Find the offset of the touched position from the current hour
    		float hr_angle = 12f*θ/(2f*(float)Math.PI);
    		// Difference in hour between finger posn and current hour hand
    		float hr_offset = hr_angle - mHour;
    	
    		// Do we need to adjust the hour hand position?
    		if (Math.abs(hr_offset) > 0.6f) {
    			mHour += Math.round(hr_offset);
    			mChanged = true;
    		}
    	}

    	if (mState == State.adjusting_minutes) {
    		// Find the offset of the touched position from the current minute
    		float min_angle = 60f*θ/(2f*(float)Math.PI);
    		// Difference in minutes between finger posn and current hour hand
    		float min_offset = min_angle - mMinutes;
    	
    		// Do we need to adjust the minute hand position?
    		if (Math.abs(min_offset) > 0.6f * mQuantum) {
    			float prev_mins = mMinutes;
    			float mins_incr = mQuantum * Math.round(min_offset/mQuantum);
    			mMinutes = (mMinutes + mins_incr) % 60f;
    			mHour = (float) (Math.floor(mHour) + mMinutes/60f);
    			// Maybe the minute hand passed the hour...
    			if (prev_mins > 44f && mMinutes < 16f) { // forwards...
    				mHour += 1f;
    		    	System.out.println("Incr hr: mins was "+Float.toString(prev_mins)+" now "+Float.toString(mMinutes));
    			}    				
    			if (prev_mins < 16f && mMinutes > 44f) { // ...or backwards
    				mHour -= 1f;
    		    	System.out.println("Decr hr: mins was "+Float.toString(prev_mins)+" now "+Float.toString(mMinutes));
    			}
    			mChanged = true;
    		}
    	}

    	if (mChanged) {
    		invalidate();  // forces a redraw
    	}
    	return true;
    }
    
    private void onTimeChanged() {


    }

}

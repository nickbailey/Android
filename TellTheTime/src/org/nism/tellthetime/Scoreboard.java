/**
 * 
 */
package org.nism.tellthetime;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * @author Nick Bailey <nick@n-ism.org>
 *
 */
public class Scoreboard extends View {
	private static final int bezelWidth   = 60;    // Width of each score bar surround
	private static final int bezelHeight  = 300;   // Height of score bar surround
	private static final int barWidth     = 20;    // Width of bars within bezel
	private static final int margin       = 8;    // Margin to edge of view
	
	public float mCurrentScore;
	public float mAverageScore;
	public float mMaxScore;
	public int   mStars;
	
	private Paint mP = new Paint();
	
	private int mTextHeight;
	private int mAvColor, mCurColor, mBGColor, mTextColor;
	private Drawable mStar;
	private int mStarWidth, mStarHeight;
	private float mTotalWidth;
	
	public Scoreboard(Context context) { this(context, null); }
	public Scoreboard(Context context, AttributeSet attrs) { this(context, attrs, 0); }
	public Scoreboard(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
        Resources r = context.getResources();

    	// Find out the margin above the bars needed to display the score
        mP.setTextAlign(Align.CENTER);
    	Rect tr = new Rect();
    	mP.getTextBounds("8", 0, 1, tr);
    	mTextHeight = tr.height();
        mAvColor    = r.getColor(org.nism.tellthetime.R.color.scoreboard_av_color);
        mCurColor   = r.getColor(org.nism.tellthetime.R.color.scoreboard_cur_color);
        mBGColor    = r.getColor(org.nism.tellthetime.R.color.scoreboard_bg_color);
        mTextColor  = r.getColor(org.nism.tellthetime.R.color.scoreboard_text_color);
        mStar       = r.getDrawable(org.nism.tellthetime.R.drawable.gold_star);
        mStarWidth  = mStar.getIntrinsicWidth();
        mStarHeight = mStar.getIntrinsicHeight();
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        // We're drawing a star rating and two bars
        // a margin each side and in between, and an additional
        // 2 margins' padding for the star background.
        mTotalWidth = bezelWidth + mStarWidth+5*margin;
        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mTotalWidth) {
            hScale = (float)widthSize / (float)mTotalWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < bezelHeight) {
            vScale = (float )heightSize / (float)bezelHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(
        		resolveSizeAndState((int) (mTotalWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (bezelHeight * scale), heightMeasureSpec, 0));
    }

	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int availableWidth = getRight() - getLeft();        
        int availableHeight = getBottom() - getTop();
        int cl = (getLeft()+getRight())/2; 

        boolean scaled = false;

        if (availableWidth < mTotalWidth || availableHeight < bezelHeight) {
            scaled = true;
            float scale = Math.min((float)availableWidth / (float)mTotalWidth,
                                   (float)availableHeight / (float)bezelHeight);
            canvas.save();
            // Scale pivot includes room for the level indicator
            canvas.scale(scale, scale, cl + (mStarWidth+3*margin)/2, availableHeight/2);
        }
        
        // bump cl along to represent the centreline of the score bars
        cl += (mStarWidth + 3*margin)/2;

        // Draw the stars' background
        mP.setColor(mBGColor);
        int l = getLeft()+margin;
        int r = getRight()-margin;
        int t = getTop()+margin;
        int b = getBottom()-margin;
        canvas.drawRect(l, t, l+mStarWidth+2*margin, b, mP);
        
        // Draw the stars
        int sb = b-margin;
        for (int star = 0; star < mStars; star++ ) {
        	mStar.setBounds(l+margin, sb-mStarHeight,
        			        l+mStarWidth+margin, sb);
        	mStar.draw(canvas);
        	sb-=(mStarHeight+margin);
        }
        
        // Draw the bars' surround
        l += getLeft() + mStarWidth + 3*margin;
        canvas.drawRect(l, t, r, b, mP);
        
        // Draw the bars
        int rim = (bezelWidth-2*barWidth)/6;
        int bottom = b - rim;
        int barHeight = bottom - (t+rim+mTextHeight);
        
        // Running average
        int top = Math.round(bottom - barHeight*mAverageScore/mMaxScore);
        mP.setColor(mAvColor);
        canvas.drawRect(cl-(rim+barWidth), top,
        		        cl-rim, bottom, mP);
        mP.setColor(mTextColor);
        canvas.drawText(Integer.toString(Math.round(mAverageScore)),
        		        cl-(rim+barWidth/2), top-2, mP);

        // Current score
        top = Math.round(bottom - barHeight*mCurrentScore/mMaxScore);
        mP.setColor(mCurColor);
        canvas.drawRect(cl+rim, top,
        		        cl+rim+barWidth, bottom, mP);
        mP.setColor(mTextColor);
        canvas.drawText(Integer.toString(Math.round(mCurrentScore)),
		                cl+(rim+barWidth/2), top-2, mP);
        
        if (scaled) {
            canvas.restore();
        }
	
	}

}

package com.mobimation.ln4solutions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by gunnarforsgren on 15-09-02.
 */
public class CustomerId extends EditText {
    // EditText that formats a string according to the Ln4 Customer Id format.
    public CustomerId(Context context) {
        super(context);
    }

    public CustomerId(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerId(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onSelectionChanged(int start, int end) {

        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }

        super.onSelectionChanged(start, end);
    }

}

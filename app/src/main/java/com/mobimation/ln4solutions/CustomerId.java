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
    // EditText that supports formatting a string
    // according to the Ln4 Customer Id format.
    // TODO: The full intention of CustomerId is
    // TODO: to move the id input formatting into
    // TODO: Customerid. For now it is handled
    // TODO: in the LoginActivity2 activity.

    public CustomerId(Context context) {
        super(context);
    }

    public CustomerId(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerId(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Prevent the user from moving the cursor about in the EditText
    // component since that would confuse the current limited complexity
    // id formatting logic. Accomplishing this trick is currently the only
    // purpose of using CustomerId over a regular EditText.
    @Override
    public void onSelectionChanged(int start, int end) {

        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length()); // Force cursor to end
                return;
            }
        }

        super.onSelectionChanged(start, end);
    }
}

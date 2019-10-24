package org.cicipu.pangukeyboard;

/**
 * Created by Stuart on 09/01/2016.
 */
import android.inputmethodservice.KeyboardView;
import android.content.Context;
import android.util.AttributeSet;
import android.inputmethodservice.Keyboard.Key;
import android.view.inputmethod.InputMethodManager;

public class PanguKeyboardView extends KeyboardView {
    public PanguKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == 32) {
            InputMethodManager imeManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imeManager != null) {
                imeManager.showInputMethodPicker();
            }
            return true;
        } else {
            return super.onLongPress(key);
        }
    }
}
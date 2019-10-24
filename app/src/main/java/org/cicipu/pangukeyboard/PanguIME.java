package org.cicipu.pangukeyboard;

/**
 * Created by Stuart on 07/01/2016.
 */
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import java.util.List;

public class PanguIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener{

    private KeyboardView kv;
    private Keyboard keyboard;

    private boolean caps = false;
    private boolean qwerty = true;

    @Override
    public boolean onShowInputRequested(int flags, boolean configChange)
    {
        boolean show = super.onShowInputRequested(flags, configChange);
        if(show)
        {
            //Set the appropriate image for the DONE button
            if(keyboard != null) {
                List<Key> keys = keyboard.getKeys();
                if (keys != null) {


                    for (int keyIndex = 0; keyIndex < keys.size(); ++keyIndex) {

                        Key key = keys.get(keyIndex);
                        if (key.codes[0] == Keyboard.KEYCODE_DONE) {
                            int image;

                            final int options = this.getCurrentInputEditorInfo().imeOptions;
                            final int actionId = options & EditorInfo.IME_MASK_ACTION;

                            switch (actionId) {
                                case EditorInfo.IME_ACTION_SEARCH:
                                    image = R.drawable.sym_keyboard_search;
                                    break;
                                case EditorInfo.IME_ACTION_GO:
                                    image = R.drawable.sym_keyboard_go;
                                    break;
                                case EditorInfo.IME_ACTION_SEND:
                                    image = R.drawable.sym_keyboard_send;
                                    break;
                                default:
                                    image = R.drawable.sym_keyboard_return;
                                    break;
                            }

                            key.icon = this.getApplicationContext().getDrawable(image);
                            kv.invalidateKey(keyIndex);
                        }
                    }
                }
            }
        }

        return show;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch(primaryCode){
            case Keyboard.KEYCODE_MODE_CHANGE :
                int keyboardID;

                if(qwerty)
                {
                    keyboardID = R.xml.symbols;
                }
                else
                {
                    keyboardID = R.xml.qwerty;
                }

                keyboard = new Keyboard(this, keyboardID);
                kv.setKeyboard(keyboard);

                qwerty = !qwerty;
                break;
            case Keyboard.KEYCODE_DELETE :
                ic.deleteSurroundingText(1, 0);
                onKeyLongPress(32, null);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);

                List<Key> keys = keyboard.getKeys();
                for (Key key : keys) {
                    if (key.codes[0] == Keyboard.KEYCODE_SHIFT)
                    {
                        int image;

                        if(caps)
                        {
                            image = R.drawable.sym_keyboard_shift_locked_80;
                        }
                        else
                        {
                            image = R.drawable.sym_keyboard_shift_80;
                        }

                        key.icon = this.getApplicationContext().getDrawable(image);
                    }
                }

                kv.invalidateAllKeys();
                break;


            case Keyboard.KEYCODE_DONE:
                final int options = this.getCurrentInputEditorInfo().imeOptions;
                final int actionId = options & EditorInfo.IME_MASK_ACTION;

                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        sendDefaultEditorAction(true);
                        break;
                    case EditorInfo.IME_ACTION_GO:
                        sendDefaultEditorAction(true);
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        sendDefaultEditorAction(true);
                        break;
                    default:
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                        break;
                }
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && caps){
                    //special hack for x which has no capital in Pangu
                    if(code == 'x') {
                        //do nothing
                    }
                    else {
                        code = Character.toUpperCase(code);
                    }
                }
                ic.commitText(String.valueOf(code),1);
        }
    }

    @Override
    public void onPress(int primaryCode) {
        kv.setPreviewEnabled(false);
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    //needed for popup menus with output text
    @Override
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();

        playClick(248); //q

        //TODO - upper case saltillo
        if(caps) {
            if (text.equals("a̱")) {
                text = "A̱";
            }
            else if (text.equals("e̱")) {
                text = "E̱";
            }
            else if (text.equals("o̱")) {
                text = "O̱";
            }
            else if (text.equals("á̱")) {
                text = "Á̱";
            }
            else if (text.equals("é̱")) {
                text = "É̱";
            }
            else if (text.equals("ó̱")) {
                text = "Ó̱";
            }
        }


        ic.commitText(text, 1);
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        return kv;
    }

    private void playClick(int keyCode){
//no sound for now
        /*
        //the sound depends on the key pressed
        int sound;
        switch(keyCode){
            case 32:
                sound = AudioManager.FX_KEYPRESS_SPACEBAR;
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                sound = AudioManager.FX_KEYPRESS_RETURN;
                break;
            case Keyboard.KEYCODE_DELETE:
                sound = AudioManager.FX_KEYPRESS_DELETE;
                break;
            default:
                sound = AudioManager.FX_KEYPRESS_STANDARD;
                break;
        }

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.playSoundEffect(sound, 0.5f); //half volume*/

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if(vibrator != null) {
            vibrator.vibrate(25);
        }
    }
}

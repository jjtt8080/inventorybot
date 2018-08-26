package com.gegejiejie.inventory;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class StateObject implements Parcelable {
    public static final int LISTNER_KEY = 10;
    public static final int TALKER_KEY = 20;
    public static final int LISTNER_STOP_KEY = 30;

    private int state = INITIAL_STATE; /* State can be 0 to 100 */
    private int confirm_state = 0; /* can be 0, or 1, or 2 , 0 means original state, 1 means confirming state, 2 means confirmed state*/
    private String promptText;
    private String answerText;

    public static final int INITIAL_STATE = 1;
    public static final int EMP_ID_STATE= 2;
    public static final int STORE_ID_STATE= 4;
    public static final int SCAN_PRODUCT_STATE= 8;
    public static final int SCAN_QUANTITY_STATE = 16;
    public static final int FINISH_STATE=32;
    public static final int FINISH_STATE_CONFIRMED=64;
    public static final String EMP_ID_PROMPT = "What is your ID?";
    public static final String STORE_ID_PROMPT = "What is your Store ID?";
    public static final String PRODUCT_ID_PROMPT = "Scan Product ID? ";
    public static final String QUANTITY_PROMPT = "What is the quantity for the Product ID?";
    public static final String CONFIRMED_TEXT = "Is that right?";
    public static final String NUMBER_FORMAT_ERROR_TEXT = "Sorry it does not look like digits. Please say like one two three, slowly.";
    public static final ArrayList<String> RIGHT_STRINGS = new ArrayList<String>( Arrays.asList("right", "okay", "ok", "yes"));
    public static final ArrayList<String> WRONG_STRINGS = new ArrayList<String>( Arrays.asList("wrong", "not okay", "no", "not"));
    public static final ArrayList<String> FINISH_STRINGS = new ArrayList<String>( Arrays.asList("finish", "done", "out", "finished"));
    public static final String FINISH_PROMPT= "FINISH";
    public static final String STATE_OBJECT_TAG = "STATE_OBJ";

    protected int getState() {return state;}
    protected int getConfirmedState() {return confirm_state;}
    protected String getPromptText() {return promptText;}
    protected String getAnswerText() {return answerText;}
    protected void setFinished() {state = FINISH_STATE;}
    protected void setConfirmState(int c) {confirm_state = c;}
    protected void setAnswerText(String t) {answerText = t;}
    protected StateObject() {}
    class InvalidStateException extends Exception{
        private int state = 0;
        public InvalidStateException(int state) {
        }
        @Override
        public String toString() {
            return "Invalid state" + Integer.toString(state);
        }
    }
    private static String composeConfirmedText(InventoryObject obj, StateObject stateObj)
    {
        String r = null;
        switch(stateObj.state)
        {
            case EMP_ID_STATE:
                r = "Your employee ID is ";
                r += Integer.toString(obj.emp_id);
                r += ", ";
                r += CONFIRMED_TEXT;
                break;
            case STORE_ID_STATE:
                r = "Your store ID is ";
                r += Integer.toString(obj.store_id);
                r += ", ";
                r += CONFIRMED_TEXT;
                break;
            case SCAN_PRODUCT_STATE :
                r = "Current product ID is ";
                r += ParseDigitInput.longDigitToString(obj.curr_prod_id);
                r += ", ";
                r += CONFIRMED_TEXT;
                break;
            case SCAN_QUANTITY_STATE:
                r = "Current quantity is ";
                r += Integer.toString(obj.curr_quantity);
                r += ", ";
                r += CONFIRMED_TEXT;
                break;
            case FINISH_STATE:
                r = "Here is the summary. ";
                r += obj.toString();
                break;
            case FINISH_STATE_CONFIRMED:
                r = "Finish confirmed.";
                break;
        }
        return r;
    }

    static public String getPromptTextOnState(InventoryObject obj, StateObject stateObj)
    {
        String r = "";
        if (stateObj.getConfirmedState() == 1) {
            return composeConfirmedText(obj, stateObj);
        }

        if (stateObj.getConfirmedState() == 2) {
            r = "Let's try again.";
            stateObj.setConfirmState(0);
        }
        if (stateObj.state == INITIAL_STATE) {
            try {
                stateObj.setNextState();
            } catch(InvalidStateException ex) {
                Log.e("Error logic", "storeResultOnState");
            }
        }
        switch(stateObj.state)
        {
            case INITIAL_STATE:
                r += "This shouldn't happen";
                break;
            case EMP_ID_STATE:
                r += EMP_ID_PROMPT;
                break;
            case STORE_ID_STATE:
                r += STORE_ID_PROMPT;
                break;
            case SCAN_PRODUCT_STATE :
                r += PRODUCT_ID_PROMPT;
                break;
            case SCAN_QUANTITY_STATE :
                r += QUANTITY_PROMPT;
                break;
            case FINISH_STATE:
                r += obj.toString();
                break;
            case FINISH_STATE_CONFIRMED:
                r += "FINISH CONFIRMED";
        }



        return r;

    }
    public void setNextState() throws InvalidStateException {
        if (state >= INITIAL_STATE && state <= FINISH_STATE) {
            if (state == SCAN_QUANTITY_STATE) {
                state = SCAN_PRODUCT_STATE;
            } else {
                state = state << 1;
            }
        }
        else {
            throw new InvalidStateException(state);
        }

    }
    protected boolean isRightString(String text) {
        return RIGHT_STRINGS.contains(text);
    }
    protected boolean isWrongString(String text) {
        return WRONG_STRINGS.contains(text);
    }
    protected boolean isFinishString(String text) {
        return FINISH_STRINGS.contains(text);
    }
    /* return true if the result is valid */
    protected boolean storeResultOnState(InventoryObject inventoryObj, String text)  {
        if (confirm_state == 1 && isRightString(text))
        {
            try {
                if (state > INITIAL_STATE && state <= FINISH_STATE)
                    setNextState();
            } catch(InvalidStateException ex) {
                Log.e("Error logic", "storeResultOnState");
            }
            //confirm_state = 0;
            return true;
        }
        if (confirm_state == 1 && isWrongString(text))
        {
            if (state == SCAN_QUANTITY_STATE) {
                int lastIndex = inventoryObj.products_list.size() - 1;
                if (lastIndex >= 0) {
                    inventoryObj.products_list.remove(lastIndex);
                    inventoryObj.quantity_list.remove(lastIndex);
                }
            }
            return false;
        }
        if (isFinishString(text)) {
            Log.e("StateObj", "finished");
            state = FINISH_STATE;
            return true;
        }
        try {
            switch (state) {
                case EMP_ID_STATE:
                    inventoryObj.emp_id = ParseDigitInput.parseInt(text);
                    Log.e("StateObj, employeid", Integer.toString(inventoryObj.emp_id));
                    break;
                case STORE_ID_STATE:
                    inventoryObj.store_id = ParseDigitInput.parseInt(text);
                    Log.e("StateObj, storeid", Integer.toString(inventoryObj.store_id));
                    break;
                case SCAN_PRODUCT_STATE:
                    inventoryObj.curr_prod_id = text;
                    Log.e("StateObj, productid", inventoryObj.curr_prod_id);

                    break;
                case SCAN_QUANTITY_STATE:
                    inventoryObj.curr_quantity = ParseDigitInput.parseInt(text);
                    Log.e("StateObj, quantity", Integer.toString(inventoryObj.curr_quantity));
                    inventoryObj.products_list.add(inventoryObj.curr_prod_id);
                    inventoryObj.quantity_list.add(inventoryObj.curr_quantity);
                    break;


            }
            return true;
        }catch(NumberFormatException ex) {
            Log.e("StateObject", ex.toString());
            return false;
        }
    }
    public static final Parcelable.Creator<StateObject> CREATOR = new Parcelable.Creator<StateObject>() {
        public StateObject createFromParcel(Parcel in) {
            return new StateObject(in);
        }

        public StateObject[] newArray(int size) {
            return new StateObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(state);
        out.writeInt(confirm_state);
        out.writeString(promptText);
        out.writeString(answerText);
    }
    private StateObject(Parcel in) {
        state = in.readInt();
        confirm_state = in.readInt();
        promptText= in.readString();
        answerText = in.readString();

    }
}

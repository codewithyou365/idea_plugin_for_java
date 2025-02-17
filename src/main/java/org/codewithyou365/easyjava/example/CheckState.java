package org.codewithyou365.easyjava.example;

import java.util.List;

public class CheckState {
    public enum MyState {
        ITEM_DISABLE,
        ITEM_ENABLE,
        ITEM_SALE_SUCCESS,
        ITEM_SALE_FAILED,
        ITEM_SALE_UNKNOWN,
        ITEM_SALE_START,
        ITEM_SHIPPED,
        ;

        public Integer getCode() {
            return 0;
        }

        public void checkState(String... s) {
        }
    }

    private void transfer(MyState... states) {
    }

    public void enable(List<Long> key, Object operator, Record r) {
        updateState(from(MyState.ITEM_DISABLE),
                MyState.ITEM_ENABLE);
    }

    private void updateState(Object from, MyState to) {
    }

    private Object from(MyState... states) {
        return null;
    }

    public boolean pay(Integer state) {
        final boolean[] ret = {false};
        if (MyState.ITEM_ENABLE.getCode().equals(state)) {
            transfer(MyState.ITEM_SALE_START,
                    MyState.ITEM_SALE_SUCCESS,
                    MyState.ITEM_SALE_FAILED,
                    MyState.ITEM_SALE_UNKNOWN);
            ret[0] = true;
            updateState(MyState.ITEM_SALE_SUCCESS, MyState.ITEM_SHIPPED);
        }
        return true;
    }

    // timer
    public boolean move(Integer state) {
        if (MyState.ITEM_SALE_SUCCESS.getCode().equals(state)) {
            updateState(MyState.ITEM_SALE_SUCCESS, MyState.ITEM_SHIPPED);
        }
        return true;
    }


    public static void main(String[] args) {
        MyState _checkState = MyState.ITEM_SALE_SUCCESS;
        // The name must be `_checkState` to activate the `autoCheckState` prompt
        _checkState.checkState("ITEM_DISABLE","ITEM_ENABLE","28","ITEM_ENABLE","ITEM_SALE_START,ITEM_SALE_SUCCESS,ITEM_SALE_FAILED,ITEM_SALE_UNKNOWN,ITEM_SALE_SUCCESS,ITEM_SHIPPED","41","ITEM_SALE_SUCCESS","ITEM_SALE_SUCCESS,ITEM_SHIPPED","54");
    }
}

package org.gdprcmplib;

import android.content.Context;
import android.content.Intent;

public class GdprCmp {

    public static void startCmpActivity(Context context) {
        context.startActivity(new Intent(context, CmpActivity.class));
    }
}

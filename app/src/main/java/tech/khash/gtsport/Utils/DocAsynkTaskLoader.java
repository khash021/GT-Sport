package tech.khash.gtsport.Utils;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class DocAsynkTaskLoader extends AsyncTaskLoader {
    public DocAsynkTaskLoader(Context context) {
        super(context);
    }

    @Override
    public Object loadInBackground() {
        return null;
    }
}

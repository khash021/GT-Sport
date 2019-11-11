package tech.khash.gtsport.Utils;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class DocAsynkLoader extends AsyncTask<String, Void, Document> {

    public interface AsyncResponse {
        void processFinish(Document doc);
    }

    private AsyncResponse delegate = null;

    public DocAsynkLoader(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Document doInBackground(String... strings) {
        try {
            Document doc = Jsoup.connect(strings[0]).get();
            return doc;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Document document) {
        delegate.processFinish(document);
    }
}

package app.grapheneos.pdfviewer.loader;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.List;

public class DocumentPropertiesAsyncTaskLoader extends AsyncTaskLoader<List<CharSequence>> {

    public static final String TAG = "DocumentPropertiesLoader";

    public static final int ID = 1;

    private final String mProperties;
    private final int mNumPages;

    String fileName;
    Long fileSize;

    public DocumentPropertiesAsyncTaskLoader(Context context, String properties, int numPages, String fileName, Long fileSize) {
        super(context);

        mProperties = properties;
        mNumPages = numPages;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }


    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<CharSequence> loadInBackground() {

        DocumentPropertiesLoader loader = new DocumentPropertiesLoader(
                getContext(),
                mProperties,
                mNumPages,
                fileName,
                fileSize
        );

        return loader.loadAsList();
    }
}

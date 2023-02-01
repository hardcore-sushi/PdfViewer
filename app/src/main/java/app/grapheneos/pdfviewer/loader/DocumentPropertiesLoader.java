package app.grapheneos.pdfviewer.loader;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import app.grapheneos.pdfviewer.R;
import app.grapheneos.pdfviewer.Utils;

public class DocumentPropertiesLoader extends AsyncTaskLoader<List<CharSequence>> {
    public static final String TAG = "DocumentPropertiesLoader";

    public static final int ID = 1;

    private final String mProperties;
    private final int mNumPages;

    String fileName;
    Long fileSize;

    public DocumentPropertiesLoader(Context context, String properties, int numPages, String fileName, Long fileSize) {
        super(context);

        mProperties = properties;
        mNumPages = numPages;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public List<CharSequence> loadInBackground() {
        final Context context = getContext();

        final String[] names = context.getResources().getStringArray(R.array.property_names);
        final List<CharSequence> properties = new ArrayList<>(names.length);

        properties.add(getProperty(null, names[0], fileName));
        properties.add(getProperty(null, names[1], Utils.parseFileSize(fileSize)));

        try {
            final JSONObject json = new JSONObject(mProperties);

            properties.add(getProperty(json, names[2], "Title"));
            properties.add(getProperty(json, names[3], "Author"));
            properties.add(getProperty(json, names[4], "Subject"));
            properties.add(getProperty(json, names[5], "Keywords"));
            properties.add(getProperty(json, names[6], "CreationDate"));
            properties.add(getProperty(json, names[7], "ModDate"));
            properties.add(getProperty(json, names[8], "Producer"));
            properties.add(getProperty(json, names[9], "Creator"));
            properties.add(getProperty(json, names[10], "PDFFormatVersion"));
            properties.add(getProperty(null, names[11], String.valueOf(mNumPages)));

            return properties;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deliverResult(List<CharSequence> properties) {
        if (!isReset() && isStarted()) {
            super.deliverResult(properties);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
    }

    private CharSequence getProperty(final JSONObject json, String name, String specName) {
        final SpannableStringBuilder property = new SpannableStringBuilder(name).append(":\n");
        final String value = json != null ? json.optString(specName, "-") : specName;

        if (specName != null && specName.endsWith("Date")) {
            final Context context = getContext();
            try {
                property.append(value.equals("-") ? value : Utils.parseDate(value));
            } catch (ParseException e) {
                Log.w(TAG, e.getMessage() + " for " + value + " at offset: " + e.getErrorOffset());
                property.append(context.getString(R.string.document_properties_invalid_date));
            }
        } else {
            property.append(value);
        }
        property.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return property;
    }
}

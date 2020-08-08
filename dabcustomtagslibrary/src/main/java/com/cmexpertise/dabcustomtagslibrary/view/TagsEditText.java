package com.cmexpertise.dabcustomtagslibrary.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.cmexpertise.dabcustomtagslibrary.R;

import com.cmexpertise.dabcustomtagslibrary.models.TagModel;
import com.cmexpertise.dabcustomtagslibrary.resources.ResourceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Created By Dwitiyabhatt....
 */

public class TagsEditText extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

    public static final String NEW_LINE = "\n";

    private static final String LAST_STRING = "lastString";
    private static final String TAGS = "tags";
    private static final String SUPER_STATE = "superState";
    private static final String UNDER_CONSTRUCTION_TAG = "underConstructionTag";
    private static final String ALLOW_SPACES_IN_TAGS = "allowSpacesInTags";

    private static final String TAGS_BACKGROUND_RESOURCE = "tagsBackground";
    private static final String TAGS_TEXT_COLOR = "tagsTextColor";
    private static final String TAGS_TEXT_SIZE = "tagsTextSize";
    private static final String LEFT_DRAWABLE_RESOURCE = "leftDrawable";
    private static final String RIGHT_DRAWABLE_RESOURCE = "rightDrawable";
    private static final String DRAWABLE_PADDING = "drawablePadding";

    private String mSeparator = " ";
    private String mLastString = "";
    private boolean mIsAfterTextWatcherEnabled = true;

    private int mTagsTextColor;
    private float mTagsTextSize;
    private Drawable mTagsBackground;
    private int mTagsBackgroundResource = 0;
    private Drawable mLeftDrawable;
    private int mLeftDrawableResouce = 0;

    private Drawable mRightDrawable;
    private int mRightDrawableResouce = 0;

    private int mDrawablePadding;

    private int mTagsPaddingLeft;
    private int mTagsPaddingRight;
    private int mTagsPaddingTop;
    private int mTagsPaddingBottom;

    private boolean mIsSpacesAllowedInTags = false;
    private boolean mIsSetTextDisabled = false;

    private List<TagSpan> mTagSpans = new ArrayList<>();
    private List<Tag> mTags = new ArrayList<>();


    private List<Tag> tags = new ArrayList<>();

    private List<TagModel> tagModelListMain;
    private List<TagModel> tagModelListRemoved;

    private ArrayAdapter tagListAdapter;
    private Activity activity;
    private int layoutId;

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mIsAfterTextWatcherEnabled) {
                setTags();
            }
        }
    };

    public List<TagModel> getTagModelListMain() {
        return tagModelListMain;
    }

    public void setTagModelListMain(List<TagModel> tagModelListMain) {
        this.tagModelListMain = tagModelListMain;
    }

    public List<TagModel> getTagModelListRemoved() {
        return tagModelListRemoved;
    }

    public void setTagModelListRemoved(List<TagModel> tagModelListRemoved) {
        this.tagModelListRemoved = tagModelListRemoved;
    }

    public void initializeAdapter(Activity activity, int layoutId, List<TagModel> initialTagModelList){

        this.activity = activity;
        this.layoutId = layoutId;
        tagModelListMain = initialTagModelList;
        tagModelListRemoved = new ArrayList<>();

        resetAdapter();
    }

    public void resetAdapter(){
        Collections.sort(tagModelListMain, new Comparator<TagModel>()
        {
            @Override
            public int compare(TagModel lhs, TagModel rhs) {
                return rhs.getName().compareTo(lhs.getName());
            }
        });


        tagListAdapter = new ArrayAdapter(
                activity,
                layoutId, tagModelListMain);
        setAdapter(tagListAdapter);
    }

    public List<String> getTags(){
        return convertTagSpanToList(mTagSpans);
    }

    public void setSeparator(String separator) {
        mSeparator = separator;
    }

    public TagsEditText(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public TagsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public TagsEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

   /* @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagsEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (getText() != null) {
            setSelection(getText().length());
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    /**
     * do not use this method to set tags
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mIsSetTextDisabled) return;
        if (!TextUtils.isEmpty(text)) {
            String source = mIsSpacesAllowedInTags ? text.toString().trim() : text.toString().replaceAll(" ", "");
            if (mTags.isEmpty()) {
                Tag tag = new Tag();
                tag.setIndex(0);
                tag.setPosition(0);
                tag.setSource(source);
                tag.setSpan(true);
                mTags.add(tag);
            } else {
                int size = mTags.size();
                Tag lastTag = mTags.get(size - 1);
                if (!lastTag.isSpan()) {
                    lastTag.setSource(source);
                    lastTag.setSpan(true);
                } else {
                    Tag newTag = new Tag();
                    newTag.setIndex(size);
                    newTag.setPosition(lastTag.getPosition() + lastTag.getSource().length() + 1);
                    newTag.setSource(source);
                    newTag.setSpan(true);
                    mTags.add(newTag);
                }
            }
            buildStringWithTags(mTags);
            mTextWatcher.afterTextChanged(getText());
        } else {
            super.setText(text, type);
        }
    }

    /**
     * use this method to set tags
     */
    public void setTags(CharSequence... tags) {
        mTagSpans.clear();
        mTags.clear();

        int length = tags != null ? tags.length : 0;
        int position = 0;
        for (int i = 0; i < length; i++) {
            Tag tag = new Tag();
            tag.setIndex(i);
            tag.setPosition(position);
            String source = mIsSpacesAllowedInTags ? tags[i].toString().trim() : tags[i].toString().replaceAll(" ", "");
            tag.setSource(source);
            tag.setSpan(true);
            mTags.add(tag);
            position += source.length() + 1;
        }
        buildStringWithTags(mTags);
        mTextWatcher.afterTextChanged(getText());
    }

    /**
     * use this method to set tags
     */

    public void setTags(String[] tags) {
        mTagSpans.clear();
        mTags.clear();

        int length = tags != null ? tags.length : 0;
        int position = 0;
        for (int i = 0; i < length; i++) {
            Tag tag = new Tag();
            tag.setIndex(i);
            tag.setPosition(position);
            String source = mIsSpacesAllowedInTags ? tags[i].trim() : tags[i].replaceAll(" ", "");
            tag.setSource(source);
            tag.setSpan(true);
            mTags.add(tag);
            position += source.length() + 1;
        }
        buildStringWithTags(mTags);
        mTextWatcher.afterTextChanged(getText());
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());

        Tag[] tags = new Tag[mTags.size()];
        mTags.toArray(tags);

        bundle.putParcelableArray(TAGS, tags);
        bundle.putString(LAST_STRING, mLastString);
        bundle.putString(UNDER_CONSTRUCTION_TAG, getNewTag(getText().toString()));

        bundle.putInt(TAGS_TEXT_COLOR, mTagsTextColor);
        bundle.putInt(TAGS_BACKGROUND_RESOURCE, mTagsBackgroundResource);
        bundle.putFloat(TAGS_TEXT_SIZE, mTagsTextSize);
        bundle.putInt(LEFT_DRAWABLE_RESOURCE, mLeftDrawableResouce);
        bundle.putInt(RIGHT_DRAWABLE_RESOURCE, mRightDrawableResouce);
        bundle.putInt(DRAWABLE_PADDING, mDrawablePadding);
        bundle.putBoolean(ALLOW_SPACES_IN_TAGS, mIsSpacesAllowedInTags);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Context context = getContext();
            Bundle bundle = (Bundle) state;

            mTagsTextColor = bundle.getInt(TAGS_TEXT_COLOR, mTagsTextColor);

            mTagsBackgroundResource = bundle.getInt(TAGS_BACKGROUND_RESOURCE, mTagsBackgroundResource);
            if (mTagsBackgroundResource != 0) {
                mTagsBackground = ContextCompat.getDrawable(context, mTagsBackgroundResource);
            }

            mTagsTextSize = bundle.getFloat(TAGS_TEXT_SIZE, mTagsTextSize);

            mLeftDrawableResouce = bundle.getInt(LEFT_DRAWABLE_RESOURCE, mLeftDrawableResouce);
            if (mLeftDrawableResouce != 0) {
                mLeftDrawable = ContextCompat.getDrawable(context, mLeftDrawableResouce);
            }

            mRightDrawableResouce = bundle.getInt(RIGHT_DRAWABLE_RESOURCE, mRightDrawableResouce);
            if (mRightDrawableResouce != 0) {
                mRightDrawable = ContextCompat.getDrawable(context, mRightDrawableResouce);
            }

            mDrawablePadding = bundle.getInt(DRAWABLE_PADDING, mDrawablePadding);
            mIsSpacesAllowedInTags = bundle.getBoolean(ALLOW_SPACES_IN_TAGS, mIsSpacesAllowedInTags);

            mLastString = bundle.getString(LAST_STRING);
            Parcelable[] tagsParcelables = bundle.getParcelableArray(TAGS);
            if (tagsParcelables != null) {
                Tag[] tags = new Tag[tagsParcelables.length];
                System.arraycopy(tagsParcelables, 0, tags, 0, tagsParcelables.length);
                mTags = new ArrayList<>();
                Collections.addAll(mTags, tags);
                buildStringWithTags(mTags);
                mTextWatcher.afterTextChanged(getText());
            }
            state = bundle.getParcelable(SUPER_STATE);
            mIsSetTextDisabled = true;
            super.onRestoreInstanceState(state);
            mIsSetTextDisabled = false;

            String temp = bundle.getString(UNDER_CONSTRUCTION_TAG);
            if (!TextUtils.isEmpty(temp))
                getText().append(temp);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void buildStringWithTags(List<Tag> tags) {
        mIsAfterTextWatcherEnabled = false;
        this.mTags = tags;
        getText().clear();
        for (Tag tag : tags) {
            getText().append(tag.getSource()).append(mSeparator);
        }
        mLastString = getText().toString();
        if (!TextUtils.isEmpty(mLastString)) {
            getText().append(NEW_LINE);
        }
        mIsAfterTextWatcherEnabled = true;
    }

    public void setTagsWithSpacesEnabled(boolean isSpacesAllowedInTags) {
        mIsSpacesAllowedInTags = isSpacesAllowedInTags;
        setTags(convertTagSpanToArray(mTagSpans));
    }



    @ColorInt
    private int getColor(Context context, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23) {
            return ContextCompat.getColor(context, colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context = getContext();
        if (attrs == null) {
            mIsSpacesAllowedInTags = false;
            mTagsTextColor = getColor(context, R.color.defaultTagsTextColor);
            mTagsTextSize = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsTextSize);
            mTagsBackground = ContextCompat.getDrawable(context, R.drawable.oval);
            mRightDrawable = ContextCompat.getDrawable(context, R.drawable.tag_close);
            mDrawablePadding = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsCloseImagePadding);
            mTagsPaddingRight = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding);
            mTagsPaddingLeft = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding);
            mTagsPaddingTop = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding);
            mTagsPaddingBottom = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding);
        } else {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagsEditText, defStyleAttr, defStyleRes);
            try {
                mIsSpacesAllowedInTags = typedArray.getBoolean(R.styleable.TagsEditText_allowSpaceInTag, false);
                mTagsTextColor = typedArray.getColor(R.styleable.TagsEditText_tagsTextColor,
                        getColor(context, R.color.defaultTagsTextColor));
                mTagsTextSize = typedArray.getDimensionPixelSize(R.styleable.TagsEditText_tagsTextSize,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsTextSize));
                mTagsBackground = typedArray.getDrawable(R.styleable.TagsEditText_tagsBackground);
                mRightDrawable = typedArray.getDrawable(R.styleable.TagsEditText_tagsCloseImageRight);
                mLeftDrawable = typedArray.getDrawable(R.styleable.TagsEditText_tagsCloseImageLeft);
                mDrawablePadding = typedArray.getDimensionPixelOffset(R.styleable.TagsEditText_tagsCloseImagePadding,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsCloseImagePadding));
                mTagsPaddingRight = typedArray.getDimensionPixelSize(R.styleable.TagsEditText_tagsPaddingRight,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding));
                mTagsPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.TagsEditText_tagsPaddingLeft,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding));
                mTagsPaddingTop = typedArray.getDimensionPixelSize(R.styleable.TagsEditText_tagsPaddingTop,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding));
                mTagsPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.TagsEditText_tagsPaddingBottom,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsPadding));
            } finally {
                typedArray.recycle();
            }
        }

        setMovementMethod(LinkMovementMethod.getInstance());
        setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    addTextChangedListener(mTextWatcher);
                    mTextWatcher.afterTextChanged(getText());
                }
            });
        }
    }

    private void setTags() {
        mIsAfterTextWatcherEnabled = false;
        boolean isEnterClicked = false;

        final Editable editable = getText();
        String str = editable.toString();

        if (str.endsWith(NEW_LINE)) {
            isEnterClicked = true;
        }

        boolean isDeleting = mLastString.length() > str.length();
        if (mLastString.endsWith(mSeparator)
                && !str.endsWith(NEW_LINE)
                && isDeleting
                && !mTagSpans.isEmpty()) {
            TagSpan toRemoveSpan = mTagSpans.get(mTagSpans.size() - 1);
            Tag tag = toRemoveSpan.getTag();
            if (tag.getPosition() + tag.getSource().length() == str.length()) {
                removeTagSpan(editable, toRemoveSpan, false);
                str = editable.toString();
            }
        }

        if (getFilter() != null) {
            performFiltering(getNewTag(str), 0);
        }

        if (str.endsWith(NEW_LINE) || (!mIsSpacesAllowedInTags && str.endsWith(mSeparator)) && !isDeleting) {
            buildTags(str);
        }

        mLastString = getText().toString();
        mIsAfterTextWatcherEnabled = true;
        /*if (isEnterClicked && mListener != null) {
            mListener.onEditingFinished();
        }*/
    }

    private void buildTags(String str) {
        if (str.length() != 0) {
           // Log.d("tags_xxx", "str1 "+str+" lst_str "+str);
            Log.d("tags_xxx", "called");
            updateTags(str);

            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (final TagSpan tagSpan : mTagSpans) {
                addTagSpan(sb, tagSpan);

            }

            int size = mTags.size();
            Log.d("tags_xxx", "Tag spans size "+mTagSpans.size());
            Log.d("tags_xxx", "Tags size "+mTags.size());
            for (int i = mTagSpans.size(); i < size; i++) {
                Tag tag = mTags.get(i);
                String source = tag.getSource();
                Log.d("tags_xxx", "source "+tag.getSource());
                if (tag.isSpan()) {

                    // 13 Nov 2019
                    // Code to shorten player name
                  /*  String[] splited = source.split(" ");
                    String shortname="";
                    for(int k =0;k<splited.length;k++){
                        String temp = splited[k];
                        shortname+=temp.charAt(0);

                    }*/
                    TextView tv = createTextView(source);
                    Drawable bd = convertViewToDrawable(tv);
                    bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
                    final TagSpan span = new TagSpan(bd, source);
                    addTagSpan(sb, span);
                    span.setTag(tag);
                    mTagSpans.add(span);
                } else {
                    sb.append(source);
                }
            }

            getText().clear();
            getText().append(sb);
            setMovementMethod(LinkMovementMethod.getInstance());
            setSelection(sb.length());

            onTagsChanged(convertTagSpanToList(mTagSpans),sb.toString(),true);

            /*if (str.equals(mLastString)) {
                //Log.d("tags_xxxx", "str1 "+str+" lst_str "+mLastString);
                onTagsChanged(convertTagSpanToList(mTagSpans),sb.toString(),true);
            }else{
              //  Log.d("tags_xxxx", "str2 "+str+" lst_str"+mLastString);
            }*/
        }
    }

    private void updateTags(String newString) {
        String source = getNewTag(newString);
        if (!TextUtils.isEmpty(source) && !source.equals(NEW_LINE)) {
            boolean isSpan = source.endsWith(NEW_LINE) ||
                    (!mIsSpacesAllowedInTags && source.endsWith(mSeparator));
            if (isSpan) {
                source = source.substring(0, source.length() - 1);
                source = source.trim();
            }
            Tag tag = new Tag();
            tag.setSource(source);
            tag.setSpan(isSpan);
            int size = mTags.size();
            if (size <= 0) {
                tag.setIndex(0);
                tag.setPosition(0);
            } else {
                Tag lastTag = mTags.get(size - 1);
                tag.setIndex(size);
                tag.setPosition(lastTag.getPosition() + lastTag.getSource().length() + 1);
            }
            mTags.add(tag);
        }
    }

    private String getNewTag(String newString) {
        StringBuilder builder = new StringBuilder();
        for (Tag tag : mTags) {
            if (!tag.isSpan()) continue;
            builder.append(tag.getSource()).append(mSeparator);
        }
        return newString.replace(builder.toString(), "");
    }

    private void addTagSpan(SpannableStringBuilder sb, final TagSpan tagSpan) {
        String source = tagSpan.getSource();
        sb.append(source).append(mSeparator);
        int length = sb.length();
        int startSpan = length - (source.length() + 1);
        int endSpan = length - 1;
        sb.setSpan(tagSpan, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Editable editable = ((EditText) widget).getText();
                mIsAfterTextWatcherEnabled = false;
                removeTagSpan(editable, tagSpan, true);
                mIsAfterTextWatcherEnabled = true;
            }
        }, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void removeTagSpan(Editable editable, TagSpan span, boolean includeSpace) {
        int extraLength = includeSpace ? 1 : 0;
        // include space
        Tag tag = span.getTag();
        int tagPosition = tag.getPosition();
        int tagIndex = tag.getIndex();
        int tagLength = span.getSource().length() + extraLength;
        editable.replace(tagPosition, tagPosition + tagLength, "");
        int size = mTags.size();
        for (int i = tagIndex + 1; i < size; i++) {
            Tag newTag = mTags.get(i);
            newTag.setIndex(i - 1);
            newTag.setPosition(newTag.getPosition() - tagLength);
        }

        if (mTags == null || mTags.size() == 0) return;
        onTagsChanged(convertTagSpanToList(mTagSpans),mTags.get(tagIndex).getSource(),false);

        mTags.remove(tagIndex);
        mTagSpans.remove(tagIndex);
    }

    private static List<String> convertTagSpanToList(List<TagSpan> tagSpans) {
        List<String> tags = new ArrayList<>(tagSpans.size());
        for (TagSpan tagSpan : tagSpans) {
            tags.add(tagSpan.getSource());
        }
        return tags;
    }

    private static CharSequence[] convertTagSpanToArray(List<TagSpan> tagSpans) {
        int size = tagSpans.size();
        CharSequence[] values = new CharSequence[size];
        for (int i = 0; i < size; i++) {
            values[i] = tagSpans.get(i).getSource();
        }
        return values;
    }

    private Drawable convertViewToDrawable(View view) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        Log.d("tags_xxx", "called 50"+view.getMeasuredWidth());
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable(getResources(), viewBmp);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(getContext());
        if (getWidth() > 0) {
            textView.setMaxWidth(getWidth() - 50);
        }
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTagsTextSize);
        textView.setTextColor(mTagsTextColor);
        textView.setPadding(mTagsPaddingLeft, mTagsPaddingTop, mTagsPaddingRight, mTagsPaddingBottom);

        // check Android version for set background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(mTagsBackground);
        } else {
            textView.setBackgroundDrawable(mTagsBackground);
        }


        textView.setCompoundDrawablesWithIntrinsicBounds(mLeftDrawable, null, mRightDrawable, null);
        textView.setCompoundDrawablePadding(mDrawablePadding);
        return textView;
    }


    private static final class Tag implements Parcelable {

        private int mPosition;
        private int mIndex;
        private String mSource;
        private boolean mSpan;

        public static final Creator<Tag> CREATOR = new Creator<Tag>() {
            @Override
            public Tag createFromParcel(Parcel in) {
                return new Tag(in);
            }

            @Override
            public Tag[] newArray(int size) {
                return new Tag[size];
            }
        };

        private Tag() {
        }

        protected Tag(Parcel in) {
            mPosition = in.readInt();
            mIndex = in.readInt();
            mSource = in.readString();
            mSpan = in.readInt() == 1;
        }

        private void setPosition(int pos) {
            mPosition = pos;
        }

        private int getPosition() {
            return mPosition;
        }

        private void setIndex(int index) {
            mIndex = index;
        }

        private int getIndex() {
            return mIndex;
        }

        public void setSource(String source) {
            mSource = source;
        }

        public String getSource() {
            return mSource;
        }

        public void setSpan(boolean span) {
            mSpan = span;
        }

        public boolean isSpan() {
            return mSpan;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mPosition);
            dest.writeInt(mIndex);
            dest.writeString(mSource);
            dest.writeInt(mSpan ? 1 : 0);
        }

    }

    private static final class TagSpan extends ImageSpan {

        private Tag mTag;

        public TagSpan(Drawable d, String source) {
            super(d, source);
        }

        private void setTag(Tag tag) {
            mTag = tag;
        }

        public Tag getTag() {
            return mTag;
        }

    }

    /*public interface TagsEditListener {

        void onTagsChanged(List<String> tags, String changedString, boolean isDeleted);

        void onEditingFinished();

    }*/

     public void onTagsChanged(List<String> tags, String changedString, boolean isDeleted) {
         try {
             if(isDeleted){
                 String isTeamSelected = "FALSE";
                 Iterator<TagModel> iter = tagModelListMain.iterator();

                 Log.d("tags_deleted", "size "+tagModelListMain.size());

                 while (iter.hasNext()) {
                     TagModel tagModel = iter.next();
                     if(tags != null && tags.size() >0){
                         if (tagModel.getName().equalsIgnoreCase(tags.get(tags.size()-1))){


                             tagModelListRemoved.add(tagModel);
                             iter.remove();
                             Log.d("tags_deleted", "tags size "+tags.size());
                             Log.d("tags_deleted", "changed "+tagModel.getName());
                             Log.d("tags_deleted", "changed "+tagModel.getName());


                         }
                     }

                 }
                 Log.d("tags_deleted", "list size after player selected "+tagModelListMain.size());
                 Log.d("tags_deleted", "list size after teams filtered "+tagModelListMain.size());
                 resetAdapter();

             }
             else{
                 try {
                     TagModel chipsInput = new TagModel();
                     chipsInput.setName(changedString);
                     Log.d("tags_added", "removed list size "+tagModelListRemoved.size());
                     Log.d("tags_added", "suggestion list size "+tagModelListMain.size());
                     Log.d("tags_added", "Tags size "+tags.size());

                     Iterator<TagModel> iter = tagModelListRemoved.iterator();


                     while (iter.hasNext()){
                         TagModel tagModel = iter.next();
                         if (chipsInput.getName().equalsIgnoreCase(tagModel.getName())) {
                             chipsInput.setName(tagModel.getName());

                             iter.remove();
                             Log.d("tags_added", "removing "+chipsInput.getName());

                         }


                     }

                     Log.d("tags_added", "suggestion list sizeaftr "+tagModelListMain.size());
                     Log.d("tags_added", "Removal list sizeaftr "+tagModelListRemoved.size());
                     resetAdapter();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             Log.d("tags_added", "error "+e.toString());
         }
        }
}
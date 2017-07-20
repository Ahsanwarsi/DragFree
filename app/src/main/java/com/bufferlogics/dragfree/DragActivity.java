package com.bufferlogics.dragfree;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bufferlogics.dragfree.gestures.DragView;

import java.io.File;
import java.util.Random;

public class DragActivity extends AppCompatActivity {


    private RelativeLayout dragArea;
    private ImageView imgEmoticon;
    private TextView dragText;
    private EditText etPost;
    private DragView draggableImageText;

    private boolean isEditing = false;
    private int selectedColor = Color.WHITE;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);

        view = getCurrentFocus();

        //Drag
        dragArea = (RelativeLayout) findViewById(R.id.lo_drag_area);

        imgEmoticon = (ImageView) findViewById(R.id.img_emo);
        dragText = (TextView) findViewById(R.id.tv_post_title);
        etPost = (EditText) findViewById(R.id.et_post_text);


        etPost.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            isEditing = false;
                            etPost.setVisibility(View.GONE);
                            //findViewById(R.id.lo_text_colors).setVisibility(View.INVISIBLE);

                            /*if (utility.stringsValidater(etPost.getText().toString())) {
                                postText = etPost.getText().toString();
                            }*/
                            setText(etPost.getText().toString());

                            if (v != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            }

                            return true;
                        }
                        return false;
                    }
                });
    }

    /*********
     * Add text
     ***********/
    public void onAddTextClick(View v) {
        if (draggableImageText != null) {
            if (draggableImageText.getParent() == dragArea) {
                dragArea.removeView(view);
            }
        }

        //findViewById(R.id.lo_text_colors).setVisibility(View.VISIBLE);
        etPost.setVisibility(View.VISIBLE);
        etPost.setText("");
        etPost.requestFocus();
        etPost.setTextColor(selectedColor);
        //etPost.setSelection(etPost.getText().length());

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(etPost.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        isEditing = true;
    }

    /*********
     * Set Text
     *********/
    private void setText(String postText) {
        dragText.setText(postText);
        dragText.setTextColor(selectedColor);

        Bitmap imgBitmap = getViewBitmaps(findViewById(R.id.wrapper_text));
        draggableImageText = new DragView(DragActivity.this, imgBitmap, onViewClickListener);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        draggableImageText.setLayoutParams(params);

        draggableImageText.setTag(postText);
        dragArea.addView(draggableImageText);
    }

    public void updateText(View v, String text) {
        if (v != null) {
            if (v.getParent() == dragArea) {
                dragArea.removeView(v);
            }
        }

        etPost.setVisibility(View.VISIBLE);
        etPost.setText(text);
        etPost.requestFocus();
        etPost.setTextColor(selectedColor);
        etPost.setSelection(etPost.getText().length());

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(etPost.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        isEditing = true;
    }

    private void stopEditing() {
        isEditing = false;
        etPost.setVisibility(View.GONE);
        //findViewById(R.id.lo_text_colors).setVisibility(View.INVISIBLE);

        /*if (utility.stringsValidater(etPost.getText().toString())) {
            postText = etPost.getText().toString();
        }*/
        setText(etPost.getText().toString());

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    DragView.OnClickPressedWithTouch onViewClickListener = new DragView.OnClickPressedWithTouch() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null) {
                if (!isEditing) {
                    //findViewById(R.id.lo_text_colors).setVisibility(View.VISIBLE);
                    //lineColorPicker.setSelectedColor(ctx.getResources().getColor(R.color.theme_white));
                    selectedColor = Color.WHITE;
                    updateText(view, view.getTag().toString());
                }
            }
        }

        @Override
        public void onDoubleTab(final View view) {
            if (view.getParent() == dragArea) {
                dragArea.removeView(view);
            }
        }
    };

    private Bitmap getViewBitmaps(View view) {
        view.setVisibility(View.VISIBLE);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(dragArea.getWidth(), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache(true);
        view.setVisibility(View.INVISIBLE);
        return view.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
    }

    public void onAddEmojiClick(View view){
        int emoji = Data.getEmoticons().get(new Random().nextInt(Data.getEmoticons().size()));
        addEmoticonToDragArea(emoji);
    }

    private void addEmoticonToDragArea(int emoticonId) {
        imgEmoticon.setImageResource(emoticonId);
        Bitmap imgBitmap = getViewBitmaps(findViewById(R.id.wrapper_emo));
        DragView draggableImage = new DragView(DragActivity.this, imgBitmap, onViewClickListener);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        draggableImage.setLayoutParams(params);

        dragArea.addView(draggableImage);
    }

    @Override
    public void onBackPressed() {
        if (isEditing == true) {
            stopEditing();
        } else {
            finish();
        }
    }
}
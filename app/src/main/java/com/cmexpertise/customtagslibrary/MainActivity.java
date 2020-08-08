package com.cmexpertise.customtagslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;


import com.cmexpertise.dabcustomtagslibrary.models.TagModel;
import com.cmexpertise.dabcustomtagslibrary.view.TagsEditText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private List<TagModel> tagModelListMain;
    private TagsEditText mTagsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupInitialListData();
        setUpTagEditText();

    }

    private void setUpTagEditText() {
        mTagsEditText = findViewById(R.id.tagsEditText);
        mTagsEditText.setTagsWithSpacesEnabled(true);
        mTagsEditText.initializeAdapter(MainActivity.this, R.layout.row_player_search,tagModelListMain);
        mTagsEditText.setSelection(0);
        mTagsEditText.requestFocus();
    }

    private void setupInitialListData() {
        tagModelListMain = new ArrayList<>();
        tagModelListMain.add(new TagModel("id001","Aashay"));
        tagModelListMain.add(new TagModel("id002","Aamir"));
        tagModelListMain.add(new TagModel("id003","Aamod"));
        tagModelListMain.add(new TagModel("id004","aadolf"));
        tagModelListMain.add(new TagModel("id005","aali"));
        tagModelListMain.add(new TagModel("id006","aarya"));
        tagModelListMain.add(new TagModel("id007","Russel"));
        tagModelListMain.add(new TagModel("id008","Jackson"));
    }



}
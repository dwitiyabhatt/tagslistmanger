package com.cmexpertise.customtagslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;


import com.cmexpertise.customtagslibrary.models.TagModel;
import com.cmexpertise.dabcustomtagslibrary.view.TagsEditText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TagsEditText.TagsEditListener {

    private List<TagModel> tagModelListMain;
    private List<TagModel> tagModelListRemoved;
    private TagsEditText mTagsEditText;
    private ArrayAdapter newAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tagModelListMain = new ArrayList<>();
        tagModelListRemoved = new ArrayList<>();
        tagModelListMain.add(new TagModel("Aashay"));
        tagModelListMain.add(new TagModel("Aakash"));
        tagModelListMain.add(new TagModel("Aamir"));
        tagModelListMain.add(new TagModel("Aamod"));
        tagModelListMain.add(new TagModel("Akshay"));
        tagModelListMain.add(new TagModel("James"));
        tagModelListMain.add(new TagModel("Ahemad"));
        tagModelListMain.add(new TagModel("Russel"));
        tagModelListMain.add(new TagModel("Jackson"));

        mTagsEditText = findViewById(R.id.tagsEditText);
        mTagsEditText.setTagsListener(this);
        mTagsEditText.setTagsWithSpacesEnabled(true);
        mTagsEditText.setSelection(0);
        mTagsEditText.requestFocus();


        newAdapter = new ArrayAdapter(MainActivity.this,
                R.layout.row_player_search, tagModelListMain);
        mTagsEditText.setAdapter(newAdapter);


    }

    @Override
    public void onTagsChanged(List<String> tags, String changedString, boolean isDeleted) {

        try {
            if(isDeleted){
                String isTeamSelected = "FALSE";
                Iterator<TagModel> iter = tagModelListMain.iterator();

                Log.d("tags", "list size original "+tagModelListMain.size());

                while (iter.hasNext()) {
                    TagModel tagModel = iter.next();
                    if(tags != null && tags.size() >0){
                        if (tagModel.getName().equalsIgnoreCase(tags.get(tags.size()-1))){


                            tagModelListRemoved.add(tagModel);
                            iter.remove();
                            Log.d("tags", "tags size "+tags.size());
                            Log.d("tags", "changed "+tagModel.getName());
                            Log.d("tags", "changed "+tagModel.getName());


                        }
                    }

                }
                Log.d("tags", "list size after player selected "+tagModelListMain.size());
                Log.d("tags", "list size after teams filtered "+tagModelListMain.size());
                newAdapter = new ArrayAdapter(
                        MainActivity.this,
                        R.layout.row_player_search, tagModelListMain);
                mTagsEditText.setAdapter(newAdapter);

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
                                tagModelListMain.add(chipsInput);
                                iter.remove();
                                Log.d("tags_added", "removing "+chipsInput.getName());
                            }


                        }

                    Log.d("tags_added", "suggestion list sizeaftr "+tagModelListMain.size());
                    Log.d("tags_added", "Removal list sizeaftr "+tagModelListRemoved.size());
                    newAdapter = new ArrayAdapter(MainActivity.this,
                            R.layout.row_player_search, tagModelListMain);


                    mTagsEditText.setAdapter(newAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tags_added", "error "+e.toString());
        }


    }

    @Override
    public void onEditingFinished() {

    }
}
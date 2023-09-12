package com.example.yrmultimediaco.keepnotes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.listeners.OnLongClickListener;
import com.mikepenz.fastadapter_extensions.ActionModeHelper;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CreateNotesAdapter.OnBtnDetailClickListener, NotesDetailBottomSheetFragment.NotesDetailBottomSheetFragmentListener, ItemTouchCallback {

    private HashSet<String> colorListsSet = new HashSet<>();

    private boolean moveToTrash = false;
    RecyclerView notesRecView;
    FastAdapter<AbstractItem> fastAdapter;
    ItemAdapter<AbstractItem> itemAdapter;
    DBhelper mDBhelper;
    private int selectedItemID;
    public static final int REQUEST_CODE_AND_NOTE = 1;
    public static final int REQUEST_CODE_TRASH_ACTIVITY = 2;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    ImageView imageAddNoteMain, imageAddImageMain;
    //EditText searchText;
    //ImageView layoutChange;
    androidx.appcompat.widget.SearchView searchView ;
    private boolean isGridView = false;
    private static final String PREF_LAYOUT_MODE = "layout_mode";
    private static final String STATE_IS_GRID_VIEW = "state_is_grid_view";
    private static final String PREF_SORTING_MODE = "sorting_mode";
    private static final String PREF_SORTING_NAME = "sorting_name";
    private boolean isMoveOperation = false;
    private boolean isCopyOperation = true;
    public static final int REQUEST_CODE_SELECT_IMAGE = 3;
    AlertDialog dialogWebUrl;
    Toolbar mToolbar;
    private boolean isItemSelected = false;
    private int selectedItemCount = 0;
    private ActionModeHelper<AbstractItem> mActionModeHelper;
    AlertDialog dialogDeleteNote;
    AlertDialog lockNote;
    AlertDialog UnlockNote;
    boolean isSortingByName;
    AlertDialog pickColor;
    AlertDialog labelDialog;
    LottieAnimationView noFilesFoundView;
    LottieAnimationView noSaerchFilesFoundView;
    List<AbstractItem> loadAddLabel = new ArrayList<>();

    HorizontalScrollView showcategoryLayout;
    ImageView imageCategoryIcon, imageLinkIcon, imageFoodIcon, imagePlaceIcon;
    TextView imageCategory;


    Chip chipAll, chipImages, chipsUrl, chipsFav, chipsColorToSortNotesByColor, addLabelFromCgip;


    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(itemAdapter.getAdapterItems(), oldPosition, newPosition);
        fastAdapter.notifyAdapterItemMoved(oldPosition, newPosition);

        return true;
    }

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {
        if (oldPosition != newPosition) {
            // Update the positions in the database
            for (int i = 0; i < itemAdapter.getAdapterItems().size(); i++) {
                AbstractItem item = itemAdapter.getAdapterItems().get(i);
                // Update position for items in your CreateNotesAdapter class
                if (item instanceof CreateNotesAdapter) {
                    CreateNotesAdapter note = (CreateNotesAdapter) item;
                    int noteId = note.getId();
                    mDBhelper.updateNotePosition(noteId, i); // Update the position
                }
            }
        }
    }



    public enum NoteFilter {
        ALL, IMAGES, WEB, FAV, SORT_COLOR
    }
    private NoteFilter currentFilter = NoteFilter.ALL;

    private CreateNotesAdapter selectedAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchText);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("My Notes");

        int colorTitle = ContextCompat.getColor(this, R.color.colorWhite);
        mToolbar.setTitleTextColor(colorTitle);

//        showcategoryLayout = findViewById(R.id.layoutCategory);
//        showcategoryLayout.setVisibility(View.GONE);

        /*imageCategoryIcon = findViewById(R.id.image_category);
        imageLinkIcon = findViewById(R.id.image_link);
        imageFoodIcon = findViewById(R.id.image_food);
        imagePlaceIcon = findViewById(R.id.image_location);
        imageCategory = findViewById(R.id.allCategory);*/

        chipAll = findViewById(R.id.chipAll);
        chipImages = findViewById(R.id.chipImages);
        chipsUrl = findViewById(R.id.chipUrl);
        chipsFav = findViewById(R.id.chipFav);
        chipsColorToSortNotesByColor = findViewById(R.id.chipColor);
        addLabelFromCgip = findViewById(R.id.chipLabel);
        noFilesFoundView = findViewById(R.id.noFilesFound);
        noSaerchFilesFoundView = findViewById(R.id.noSearchFilesFound);

        chipAll.setOnClickListener(v -> {
            if (chipAll.isChecked()){
                deselectAllChipsExcept(chipAll);
                applyFilter(NoteFilter.ALL);
            }
        });
        chipImages.setOnClickListener(v -> {
            if (chipImages.isChecked()){
                deselectAllChipsExcept(chipImages);
                applyFilter(NoteFilter.IMAGES);
            }
        });
        chipsUrl.setOnClickListener(v -> {
            if (chipsUrl.isChecked()){
                deselectAllChipsExcept(chipsUrl);
                applyFilter(NoteFilter.WEB);
            }
        });
        chipsFav.setOnClickListener(v -> {
            deselectAllChipsExcept(chipsFav);
            applyFilter(NoteFilter.FAV);
        });
        chipsColorToSortNotesByColor.setOnClickListener(v -> {
            deselectAllChipsExcept(chipsColorToSortNotesByColor);
            applyFilter(NoteFilter.SORT_COLOR);
        });
        addLabelFromCgip.setOnClickListener(v -> {
            deselectAllChipsExcept(addLabelFromCgip);
            showLabelDialog();
        });


        /*imageCategory.setOnClickListener(v -> applyFilter(NoteFilter.ALL));
        imageCategoryIcon.setOnClickListener(v -> applyFilter(NoteFilter.IMAGES));
        imageLinkIcon.setOnClickListener(v -> applyFilter(NoteFilter.WEB));
        imageFoodIcon.setOnClickListener(v -> applyFilter(NoteFilter.FOOD));
        imagePlaceIcon.setOnClickListener(v -> applyFilter(NoteFilter.PLACE));*/

//        Typeface customFont = Typeface.createFromAsset(getAssets(), "ubuntu_bold.ttf");
//        mToolbar.setTitleTypeface(customFont);

        setSupportActionBar(mToolbar);

        Context context = this; // Use appropriate context here

        // Get the current configuration
        Configuration configuration = context.getResources().getConfiguration();

        // Check if the configuration's UI mode is set to night mode
        boolean isDarkMode = (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;


        searchView.setIconifiedByDefault(false);

       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               performSearch(query);
               //showcategoryLayout.setVisibility(View.VISIBLE);
               //noSaerchFilesFoundView.setVisibility(View.GONE);
               return true;
           }


           @Override
           public boolean onQueryTextChange(String newText) {
               //performSearch(newText);
               boolean hasSearchResults = performSearch(newText);
               if (newText.isEmpty() || hasSearchResults) {
                   noSaerchFilesFoundView.setVisibility(View.GONE);
                   //showcategoryLayout.setVisibility(View.GONE);
               } else {
                   noSaerchFilesFoundView.setVisibility(View.VISIBLE);
                   //showcategoryLayout.setVisibility(View.VISIBLE);
               }
               return true;
           }
       });

       searchView.setOnCloseListener(new SearchView.OnCloseListener() {
           @Override
           public boolean onClose() {
               //showcategoryLayout.setVisibility(View.GONE);
               noSaerchFilesFoundView.setVisibility(View.GONE);
               return false;
           }
       });

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        notesRecView = findViewById(R.id.notesRecView);
        notesRecView.setLayoutManager(new LinearLayoutManager(this));
        notesRecView.setAdapter(fastAdapter);

        isGridView = loadLayoutMode();
        setLayoutManager();


        mDBhelper = new DBhelper(MainActivity.this);
        getNotes();
        loadLabels();

        imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    startActivityForResult(
                            new Intent(getApplicationContext(), CreateNotesActivity.class),
                            REQUEST_CODE_AND_NOTE
                    );

            }

        });
        imageAddImageMain = findViewById(R.id.imageAddImageQuick);
        imageAddImageMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handlePermissions();
            }
        });


        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLottieVisible = preferences.getBoolean("lottieVisible", false); // Default to false

        // Apply the saved state
        if (isLottieVisible) {
            noFilesFoundView.setVisibility(View.VISIBLE);
        } else {
            noFilesFoundView.setVisibility(View.GONE);
        }


        if (savedInstanceState != null) {
            isGridView = savedInstanceState.getBoolean(STATE_IS_GRID_VIEW, false);

        } else {
            // If there's no saved instance state, load the layout mode from SharedPreferences
            isGridView = loadLayoutMode();
        }

        isSortingByName = loadSortingMode();

        if (isSortingByName) {
            sortNotesByNameAscending(); // Call the sorting method you want to apply
        } else {
            sortNotesByDateAscending(); // Call the other sorting method
        }


        ImageView imageAddNoteQuick = findViewById(R.id.imageAddNote);
        imageAddNoteQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNotesActivity.class),
                        REQUEST_CODE_AND_NOTE
                );

            }
        });

        ImageView imageAddWebLink = findViewById(R.id.imageAddWeblink);
        imageAddWebLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
            }
        });

//        Intent openMainActivityIntent = new Intent(context, MainActivity.class);
//        openMainActivityIntent.setAction("OPEN_MAIN_ACTIVITY_ACTION"); // You can use any unique action string
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openMainActivityIntent, PendingIntent.FLAG_IMMUTABLE);

        // Check if the activity was launched from the widget
        if (getIntent() != null && "OPEN_MAIN_ACTIVITY_ACTION".equals(getIntent().getAction())) {
            // Retrieve data passed from the widget (replace with your actual data key)
            int itemId = getIntent().getIntExtra("ITEM_ID", -1);

            // Handle the item click here based on the itemId
            if (itemId != -1) {
                // Assuming you have a method to open the item by its ID, e.g., openItemById(itemId)
                openItemById(itemId);

                // Optionally, finish the activity if you want to return to the widget
                finish();
            }
        }

        fastAdapter.withOnClickListener(new OnClickListener<AbstractItem>() {
            @Override
            public boolean onClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {
                if (item instanceof CreateNotesAdapter) {
                    CreateNotesAdapter notesAdapter = (CreateNotesAdapter) item;

                    boolean isLockNoteState = !notesAdapter.isNoteLock();

                    if (notesAdapter.isNoteLock()){
                        //Note is lockeed then show the toast here
                        Toast.makeText(MainActivity.this, "This note is locked. Unlock to Open", Toast.LENGTH_SHORT).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        //View dialogView = getLayoutInflater().inflate(R.layout.layout_passcode_open, null);

                        View view = LayoutInflater.from(MainActivity.this).inflate(
                                R.layout.layout_passcode_open,
                                findViewById(R.id.unlockNoteContainer)
                        );
                        builder.setView(view);

                        UnlockNote = builder.create();
                        if (UnlockNote.getWindow() != null){
                            UnlockNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }


                        TextInputEditText inputUnlockPasscode = view.findViewById(R.id.inputUnlockPasscode);
                        Button unlockNoteBtn = view.findViewById(R.id.unloackBtn);

                        unlockNoteBtn.setOnClickListener(vi -> {
                            // Get the user's entered PIN
                            String enteredPin = inputUnlockPasscode.getText().toString();

                            // Get the stored PIN for the selected note from the database
                            String storedPin = mDBhelper.getNoteLockPIN(notesAdapter.getId());

                            if (enteredPin.equals(storedPin)) {
                                // Unlock the note
                                notesAdapter.setNoteLock(false);
                                mDBhelper.unlockNote(notesAdapter.getId());

                                // Dismiss the dialog
                                UnlockNote.dismiss();
                                Toast.makeText(MainActivity.this, "Note Unlocked", Toast.LENGTH_SHORT).show();
                                updateUIAfterLockingStatusChange(notesAdapter.getId(), true);
                            } else {
                                // Show an error message, wrong PIN entered
                                Toast.makeText(MainActivity.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                            }
                        });
                        UnlockNote.show();

                        return true;
                    }

                    if (mActionModeHelper.isActive()) {
                        if (item.isSelected()) {
                            notesAdapter.setSelected(false);
                            selectedItemCount--;
                        } else {
                            notesAdapter.setSelected(true);
                            selectedItemCount++;
                        }


                        updateActionModeTitle(selectedItemCount);
                        fastAdapter.notifyAdapterDataSetChanged();

                    } else {

                        Intent intent = new Intent(MainActivity.this, CreateNotesActivity.class);

                        intent.putExtra("isDeletingNote", true);

                        intent.putExtra("note_id", notesAdapter.getId());
                        intent.putExtra("note_title", notesAdapter.getTitle());
                        intent.putExtra("note_subTitle", notesAdapter.getSubTitle());
                        intent.putExtra("note_desc", notesAdapter.getDescription());
                        intent.putExtra("get_url", notesAdapter.getUrl());
                        intent.putExtra("image_path", notesAdapter.getImagePath());
                        intent.putExtra("sub_title_indicator_color", notesAdapter.getColor());

                        startActivityForResult(intent, REQUEST_CODE_AND_NOTE);


                    }
                    return true;
                } else {
                    return false; // Return false here, as the default case should not handle the click

                }
            }

        });

        fastAdapter.withMultiSelect(true);
        fastAdapter.withSelectable(true);
        fastAdapter.withSelectOnLongClick(true);
        fastAdapter.withSelectWithItemUpdate(true);

        fastAdapter.withOnPreLongClickListener(new OnLongClickListener<AbstractItem>() {
            @Override
            public boolean onLongClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {
                ActionMode actionMode = mActionModeHelper.onLongClick(MainActivity.this, position);
                if (actionMode != null){
                    RecyclerView.ViewHolder viewHolder = notesRecView.findViewHolderForAdapterPosition(position);
                    if (viewHolder instanceof CreateNotesAdapter.ViewHolder){
                        CreateNotesAdapter.ViewHolder createViewHolder = (CreateNotesAdapter.ViewHolder) viewHolder;
                        if (item instanceof CreateNotesAdapter){
                            CreateNotesAdapter createNotesAdapter = (CreateNotesAdapter) item;
                            createNotesAdapter.setSelected(!item.isSelected());
                            createViewHolder.selectedImg.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
                        }
                    }

                    selectedItemCount++;
                    updateActionModeTitle(selectedItemCount);
                    fastAdapter.notifyAdapterDataSetChanged();
                }

                return actionMode != null;
            }


        });




        SimpleDragCallback dragCallback;
        dragCallback = new SimpleDragCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(dragCallback);
        touchHelper.attachToRecyclerView(notesRecView);


        mActionModeHelper = new ActionModeHelper<>(fastAdapter, R.menu.cab, new ActionBarCallback());

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                //.withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Keep Notes").withIcon(getResources().getDrawable(R.drawable.baseline_edit_note_24))
                )
                /*.withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })*/
                .build();

        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("My Notes").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon2.cmd_home)
                .colorRes(isDarkMode ? R.color.white : com.mikepenz.materialdrawer.R.color.md_black_1000)); // Use white color for dark mode);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("Trash").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon2.cmd_trash_can_outline)
                .colorRes(isDarkMode ? R.color.white : com.mikepenz.materialdrawer.R.color.md_black_1000));
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName("Add Label").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon2.cmd_label_outline)
                .colorRes(isDarkMode ? R.color.white : com.mikepenz.materialdrawer.R.color.md_black_1000));
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(4).withName("Favourites").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon2.cmd_star_box)
                .colorRes(isDarkMode ? R.color.white : com.mikepenz.materialdrawer.R.color.md_black_1000));
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(5).withName("Archives").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon2.cmd_inbox_arrow_down)
                .colorRes(isDarkMode ? R.color.white : com.mikepenz.materialdrawer.R.color.md_black_1000));


        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(mToolbar)
                .withSavedInstance(savedInstanceState)
                .addDrawerItems(item1,
                        new DividerDrawerItem(),
                        item2,
                        //new SecondaryDrawerItem(),
                        item3,
                        item4,
                        item5
                ).withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    long identifier = drawerItem.getIdentifier();
                    if (identifier == 1){

                    } else if (identifier == 2) {
                        startActivity(new Intent(getApplicationContext(), TrashNotesActivity.class));
                    } else if (identifier == 3) {
                        startActivity(new Intent(getApplicationContext(), AddCategoryLabel.class));
                    } else if (identifier == 4) {
                        applyFilter(NoteFilter.FAV);
                    } else if (identifier == 5) {
                        startActivity(new Intent(getApplicationContext(), ArchivedNotesActivity.class));
                    }

                    return false;
                })
                .build();

        result.addItem(new DividerDrawerItem());
        result.addStickyFooterItem(new PrimaryDrawerItem().withName("Privacy Policy"));
        result.addStickyFooterItem(new PrimaryDrawerItem().withName("Terms of Service"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }


    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true); // Close the search view if it's open
        } else {
            //showcategoryLayout.setVisibility(View.GONE);
            super.onBackPressed(); // Perform default back button behavior
        }
    }

    private void loadLabels() {

        ArrayList<AbstractItem> loadlabel = mDBhelper.getAllLabels();
        loadAddLabel.clear();

        if (loadlabel.isEmpty()){
            //noLabelText.setVisibility(View.VISIBLE);
        }else {
            //noLabelText.setVisibility(View.GONE);
        }

        for (AbstractItem item : loadlabel) {

            if (item instanceof AddLabelAdapter) {

                AddLabelAdapter labelAdapter = (AddLabelAdapter) item;

                AddLabelAdapter addLabelAdapter = new AddLabelAdapter(
                        labelAdapter.getId(),
                        labelAdapter.getAddLabel()
                );

               // loadAddLabel.add(addLabelAdapter);

                //addLabelAdapter.setButtonClickListner(this);
            }

        }


        itemAdapter.add(loadAddLabel);
        fastAdapter.notifyAdapterDataSetChanged();

    }

    private void openItemById(int itemId) {
        // Here, you can implement logic to open the item with the given itemId.
        // This might involve launching a new activity, fragment, or updating the UI.
        // You should replace this with your actual implementation.

        // Example:
        Intent intent = new Intent(this, CreateNotesActivity.class);
        intent.putExtra("ITEM_ID", itemId);
        startActivity(intent);
    }
    private void showLabelDialog() {

        //if (labelDialog == null) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.add_label_dialog,
                findViewById(R.id.layoutAddLabelContainer)
        );
        builder.setView(view);
        builder.setCancelable(false);

        labelDialog = builder.create();
        if (labelDialog.getWindow() != null) {
            labelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        final EditText inputLabelName = view.findViewById(R.id.inputLabelName);
        inputLabelName.requestFocus();

        view.findViewById(R.id.textAddLabel).setOnClickListener(v -> {

            String labelName = inputLabelName.getText().toString().trim();

            if (inputLabelName.getText().toString().trim().isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter Label Name First", Toast.LENGTH_SHORT).show();
            } else {

                itemAdapter.clear();
                List<AbstractItem> loadAddLabel = new ArrayList<>();

                long labelId = mDBhelper.addLabel(labelName);
                AddLabelAdapter addLabelAdapter = new AddLabelAdapter(labelId, labelName);
                loadAddLabel.add(addLabelAdapter);

                labelDialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), AddCategoryLabel.class);
                startActivityForResult(intent, REQUEST_CODE_AND_NOTE);

            }

        });

        view.findViewById(R.id.textCancelLabel).setOnClickListener(v -> {
            labelDialog.dismiss();
            // Refresh the adapter to reflect the changes
            fastAdapter.notifyAdapterDataSetChanged();

        });

        labelDialog.show();

    }


    @Override
    public void onBtnDetailsClicked(int itemId) {
        selectedItemID = itemId;
        NotesDetailBottomSheetFragment bottomSheetFragment = NotesDetailBottomSheetFragment.newInstance(itemId);
        bottomSheetFragment.setItemId(itemId);
        bottomSheetFragment.setListener(this);
        bottomSheetFragment.show(getSupportFragmentManager(), "bottomSheetTag");
        Toast.makeText(this, "Bottom Sheet id = " + itemId, Toast.LENGTH_SHORT).show();
        Log.d("CreateNotesAdapter", "onBtnDetailsClicked called for itemId: " + itemId + selectedItemID);
    }

    @Override
    public void onNotesOptionSelected(int idOption) {

        CreateNotesAdapter selectedAdapter = findAdapterById(selectedItemID);
        if (selectedItemID != 0){
            if (idOption == R.id.layoutEdit){


               if (selectedAdapter != null){
                   if (!selectedAdapter.isNoteLock()){
                       openEditActivity(selectedAdapter);
                       Toast.makeText(this, "You are Editing the item : " + selectedItemID, Toast.LENGTH_SHORT).show();
                   } else{
                       Toast.makeText(this, "This note iss locked. Unlock to edit", Toast.LENGTH_SHORT).show();
                   }
               }


                //Toast.makeText(this, "Edit Item Click For Id = " + selectedItemID, Toast.LENGTH_SHORT).show();
            } else if (idOption == R.id.layoutShare) {

                if (selectedAdapter != null) {
                    if (!selectedAdapter.isNoteLock()){
                        shareNote(selectedAdapter);
                        Toast.makeText(this, "You are sharing the item : " + selectedItemID, Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(this, "This note is locked. Unlock to share", Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (idOption == R.id.layoutDeletee){

                Toast.makeText(this, "Delete Item Click For Id = " + selectedItemID, Toast.LENGTH_SHORT).show();

              /*  if (selectedAdapter != null) {
                    Toast.makeText(this, "Delete Item Click For Id = " + selectedItemID, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Selected Item is null", Toast.LENGTH_SHORT).show();
                }*/

                //int hasRequiredItem = selectAdapter.getId();

                if (selectedAdapter  != null){

                    if (!selectedAdapter.isNoteLock()){

                        if (dialogDeleteNote == null){
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            View view = LayoutInflater.from(this).inflate(
                                    R.layout.layout_delete_note,
                                    findViewById(R.id.layoutDeleteContainer)
                            );
                            builder.setView(view);
                            builder.setCancelable(false);

                            dialogDeleteNote = builder.create();
                            if (dialogDeleteNote.getWindow() != null){
                                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                            }

                            CheckBox checkBox = view.findViewById(R.id.checkBox);


                            view.findViewById(R.id.textDelete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //mDBhelper.deleteNotes(selectedItemID);
                                    if (checkBox.isChecked()){
                                        mDBhelper.moveNoteToTrash(selectedItemID);
                                        Toast.makeText(MainActivity.this, "Note moved to trash : " + selectedItemID, Toast.LENGTH_SHORT).show();
                                    } else {
                                        mDBhelper.deleteNotes(selectedItemID);
                                        Toast.makeText(MainActivity.this, "Note deleted permanently : " + selectedItemID, Toast.LENGTH_SHORT).show();
                                    }

                                    dialogDeleteNote.dismiss();

                                    getNotes();
                                    checkBox.setChecked(false);
                                }
                            });
                            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogDeleteNote.dismiss();

                                }
                            });
                        }
                        dialogDeleteNote.show();

                    }else {
                        Toast.makeText(MainActivity.this, "This note is locked. Unlock to delete.", Toast.LENGTH_SHORT).show();
                    }

                }


                } else if (idOption == R.id.layoutFavorites) {

                if (selectedAdapter != null) {

                    if (!selectedAdapter.isNoteLock()){

                        Toast.makeText(this, "Fav Item is : " + selectedItemID, Toast.LENGTH_SHORT).show();

                        boolean isFavorite = !selectedAdapter.isFavorite();
                        selectedAdapter.setFavorite(isFavorite);
                        mDBhelper.updateFavoriteStatus(selectedAdapter.getId(), isFavorite);

                        updateUIAfterFavoriteStatusChange(selectedAdapter.getId(), isFavorite);

                    } else{
                        Toast.makeText(this, "This note is locked. Unlock to favourite", Toast.LENGTH_SHORT).show();
                    }




                }
            } else if (idOption == R.id.layoutAddToCategory) {

                if (selectedAdapter != null){
                    if (!selectedAdapter.isNoteLock()){

                        Toast.makeText(this, "You Tapped Layout Copy To Category Option " + selectedItemID , Toast.LENGTH_SHORT).show();
                        isCopyOperation = true;

                        int selectedNoteId = selectedAdapter.getId();
                        Intent intent = new Intent(this, AddCategoryLabel.class);
                        intent.putExtra("noteId", selectedNoteId);
                        intent.putExtra("isCopyOperation", isCopyOperation);
                        startActivity(intent);

                    } else{

                        Toast.makeText(this, "This note is locked. Unlock to Add To Category", Toast.LENGTH_SHORT).show();
                    }



                }
            } else if (idOption == R.id.layoutMoveToCategory) {
                if (selectedAdapter != null){
                    if (!selectedAdapter.isNoteLock()){

                        Toast.makeText(this, "You Tapped Layout Move To Category Option " + selectedItemID , Toast.LENGTH_SHORT).show();
                        isMoveOperation = true;

                        int selectedNoteId = selectedAdapter.getId();
                        Intent intent = new Intent(this, AddCategoryLabel.class);
                        intent.putExtra("noteId", selectedNoteId);
                        intent.putExtra("isMoveOperation", isMoveOperation);
                        startActivity(intent);

                    } else{

                        Toast.makeText(this, "This note is locked. Unlock to Add To Category", Toast.LENGTH_SHORT).show();
                    }



                }
            } else if (idOption == R.id.layoutDuplicateNote) {
                if (selectedAdapter != null){
                    if (!selectedAdapter.isNoteLock()){

                        long duplicatedNoteId = mDBhelper.duplicateNote(selectedAdapter);

                        isMoveOperation = true;

                        int selectedNoteId = selectedAdapter.getId();
                        Intent intent = new Intent(this, AddCategoryLabel.class);
                        intent.putExtra("noteId", selectedNoteId);
                        intent.putExtra("isMoveOperation", isMoveOperation);
                        startActivity(intent);

                        getNotes();

                        Toast.makeText(this, "You can set this duplicated note as you want " + duplicatedNoteId, Toast.LENGTH_SHORT).show();

                    } else{

                        Toast.makeText(this, "This note is locked. Unlock to Duplicate", Toast.LENGTH_SHORT).show();

                    }

                }
            } else if (idOption == R.id.layoutArchievedNote) {
                if (selectedAdapter != null){
                    if (!selectedAdapter.isNoteLock()){
                        mDBhelper.moveNoteToArchive(selectedItemID);

                        getNotes();
                        Toast.makeText(this,"Note Archieved with Id : " + selectedItemID, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "This note is locked. Unlock to Archived", Toast.LENGTH_SHORT).show();
                    }

                }
            } else if (idOption == R.id.layoutSaveNote) {
                if (selectedAdapter != null){
                    if (!selectedAdapter.isNoteLock()){
                        this.selectedAdapter = selectedAdapter;

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

                            requestAllFilesAccessPermission();

                        } else {

                            if (isExternalStorageWritable()) {
                                // External storage is available and writable, proceed with saving the file
                                saveNoteAsTextFile(selectedAdapter);
                            } else {
                                // External storage is not writable, display an error message or handle it appropriately
                                Toast.makeText(this, "External storage is not writable.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    } else{
                        Toast.makeText(this, "This note is locked. Unlock to Save To device", Toast.LENGTH_SHORT).show();
                    }

                }
            } else if (idOption == R.id.copyToClipBoard) {
                if (selectedAdapter != null) {
                    if (!selectedAdapter.isNoteLock()){

                        String title = selectedAdapter.getTitle();
                        String subTitle = selectedAdapter.getSubTitle();
                        String description = selectedAdapter.getDescription();
                        String url = selectedAdapter.getUrl();

                        String clipboardText = "Title: " + title + "\n" +
                                "Subtitle: " + subTitle + "\n" +
                                "Description: " + description + "\n" +
                                "URL: " + url;

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null){

                            ClipData clip = ClipData.newPlainText("Note details", clipboardText);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Text Copied To Clipboard", Toast.LENGTH_SHORT).show();

                        }
                    } else{
                        Toast.makeText(this, "This note is locked. Unlock to Copy to ClipBoard", Toast.LENGTH_SHORT).show();
                    }

                }

            } /*else if (idOption == R.id.layoutShortcutNote) {
                if (selectedAdapter != null) {
                    if (!selectedAdapter.isNoteLock()) {
                        // Log a message to indicate that this block is being executed
                        Log.d("ShortcutNote", "Creating shortcut...");

                        Intent shortCutIntent = new Intent(this, MainActivity.class);
                        shortCutIntent.setAction(Intent.ACTION_MAIN);

                        Intent addIntent = new Intent();
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortCutIntent);
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Note Shortcut");
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource.fromContext(this, R.drawable.keep_note));
                        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                        addIntent.putExtra("duplicate", false);

                        // Log a message to indicate that the shortcut intent is being broadcasted
                        Log.d("ShortcutNote", "Broadcasting shortcut intent...");

                        this.sendBroadcast(addIntent);

                        // Log a message to indicate that the shortcut has been added
                        Log.d("ShortcutNote", "Shortcut added to HomeScreen");

                        Toast.makeText(this, "Note Added Shortcut To HomeScreen", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "This note is locked. Unlock to Make Shortcut", Toast.LENGTH_SHORT).show();
                    }
                }
            }*/
            else if (idOption == R.id.layoutLockNote) {
                boolean isLockNoteState = !selectedAdapter.isNoteLock();

                if (isLockNoteState) {

                        // Show the set PIN layout and capture the user's input
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        View view = LayoutInflater.from(this).inflate(
                                R.layout.passcode_view,
                                findViewById(R.id.lockNoteContainer)
                        );
                        builder.setView(view);

                        lockNote = builder.create();
                        if (lockNote.getWindow() != null){
                            lockNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }

                        TextInputEditText inputNoteLockPasscode = view.findViewById(R.id.inputNoteLockPasscode);
                        Button setPinBtn = view.findViewById(R.id.setPinBtn);


                        setPinBtn.setOnClickListener(v -> {
                            // Get the user's entered PIN
                            String enteredPin = inputNoteLockPasscode.getText().toString();
                            if (enteredPin.length() == 4) {
                                // Update the database with the new lock state and PIN
                                mDBhelper.lockNoteWithPIN(selectedAdapter.getId(), enteredPin);

                                // Update the UI lock state
                                selectedAdapter.setNoteLock(isLockNoteState);

                                // Dismiss the dialog
                                lockNote.dismiss();
                                Toast.makeText(this, "Note Locked", Toast.LENGTH_SHORT).show();
                                updateUIAfterLockingStatusChange(selectedAdapter.getId(), isLockNoteState);
                            } else {
                                // Display an error message, PIN should be 4 digits
                                Toast.makeText(this, "Please set the 4 digit of PIn", Toast.LENGTH_SHORT).show();
                            }
                        });

                    lockNote.show();

                    }  else {
                    // Show the unlock PIN dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    //View dialogView = getLayoutInflater().inflate(R.layout.layout_passcode_open, null);

                    View view = LayoutInflater.from(this).inflate(
                            R.layout.layout_passcode_open,
                            findViewById(R.id.unlockNoteContainer)
                    );
                    builder.setView(view);

                    UnlockNote = builder.create();
                    if (UnlockNote.getWindow() != null){
                        UnlockNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    }


                    TextInputEditText inputUnlockPasscode = view.findViewById(R.id.inputUnlockPasscode);
                    Button unlockNoteBtn = view.findViewById(R.id.unloackBtn);

                    unlockNoteBtn.setOnClickListener(v -> {
                        // Get the user's entered PIN
                        String enteredPin = inputUnlockPasscode.getText().toString();

                        // Get the stored PIN for the selected note from the database
                        String storedPin = mDBhelper.getNoteLockPIN(selectedAdapter.getId());

                        if (enteredPin.equals(storedPin)) {
                            // Unlock the note
                            selectedAdapter.setNoteLock(false);
                            mDBhelper.unlockNote(selectedAdapter.getId());

                            // Dismiss the dialog
                            UnlockNote.dismiss();
                            Toast.makeText(this, "Note Unlocked", Toast.LENGTH_SHORT).show();
                            updateUIAfterLockingStatusChange(selectedAdapter.getId(), isLockNoteState);
                        } else {
                            // Show an error message, wrong PIN entered
                            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                        }
                    });
                    UnlockNote.show();


                }


            }

        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void updateUIAfterFavoriteStatusChange(int noteId, boolean isFavorite) {

        for (AbstractItem item : mDBhelper.fetchRegularNotes()) {
            if (item instanceof CreateNotesAdapter) {
                CreateNotesAdapter createNotesAdapter = (CreateNotesAdapter) item;
                if (createNotesAdapter.getId() == noteId) {
                    createNotesAdapter.setFavorite(isFavorite);
                    break; // No need to continue searching
                }
            }
        }

        fastAdapter.notifyAdapterDataSetChanged();
    }

    private void updateUIAfterLockingStatusChange(int noteId, boolean isLock) {

        for (AbstractItem item : mDBhelper.fetchRegularNotes()) {
            if (item instanceof CreateNotesAdapter) {
                CreateNotesAdapter createNotesAdapter = (CreateNotesAdapter) item;
                if (createNotesAdapter.getId() == noteId) {
                    createNotesAdapter.setNoteLock(isLock);
                    break; // No need to continue searching
                }
            }
        }

        fastAdapter.notifyAdapterDataSetChanged();
    }


    private void saveNoteAsTextFile(CreateNotesAdapter adapter){

        String title = adapter.getTitle();
        String subTitle = adapter.getSubTitle();
        String description = adapter.getDescription();
        String dateTime = adapter.getDateTime();
        String url = adapter.getUrl();
        String imagePath = adapter.getImagePath();

        String noteText = "Title: " + title + "\n" +
                "Subtitle: " + subTitle + "\n" +
                "Description: " + description + "\n" +
                "Date: " + dateTime + "\n" +
                "Url: " + url + "\n" +
                "Image: " + imagePath;

        String fileName = title + ".txt";

        File appDirectory = new File(Environment.getExternalStorageDirectory(), "Keep Notes");
        if (!appDirectory.exists()){
            appDirectory.mkdirs();
        }

        File filePath = new File(appDirectory, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(noteText);
            writer.flush();
            writer.close();

            MediaScannerConnection.scanFile(
                    this,
                    new String[]{filePath.getAbsolutePath()},
                    null,
                    null
            );

            Toast.makeText(this, "File Saved at : " + filePath.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(this, "Error saving file : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void applyFilter(NoteFilter filter){

        currentFilter = filter;

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();


// Assuming you have a LottieAnimationView called lottieAnimationView
        boolean isLottieVisible = ( noFilesFoundView.getVisibility() == View.VISIBLE);
        editor.putBoolean("lottieVisible", isLottieVisible);
        editor.apply();

        ArrayList<AbstractItem> filteredList = new ArrayList<>();

        boolean hasFavoriteItems = false;
        boolean hasColorNotes = false;
        boolean hasImagesNote = false;
        boolean hasUrlNote = false;

        for (AbstractItem item : mDBhelper.fetchRegularNotes()){

            if (item instanceof CreateNotesAdapter){

                CreateNotesAdapter note = (CreateNotesAdapter) item;

                switch (filter){

                    case ALL:
                        filteredList.add(note);
                        break;

                    case IMAGES:
                        if (note.hasImage()) {
                            filteredList.add(note);

                            hasImagesNote = true;
                        }
                        break;
                    case WEB:
                        if (note.hasUrl()){
                            filteredList.add(note);

                            hasUrlNote = true;
                        }
                        break;
                    case FAV:
                        if (note.isFavorite()) {
                            filteredList.add(note);
                            hasFavoriteItems = true;
                        }
                        break;
                    case SORT_COLOR:
                        if (note.hasColor()/* && !note.getColor().equals("333333")*/){
                            //filteredList.add(note);
                            hasColorNotes  = true;

                            showImagePickerDialog();
                        }

                        break;
                }

            }

        }

        itemAdapter.set(filteredList);

        if (filter == NoteFilter.SORT_COLOR){
            if (!hasColorNotes){
                noFilesFoundView.setVisibility(View.VISIBLE);
                notesRecView.setVisibility(View.GONE);
                Toast.makeText(this, "No Color Notes to display", Toast.LENGTH_SHORT).show();
            }else{
                noFilesFoundView.setVisibility(View.GONE);
                notesRecView.setVisibility(View.VISIBLE);
            }
        } else if (filter == NoteFilter.IMAGES){
            if (!hasImagesNote){
                noFilesFoundView.setVisibility(View.VISIBLE);
                notesRecView.setVisibility(View.GONE);
                Toast.makeText(this, "No Images Notes to display", Toast.LENGTH_SHORT).show();
            }else{
                noFilesFoundView.setVisibility(View.GONE);
                notesRecView.setVisibility(View.VISIBLE);
            }
        } else if (filter == NoteFilter.WEB){
            if (!hasUrlNote){
                noFilesFoundView.setVisibility(View.VISIBLE);
                notesRecView.setVisibility(View.GONE);
                Toast.makeText(this, "No Url Notes to display", Toast.LENGTH_SHORT).show();
            }else{
                noFilesFoundView.setVisibility(View.GONE);
                notesRecView.setVisibility(View.VISIBLE);
            }
        } else if (filter == NoteFilter.FAV ){
            if (!hasFavoriteItems){
                noFilesFoundView.setVisibility(View.VISIBLE);
                notesRecView.setVisibility(View.GONE);
                Toast.makeText(this, "No favorite Notes to display", Toast.LENGTH_SHORT).show();
            }else{
                noFilesFoundView.setVisibility(View.GONE);
                notesRecView.setVisibility(View.VISIBLE);
            }
        } else {
            noFilesFoundView.setVisibility(View.GONE);
            notesRecView.setVisibility(View.VISIBLE);
        }


    }

    private void deselectAllChipsExcept(Chip selectedChip){

        if (selectedChip != chipAll){
            chipAll.setChecked(false);
        } if (selectedChip != chipImages){
            chipImages.setChecked(false);
        } if (selectedChip != chipsFav){
            chipsFav.setChecked(false);
        } if (selectedChip != chipsUrl){
            chipsUrl.setChecked(false);
        } if (selectedChip != chipsColorToSortNotesByColor){
            chipsColorToSortNotesByColor.setChecked(false);
        } if (selectedChip != addLabelFromCgip){
            addLabelFromCgip.setChecked(false);
        }

    }



    private void showImagePickerDialog(){

        if (pickColor == null){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.show_reminder_time_dialog,
                    null
                    //findViewById(R.id.layoutChooseColor)
            );
            builder.setView(view);

            pickColor = builder.create();
            if (pickColor.getWindow() != null){
                pickColor.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            // Fetch notes by color and update your adapter's data
            ArrayList<AbstractItem> colorFilteredItems = new ArrayList<>();

            view.findViewById(R.id.layoutSelectYellow).setOnClickListener(v -> {


                String selectedColor = "#FDBE3B"; // Selected color code

                if (colorListsSet.contains(selectedColor)) {
                    Toast.makeText(this, "List of selected color is already set", Toast.LENGTH_SHORT).show();
                } else {
                    for (AbstractItem item : mDBhelper.fetchNotesByColor(selectedColor)) {
                        if (item instanceof CreateNotesAdapter) {
                            CreateNotesAdapter note = (CreateNotesAdapter) item;
                            colorFilteredItems.add(note);
                        }
                    }

                    if (colorFilteredItems.isEmpty()) {
                        Toast.makeText(this, "No notes found for the selected color", Toast.LENGTH_SHORT).show();
                    } else {
                        itemAdapter.clear();
                        itemAdapter.set(colorFilteredItems);
                        fastAdapter.notifyDataSetChanged();
                        colorListsSet.add(selectedColor); // Update the set with the selected color
                        pickColor.dismiss();
                    }
                }
            });



            view.findViewById(R.id.layoutSelectBlue).setOnClickListener(v -> {

                for (AbstractItem item : mDBhelper.fetchNotesByColor("#3A52FC")){

                    if (item instanceof CreateNotesAdapter){
                        CreateNotesAdapter note = (CreateNotesAdapter) item;

                        Log.d("ColorFilter", "Note Color: " + note.getColor());
                        //if (note.hasColor()){
                        colorFilteredItems.add(note);

                        //}
                        pickColor.dismiss();

                    }
                }

                //mNotesAdapter.setData(colorFilteredItems);
                itemAdapter.set(colorFilteredItems);
                fastAdapter.notifyDataSetChanged();


            });

            view.findViewById(R.id.layoutSelectRed).setOnClickListener(v -> {

                for (AbstractItem item : mDBhelper.fetchNotesByColor("#FF4842")){

                    if (item instanceof CreateNotesAdapter){
                        CreateNotesAdapter note = (CreateNotesAdapter) item;

                        Log.d("ColorFilter", "Note Color: " + note.getColor());
                        //if (note.hasColor()){
                        colorFilteredItems.add(note);

                        //}
                        pickColor.dismiss();

                    }
                }

                //mNotesAdapter.setData(colorFilteredItems);
                itemAdapter.set(colorFilteredItems);
                fastAdapter.notifyDataSetChanged();

            });

            view.findViewById(R.id.layoutSelectBlack).setOnClickListener(v -> {

                for (AbstractItem item : mDBhelper.fetchNotesByColor("#000000")){

                    if (item instanceof CreateNotesAdapter){
                        CreateNotesAdapter note = (CreateNotesAdapter) item;

                        Log.d("ColorFilter", "Note Color: " + note.getColor());
                        //if (note.hasColor()){
                        colorFilteredItems.add(note);

                        //}
                        pickColor.dismiss();

                    }
                }

                //mNotesAdapter.setData(colorFilteredItems);
                itemAdapter.set(colorFilteredItems);
                fastAdapter.notifyDataSetChanged();

            });


        }

        pickColor.show();

    }

    private CreateNotesAdapter findAdapterById(int itemId){
        for(AbstractItem adapter : itemAdapter.getAdapterItems()){
            if (adapter instanceof CreateNotesAdapter){
                CreateNotesAdapter notesAdapter = (CreateNotesAdapter) adapter;
                if (notesAdapter.getId() == itemId){
                    return notesAdapter;
                }
            }

        }
        return null;
    }

    private void shareNote(CreateNotesAdapter adapter) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*"); // Change this to the appropriate MIME type

        String title = adapter.getTitle();
        String subTitle = adapter.getSubTitle();
        String description = adapter.getDescription();
        String dateTime = adapter.getDateTime();
        String imagePath = adapter.getImagePath();

        String shareText = title + "\n" +
                "Subtitle: " + subTitle + "\n" +
                "Description: " + description + "\n" +
                "Date: " + dateTime;

        // Add the image if available
        if (imagePath != null) {
            try {
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.yrmultimediaco.keepnotes.fileprovider", new File(imagePath));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                Log.e("Share Image Error", "Failed to create content URI for image: " + e.getMessage());
            }
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Note"));
    }

    private void openEditActivity(CreateNotesAdapter adapter){
        Intent
                intent = new Intent(MainActivity.this, CreateNotesActivity.class);

        intent.putExtra("isDeletingNote", true);

        intent.putExtra("note_id", adapter.getId());
        intent.putExtra("note_title", adapter.getTitle());
        intent.putExtra("note_subTitle", adapter.getSubTitle());
        intent.putExtra("note_desc", adapter.getDescription());
        intent.putExtra("get_url", adapter.getUrl());
        intent.putExtra("image_path", adapter.getImagePath());
        intent.putExtra("sub_title_indicator_color", adapter.getColor());

        startActivityForResult(intent, REQUEST_CODE_AND_NOTE);
    }

    class ActionBarCallback implements ActionMode.Callback{

        ActionMode currentActionMode;


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            currentActionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getItemId() == R.id.action_close){
                clearSelectionItem();
                mode.finish();
            } else if (item.getItemId() == android.R.id.home) {
                clearSelectionItem();
                onBackPressed();
                selectedItemCount = 0;
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                List<AbstractItem> selectedFiles = getSelectedNotes();
                showDeleteConfirmationDialog(selectedFiles);
                mode.finish();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearSelectionItem();
        }
    }

    private void showDeleteConfirmationDialog(List<AbstractItem> deleteSelectedNote) {

        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteContainer)
            );
            builder.setView(view);
            builder.setCancelable(false);

            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            CheckBox checkBox = view.findViewById(R.id.checkBox);


            view.findViewById(R.id.textDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mDBhelper.deleteNotes(selectedItemID);


                    for (AbstractItem selectedNote : deleteSelectedNote) {

                        if (selectedNote instanceof CreateNotesAdapter){
                            CreateNotesAdapter notesAdapter = (CreateNotesAdapter) selectedNote;
                            if (checkBox.isChecked()) {
                                mDBhelper.moveNoteToTrash(notesAdapter.getId());
                                Toast.makeText(MainActivity.this, "Note moved to trash", Toast.LENGTH_SHORT).show();
                            } else {
                                mDBhelper.deleteNotes(notesAdapter.getId());
                                Toast.makeText(MainActivity.this, "Note deleted permanently", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }


                    // Clear selection and update adapter
                    clearSelectionItem();
                    fastAdapter.notifyAdapterDataSetChanged();

                    // Dismiss dialog
                    dialogDeleteNote.dismiss();
                    dialogDeleteNote = null;

                    if (mActionModeHelper.getActionMode() != null) {
                        mActionModeHelper.getActionMode().invalidate();
                    }
                    // Update ActionMode title
                    updateActionModeTitle(selectedItemCount);

                    // Refresh notes
                    getNotes();
                    checkBox.setChecked(false);
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                    dialogDeleteNote = null;

                    if (mActionModeHelper.getActionMode() != null) {
                        mActionModeHelper.getActionMode().invalidate();
                    }
                    // Update ActionMode title
                    updateActionModeTitle(selectedItemCount);

                }
            });
        }
        dialogDeleteNote.show();
    }

private List<AbstractItem> getSelectedNotes(){

        List<AbstractItem> selectedNotes = new ArrayList<>();
    for (AbstractItem item : fastAdapter.getSelectedItems()) {
        if (item instanceof CreateNotesAdapter && item.isSelected()) {
            CreateNotesAdapter selectFiles = (CreateNotesAdapter) item;
            if (selectFiles.getId() != -1) {
                selectedNotes.add(selectFiles);
            } else {
                Log.d("DEBUG", "Found null FileName object: " + selectFiles);
            }
        }
    }

    return selectedNotes;
}

    private void clearSelectionItem() {
        for (AbstractItem item : itemAdapter.getAdapterItems()) {
            if (item instanceof CreateNotesAdapter) {
                CreateNotesAdapter adapter = (CreateNotesAdapter) item;
                adapter.setSelected(false);
            }
        }

        fastAdapter.notifyAdapterDataSetChanged();
        selectedItemCount = 0;
    }


    private void updateActionModeTitle(int count){

        if (mActionModeHelper.isActive()){
            if (count == 0){
                mActionModeHelper.getActionMode().finish();
            } else {
                mActionModeHelper.getActionMode().setTitle(getString(R.string.action_mode_title,count));
            }
        }
    }

    private void requestAllFilesAccessPermission() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        }
        Uri uri = Uri.parse("package:" + getPackageName());
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
    }


    public static String[] storage_permission = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permission_33 = {

            Manifest.permission.READ_MEDIA_IMAGES
    };

    public static String [] permissions(){
        String [] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            p = storage_permission_33;

        } else {
            p = storage_permission;
        }
        return p;
    }

    private void handlePermissions(){


        if (ActivityCompat.checkSelfPermission(this, permissions()[0])
                == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions()[0])){

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("This App requires Read Images Permission for add image feature to work as expected.")
                    .setTitle("Permission Required")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    permissions(),
                                    REQUEST_CODE_STORAGE_PERMISSION);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();

        } else{

            ActivityCompat.requestPermissions(MainActivity.this, permissions(), REQUEST_CODE_STORAGE_PERMISSION);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION ){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions()[0])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This feature is unavailable because this feature requires permission that you have denied. " +
                                "Please allow Images Permission from settings (Add Images) for proceed further")
                        .setTitle("Permission Required")
                        .setCancelable(false);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);

                        dialog.dismiss();
                    }
                });

                builder.show();
            } else {
                handlePermissions();
            }
        }

    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            // Handle the selected image
                            try {
                                String selectedImagePath = getPathFromUri(selectedImage);
                                Intent intent = new Intent(getApplicationContext(), CreateNotesActivity.class);
                                intent.putExtra("isFromQuickActions", true);
                                intent.putExtra("quickActionType", "image");
                                intent.putExtra("imagePath", selectedImagePath);
                                startActivityForResult(intent, REQUEST_CODE_AND_NOTE);

                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Specify image MIME type
        imagePickerLauncher.launch(intent);
    }

   /* private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {

                boolean allPermissionsGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted){
                    selectImage();
                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This feature is unavailable because this feature requires permission that you have denied. " +
                                    "Please allow Images Permission from settings (Add Images) for proceed further")
                            .setTitle("Permission Required")
                            .setCancelable(false)
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Settings", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);

                                dialog.dismiss();
                            });
                    builder.show();

                } else{
                    handlePermissions();
                }

            });*/

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private boolean performSearch(String query){

        ArrayList<AbstractItem> filteredNotes = new ArrayList<>();
        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes();
        boolean hasSearchResults = false;

        for (AbstractItem note : listNotes){

            if (note instanceof CreateNotesAdapter){
                CreateNotesAdapter notesAdapter = (CreateNotesAdapter) note;
                if (notesAdapter.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        notesAdapter.getSubTitle().toLowerCase().contains(query.toLowerCase()) ||
                        notesAdapter.getDescription().toLowerCase().contains(query.toLowerCase())){
                    filteredNotes.add(note);
                    hasSearchResults = true;
                }
            }


        }

        itemAdapter.clear();
        itemAdapter.add(filteredNotes);

        return hasSearchResults;
    }

   /* private void getNotes() {

        SharedPreferences sharedPreferences = getSharedPreferences("dragged_items", Context.MODE_PRIVATE);

        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes(); // Fetch notes not in trash

        itemAdapter.clear();

        ArrayList<AbstractItem> filteredlist = new ArrayList<>();
        for (AbstractItem item : listNotes) {
            if (item instanceof CreateNotesAdapter) {
                CreateNotesAdapter note = (CreateNotesAdapter) item;
                filteredlist.add(item);
                note.setBtnDetailClickListener(this);
            }
        }


        itemAdapter.add(filteredlist);
        // Update UI for regular notes

        // You can also update the UI for trashed notes if needed
        // ... (Update UI for trashed notes)
    }*/

    private void getNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences("dragged_items", Context.MODE_PRIVATE);

        // Load the sorting mode from shared preferences
        //boolean isSortingByName = loadSortingMode(); // Adjust this based on your implementation


        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes(); // Fetch notes not in trash
        itemAdapter.clear();

        ArrayList<AbstractItem> filteredlist = new ArrayList<>();
        for (int i = 0; i < listNotes.size(); i++) {
            AbstractItem item = listNotes.get(i);
            if (item instanceof CreateNotesAdapter) {
                CreateNotesAdapter note = (CreateNotesAdapter) item;

                // Retrieve the state from SharedPreferences
                boolean isItemSelected = sharedPreferences.getBoolean("item_" + i, false);
                note.setSelected(isItemSelected);
                filteredlist.add(item);

                note.setBtnDetailClickListener(this);
            }
        }

      /*  // Apply the appropriate sorting based on the loaded mode
        if (isSortingByName) {
            sortNotesByNameAscending(); // Adjust this based on your implementation
        } else {
            sortNotesByDateDescending(); // Adjust this based on your implementation
        }*/


        itemAdapter.add(filteredlist);

        if (filteredlist.isEmpty()){
            noFilesFoundView.setVisibility(View.VISIBLE);
        } else {
            noFilesFoundView.setVisibility(View.GONE);
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AND_NOTE && resultCode == RESULT_OK) {
            getNotes();
            loadLabels();
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNotesActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_AND_NOTE);
                    } catch (Exception e){
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                // Permission granted, proceed with saving the text file
                if (isExternalStorageWritable()) {
                    // External storage is available and writable, proceed with saving the file
                    saveNoteAsTextFile(selectedAdapter);
                } else {
                    // External storage is not writable, display an error message or handle it appropriately
                    Toast.makeText(this, "External storage is not writable.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void saveLayoutMode(boolean isGridView){

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefes", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_LAYOUT_MODE, isGridView);
        editor.apply();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationAction(intent);
    }

    private void handleNotificationAction(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if ("dismiss".equals(intent.getAction())) {
                // Extract data from the intent
                long noteId = intent.getLongExtra("noteId", -1);
                String taskTitle = intent.getStringExtra("taskTitle");

                // Show a dialog to cancel the alarm
                showCancelAlarmDialog(noteId, taskTitle);
            }
        }
    }
    private void showCancelAlarmDialog(long noteId, String taskTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Alarm")
                .setMessage("Do you want to cancel the alarm for note ID: " + noteId + " (" + taskTitle + ")?")
                .setPositiveButton("Cancel Alarm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle alarm cancellation here
                        AlarmReceiver.cancelAlarm(MainActivity.this, noteId);
                    }
                })
                .setNegativeButton("Dismiss", null) // No action needed for Dismiss button
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isGridView = loadLayoutMode();
        setLayoutManager();

        getNotes();
        loadLabels();

        boolean isSortingByName = loadSortingMode();
        // Apply the appropriate sorting based on the loaded mode
        if (isSortingByName) {
            sortNotesByNameAscending(); // Adjust this based on your implementation
        } else {
            sortNotesByDateDescending(); // Adjust this based on your implementation
        }

    }

    private boolean loadLayoutMode(){

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefes", MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_LAYOUT_MODE, false);

    }
    private void setLayoutManager(){

        if (isGridView){
            notesRecView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else{
            notesRecView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setLayoutMode() {

        isGridView = !isGridView;
        saveLayoutMode(isGridView);
        setLayoutManager();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_GRID_VIEW, isGridView);


    }
    private void showAddUrlDialog(){
        if (dialogWebUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogWebUrl = builder.create();
            if (dialogWebUrl.getWindow() != null){
                dialogWebUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputUrl = view.findViewById(R.id.inputUrl);
            inputUrl.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                        Toast.makeText(MainActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        dialogWebUrl.dismiss();

                        Intent intent = new Intent(getApplicationContext(), CreateNotesActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "URL");
                        intent.putExtra("URL", inputUrl.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_AND_NOTE);
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogWebUrl.dismiss();
                }
            });
        }

        dialogWebUrl.show();
    }

    private void saveSortingMode(boolean isSortingByName) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.putBoolean(PREF_SORTING_NAME, isSortingByName);
        editor.apply();
    }

    private boolean loadSortingMode() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getBoolean(PREF_SORTING_NAME, false); // Default to false (sort by date)
    }

    private void sortNotesByNameAscending() {

        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes();

        itemAdapter.clear();

        Collections.sort(listNotes, new Comparator<AbstractItem>() {
            @Override
            public int compare(AbstractItem item1, AbstractItem item2) {
                if (item1 instanceof CreateNotesAdapter && item2 instanceof CreateNotesAdapter) {
                    CreateNotesAdapter note1 = (CreateNotesAdapter) item1;
                    CreateNotesAdapter note2 = (CreateNotesAdapter) item2;
                    return note1.getTitle().compareToIgnoreCase(note2.getTitle());
                }
                return 0;
            }
        });
        itemAdapter.add(listNotes);
    }

    private void sortNotesByNameDescending() {

        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes();

        itemAdapter.clear();

        Collections.sort(listNotes, new Comparator<AbstractItem>() {
            @Override
            public int compare(AbstractItem item1, AbstractItem item2) {
                if (item1 instanceof CreateNotesAdapter && item2 instanceof CreateNotesAdapter) {
                    CreateNotesAdapter note1 = (CreateNotesAdapter) item1;
                    CreateNotesAdapter note2 = (CreateNotesAdapter) item2;
                    return note2.getTitle().compareToIgnoreCase(note1.getTitle());
                }
                return 0;
            }
        });
        itemAdapter.add(listNotes);
    }

    private void sortNotesByDateAscending() {

        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes();

        itemAdapter.clear();

        Collections.sort(listNotes, new Comparator<AbstractItem>() {
            @Override
            public int compare(AbstractItem item1, AbstractItem item2) {
                if (item1 instanceof CreateNotesAdapter && item2 instanceof CreateNotesAdapter) {
                    CreateNotesAdapter note1 = (CreateNotesAdapter) item1;
                    CreateNotesAdapter note2 = (CreateNotesAdapter) item2;
                    return note1.getDateTime().compareTo(note2.getDateTime());
                }
                return 0;
            }
        });
        itemAdapter.add(listNotes);
    }

    private void sortNotesByDateDescending() {

        ArrayList<AbstractItem> listNotes = mDBhelper.fetchRegularNotes();

        itemAdapter.clear();

        Collections.sort(listNotes, new Comparator<AbstractItem>() {
            @Override
            public int compare(AbstractItem item1, AbstractItem item2) {
                if (item1 instanceof CreateNotesAdapter && item2 instanceof CreateNotesAdapter) {
                    CreateNotesAdapter note1 = (CreateNotesAdapter) item1;
                    CreateNotesAdapter note2 = (CreateNotesAdapter) item2;
                    return note2.getDateTime().compareTo(note1.getDateTime());
                }
                return 0;
            }
        });
        itemAdapter.add(listNotes);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);

        MenuItem layoutItem = menu.findItem(R.id.layoutMode);


        layoutItem.setChecked(isGridView);

        // isGridView = isChecked;
        saveLayoutMode(isGridView);
        if (isGridView){
            layoutItem.setIcon(R.drawable.baseline_grid_view_24);
            notesRecView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else{
            layoutItem.setIcon(R.drawable.outline_view_agenda_24);
            notesRecView.setLayoutManager(new LinearLayoutManager(this));
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.layoutMode){
            setLayoutMode();

            if (isGridView){
                item.setIcon(R.drawable.baseline_grid_view_24);

            } else {
                item.setIcon(R.drawable.outline_view_agenda_24);

            }
        } else if (item.getItemId() == R.id.sortByNameAscending) {

            sortNotesByNameAscending();
            saveSortingMode(true);

            Toast.makeText(this, "Notes Sorted By Name (Ascending) ", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.sortByDateAscending) {
            sortNotesByDateAscending();
            saveSortingMode(false);

            Toast.makeText(this, "Notes Sorted By Date (Ascending) ", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.sortByDateDescending) {
            sortNotesByDateDescending();
            saveSortingMode(false);

            Toast.makeText(this, "Notes Sorted By Date (Descending) ", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.sortByNameDescending) {
            sortNotesByNameDescending();
            saveSortingMode(false);

            Toast.makeText(this, "Notes Sorted By Date (Descending) ", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);

    }
}